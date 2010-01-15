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

// Copyright 2008 Google Inc.  All Rights Reserved.

package org.openqa.selenium.remote.server.handler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

public class BySelector {

  public By pickFrom(String method, String selector) {
    if ("class name".equals(method)) {
      return By.className(selector);
    } else if ("id".equals(method)) {
      return By.id(selector);
    } else if ("link text".equals(method)) {
      return By.linkText(selector);
    } else if ("partial link text".equals(method)) {
      return By.partialLinkText(selector);
    } else if ("name".equals(method)) {
      return By.name(selector);
    } else if ("tag name".equals(method)) {
      return By.tagName(selector);
    } else if ("xpath".equals(method)) {
      return By.xpath(selector);
    } else {
      throw new WebDriverException("Cannot find matching element locator to: " + method);
    }
  }
}
