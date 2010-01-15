package com.thoughtworks.selenium;

import junit.framework.TestCase;

/**
 * Provides a JUnit TestCase base class that implements some handy functionality 
 * for Selenium testing (you are <i>not</i> required to extend this class).
 * 
 * <p>This class adds a number of "verify" commands, which are like "assert" commands,
 * but they don't stop the test when they fail.  Instead, verification errors are all
 * thrown at once during tearDown.</p>
 * 
 * @author Nelson Sproul (nsproul@bea.com) Mar 13-06
 */
public class SeleneseTestCase extends TestCase {

    private SeleneseTestBase stb = new SeleneseTestBase();
    
    /** Use this object to run all of your selenium tests */
    protected Selenium selenium;
    
    public SeleneseTestCase() {
        super();
    }

    
    public SeleneseTestCase(String name) {
        super(name);
    }
    
    /** Asserts that there were no verification errors during the current test, failing immediately if any are found */
    public void checkForVerificationErrors() {
        stb.checkForVerificationErrors();
    }
    
    /** Clears out the list of verification errors */
    public void clearVerificationErrors() {
        stb.clearVerificationErrors();
    }
    
    /** Returns the body text of the current page */
    public String getText() {
        return stb.getText();
    }
    
    /** Sleeps for the specified number of milliseconds */
    public void pause(int millisecs) {
        stb.pause(millisecs);
    }
    
    /** Calls this.setUp(null)
     * @see #setUp(String)
     */
    public void setUp() throws Exception {
        stb.setUp();
        selenium = stb.selenium;
    }
    
    /**
     * Calls this.setUp with the specified url and a default browser.  On Windows, the default browser is *iexplore; otherwise, the default browser is *firefox.
     * @see #setUp(String, String)
     * @param url the baseUrl to use for your Selenium tests
     * @throws Exception
     * 
     */
    public void setUp(String url) throws Exception {
        stb.setUp(url);
        selenium = stb.selenium;
    }
    
    /**
     * Creates a new DefaultSelenium object and starts it using the specified baseUrl and browser string
     * @param url the baseUrl for your tests
     * @param browserString the browser to use, e.g. *firefox
     * @throws Exception
     */
    public void setUp(String url, String browserString) throws Exception {
        stb.setUp(url, browserString);
        selenium = stb.selenium;
    }
    
    /** checks for verification errors and stops the browser */
    public void tearDown() throws Exception {
        stb.tearDown();
    }
    
    /** Like assertEquals, but fails at the end of the test (during tearDown) */
    public void verifyEquals(boolean arg1, boolean arg2) {
        stb.verifyEquals(arg1, arg2);
    }
    
    /** Like assertEquals, but fails at the end of the test (during tearDown) */
    public void verifyEquals(Object s1, Object s2) {
        stb.verifyEquals(s1, s2);
    }
    
    /** Like assertEquals, but fails at the end of the test (during tearDown) */
    public void verifyEquals(String[] s1, String[] s2) {
        stb.verifyEquals(s1, s2);
    }
    
    /** Like assertFalse, but fails at the end of the test (during tearDown) */
    public void verifyFalse(boolean b) {
        stb.verifyFalse(b);
    }
    
    /** Like assertNotEquals, but fails at the end of the test (during tearDown) */
    public void verifyNotEquals(boolean s1, boolean s2) {
        stb.verifyNotEquals(s1, s2);
    }
    
    /** Like assertNotEquals, but fails at the end of the test (during tearDown) */
    public void verifyNotEquals(Object s1, Object s2) {
        stb.verifyNotEquals(s1, s2);
    }
    
    /** Like assertTrue, but fails at the end of the test (during tearDown) */
    public void verifyTrue(boolean b) {
        stb.verifyTrue(b);
    }

    /** Like JUnit's Assert.assertEquals, but knows how to compare string arrays */
    public static void assertEquals(Object s1, Object s2) {
        SeleneseTestBase.assertEquals(s1, s2);
    }
    
    /** Like JUnit's Assert.assertEquals, but handles "regexp:" strings like HTML Selenese */
    public static void assertEquals(String s1, String s2) {
        SeleneseTestBase.assertEquals(s1, s2);
    }
    
    /** Like JUnit's Assert.assertEquals, but joins the string array with commas, and 
     * handles "regexp:" strings like HTML Selenese
     */
    public static void assertEquals(String s1, String[] s2) {
        SeleneseTestBase.assertEquals(s1, s2);
    }
    
    /** Asserts that two string arrays have identical string contents */
    public static void assertEquals(String[] s1, String[] s2) {
        SeleneseTestBase.assertEquals(s1, s2);
    }
    
    /** Asserts that two booleans are not the same */
    public static void assertNotEquals(boolean b1, boolean b2) {
        SeleneseTestBase.assertNotEquals(b1, b2);
    }
    
    /** Asserts that two objects are not the same (compares using .equals()) */
    public static void assertNotEquals(Object obj1, Object obj2) {
        SeleneseTestBase.assertNotEquals(obj1, obj2);
    }
    
    /** Compares two objects, but handles "regexp:" strings like HTML Selenese
     * @see #seleniumEquals(String, String)
     * @return true if actual matches the expectedPattern, or false otherwise
     */
    public static boolean seleniumEquals(Object expected, Object actual) {
        return SeleneseTestBase.seleniumEquals(expected, actual);
    }
    
    /** Compares two strings, but handles "regexp:" strings like HTML Selenese
     * 
     * @param expectedPattern
     * @param actual
     * @return true if actual matches the expectedPattern, or false otherwise
     */
    public static boolean seleniumEquals(String expected, String actual) {
        return SeleneseTestBase.seleniumEquals(expected, actual);
    }
    
    
    /**
     * @deprecated Use {@link #isCaptureScreenShotOnFailure()} instead
     */
    protected boolean isCaptureScreetShotOnFailure() {
        return isCaptureScreenShotOnFailure();
    }


    protected boolean isCaptureScreenShotOnFailure() {
        return stb.isCaptureScreenShotOnFailure();
    }
    
    protected String runtimeBrowserString() {
        return stb.runtimeBrowserString();
    }
    
    /**
     * @deprecated Use {@link #setCaptureScreenShotOnFailure(boolean)} instead
     */
    protected void setCaptureScreetShotOnFailure(boolean b) {
        setCaptureScreenShotOnFailure(b);
    }


    protected void setCaptureScreenShotOnFailure(boolean b) {
        stb.setCaptureScreenShotOnFailure(b);
    }
    
    protected void setTestContext() {
        selenium.setContext(this.getClass().getSimpleName() + "." + getName());
    }
    
    /**
     * Runs the bare test sequence, capturing a screenshot if a test fails
     * @exception Throwable if any exception is thrown
     */
    // @Override
    public void runBare() throws Throwable {
        if (!isCaptureScreenShotOnFailure()) {
            super.runBare();
            return;
        }
        setUp();
        try {
            runTest();
        } catch (Throwable t) {
            if (selenium != null) {
                String filename = getName() + ".png";
                try {
                    selenium.captureScreenshot(filename);
                    System.err.println("Saved screenshot " + filename);
                } catch (Exception e) {
                    System.err.println("Couldn't save screenshot " + filename + ": " + e.getMessage());
                    e.printStackTrace();
                }
                throw t;
            }
        }
        finally {
            tearDown();
        }
    }

    public String join(String[] array, char c) {
        return stb.join(array, c);
    }
    
}
