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

package org.openqa.selenium.support.pagefactory.internal;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import java.lang.reflect.Proxy;

public class LocatingElementHandlerTest extends MockObjectTestCase {
    public void testShouldAlwaysLocateTheElementPerCall() throws NoSuchFieldException {
        final ElementLocator locator = mock(ElementLocator.class);
        final WebElement element = mock(WebElement.class);

        checking(new Expectations() {{
                exactly(2).of(locator).findElement(); will(returnValue(element));
                one(element).sendKeys("Fishy");
                one(element).submit();
        }});

        LocatingElementHandler handler = new LocatingElementHandler(locator);
        WebElement proxy = (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{WebElement.class}, handler);

        proxy.sendKeys("Fishy");
        proxy.submit();
    }

    public void testShouldDelegateToARenderedWebElementIfNecessary() throws NoSuchFieldException {
      final ElementLocator locator = mock(ElementLocator.class);
      final RenderedWebElement element = mock(RenderedWebElement.class);

      checking(new Expectations() {{
            allowing(locator).findElement(); will(returnValue(element));
            one(element).getLocation();
      }});

      LocatingElementHandler handler = new LocatingElementHandler(locator);
      RenderedWebElement proxy = (RenderedWebElement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{RenderedWebElement.class}, handler);

      proxy.getLocation();
    }

    public void testShouldUseAnnotationsToLookUpByAlternativeMechanisms() {
        final WebDriver driver = mock(WebDriver.class);
        final WebElement element = mock(WebElement.class);

        final By by = By.xpath("//input[@name='q']");

        checking(new Expectations() {{
            allowing(driver).findElement(by); will(returnValue(element));
            one(element).clear();
            one(element).sendKeys("cheese");
        }});

        Page page = PageFactory.initElements(driver, Page.class);
        page.doQuery("cheese");
    }

    public void testShouldNotRepeatedlyLookUpElementsMarkedAsNeverChanging() throws Exception {
      final ElementLocator locator = mock(ElementLocator.class);
      final WebElement element = mock(WebElement.class);

      checking(new Expectations() {{
        allowing(locator).findElement(); will(returnValue(element));
        one(element).isEnabled();
        one(element).sendKeys("Cheese");
      }});

      LocatingElementHandler handler = new LocatingElementHandler(locator);
      WebElement proxy = (WebElement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{WebElement.class}, handler);

      proxy.isEnabled();
      proxy.sendKeys("Cheese");
    }

  public void testFindByAnnotationShouldBeInherited() {
    ChildPage page = new ChildPage();

    final WebDriver driver = mock(WebDriver.class);
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(driver).findElement(By.xpath("//input[@name='q']")); will(returnValue(element));
      one(element).getValue(); will(returnValue(""));
    }});

    PageFactory.initElements(driver, page);
    page.doChildQuery();
  }

  public static class Page {

    @SuppressWarnings("unused")
    private WebElement q;

    @FindBy(how = How.XPATH, using = "//input[@name='q']")
    protected WebElement query;

    @SuppressWarnings("unused")
    @CacheLookup
    private WebElement staysTheSame;

    @SuppressWarnings("unused")
    private RenderedWebElement rendered;

    public void doQuery(String foo) {
      query.clear();
      query.sendKeys(foo);
    }
  }

  public static class ChildPage extends Page {
    public void doChildQuery() {
      query.getValue();
    }
  }
}
