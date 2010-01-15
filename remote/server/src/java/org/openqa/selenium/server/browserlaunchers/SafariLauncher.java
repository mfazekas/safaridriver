/*
 * Copyright 2006 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.openqa.selenium.server.browserlaunchers;

import org.openqa.selenium.server.BrowserConfigurationOptions;
import org.openqa.selenium.server.RemoteControlConfiguration;

public class SafariLauncher implements BrowserLauncher {

    final BrowserLauncher realLauncher;
    
    public SafariLauncher(BrowserConfigurationOptions browserOptions, RemoteControlConfiguration configuration, String sessionId, String browserLaunchLocation) {

        String mode = browserOptions.get("mode");
        if (mode == null) mode = "filebased";
        if ("default".equals(mode)) mode = "filebased";
        
        if ("filebased".equals(mode)) {
            realLauncher = new SafariFileBasedLauncher(browserOptions, configuration, sessionId, browserLaunchLocation);
            return;
        }
        
        boolean proxyInjectionMode = browserOptions.is("proxyInjectionMode") || "proxyInjection".equals(mode);
        
        // You can't just individually configure a browser for PI mode; it's a server-level configuration parameter
        boolean globalProxyInjectionMode = configuration.getProxyInjectionModeArg();
        if (proxyInjectionMode && !globalProxyInjectionMode) {
            if (proxyInjectionMode) {
                throw new RuntimeException("You requested proxy injection mode, but this server wasn't configured with -proxyInjectionMode on the command line");
            }
        }
        
        // if user didn't request PI, but the server is configured that way, just switch up to PI
        proxyInjectionMode = globalProxyInjectionMode;
        if (proxyInjectionMode) {
            realLauncher = new ProxyInjectionSafariCustomProfileLauncher(browserOptions, configuration, sessionId, browserLaunchLocation);
            return;
        }
        
        // the mode isn't "chrome" or "proxyInjection"; at this point it had better be "proxy"
        if (!"proxy".equals(mode)) {
            throw new RuntimeException("Unrecognized browser mode: " + mode);
        }
        
        realLauncher = new SafariCustomProfileLauncher(browserOptions, configuration, sessionId, browserLaunchLocation);
                
    }

    public void close() {
        realLauncher.close();
    }

    public Process getProcess() {
        return realLauncher.getProcess();
    }

    public void launchHTMLSuite(String suiteUrl, String baseUrl) {
        realLauncher.launchHTMLSuite(suiteUrl, baseUrl);
    }

    public void launchRemoteSession(String url) {
        realLauncher.launchRemoteSession(url);
    }

}