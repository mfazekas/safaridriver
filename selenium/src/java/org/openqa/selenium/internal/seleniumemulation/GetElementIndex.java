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

public class GetElementIndex extends SeleneseCommand<Number> {
  private ElementFinder finder;
  private JavascriptLibrary js;

  public GetElementIndex(ElementFinder finder, JavascriptLibrary js) {
    this.finder = finder;
    this.js = js;
  }

  @Override
  protected Number handleSeleneseCommand(WebDriver driver, String locator, String value) {
    WebElement element = finder.findElement(driver, locator);
    String script =
      "var _isCommentOrEmptyTextNode = function(node) {\n" +
      "    return node.nodeType == 8 || ((node.nodeType == 3) && !(/[^\\t\\n\\r ]/.test(node.data)));\n" +
      "}\n" +
      "    var element = arguments[0];\n" +
      "    var previousSibling;\n" +
      "    var index = 0;\n" +
      "    while ((previousSibling = element.previousSibling) != null) {\n" +
      "        if (!_isCommentOrEmptyTextNode(previousSibling)) {\n" +
      "            index++;\n" +
      "        }\n" +
      "        element = previousSibling;\n" +
      "    }\n" +
      "    return index;";
    return (Long) js.executeScript(driver, script, element);
  }
}
