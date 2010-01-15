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

package org.openqa.selenium.remote.server.handler;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.server.DriverSessions;
import org.openqa.selenium.remote.server.rest.ResultType;

import java.util.Collections;

public class FindActiveElement extends WebDriverHandler {
    private Response response;

    public FindActiveElement(DriverSessions sessions) {
      super(sessions);
    }

    public ResultType call() throws Exception {
      response = newResponse();

      WebElement element = getDriver().switchTo().activeElement();
      String elementId = getKnownElements().add(element);
      response.setValue(Collections.singletonList(String.format("element/%s", elementId)));

      return ResultType.SUCCESS;
    }

    public Response getResponse() {
      return response;
    }
    
    @Override
    public String toString() {
      return "[find active element]";
    }
}
