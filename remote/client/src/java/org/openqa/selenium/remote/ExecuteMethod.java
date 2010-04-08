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

package org.openqa.selenium.remote;

import java.util.Map;

/**
 * An encapsulation of
 * {@link org.openqa.selenium.remote.RemoteWebDriver#executeScript(String, Object...)}.
 */
public class ExecuteMethod {
  private final RemoteWebDriver driver;

  public ExecuteMethod(RemoteWebDriver driver) {
    this.driver = driver;
  }

  /**
   * Execute the given command on the remote webdriver server. Any exceptions
   * will be thrown by the underlying execute method.
   *
   * @param commandName The remote command to execute
   * @param parameters The parameters to execute that command with
   * @return The result of {@link Response#getValue()}.
   */
  public Object execute(DriverCommand commandName, Map<String, Object> parameters) {
    Response response;

    if (parameters == null || parameters.size() == 0) {
      response = driver.execute(commandName);
    } else {
      response = driver.execute(commandName, parameters);
    }

    return response.getValue();
  }
}
