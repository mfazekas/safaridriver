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

public class FireEvent extends SeleneseCommand<Void> {
  private final ElementFinder elementFinder;
  private final JavascriptLibrary js;

  public FireEvent(ElementFinder elementFinder, JavascriptLibrary js) {
    this.elementFinder = elementFinder;
    this.js = js;
  }

  @Override
  protected Void handleSeleneseCommand(WebDriver driver, String locator, String value) {
    WebElement element = elementFinder.findElement(driver, locator);
    js.callEmbeddedSelenium(driver, "doFireEvent", element, value);

    return null;
  }
}
