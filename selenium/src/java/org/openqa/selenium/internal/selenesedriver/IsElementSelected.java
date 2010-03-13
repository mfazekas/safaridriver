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

public class IsElementSelected extends ElementFunction<Boolean> {

  public Boolean apply(Selenium selenium, Map<String, ?> args) {
    String locator = getLocator(args);

    // What are we dealing with?
    String value = selenium.getEval(selectedJs.replace("LOCATOR", locator));

    return Boolean.valueOf(value);
  }

  private final String selectedJs =
      "(function isSelected() { "
      + "  var e = selenium.browserbot.findElement('LOCATOR'); "
      + "  if ('OPTION' == e.tagName) return e.selected ? 'true' : 'false'; "
      + "  if (e.type == 'checkbox' || e.type == 'radio') return !!e.checked ? 'true' : 'false'; "
      + "  return 'false'"
      + "})()";
}
