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

package org.openqa.selenium.internal.seleniumemulation;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class MouseEventAt extends SeleneseCommand<Void> {
  private final ElementFinder finder;
  private final JavascriptLibrary js;
  private String type;

  public MouseEventAt(ElementFinder finder, JavascriptLibrary js, String type) {
    this.finder = finder;
    this.js = js;
    this.type = type;
  }

  @Override
  protected Void handleSeleneseCommand(WebDriver driver, String locator, String coordString) {
    WebElement element = finder.findElement(driver, locator);
    js.callEmbeddedSelenium(driver, "triggerMouseEventAt", element, type, coordString);
    
    return null;
  }
}