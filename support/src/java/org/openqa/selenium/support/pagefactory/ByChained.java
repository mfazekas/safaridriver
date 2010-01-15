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

package org.openqa.selenium.support.pagefactory;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;

import java.util.ArrayList;
import java.util.List;

/**
 * Mechanism used to locate elements within a document using a series
 * of other lookups.  This class will find all DOM elements that much each
 * of the locators in sequence, eg.
 *
 * <pre>
 * driver.findElements(new ByChained(by1, by2))
 * </pre>
 *
 * will find all elements that match by2 and appear under an element
 * that matches by1.
 */
public class ByChained extends By {

  private By[] bys;

  public ByChained(By... bys) {
    this.bys = bys;
  }

  @Override
  public WebElement findElement(SearchContext context) {
    List<WebElement> elements = findElements(context);
    if (elements.size() == 0)
      throw new NoSuchElementException("Cannot locate an element using " + toString());
    return elements.get(0);
  }

  @Override
  public List<WebElement> findElements(SearchContext context) {
    if (bys.length == 0) {
      return new ArrayList<WebElement>();
    }

    List<WebElement> elems = null;
    for (By by : bys) {
      List<WebElement> newElems = new ArrayList<WebElement>();

      if (elems == null) {
        newElems.addAll(by.findElements(context));
      } else {
        for (WebElement elem : elems) {
          newElems.addAll(elem.findElements(by));
        }
      }
      elems = newElems;
    }

    return elems;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder("By.chained(");
    stringBuilder.append("{");

    boolean first = true;
    for (By by : bys) {
      stringBuilder.append((first ? "" : ",")).append(by);
      first = false;
    }
    stringBuilder.append("})");
    return stringBuilder.toString();
  }
}
