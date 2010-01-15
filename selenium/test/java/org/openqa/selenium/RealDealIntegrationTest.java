/*
 * Copyright 2004 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.openqa.selenium;

import org.testng.annotations.Test;

import com.thoughtworks.selenium.SeleneseTestNgHelper;
import com.thoughtworks.selenium.SeleniumException;
import com.thoughtworks.selenium.SeleniumLogLevels;

/**
 * @author Paul Hammant
 * @version $Revision: 131 $
 */
public class RealDealIntegrationTest extends SeleneseTestNgHelper {

    @Test
    public void testWithJavaScript() {
        selenium.setContext("A real test, using the real Selenium on the browser side served by Jetty, driven from Java");
        selenium.setBrowserLogLevel(SeleniumLogLevels.DEBUG);
        selenium.open("/selenium-server/tests/html/test_click_page1.html");
        assertTrue("link 'link' doesn't contain expected text", 
                selenium.getText("link").indexOf("Click here for next page") != -1);
        String[] links = selenium.getAllLinks();
        assertTrue(links.length > 3);
        assertEquals(links[3], "linkToAnchorOnThisPage");
        selenium.click("link");
        selenium.waitForPageToLoad("10000");
        assertTrue(selenium.getLocation().endsWith("/selenium-server/tests/html/test_click_page2.html"));
        selenium.click("previousPage");
        selenium.waitForPageToLoad("10000");
        assertTrue(selenium.getLocation().endsWith("/selenium-server/tests/html/test_click_page1.html"));
    }
    
    @Test
    public void testAgain() {
        testWithJavaScript();
    }
    
    @Test
    public void testFailure() {
        selenium.setContext("A real negative test, using the real Selenium on the browser side served by Jetty, driven from Java");
        selenium.open("/selenium-server/tests/html/test_click_page1.html");
        String badElementName = "This element doesn't exist, so Selenium should throw an exception";
        try {
            selenium.getText(badElementName);
            fail("No exception was thrown!");
        } catch (SeleniumException se) {
           assertTrue("Exception message isn't as expected: " + se.getMessage(), se.getMessage().indexOf(badElementName + " not found") != -1);
        }
        
        assertFalse("Negative test", selenium.isTextPresent("Negative test: verify non-existent text"));
   }

}

