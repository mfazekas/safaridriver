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

package org.openqa.selenium.remote.server;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DriverSessions {
  private final DriverFactory factory;

  private final Map<SessionId, Session> sessionIdToDriver =
      new ConcurrentHashMap<SessionId, Session>();

  private static Map<Capabilities, String> defaultDrivers = new HashMap<Capabilities, String>() {{
    put(DesiredCapabilities.chrome(), "org.openqa.selenium.chrome.ChromeDriver");
    put(DesiredCapabilities.firefox(), "org.openqa.selenium.firefox.FirefoxDriver");
    put(DesiredCapabilities.htmlUnit(), "org.openqa.selenium.htmlunit.HtmlUnitDriver");
    put(DesiredCapabilities.internetExplorer(), "org.openqa.selenium.ie.InternetExplorerDriver");
  }};

  public DriverSessions() {
    this(Platform.getCurrent(), new DriverFactory());
  }

  protected DriverSessions(Platform runningOn, DriverFactory factory) {
    this.factory = factory;
    registerDefaults(runningOn);
  }

  private void registerDefaults(Platform current) {
    for (Map.Entry<Capabilities, String> entry : defaultDrivers.entrySet()) {
      Capabilities caps = entry.getKey();
      if (caps.getPlatform() != null && caps.getPlatform().is(current)) {
        registerDriver(caps, entry.getValue());
      } else if (caps.getPlatform() == null) {
        registerDriver(caps, entry.getValue());
      }
    }
  }

  private void registerDriver(Capabilities caps, String className) {
    try {
      registerDriver(caps, Class.forName(className).asSubclass(WebDriver.class));
    } catch (ClassNotFoundException e) {
      // OK. Fall through. We just won't be able to create these
      e.printStackTrace();
    } catch (NoClassDefFoundError e) {
      // OK. Missing a dependency, which is obviously a Bad Thing
      // TODO(simon): Log this!
    }
  }

  public SessionId newSession(Capabilities desiredCapabilities) throws Exception {
    Session session = new Session(factory, desiredCapabilities);
    
    SessionId sessionId = new SessionId(String.valueOf(System.currentTimeMillis()));
    sessionIdToDriver.put(sessionId, session);
    return sessionId;
  }
  
  public Session get(SessionId sessionId) {
    return sessionIdToDriver.get(sessionId);
  }

  public void deleteSession(SessionId sessionId) {
    sessionIdToDriver.remove(sessionId);
  }

  public void registerDriver(Capabilities capabilities, Class<? extends WebDriver> implementation) {
    factory.registerDriver(capabilities, implementation);
  }
}
