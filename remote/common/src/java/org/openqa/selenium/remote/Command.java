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

package org.openqa.selenium.remote;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Command {

  private SessionId sessionId;
  private DriverCommand name;
  private Map<String, ?> parameters;

  public Command(SessionId sessionId, DriverCommand name) {
    this(sessionId, name, new HashMap<String, Object>());
  }

  public Command(SessionId sessionId, DriverCommand name, Map<String, ?> parameters) {
    this.sessionId = sessionId;
    this.parameters = parameters;
    this.name = name;
  }

  public SessionId getSessionId() {
    return sessionId;
  }

  public DriverCommand getName() {
    return name;
  }

  public Map<String, ?> getParameters() {
    return parameters;
  }

  @Override
  public String toString() {
    return "[" + sessionId + ", " + "]: " + name + " " + new JSONObject(parameters).toString();
  }
}
