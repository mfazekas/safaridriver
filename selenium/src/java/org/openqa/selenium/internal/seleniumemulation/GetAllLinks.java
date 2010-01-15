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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class GetAllLinks extends SeleneseCommand<String[]> {
  @Override
  protected String[] handleSeleneseCommand(WebDriver driver, String locator, String value) {
    List<WebElement> allLinks = driver.findElements(By.xpath("//a"));
    Iterator<WebElement> i = allLinks.iterator();
    List<String> links = new ArrayList<String>();
    while (i.hasNext()) {
      WebElement link = i.next();
      String id = link.getAttribute("id");
      if (id == null)
        links.add("");
      else
        links.add(id);
    }

    return links.toArray(new String[links.size()]);

  }
}
