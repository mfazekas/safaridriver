package org.openqa.selenium.server;

import org.openqa.selenium.server.BrowserSessionFactory.BrowserSessionInfo;
import org.openqa.selenium.server.browserlaunchers.BrowserLauncherFactory;
import org.openqa.selenium.server.browserlaunchers.DummyLauncher;
import org.openqa.selenium.server.log.LoggingManager;
import org.openqa.selenium.server.log.StdOutHandler;
import org.openqa.selenium.server.log.TerseFormatter;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

public class BrowserSessionFactoryUnitTest extends TestCase {

    private static final String SESSION_ID_1 = "testLookupByBrowserAndUrl1";
    private static final String BROWSER_1 = "*firefox";
    private static final String BASEURL1 = "http://www.google.com";

    private static final String SESSION_ID_2 = "testLookupByBrowserAndUrl2";
    private static final String BROWSER2 = "*firefox";
    private static final String BASEURL2 = "http://maps.google.com";

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    configureLogging();
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

  public void testBrowserSessionFactorySetsLastSessionIdOfSeleniumDriverResourceHandler() throws Exception {

        final RemoteControlConfiguration configuration;
        
        BrowserLauncherFactory blf = createMock(BrowserLauncherFactory.class);
        DummyLauncher launcherMock = createMock(DummyLauncher.class);
        
        configuration = new RemoteControlConfiguration();
        configuration.setTimeoutInSeconds(1);
        
        BrowserConfigurationOptions bco = new BrowserConfigurationOptions();
        
        BrowserSessionFactory factory = new BrowserSessionFactory(blf) {
        	@Override
        	protected FrameGroupCommandQueueSet makeQueueSet(String sessionId,
        			int port, RemoteControlConfiguration configuration) {
        		return createMock(FrameGroupCommandQueueSet.class);
        	}
        	
        	@Override
        	protected FrameGroupCommandQueueSet getQueueSet(String sessionId) {
        		return createMock(FrameGroupCommandQueueSet.class);
        	}
        };
        
        expect(blf.getBrowserLauncher(isA(String.class), isA(String.class), 
                isA(RemoteControlConfiguration.class), isA(BrowserConfigurationOptions.class))).andReturn(launcherMock);
        launcherMock.launchRemoteSession("");
        expectLastCall().once();
        replay(launcherMock);
        replay(blf);
        factory.createNewRemoteSession("", "", "", bco, true, configuration);
        String expected = ((BrowserSessionInfo)(factory.activeSessions.toArray()[0])).sessionId;
        assertEquals(expected, SeleniumDriverResourceHandler.getLastSessionId());
    }
    
    public void testInvalidLauncherPreventsNewRemoteSessionCreationWithException() {
        final BrowserSessionFactory factory;
        final RemoteControlConfiguration configuration;

        factory = new BrowserSessionFactory(new BrowserLauncherFactory());
        configuration = new RemoteControlConfiguration();
        configuration.setTimeoutInSeconds(1);
        try {
          factory.createNewRemoteSession("*chrome invalid", "http://amazon.com", "", null, false, configuration);
          fail("Did not catch a RemoteCommandException when timing out on browser launch.");
        } catch (RuntimeException  rte) {
            /* As expected */
        } catch (RemoteCommandException rce) {
        	fail("RuntimeException was expected...");
        }
    }
    
    public void testIsValidWithInvalidSessionInfo() {
        BrowserSessionInfo info = new BrowserSessionInfo("id1", "*firefox",
                null, null, null);
        assertNotNull(info);
    }

    public void testLookupByBrowserAndUrl() {
        BrowserSessionFactory factory = getTestSessionFactory();
        Set<BrowserSessionInfo> infos = getTestSessionSet();
        BrowserSessionInfo result = factory.lookupInfoByBrowserAndUrl(
                BROWSER_1, BASEURL1, infos);
        assertEquals(SESSION_ID_1, result.sessionId);
    }

    public void testLookupByBrowserAndUrlWithNoMatch() {
        BrowserSessionFactory factory = getTestSessionFactory();
        Set<BrowserSessionInfo> infos = getTestSessionSet();
        BrowserSessionInfo result = factory.lookupInfoByBrowserAndUrl(
                BROWSER_1, "fooey", infos);
        assertNull(result);
    }

    public void testLookupBySessionId() {
        BrowserSessionFactory factory = getTestSessionFactory();
        Set<BrowserSessionInfo> infos = getTestSessionSet();
        BrowserSessionInfo result = factory.lookupInfoBySessionId(
                SESSION_ID_2, infos);
        assertEquals(BASEURL2, result.baseUrl);
    }

    public void testLookupBySessionIdWithNoMatch() {
        BrowserSessionFactory factory = getTestSessionFactory();
        Set<BrowserSessionInfo> infos = getTestSessionSet();
        BrowserSessionInfo result = factory.lookupInfoBySessionId(
                "fooey", infos);
        assertNull(result);
    }

    public void testRegisterValidExternalSession() {
        BrowserSessionFactory factory = getTestSessionFactory();
        BrowserSessionInfo info1 = getTestSession1();
        factory.registerExternalSession(info1);
        assertTrue(factory.hasActiveSession(info1.sessionId));
    }

    public void testRegisterInValidExternalSession() {
        BrowserSessionFactory factory = getTestSessionFactory();
        BrowserSessionInfo info = new BrowserSessionInfo(SESSION_ID_1, "*firefox",
                null, null, null);
        factory.registerExternalSession(info);
        assertFalse(factory.hasActiveSession(info.sessionId));
    }

    public void testGrabAvailableSession() {
        BrowserSessionFactory factory = getTestSessionFactory();
        factory.addToAvailableSessions(getTestSession1());
        assertTrue(factory.hasAvailableSession(SESSION_ID_1));
        assertFalse(factory.hasActiveSession(SESSION_ID_1));
        BrowserSessionInfo result = factory.grabAvailableSession(BROWSER_1, BASEURL1);
        assertEquals(SESSION_ID_1, result.sessionId);
        assertFalse(factory.hasAvailableSession(SESSION_ID_1));
        assertTrue(factory.hasActiveSession(SESSION_ID_1));
    }

    public void testEndSessionWithNoCaching() {
        BrowserSessionFactory factory = getTestSessionFactory();
        factory.registerExternalSession(getTestSession1());
        assertTrue(factory.hasActiveSession(SESSION_ID_1));
        factory.endBrowserSession(SESSION_ID_1, new RemoteControlConfiguration());
        assertFalse(factory.hasActiveSession(SESSION_ID_1));
        assertFalse(factory.hasAvailableSession(SESSION_ID_1));
    }

    public void testEndSessionWithCaching() {
        BrowserSessionFactory factory = getTestSessionFactory();
        factory.registerExternalSession(getTestSession1());
        assertTrue(factory.hasActiveSession(SESSION_ID_1));
        long closingTime = System.currentTimeMillis();
        RemoteControlConfiguration configuration = new RemoteControlConfiguration();
        configuration.setReuseBrowserSessions(true);
        factory.endBrowserSession(SESSION_ID_1, configuration);
        assertFalse(factory.hasActiveSession(SESSION_ID_1));
        assertTrue(factory.hasAvailableSession(SESSION_ID_1));
        BrowserSessionInfo info = factory.lookupInfoBySessionId(SESSION_ID_1,
                factory.availableSessions);
        assertTrue(info.lastClosedAt >= closingTime);
    }

    public void testEndAllBrowserSessions() {
        BrowserSessionFactory factory = getTestSessionFactory();
        factory.registerExternalSession(getTestSession1());
        factory.addToAvailableSessions(getTestSession2());
        factory.endAllBrowserSessions(new RemoteControlConfiguration());
        assertFalse(factory.hasActiveSession(SESSION_ID_1));
        assertFalse(factory.hasAvailableSession(SESSION_ID_2));
        assertFalse(factory.hasAvailableSession(SESSION_ID_1));
    }

    public void testRemoveIdleAvailableSessions() {
        BrowserSessionFactory factory = getTestSessionFactory();
        factory.addToAvailableSessions(getTestSession1());
        assertTrue(factory.hasAvailableSession(SESSION_ID_1));
        factory.removeIdleAvailableSessions();
        assertFalse(factory.hasAvailableSession(SESSION_ID_1));
    }

    public void disable_testRemoveIdleAvailableSessionsViaCleanup() {
        BrowserSessionFactory factory = new BrowserSessionFactory(null, 5, 0, true);
        BrowserSessionInfo info1 = getTestSession1();
        info1.lastClosedAt = 0; // very idle.
        factory.addToAvailableSessions(info1);
        FrameGroupCommandQueueSet.sleepForAtLeast(5);
        assertFalse(factory.hasAvailableSession(SESSION_ID_1));
    }

    private Set<BrowserSessionInfo> getTestSessionSet() {
        Set<BrowserSessionInfo> infos = new HashSet<BrowserSessionInfo>();
        BrowserSessionInfo info1 = getTestSession1();
        infos.add(info1);
        BrowserSessionInfo info2 = getTestSession2();
        infos.add(info2);
        return infos;
    }

    private BrowserSessionInfo getTestSession1() {
        DummyLauncher mockLauncher1 = new DummyLauncher();
        return new BrowserSessionInfo(
                SESSION_ID_1, BROWSER_1, BASEURL1, mockLauncher1, null);
    }

    private BrowserSessionInfo getTestSession2() {
        DummyLauncher mockLauncher2 = new DummyLauncher();
        return new BrowserSessionInfo(
                SESSION_ID_2, BROWSER2, BASEURL2, mockLauncher2, null);
    }

    private BrowserSessionFactory getTestSessionFactory() {
        return new BrowserSessionFactory(null, 0, 0, false);
    }

}
