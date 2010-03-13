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

import java.util.Map;

public class ToggleElement extends ElementFunction<Boolean> {
  public Boolean apply(Selenium selenium, Map<String, ?> args) {
    String locator = getLocator(args);

    String type = selenium.getAttribute(locator + "@type");
    if ("radio".equalsIgnoreCase(type)) {
      throw new UnsupportedOperationException("You may not toggle a radio button");
    }

    boolean wasChecked = selenium.isChecked(locator);
    if (wasChecked) {
      selenium.uncheck(locator);
    } else {
      selenium.check(locator);
    }

    return selenium.isChecked(locator);
  }
}
