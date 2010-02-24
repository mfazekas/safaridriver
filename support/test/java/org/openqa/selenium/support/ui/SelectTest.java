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

package org.openqa.selenium.support.ui;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelectTest extends MockObjectTestCase {
  public void testShouldThrowAnExceptionIfTheElementIsNotASelectElement() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      exactly(1).of(element).getTagName(); will(returnValue("a"));
    }});

    try {
      new Select(element);
      fail("Should not have passed");
    } catch (UnexpectedTagNameException e) {
      // This is expected
    }
  }

  public void testShouldIndicateThatASelectCanSupportMultipleOptions() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      exactly(1).of(element).getAttribute("multiple"); will(returnValue("multiple"));
    }});

    Select select = new Select(element);
    assertTrue(select.isMultiple());
  }

  public void testShouldNotIndicateThatANormalSelectSupportsMulitpleOptions() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      exactly(1).of(element).getAttribute("multiple"); will(returnValue(null));
    }});

    Select select = new Select(element);
    assertFalse(select.isMultiple());
  }

  public void testShouldReturnAllOptionsWhenAsked() {
    final WebElement element = mock(WebElement.class);
    final List<WebElement> options = Collections.emptyList();

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.tagName("option")); will(returnValue(options));
    }});

    Select select = new Select(element);
    List<WebElement> returnedOptions = select.getOptions();

    assertSame(options, returnedOptions);
  }

  public void testShouldReturnOptionsWhichAreSelected() {
    final WebElement element = mock(WebElement.class);
    final WebElement optionGood = mock(WebElement.class, "good");
    final WebElement optionBad = mock(WebElement.class, "bad");
    final List<WebElement> options = Arrays.asList(optionBad, optionGood);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.tagName("option")); will(returnValue(options));
      exactly(1).of(optionBad).isSelected(); will(returnValue(false));
      exactly(1).of(optionGood).isSelected(); will(returnValue(true));
    }});

    Select select = new Select(element);
    List<WebElement> returnedOptions = select.getAllSelectedOptions();

    assertEquals(1, returnedOptions.size());
    assertSame(optionGood, returnedOptions.get(0));
  }

  public void testShouldReturnFirstSelectedOptions() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final WebElement secondOption = mock(WebElement.class, "second");
    final List<WebElement> options = Arrays.asList(firstOption, secondOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.tagName("option")); will(returnValue(options));
      exactly(1).of(firstOption).isSelected(); will(returnValue(true));
      never(secondOption).isSelected(); will(returnValue(true));
    }});

    Select select = new Select(element);
    WebElement firstSelected = select.getFirstSelectedOption();

    assertSame(firstOption, firstSelected);
  }

  public void testShouldThrowANoSuchElementExceptionIfNothingIsSelected() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final List<WebElement> options = Arrays.asList(firstOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.tagName("option")); will(returnValue(options));
      exactly(1).of(firstOption).isSelected(); will(returnValue(false));
    }});

    Select select = new Select(element);

    try {
      select.getFirstSelectedOption();
      fail();
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  public void testShouldAllowOptionsToBeSelectedByVisibleText() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final List<WebElement> options = Arrays.asList(firstOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.xpath(".//option[. = \"fish\"]")); will(returnValue(options));
      exactly(1).of(firstOption).setSelected();
    }});

    Select select = new Select(element);
    select.selectByVisibleText("fish");
  }

  public void testShouldAllowOptionsToBeSelectedByIndex() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final WebElement secondOption = mock(WebElement.class, "second");
    final List<WebElement> options = Arrays.asList(firstOption, secondOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.tagName("option")); will(returnValue(options));
      exactly(1).of(firstOption).getAttribute("index"); will(returnValue("0"));
      never(firstOption).setSelected();
      exactly(1).of(secondOption).getAttribute("index"); will(returnValue("1"));
      exactly(1).of(secondOption).setSelected();
    }});

    Select select = new Select(element);
    select.selectByIndex(1);
  }

  public void testShouldAllowOptionsToBeSelectedByReturnedValue() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final List<WebElement> options = Arrays.asList(firstOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.xpath(".//option[@value = \"b\"]")); will(returnValue(options));
      exactly(1).of(firstOption).setSelected();
    }});

    Select select = new Select(element);
    select.selectByValue("b");
  }

  public void testShouldAllowUserToDeselectAllWhenSelectSupportsMultipleSelections() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final WebElement secondOption = mock(WebElement.class, "second");
    final List<WebElement> options = Arrays.asList(firstOption, secondOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.tagName("option")); will(returnValue(options));
      exactly(1).of(firstOption).isSelected(); will(returnValue(true));
      exactly(1).of(firstOption).toggle();
      exactly(1).of(secondOption).isSelected(); will(returnValue(false));
      never(secondOption).toggle();
    }});

    Select select = new Select(element);
    select.deselectAll();
  }

  public void testShouldNotAllowUserToDeselectAllWhenSelectDoesNotSupportMultipleSelections() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      exactly(1).of(element).getAttribute("multiple"); will(returnValue(""));
    }});

    Select select = new Select(element);
    try {
      select.deselectAll();
      fail();
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  public void testShouldAllowUserToDeselectOptionsByVisibleText() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final WebElement secondOption = mock(WebElement.class, "second");
    final List<WebElement> options = Arrays.asList(firstOption, secondOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.xpath(".//option[. = \"b\"]")); will(returnValue(options));
      exactly(1).of(firstOption).isSelected(); will(returnValue(true));
      exactly(1).of(firstOption).toggle();
      exactly(1).of(secondOption).isSelected(); will(returnValue(false));
      never(secondOption).toggle();
    }});

    Select select = new Select(element);
    select.deselectByVisibleText("b");
  }

  public void testShouldAllowOptionsToBeDeselectedByIndex() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final WebElement secondOption = mock(WebElement.class, "second");
    final List<WebElement> options = Arrays.asList(firstOption, secondOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.tagName("option")); will(returnValue(options));
      exactly(1).of(firstOption).getAttribute("index"); will(returnValue("2"));
      exactly(1).of(firstOption).isSelected(); will(returnValue(true));
      exactly(1).of(firstOption).toggle();
      exactly(1).of(secondOption).getAttribute("index"); will(returnValue("1"));
      never(secondOption).setSelected();
    }});

    Select select = new Select(element);
    select.deselectByIndex(2);
  }

  public void testShouldAllowOptionsToBeDeselectedByReturnedValue() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final WebElement secondOption = mock(WebElement.class, "third");
    final List<WebElement> options = Arrays.asList(firstOption, secondOption);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
      exactly(1).of(element).findElements(By.xpath(".//option[@value = \"b\"]")); will(returnValue(options));
      exactly(1).of(firstOption).isSelected(); will(returnValue(true));
      exactly(1).of(firstOption).toggle();
      exactly(1).of(secondOption).isSelected(); will(returnValue(false));
      never(secondOption).toggle();
    }});

    Select select = new Select(element);
    select.deselectByValue("b");
  }

  public void testShouldConvertAnUnquotedStringIntoOneWithQuotes() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
    }});
    
    Select select = new Select(element);
    String result = select.escapeQuotes("foo");

    assertEquals("\"foo\"", result);
  }

  public void testShouldConvertAStringWithATickIntoOneWithQuotes() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
    }});

    Select select = new Select(element);
    String result = select.escapeQuotes("f'oo");

    assertEquals("\"f'oo\"", result);
  }

  public void testShouldConvertAStringWithAQuotIntoOneWithTicks() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
    }});

    Select select = new Select(element);
    String result = select.escapeQuotes("f\"oo");

    assertEquals("'f\"oo'", result);
  }
  
  public void testShouldProvideConcatenatedStringsWhenStringToEscapeContainsTicksAndQuotes() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
    }});

    Select select = new Select(element);
    String result = select.escapeQuotes("f\"o'o");

    assertEquals("concat(\"f\", '\"', \"o'o\")", result);
  }
  
  /**
   * Tests that escapeQuotes returns concatenated strings when the given
   * string contains a tick and and ends with a quote.
   */
  public void testShouldProvideConcatenatedStringsWhenStringEndsWithQuote() {
    final WebElement element = mock(WebElement.class);

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue("multiple"));
    }});

    Select select = new Select(element);
    String result = select.escapeQuotes("'\"");

    assertEquals("concat(\"'\", '\"')", result);
  }

  public void testShouldFallBackToSlowLooksUpsWhenGetByVisibleTextFailsAndThereIsASpace() {
    final WebElement element = mock(WebElement.class);
    final WebElement firstOption = mock(WebElement.class, "first");
    final By xpath1 = By.xpath(".//option[. = \"foo bar\"]");
    final By xpath2 = By.xpath(".//option[contains(., \"foo\")]");

    checking(new Expectations() {{
      allowing(element).getTagName(); will(returnValue("select"));
      allowing(element).getAttribute("multiple"); will(returnValue(""));
      one(element).findElements(xpath1); will(returnValue(Collections.EMPTY_LIST));
      one(element).findElements(xpath2); will(returnValue(Collections.singletonList(firstOption)));
      one(firstOption).getText(); will(returnValue("foo bar"));
      one(firstOption).setSelected();
    }});

    Select select = new Select(element);
    select.selectByVisibleText("foo bar");
  }
}
