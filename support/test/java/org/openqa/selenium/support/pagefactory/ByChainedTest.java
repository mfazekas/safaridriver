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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

public class ByChainedTest extends MockObjectTestCase {
  public void testFindElementZeroBy() {
    final AllDriver driver = mock(AllDriver.class);

    ByChained by = new ByChained();
    try {
      by.findElement(driver);
      fail("Expected NoSuchElementException!");
    } catch (NoSuchElementException e) {
      // Expected
    }
  }

  public void testFindElementsZeroBy() {
    final AllDriver driver = mock(AllDriver.class);

    ByChained by = new ByChained();
    assertThat(by.findElements(driver),
               equalTo((List<WebElement>) new ArrayList<WebElement>()));
  }

  public void testFindElementOneBy() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(WebElement.class, "webElement2");
    final List<WebElement> elems12 = new ArrayList();
    elems12.add(elem1);
    elems12.add(elem2);

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems12));
    }});

    ByChained by = new ByChained(By.name("cheese"));
    assertThat(by.findElement(driver), equalTo(elem1));
  }

  public void testFindElementsOneBy() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(WebElement.class, "webElement2");
    final List<WebElement> elems12 = new ArrayList();
    elems12.add(elem1);
    elems12.add(elem2);

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems12));
    }});

    ByChained by = new ByChained(By.name("cheese"));
    assertThat(by.findElements(driver), equalTo(elems12));
  }

  public void testFindElementOneByEmpty() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(WebElement.class, "webElement2");
    final List<WebElement> elems = new ArrayList();

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems));
    }});

    ByChained by = new ByChained(By.name("cheese"));
    try {
      by.findElement(driver);
      fail("Expected NoSuchElementException!");
    } catch (NoSuchElementException e) {
      // Expected
    }
  }

  public void testFindElementsOneByEmpty() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(WebElement.class, "webElement2");
    final List<WebElement> elems = new ArrayList();

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems));
    }});

    ByChained by = new ByChained(By.name("cheese"));
    assertThat(by.findElements(driver), equalTo(elems));
  }

  public void testFindElementTwoBy() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(AllElement.class, "webElement1");
    final WebElement elem2 = mock(AllElement.class, "webElement2");
    final WebElement elem3 = mock(AllElement.class, "webElement3");
    final WebElement elem4 = mock(AllElement.class, "webElement4");
    final WebElement elem5 = mock(AllElement.class, "webElement5");
    final List<WebElement> elems12 = new ArrayList();
    elems12.add(elem1);
    elems12.add(elem2);
    final List<WebElement> elems34 = new ArrayList();
    elems34.add(elem3);
    elems34.add(elem4);
    final List<WebElement> elems5 = new ArrayList();
    elems5.add(elem5);
    final List<WebElement> elems345 = new ArrayList();
    elems345.addAll(elems34);
    elems345.addAll(elems5);

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems12));
      one(elem1).findElements(By.name("photo")); will(returnValue(elems34));
      one(elem2).findElements(By.name("photo")); will(returnValue(elems5));
    }});

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    assertThat(by.findElement(driver), equalTo(elem3));
  }

  public void testFindElementTwoByEmptyParent() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(AllElement.class, "webElement2");
    final WebElement elem3 = mock(AllElement.class, "webElement3");
    final WebElement elem4 = mock(AllElement.class, "webElement4");
    final WebElement elem5 = mock(AllElement.class, "webElement5");

    final List<WebElement> elems = new ArrayList();
    final List<WebElement> elems12 = new ArrayList();
    elems12.add(elem1);
    elems12.add(elem2);
    final List<WebElement> elems34 = new ArrayList();
    elems34.add(elem3);
    elems34.add(elem4);
    final List<WebElement> elems5 = new ArrayList();
    elems5.add(elem5);
    final List<WebElement> elems345 = new ArrayList();
    elems345.addAll(elems34);
    elems345.addAll(elems5);

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems));
    }});

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    try {
      by.findElement(driver);
      fail("Expected NoSuchElementException!");
    } catch (NoSuchElementException e) {
      // Expected
    }
  }

  public void testFindElementsTwoByEmptyParent() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(AllElement.class, "webElement2");
    final WebElement elem3 = mock(AllElement.class, "webElement3");
    final WebElement elem4 = mock(AllElement.class, "webElement4");
    final WebElement elem5 = mock(AllElement.class, "webElement5");

    final List<WebElement> elems = new ArrayList();
    final List<WebElement> elems12 = new ArrayList();
    elems12.add(elem1);
    elems12.add(elem2);
    final List<WebElement> elems34 = new ArrayList();
    elems34.add(elem3);
    elems34.add(elem4);
    final List<WebElement> elems5 = new ArrayList();
    elems5.add(elem5);
    final List<WebElement> elems345 = new ArrayList();
    elems345.addAll(elems34);
    elems345.addAll(elems5);

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems));
    }});

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    assertThat(by.findElements(driver), equalTo(elems));
  }

  public void testFindElementTwoByEmptyChild() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(AllElement.class, "webElement2");
    final WebElement elem3 = mock(AllElement.class, "webElement3");
    final WebElement elem4 = mock(AllElement.class, "webElement4");
    final WebElement elem5 = mock(AllElement.class, "webElement5");

    final List<WebElement> elems = new ArrayList();
    final List<WebElement> elems12 = new ArrayList();
    elems12.add(elem1);
    elems12.add(elem2);
    final List<WebElement> elems34 = new ArrayList();
    elems34.add(elem3);
    elems34.add(elem4);
    final List<WebElement> elems5 = new ArrayList();
    elems5.add(elem5);
    final List<WebElement> elems345 = new ArrayList();
    elems345.addAll(elems34);
    elems345.addAll(elems5);

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems12));
      one(elem1).findElements(By.name("photo")); will(returnValue(elems));
      one(elem2).findElements(By.name("photo")); will(returnValue(elems5));
    }});

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    assertThat(by.findElement(driver), equalTo(elem5));
  }

  public void testFindElementsTwoByEmptyChild() {
    final AllDriver driver = mock(AllDriver.class);
    final WebElement elem1 = mock(WebElement.class, "webElement1");
    final WebElement elem2 = mock(AllElement.class, "webElement2");
    final WebElement elem3 = mock(AllElement.class, "webElement3");
    final WebElement elem4 = mock(AllElement.class, "webElement4");
    final WebElement elem5 = mock(AllElement.class, "webElement5");

    final List<WebElement> elems = new ArrayList();
    final List<WebElement> elems12 = new ArrayList();
    elems12.add(elem1);
    elems12.add(elem2);
    final List<WebElement> elems34 = new ArrayList();
    elems34.add(elem3);
    elems34.add(elem4);
    final List<WebElement> elems5 = new ArrayList();
    elems5.add(elem5);
    final List<WebElement> elems345 = new ArrayList();
    elems345.addAll(elems34);
    elems345.addAll(elems5);

    checking(new Expectations() {{
      one(driver).findElementsByName("cheese"); will(returnValue(elems12));
      one(elem1).findElements(By.name("photo")); will(returnValue(elems));
      one(elem2).findElements(By.name("photo")); will(returnValue(elems5));
    }});

    ByChained by = new ByChained(By.name("cheese"), By.name("photo"));
    assertThat(by.findElements(driver), equalTo(elems5));
  }

  public void testEquals() {
    assertThat(new ByChained(By.id("cheese"), By.name("photo")),
               equalTo(new ByChained(By.id("cheese"), By.name("photo"))));
  }

  private interface AllDriver extends FindsById, FindsByLinkText, FindsByName, FindsByXPath, SearchContext {
    // Place holder
  }

  private interface AllElement extends WebElement, SearchContext {
    // Place holder
  }
}
