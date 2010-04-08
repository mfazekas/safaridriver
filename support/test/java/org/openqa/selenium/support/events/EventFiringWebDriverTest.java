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

package org.openqa.selenium.support.events;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StubDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebElement;

/**
 * @author Michael Tamm
 */
public class EventFiringWebDriverTest extends MockObjectTestCase {

    public void testNavigationEvents() {
        final WebDriver mockedDriver = mock(WebDriver.class);
        final Navigation mockedNavigation = mock(Navigation.class);
        final StringBuilder log = new StringBuilder();

        checking(new Expectations() {{
            one(mockedDriver).get("http://www.get.com");
            exactly(3).of(mockedDriver).navigate(); will(returnValue(mockedNavigation));
            one(mockedNavigation).to("http://www.navigate-to.com");
            one(mockedNavigation).back();
            one(mockedNavigation).forward();
        }});

        EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(new AbstractWebDriverEventListener() {
            public void beforeNavigateTo(String url, WebDriver driver) { log.append("beforeNavigateTo ").append(url).append("\n"); }
            public void afterNavigateTo(String url, WebDriver driver) { log.append("afterNavigateTo ").append(url).append("\n"); }
            public void beforeNavigateBack(WebDriver driver) { log.append("beforeNavigateBack\n"); }
            public void afterNavigateBack(WebDriver driver) { log.append("afterNavigateBack\n"); }
            public void beforeNavigateForward(WebDriver driver) { log.append("beforeNavigateForward\n"); }
            public void afterNavigateForward(WebDriver driver) { log.append("afterNavigateForward\n"); }
        });

        testedDriver.get("http://www.get.com");
        testedDriver.navigate().to("http://www.navigate-to.com");
        testedDriver.navigate().back();
        testedDriver.navigate().forward();

        assertEquals(
            "beforeNavigateTo http://www.get.com\n" +
            "afterNavigateTo http://www.get.com\n" +
            "beforeNavigateTo http://www.navigate-to.com\n" +
            "afterNavigateTo http://www.navigate-to.com\n" +
            "beforeNavigateBack\n" +
            "afterNavigateBack\n" +
            "beforeNavigateForward\n" +
            "afterNavigateForward\n",
            log.toString()
        );
    }

    public void testClickEvent() {
        final WebDriver mockedDriver = mock(WebDriver.class);
        final WebElement mockedElement = mock(WebElement.class);
        final StringBuilder log = new StringBuilder();

        checking(new Expectations() {{
            one(mockedDriver).findElement(By.name("foo")); will(returnValue(mockedElement));
            one(mockedElement).click();
        }});

        EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(new AbstractWebDriverEventListener() {
            public void beforeClickOn(WebElement element, WebDriver driver) { log.append("beforeClickOn\n"); }
            public void afterClickOn(WebElement element, WebDriver driver) { log.append("afterClickOn\n"); }
        });

        testedDriver.findElement(By.name("foo")).click();

        assertEquals(
            "beforeClickOn\n" +
            "afterClickOn\n",
            log.toString()
        );
    }

    public void testChangeValueEvent() {
        final WebDriver mockedDriver = mock(WebDriver.class);
        final WebElement mockedElement = mock(WebElement.class);
        final StringBuilder log = new StringBuilder();

        checking(new Expectations() {{
            exactly(3).of(mockedDriver).findElement(By.name("foo")); will(returnValue(mockedElement));
            one(mockedElement).clear();
            one(mockedElement).sendKeys("some text");
            one(mockedElement).toggle();
        }});

        EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(new AbstractWebDriverEventListener() {
            public void beforeChangeValueOf(WebElement element, WebDriver driver) { log.append("beforeChangeValueOf\n"); }
            public void afterChangeValueOf(WebElement element, WebDriver driver) { log.append("afterChangeValueOf\n"); }
        });

        testedDriver.findElement(By.name("foo")).clear();
        testedDriver.findElement(By.name("foo")).sendKeys("some text");
        testedDriver.findElement(By.name("foo")).toggle();

        assertEquals(
            "beforeChangeValueOf\n" +
            "afterChangeValueOf\n" +
            "beforeChangeValueOf\n" +
            "afterChangeValueOf\n" +
            "beforeChangeValueOf\n" +
            "afterChangeValueOf\n",
            log.toString()
        );
    }

    public void testFindByEvent() {
        final WebDriver mockedDriver = mock(WebDriver.class);
        final WebElement mockedElement = mock(WebElement.class);
        final StringBuilder log = new StringBuilder();

        checking(new Expectations() {{
            one(mockedDriver).findElement(By.id("foo")); will(returnValue(mockedElement));
            one(mockedElement).findElement(By.linkText("bar"));
            one(mockedElement).findElements(By.name("xyz"));
            one(mockedDriver).findElements(By.xpath("//link[@type = 'text/css']"));
        }});

        EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver).register(new AbstractWebDriverEventListener() {
            public void beforeFindBy(By by, WebElement element, WebDriver driver) { log.append("beforeFindBy from ").append(element == null ? "WebDriver" : "WebElement").append(" ").append(by).append("\n"); }
            public void afterFindBy(By by, WebElement element, WebDriver driver) { log.append("afterFindBy from ").append(element == null ? "WebDriver" : "WebElement").append(" ").append(by).append("\n"); }
        });

        WebElement element = testedDriver.findElement(By.id("foo"));
        element.findElement(By.linkText("bar"));
        element.findElements(By.name("xyz"));
        testedDriver.findElements(By.xpath("//link[@type = 'text/css']"));

        assertEquals(
            "beforeFindBy from WebDriver By.id: foo\n" +
            "afterFindBy from WebDriver By.id: foo\n" +
            "beforeFindBy from WebElement By.linkText: bar\n" +
            "afterFindBy from WebElement By.linkText: bar\n" +
            "beforeFindBy from WebElement By.name: xyz\n" +
            "afterFindBy from WebElement By.name: xyz\n" +
            "beforeFindBy from WebDriver By.xpath: //link[@type = 'text/css']\n" +
            "afterFindBy from WebDriver By.xpath: //link[@type = 'text/css']\n",
            log.toString()
        );
    }

  public void testShouldCallListenersWhenAnExceptionIsThrown() {
    final WebDriver mockedDriver = mock(WebDriver.class);
    final StringBuilder log = new StringBuilder();

    final NoSuchElementException exception = new NoSuchElementException("argh");

    checking(new Expectations() {{
        one(mockedDriver).findElement(By.id("foo")); will(throwException(exception));
      }});

    EventFiringWebDriver testedDriver =
        new EventFiringWebDriver(mockedDriver).register(new AbstractWebDriverEventListener() {
          public void onException(Throwable throwable, WebDriver driver) {
            log.append(throwable.getMessage());
          }
        });

    try {
      testedDriver.findElement(By.id("foo"));
      fail("Expected exception to be propogated");
    } catch (NoSuchElementException e) {
      // Fine
    }

    assertEquals(exception.getMessage(), log.toString());
  }

  public void testShouldUnpackElementArgsWhenCallingScripts() {
    final ExececutingDriver mockedDriver = mock(ExececutingDriver.class);
    final WebElement stubbedElement = mock(WebElement.class);

    checking(new Expectations() {{
        one(mockedDriver).findElement(By.id("foo")); will(returnValue(stubbedElement));
        allowing(stubbedElement);
        one(mockedDriver).executeScript("foo", stubbedElement); will(returnValue("foo"));
      }});

    EventFiringWebDriver testedDriver = new EventFiringWebDriver(mockedDriver);
    testedDriver.register(new AbstractWebDriverEventListener() {} );

    WebElement element = testedDriver.findElement(By.id("foo"));
    try {
      testedDriver.executeScript("foo", element);
    } catch (RuntimeException e) {
      // This is the error we're trying to fix
      throw e;
    }
  }

  public void testShouldBeAbleToWrapSubclassesOfSomethingImplementingTheWebDriverInterface() {
    try {
      new EventFiringWebDriver(new ChildDriver());
      // We should get this far
    } catch (ClassCastException e) {
      e.printStackTrace();
      fail("Should have been able to wrap the child of a webdriver implementing interface");
    }
  }

  private static interface ExececutingDriver extends WebDriver, JavascriptExecutor {}

  private static class ChildDriver extends StubDriver {}
}
