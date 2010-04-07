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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import static org.openqa.selenium.Ignore.Driver.IE;
import static org.openqa.selenium.Ignore.Driver.REMOTE;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TestSuiteBuilder;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

public class RemoteWebDriverIeTestSuite extends TestCase {
  public static Test suite() throws Exception {
    if (!(Platform.getCurrent().is(Platform.WINDOWS))) {      
      TestSuite toReturn = new TestSuite();
      toReturn.addTestSuite(EmptyTest.class);
      return toReturn;
    }

//    System.setProperty("webdriver.development", "true");
//    String arch = System.getProperty("os.arch").toLowerCase() + "/";
//    if (arch.contains("64")) {
//      System.setProperty("jna.library.path", "..\\build\\x64\\Debug;build\\x64\\Debug");
//    } else {
//      System.setProperty("jna.library.path", "..\\build\\Win32\\Debug;build\\Win32\\Debug");
//    }

    Test rawSuite =
        new TestSuiteBuilder()
            .addSourceDir("common")
            .addSourceDir("remote/client")
            .addSourceDir("remote/server")
            .keepDriverInstance()
            .includeJavascriptTests()
            .usingDriver(RemoteIeWebDriverForTest.class)
            .exclude(IE)
            .exclude(REMOTE)
            .excludePattern(".*IntegrationTest")
            .excludePattern(".*CaptureNetworkTrafficTest")
            .excludePattern(".*FirefoxMiniHTMLRunnerTest")
            .excludePattern(".*LinuxHTMLRunnerMultiWindowTest")
            .excludePattern(".*CaptureNetworkTrafficCommandTest")
            .excludePattern(".*HtmlIdentifierTest")
            .excludePattern(".*\\.browserlaunchers\\..*")
            .excludePattern(".*FunctionalTest")
            .excludePattern(".*UnitTest")
            .excludePattern(".*LauncherTest")
            .create();

    TestSuite toReturn = new TestSuite();
    toReturn.addTest(new RemoteWebDriverTestSuite.RemoteDriverServerStarter(rawSuite));
    return toReturn;
  }

  public static class RemoteIeWebDriverForTest extends RemoteWebDriver {
    public RemoteIeWebDriverForTest() throws Exception {
      super(new URL("http://localhost:6000/common/hub"), DesiredCapabilities.internetExplorer());
    }
  }

  public static class EmptyTest extends TestCase {
    public void testDoNothingButLetTheSuiteWork() {
      // Does nothing
    }
  }
}
