/*
Copyright 2009 WebDriver committers
Copyright 2009 Google Inc.

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

import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.OutputType.*;

import java.io.File;

public class TakesScreenshotTest extends AbstractDriverTestCase {
  public void testSaveScreenshotAsFile() throws Exception {
    if (!isAbleToTakeScreenshots(driver)) {
      return;
    }

    driver.get(simpleTestPage);
    File tempFile = getScreenshot().getScreenshotAs(OutputType.FILE);
    assertTrue(tempFile.exists());
    assertTrue(tempFile.length() > 0);
    tempFile.delete();
  }

  public void testCaptureToBase64() throws Exception {
    if (!isAbleToTakeScreenshots(driver)) {
      return;
    }

    driver.get(simpleTestPage);
    String screenshot = getScreenshot().getScreenshotAs(BASE64);
    assertTrue(screenshot.length() > 0);
  }
  
  public TakesScreenshot getScreenshot() {
    return (TakesScreenshot)driver;
  }

  private boolean isAbleToTakeScreenshots(WebDriver driver) throws Exception {
    return driver instanceof TakesScreenshot;
  }
}
