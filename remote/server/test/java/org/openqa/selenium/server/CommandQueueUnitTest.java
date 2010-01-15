package org.openqa.selenium.server;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.openqa.jetty.log.LogFactory;
import org.openqa.selenium.server.log.LoggingManager;
import org.openqa.selenium.server.log.StdOutHandler;
import org.openqa.selenium.server.log.TerseFormatter;
import org.openqa.selenium.testworker.TrackableRunnable;
import org.openqa.selenium.testworker.TrackableThread;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class CommandQueueUnitTest extends TestCase {

    private static final String sessionId = "1";
    private static final String testCommand = "testCommand";
    private static final String waitCommand = "waitForSomethingCommand";
    private static final String testResult = "OK";
    private static final int defaultTimeout = 7;
    private static final int retryTimeout = 5;

    private static Log log = LogFactory.getLog(CommandQueueUnitTest.class);

    private CommandQueue cq;
    private RemoteControlConfiguration configuration;

    @Override
    public void setUp() throws Exception {
        configureLogging();
        configuration = new RemoteControlConfiguration();
        configuration.setTimeoutInSeconds(defaultTimeout);
        configuration.setProxyInjectionModeArg(false);
        cq = new CommandQueue(sessionId, getName(), configuration);
        log.info("Start test: " + getName());
    }

    private void configureLogging() throws Exception {
        LoggingManager.configureLogging(new RemoteControlConfiguration(), true);
        Logger logger = Logger.getLogger("");
        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof StdOutHandler) {
                handler.setFormatter(new TerseFormatter(true));
                break;
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        LoggingManager.configureLogging(new RemoteControlConfiguration(), false);
    }

    public void testQueueDelayInitializedAtDefault() {
        assertEquals(CommandQueue.getSpeed(), cq.getQueueDelay());
    }

    public void testQueueDelayChangedAsSetNoCrosstalk() {
        int defaultSpeed = CommandQueue.getSpeed();
        int newSpeed = cq.getQueueDelay() + 42;
        int newGlobalSpeed = defaultSpeed + 21;
        cq.setQueueDelay(newSpeed);
        CommandQueue.setSpeed(newGlobalSpeed);
        assertEquals(newSpeed, cq.getQueueDelay());
        assertEquals(newGlobalSpeed, CommandQueue.getSpeed());

        CommandQueue cq2 = new CommandQueue(sessionId, getName() + "2", new RemoteControlConfiguration());
        assertEquals(newGlobalSpeed, cq2.getQueueDelay());

        CommandQueue cq3 = new CommandQueue(sessionId, getName() + "3", newSpeed, new RemoteControlConfiguration());
        assertEquals(newSpeed, cq3.getQueueDelay());

        CommandQueue.setSpeed(defaultSpeed);
    }

    public void testAssertStartingState() {
        assertNull(cq.peekAtCommand());
        assertNull(cq.peekAtResult());
    }

    public void testBasicDoCommandWithoutWaiting() throws WindowClosedException {
        cq.doCommandWithoutWaitingForAResponse(testCommand, "", "");
        RemoteCommand rc = cq.peekAtCommand();
        assertNotNull(rc);
        assertEquals(testCommand, rc.getCommand());
        assertNull(cq.peekAtResult());
    }

    public void testDoCommandWithoutWaitingWithResultAlreadyThereWithNoPI()
            throws WindowClosedException {
        cq.putResult(testResult);
        try {
            cq.doCommandWithoutWaitingForAResponse(testCommand, "", "");
            fail();
        } catch (IllegalStateException ise) {
            assertNotNull(cq.peekAtResult());
        }
    }

    public void testDoCommandWithoutWaitingWithResultAlreadyThereWithPI()
            throws WindowClosedException {
        configuration = new RemoteControlConfiguration();
        configuration.setTimeoutInSeconds(defaultTimeout);
        configuration.setProxyInjectionModeArg(true);
        cq = new CommandQueue(sessionId, getName(), configuration);
        cq.putResult(testResult);
        cq.doCommandWithoutWaitingForAResponse(testCommand, "", "");
        assertEquals(testResult, cq.peekAtResult());
    }

    public void testDoCommandWithoutWaitingWithBadResultAlreadyThereWithPI()
            throws WindowClosedException {
        configuration = new RemoteControlConfiguration();
        configuration.setTimeoutInSeconds(defaultTimeout);
        configuration.setProxyInjectionModeArg(true);
        cq = new CommandQueue(sessionId, getName(), configuration);
        cq.putResult(testResult);
        cq.doCommandWithoutWaitingForAResponse(waitCommand, "", "");
        assertEquals(testResult, cq.peekAtResult());
    }

    public void testGetsTimeoutExceptionOnGetResult() {
        expectResult(CommandResultHolder.CMD_TIMED_OUT_MSG);
        assertNull(cq.peekAtResult());
    }

    public void testDoCommandTimesOut() {
        long now = System.currentTimeMillis();
        String result = cq.doCommand(testCommand, "", "");
        long after = System.currentTimeMillis();
        assertEquals(CommandResultHolder.CMD_TIMED_OUT_MSG, result);
        assertTrue((after - now) > defaultTimeout);
    }

    public void testGetsPrevCommandFromHandleNullResult() throws WindowClosedException {
        cq.doCommandWithoutWaitingForAResponse(testCommand, "", "");
        RemoteCommand rc = cq.handleCommandResult(null);
        assertEquals(testCommand, rc.getCommand());
    }

    public void testSendsRetryCommandIfNoneAfterRetryPeriod() {
        long now = System.currentTimeMillis();
        RemoteCommand rc = cq.handleCommandResult(null);
        long after = System.currentTimeMillis();
        assertTrue((after - now) > retryTimeout);
        assertEquals(CommandHolder.RETRY_CMD_STRING, rc.getCommand());
    }

    public void testHandleResultNoWaitingWithOKResultWithResExpectedNoPI() {
        cq.setResultExpected(true);
        cq.handleCommandResultWithoutWaitingForACommand(testResult);
        assertEquals(testResult, cq.peekAtResult());
    }

    public void testHandleResultWithOKResultWithNoResExpectedWithPI() {
        configuration = new RemoteControlConfiguration();
        configuration.setTimeoutInSeconds(defaultTimeout);
        configuration.setProxyInjectionModeArg(true);
        cq = new CommandQueue(sessionId, getName(), configuration);
        cq.handleCommandResultWithoutWaitingForACommand(testResult);
        assertEquals(testResult, cq.peekAtResult());
    }

    public void testGetNextCommandWhenAlreadyWaiting() throws Throwable {
        cq.setResultExpected(true);
        TrackableRunnable internalRunner = new TrackableRunnable() {
            @Override
            public Object go() throws Throwable {
                cq.doCommandWithoutWaitingForAResponse(testCommand, "", "");
                return null;
            }
        };
        String name = "launching runner";
        TrackableThread t = new TrackableThread(internalRunner, name);
        t.start();
        t.getResult();
        assertsForCommandState(cq.getNextCommand());
    }

    private void assertsForCommandState(RemoteCommand cmd) {
        assertNotNull(cmd);
        assertEquals(testCommand, cmd.getCommand());
    }

    public void testSimpleSingleThreaded() throws Exception {
        injectCommandAsIfWaiting("something", "arg1", "arg2");
        expectCommand("something", "arg1", "arg2");
        cq.handleCommandResultWithoutWaitingForACommand("OK");
        expectResult("OK");
    }

    public void testRealSimple() throws Throwable {
        TrackableThread commandRunner = launchCommandRunner("something", "arg1", "arg2");
        expectCommand("something", "arg1", "arg2");
        cq.handleCommandResultWithoutWaitingForACommand("OK");
        assertEquals("OK", commandRunner.getResult());
    }

    public void testTwoRoundsSingleThreaded() throws Exception {
        testSimpleSingleThreaded();
        cq.doCommandWithoutWaitingForAResponse("testComplete", "", "");
        expectCommand("testComplete", "", "");
        cq.handleCommandResultWithoutWaitingForACommand("OK");
        expectResult("OK");
    }

    public void testRealTwoRounds() throws Throwable {
        // do "something"
        TrackableThread commandRunner = launchCommandRunner("something", "arg1", "arg2");
        // browser receives "something"
        TrackableThread browserRequestRunner = launchBrowserResultRunner(null);
        expectCommand(browserRequestRunner, "something", "arg1", "arg2");
        // browser replies "OK"
        browserRequestRunner = launchBrowserResultRunner("OK");
        // commandRunner receives "OK"
        assertEquals("OK", commandRunner.getResult());
        // send a final "testComplete" in the current foreground thread
        cq.doCommandWithoutWaitingForAResponse("testComplete", "", "");
        expectCommand(browserRequestRunner, "testComplete", "", "");
    }

// TODO test JsVar stuff

    /**
     * In PI Mode, open replies "OK", but then we asynchronously receive "closed!"
     */
    public void testPIOpenSingleThreaded() throws Exception {
        injectCommandAsIfWaiting("open", "blah.html", "");
        expectCommand("open", "blah.html", "");
        cq.handleCommandResultWithoutWaitingForACommand("OK");
        cq.declareClosed();
        assertEquals("OK", cq.peekAtResult());
    }

    private void injectCommandAsIfWaiting(String cmd, String field, String value) throws WindowClosedException {
        cq.setResultExpected(true);
        cq.doCommandWithoutWaitingForAResponse(cmd, field, value);
    }

    private void expectCommand(TrackableThread browserRequestRunner, String cmd, String arg1, String arg2) throws Throwable {
        RemoteCommand actual = (RemoteCommand) browserRequestRunner.getResult();
        RemoteCommand expected = new DefaultRemoteCommand(cmd, arg1, arg2);
        assertEquals(cmd + " command got mangled", expected, actual);
    }

    private void expectCommand(String cmd, String arg1, String arg2) {
        RemoteCommand actual = cq.getNextCommand();
        RemoteCommand expected = new DefaultRemoteCommand(cmd, arg1, arg2);
        assertEquals(cmd + " command got mangled", expected, actual);
    }

    private void expectResult(String expected) {
        String actual = cq.getResult();
        assertEquals(expected + " result got mangled", expected, actual);
    }

    /**
     * Fire off a command in the background, so we can wait for the result
     */
    private TrackableThread launchCommandRunner(String cmd, String arg1, String arg2) {
        TrackableThread t = new TrackableThread(new AsyncCommandRunner(cmd, arg1, arg2), cmd);
        t.start();
        return t;
    }

    /**
     * Send back a browser result in the background, so we can wait for the next command
     */
    private TrackableThread launchBrowserResultRunner(String browserResult) {
        String name = browserResult;
        if (name == null) {
            name = "NULL STARTING";
        }
        TrackableThread t = new TrackableThread(new AsyncBrowserResultRunner(browserResult), name);
        t.start();
        return t;
    }

    /**
     * Passes the specified command to command queue
     */
    private class AsyncCommandRunner extends TrackableRunnable {
        private String cmd, arg1, arg2;

        public AsyncCommandRunner(String cmd, String arg1, String arg2) {
            this.cmd = cmd;
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        @Override
        public Object go() throws Throwable {
            Object result = cq.doCommand(cmd, arg1, arg2);
            log.info(Thread.currentThread().getName() + " got result: " + result);
            return result;
        }
    }

    /**
     * Passes the specified browserResult to command queue
     */
    private class AsyncBrowserResultRunner extends TrackableRunnable {
        private String browserResult;

        public AsyncBrowserResultRunner(String browserResult) {
            this.browserResult = browserResult;
        }

        @Override
        public Object go() throws Throwable {
            Object result = cq.handleCommandResult(browserResult);
            log.info(Thread.currentThread().getName() + " got result: " + result);
            return result;
        }
    }

}