/*
Copyright 2007-2010 WebDriver committers
Copyright 2007-2010 Google Inc.

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

package org.openqa.selenium.ie;

import org.openqa.selenium.AbstractDriverTestCase;
import org.openqa.selenium.JavascriptEnabled;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eran.mes@gmail.com (Eran Mes)
 *
 */
public class InternetExplorerJavascriptTests extends AbstractDriverTestCase {
  @SuppressWarnings("unchecked")
  @JavascriptEnabled
  public void testShouldBeAbleToExecuteSimpleJavascriptAndAStringsArray() {
    if (!(driver instanceof JavascriptExecutor)) {
      return;
    }

    driver.get(javascriptPage);
    List<Object> expectedResult = new ArrayList<Object>();
    expectedResult.add("zero");
    expectedResult.add("one");
    expectedResult.add("two");
    try {
      Object result = ((JavascriptExecutor) driver).executeScript(
        "return ['zero', 'one', 'two'];");
      fail("Was supposed to get an exception - no such type yet.");
    } catch (WebDriverException e) {
      assertTrue(e.getMessage().contains("Cannot determine result type"));
    }
  }
}
