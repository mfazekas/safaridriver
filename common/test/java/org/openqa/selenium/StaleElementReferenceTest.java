/*
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.openqa.selenium;

import static org.openqa.selenium.Ignore.Driver.SELENESE;

public class StaleElementReferenceTest extends AbstractDriverTestCase {

  public void testOldPage() {
    driver.get(simpleTestPage);
    WebElement elem = driver.findElement(By.id("links"));
    driver.get(xhtmlTestPage);
    try {
      elem.click();
      fail();
    } catch (StaleElementReferenceException e) {
      // do nothing. this is what we expected.
    }
  }

  @JavascriptEnabled
  @Ignore(SELENESE)
  public void testShouldNotCrashWhenCallingGetSizeOnAnObsoleteElement() {
    driver.get(simpleTestPage);
    RenderedWebElement elem = (RenderedWebElement) driver.findElement(By.id("links"));
    driver.get(xhtmlTestPage);
    try {
      elem.getSize();
      fail();
    } catch (StaleElementReferenceException e) {
      // do nothing. this is what we expected.
    }
  }

  @JavascriptEnabled
  @Ignore(SELENESE)
  public void testShouldNotCrashWhenQueryingTheAttributeOfAStaleElement() {
    driver.get(xhtmlTestPage);
    WebElement heading = driver.findElement(By.xpath("//h1"));
    driver.get(simpleTestPage);
    try {
      heading.getAttribute("class");
      fail();
    } catch (StaleElementReferenceException e) {
      // do nothing. this is what we expected.
    }
  }
}
