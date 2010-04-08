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

package org.openqa.selenium.remote.server.handler;

import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.server.DriverSessions;
import org.openqa.selenium.remote.server.rest.ResultType;

import static org.openqa.selenium.OutputType.BASE64;

public class CaptureScreenshot extends WebDriverHandler {

  private Response response;

  public CaptureScreenshot(DriverSessions sessions) {
    super(sessions);
  }

  public ResultType call() throws Exception {
    response = newResponse();

    WebDriver driver = unwrap(getDriver());

    response.setValue(((TakesScreenshot) driver).getScreenshotAs(BASE64));
    return ResultType.SUCCESS;
  }

  private WebDriver unwrap(WebDriver driver) {
    WebDriver toReturn = driver;
    while (toReturn instanceof WrapsDriver) {
      toReturn = ((WrapsDriver) toReturn).getWrappedDriver();
    }
    return toReturn;
  }

  public Response getResponse() {
    return response;
  }

  @Override
  public String toString() {
    return "[take screenshot]";
  }
}