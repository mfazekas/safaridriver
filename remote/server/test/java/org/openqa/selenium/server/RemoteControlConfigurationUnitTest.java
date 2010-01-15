package org.openqa.selenium.server;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

/**
 * #{@link org.openqa.selenium.server.RemoteControlConfiguration} unit test class.
 */
public class RemoteControlConfigurationUnitTest {
	
	private final RemoteControlConfiguration configuration = new RemoteControlConfiguration();

	@Before
	public void setUp() {
	}
	
    @Test public void testPortIs4444ByDefault() {
        assertEquals(4444, configuration.getPort());
    }

    @Test public void testPortCanBeSet() {
        configuration.setPort(1234);
        assertEquals(1234, configuration.getPort());
    }

    @Test public void testMultiWindowIsTrueByDefault() {
        assertTrue((!configuration.isSingleWindow()));
    }

    @Test public void testMultiWindowCanBeSet() {
        configuration.setSingleWindow(false);
        assertTrue((!configuration.isSingleWindow()));
    }

    @Test public void testProxyInjectionModeArgIsFalseByDefault() {
        assertFalse(configuration.getProxyInjectionModeArg());
    }

    @Test public void testProxyInjectionModeArgCanBeSet() {
        configuration.setProxyInjectionModeArg(true);
        assertTrue(configuration.getProxyInjectionModeArg());
    }

    @Test public void testPortDriversShouldContactIsSamePortByDefault() {
        configuration.setPort(1515);
        assertEquals(1515, configuration.getPortDriversShouldContact());
    }

    @Test public void testPortDriversShouldContactCanBeSet() {
        configuration.setPortDriversShouldContact(1234);
        assertEquals(1234, configuration.getPortDriversShouldContact());
    }

    @Test public void testHTMLSuiteIsFalseByDefault() {
        assertFalse(configuration.isHTMLSuite());
    }

    @Test public void testHTMLSuiteCanBeSet() {
        configuration.setHTMLSuite(true);
        assertTrue(configuration.isHTMLSuite());
    }

    @Test public void testSelfTestIsFalseByDefault() {
        assertFalse(configuration.isSelfTest());
    }

    @Test public void testSelfTestCanBeSet() {
        configuration.setSelfTest(true);
        assertTrue(configuration.isSelfTest());
    }

    @Test public void testSelfTestDirIsNullByDefault() {
        assertNull(configuration.getSelfTestDir());
    }

    @Test public void testSelfTestDirCanBeSet() {
        final File aDirectory = new File("\"A Directory Name\"");
        configuration.setSelfTestDir(aDirectory);
        assertEquals(aDirectory, configuration.getSelfTestDir());
    }

    @Test public void testInteractiveIsFalseByDefault() {
        assertFalse(configuration.isInteractive());
    }

    @Test public void testInteractiveCanBeSet() {
        configuration.setInteractive(true);
        assertTrue(configuration.isInteractive());
    }

    @Test public void testUserExtensionsIsNullByDefault() {
        assertNull(configuration.getUserExtensions());
    }

    @Test public void testUserExtensionsCanBeSet() {
        final File aDirectory = new File("\"A File Name\"");
        configuration.setUserExtensions(aDirectory);
        assertEquals(aDirectory, configuration.getUserExtensions());
    }

    @Test public void testUserJSInjectionIsFalseByDefault() {
        assertFalse(configuration.userJSInjection());
    }

    @Test public void testUserJSInjectionCanBeSet() {
        configuration.setUserJSInjection(true);
        assertTrue(configuration.userJSInjection());
    }

    @Test public void testTrustAllSSLCertificatesIsFalseByDefault() {
        assertFalse(configuration.trustAllSSLCertificates());
    }

    @Test public void testTrustAllSSLCertificatesCanBeSet() {
        configuration.setTrustAllSSLCertificates(true);
        assertTrue(configuration.trustAllSSLCertificates());
    }

    @Test public void testDebugURLIsEmptyByDefault() {
        assertEquals("", configuration.getDebugURL());
    }


    @Test public void testDebugURLCanBeSet() {
        configuration.setDebugURL("A URL");
        assertEquals("A URL", configuration.getDebugURL());
    }

    @Test public void testDontInjectRegexIsNullByDefault() {
        assertNull(configuration.getDontInjectRegex());
    }

    @Test public void testDontInjectRegexCanBeSet() {
        configuration.setDontInjectRegex("A Regex");
        assertEquals("A Regex", configuration.getDontInjectRegex());
    }

    @Test public void testFirefoxProfileTemplateIsNullByDefault() {
        assertNull(configuration.getFirefoxProfileTemplate());
    }

    @Test public void testFirefoxProfileTemplateCanBeSet() {
        final File aDirectory = new File("\"A Directory Path\"");
        configuration.setFirefoxProfileTemplate(aDirectory);
        assertEquals(aDirectory, configuration.getFirefoxProfileTemplate());
    }

    @Test public void testReuseBrowserSessionsIsFalseByDefault() {
        assertFalse(configuration.reuseBrowserSessions());
    }

    @Test public void testReuseBrowserSessionsCanBeSet() {
        configuration.setReuseBrowserSessions(true);
        assertTrue(configuration.reuseBrowserSessions());
    }

    @Test public void testLogoutFileNameIsNullByDefault() {
        assertNull(configuration.getLogOutFileName());
    }

    @Test public void testLogoutFileNameCanBeSet() {
    	configuration.setLogOutFileName("A File Name");
        assertEquals("A File Name", configuration.getLogOutFileName());
    }

    @Test public void testForcedBrowserModeIsNullByDefault() {
        assertNull(configuration.getForcedBrowserMode());
    }

    @Test public void testForcedBrowserModeCanBeSet() {
        configuration.setForcedBrowserMode("A Mode");
        assertEquals("A Mode", configuration.getForcedBrowserMode());
    }

    @Test public void testHonorSystemProxyIsFalseByDefault() {
        assertFalse(configuration.honorSystemProxy());

    }

    @Test public void testHonorSystemProxyCanBeSet() {
        configuration.setHonorSystemProxy(true);
        assertTrue(configuration.honorSystemProxy());

    }

    @Test public void testShouldOverrideSystemProxyIsTrueByDefault() {
        assertTrue(configuration.shouldOverrideSystemProxy());

    }

    @Test public void testShouldOverrideSystemProxyIsFalseIfHonorSystemProxyIsSet() {
        configuration.setHonorSystemProxy(true);
        assertFalse(configuration.shouldOverrideSystemProxy());
    }

    @Test public void testTimeoutInSecondsIs30MinutesByDefault() {
        assertEquals(30 * 60, configuration.getTimeoutInSeconds());
    }

    @Test public void testTimeoutInSecondsCanBeSet() {
        configuration.setTimeoutInSeconds(123);
        assertEquals(123, configuration.getTimeoutInSeconds());
    }

    @Test public void testRetryTimeoutInSecondsIs10SecondsByDefault() {
        assertEquals(10, configuration.getRetryTimeoutInSeconds());
    }

    @Test public void testRetryTimeoutInSecondsCanBeSet() {
        configuration.setRetryTimeoutInSeconds(123);
        assertEquals(123, configuration.getRetryTimeoutInSeconds());
    }

    @Test public void testDontTouchLoggingIsFalseByDefault() {
        assertFalse(configuration.dontTouchLogging());
    }

    @Test public void testDontTouchLoggingCanBeSet() {
        configuration.setDontTouchLogging(true);
        assertTrue(configuration.dontTouchLogging());
    }

    @Test public void testShortTermMemoryLoggerCapacityIs50Bydefault() {
        assertEquals(30, configuration.shortTermMemoryLoggerCapacity());
    }

    @Test public void remoteControlConfigurationWillBeCopiedIntoBrowserOptions() throws Exception {
    	final BrowserConfigurationOptions browserOptions = new BrowserConfigurationOptions();
    	
    	String fileName = "file";
    	int timeOut = 5;
    	boolean honorSystemProxy = true;
    	String dontInjectRegex = "newdontInjectRegex";
    	boolean trustAllSSLCertificates = true;
    	File newuserExtensions = new File("newuserExtensions");
    	boolean useUserJSInjection = true;
    	boolean useProxyInjectionMode = true;
    	boolean useSingleWindow = true;
    	boolean ensureCleanSession = true;
    	boolean avoidProxy = true;
    	boolean browserSideLogEnabled = true;
    	
    	configuration.setFirefoxProfileTemplate(new File(fileName));
    	configuration.setTimeoutInSeconds(timeOut);
    	configuration.setHonorSystemProxy(honorSystemProxy);
    	configuration.setDontInjectRegex(dontInjectRegex);
    	configuration.setTrustAllSSLCertificates(trustAllSSLCertificates);
    	configuration.setUserExtensions(newuserExtensions);
    	configuration.setUserJSInjection(useUserJSInjection);
    	configuration.setProxyInjectionModeArg(useProxyInjectionMode);
    	configuration.setSingleWindow(useSingleWindow);
    	configuration.setEnsureCleanSession(ensureCleanSession);
    	configuration.setAvoidProxy(avoidProxy);
    	configuration.setBrowserSideLogEnabled(browserSideLogEnabled);
    	
    	configuration.copySettingsIntoBrowserOptions(browserOptions);
    	
    	assertEquals(fileName, browserOptions.get("firefoxProfileTemplate"));
    	assertEquals(Integer.toString(timeOut), browserOptions.get("timeoutInSeconds"));
    	assertEquals(Boolean.toString(honorSystemProxy), browserOptions.get("honorSystemProxy"));
    	assertEquals(dontInjectRegex, browserOptions.get("dontInjectRegex"));
    	assertEquals(Boolean.toString(trustAllSSLCertificates), browserOptions.get("trustAllSSLCertificates"));
    	assertEquals(newuserExtensions.getName(), browserOptions.get("userExtensions"));
    	assertEquals(Boolean.toString(useUserJSInjection), browserOptions.get("userJSInjection"));
    	assertEquals(Boolean.toString(useProxyInjectionMode), browserOptions.get("proxyInjectionMode"));
    	assertEquals(Boolean.toString(useSingleWindow), browserOptions.get("singleWindow"));
    	assertEquals(Boolean.toString(ensureCleanSession), browserOptions.get("ensureCleanSession"));
    	assertEquals(Boolean.toString(avoidProxy), browserOptions.get("avoidProxy"));
    	assertEquals(Boolean.toString(browserSideLogEnabled), browserOptions.get("browserSideLog"));
    }
    
}
