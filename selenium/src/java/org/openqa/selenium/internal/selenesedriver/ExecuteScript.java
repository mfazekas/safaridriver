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

import com.google.common.collect.Maps;
import com.thoughtworks.selenium.Selenium;

import java.util.List;
import java.util.Map;

public class ExecuteScript implements SeleneseFunction<Object> {
  public Object apply(Selenium selenium, Map<String, ?> args) {
    String script = prepareScript(args);

    System.out.println("script = " + script);

    String value = selenium.getEval(script);

    return populateReturnValue(value);
  }

  private String prepareScript(Map<String, ?> parameters) {
    String script = (String) parameters.get("script");
    script = String.format("(function() { %s })();", script)
        .replaceAll("\\bwindow\\.", "selenium.browserbot.getCurrentWindow().")
        .replaceAll("\\bdocument\\.", "selenium.browserbot.getDocument().");

    List<?> args = (List<?>) parameters.get("args");
    if (!args.isEmpty()) {
      for (int i = 0; i < args.size(); i++) {
        script = script.replaceAll("arguments\\[\\s*" + i + "\\s*\\]",
            getArgumentValue(args.get(i)));
      }
    }

    return script;
  }

  private String getArgumentValue(Object arg) {
    if (arg == null) {
      return null;
    } else if (arg instanceof String) {
      return String.format("'%s'", ((String) arg).replaceAll("'", "\\'"));
    } else {
      return String.valueOf(arg);
    }
  }

  private Object populateReturnValue(String value) {
    if ("__undefined__".equals(value)) {
      return null;
    } else if (value.matches("^\\d+$")) {
      return Long.parseLong(value);
    } else if (value.matches("^\\d+\\.\\d+$")) {
      return Double.parseDouble(value);
    } else if ("true".equals(value) || "false".equals(value)) {
      return Boolean.parseBoolean(value);
    } else {
      // Falll back to a string
      return value;
    }
  }
}
