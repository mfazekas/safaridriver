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

import org.openqa.selenium.remote.server.rest.ResultType;
import org.openqa.selenium.remote.server.DriverSessions;
import org.openqa.selenium.remote.server.KnownElements;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;

public class ElementEquality extends WebElementHandler {
  private String otherId;
  private Response response;

  public ElementEquality(DriverSessions sessions) {
    super(sessions);
  }

  public void setOther(String otherId) {
    this.otherId = otherId;
  }


  public ResultType call() throws Exception {
    response = newResponse();

    WebElement one = getElement();
    WebElement two = getKnownElements().get(otherId);

    // Unwrap the elements, if necessary
    if (one instanceof WrapsElement) {
      one = ((WrapsElement) one).getWrappedElement();
    }
    if (two instanceof KnownElements.ProxiedElement) {
      two = ((KnownElements.ProxiedElement) two).getWrappedElement();
    }
    
    response.setValue(one.equals(two));

    return ResultType.SUCCESS;
  }

   public Response getResponse() {
    return response;
  }

  @Override
  public String toString() {
    return String.format("[equals: %s, %s]", getElementAsString(), otherId);
  }
}
