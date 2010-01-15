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

package org.openqa.selenium.internal.selenesedriver;

import com.thoughtworks.selenium.Selenium;

import org.openqa.selenium.Platform;
import org.openqa.selenium.internal.selenesedriver.SeleneseFunction;
import org.openqa.selenium.remote.Capabilities;

import java.util.HashMap;
import java.util.Map;

public class NewSession implements SeleneseFunction<Map<String, Object>> {
  public Map<String, Object> apply(Selenium selenium, Object... args) {
    selenium.start();
    Capabilities capabilities = (Capabilities) args[0];
    Map<String, Object> seenCapabilities = new HashMap<String, Object>();
    seenCapabilities.put("browserName", capabilities.getBrowserName());
    seenCapabilities.put("version", capabilities.getVersion());
    seenCapabilities.put("platform", Platform.getCurrent().toString());
    seenCapabilities.put("javascriptEnabled", true);
    return seenCapabilities;
  }
}
