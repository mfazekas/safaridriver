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

package org.openqa.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.Ignore.Driver.CHROME;
import static org.openqa.selenium.Ignore.Driver.FIREFOX;
import static org.openqa.selenium.Ignore.Driver.IE;
import static org.openqa.selenium.Ignore.Driver.IPHONE;
import static org.openqa.selenium.Ignore.Driver.SELENESE;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CorrectEventFiringTest extends AbstractDriverTestCase {

  @Ignore(value = {CHROME, FIREFOX}, reason = "Webkit bug 22261. Firefox 3.6 wants focus")
  @JavascriptEnabled
  public void testShouldFireFocusEventWhenClicking() {
    driver.get(javascriptPage);

    clickOnElementWhichRecordsEvents();

    assertEventFired("focus");
  }

  @JavascriptEnabled
  public void testShouldFireClickEventWhenClicking() {
    driver.get(javascriptPage);

    clickOnElementWhichRecordsEvents();

    assertEventFired("click");
  }

  @JavascriptEnabled
  @Ignore(SELENESE)
  public void testShouldFireMouseDownEventWhenClicking() {
    driver.get(javascriptPage);

    clickOnElementWhichRecordsEvents();

    assertEventFired("mousedown");
  }

  @JavascriptEnabled
  @Ignore(SELENESE)
  public void testShouldFireMouseUpEventWhenClicking() {
    driver.get(javascriptPage);

    clickOnElementWhichRecordsEvents();

    assertEventFired("mouseup");
  }

  @JavascriptEnabled
  @Ignore(value = {SELENESE, CHROME})
  public void testShouldFireMouseOverEventWhenClicking() {
    driver.get(javascriptPage);

    clickOnElementWhichRecordsEvents();

    assertEventFired("mouseover");
  }

  @JavascriptEnabled
  @Ignore({SELENESE, CHROME, FIREFOX})
  public void testShouldFireMouseMoveEventWhenClicking() {
    driver.get(javascriptPage);

    clickOnElementWhichRecordsEvents();

    assertEventFired("mousemove");
  }

  @Ignore(value = {CHROME, SELENESE, FIREFOX}, reason = "Webkit bug 22261. Firefox 3.6 wants focus")
  @JavascriptEnabled
  public void testShouldFireEventsInTheRightOrder() {
    driver.get(javascriptPage);

    clickOnElementWhichRecordsEvents();

    String text = driver.findElement(By.id("result")).getText();

    int lastIndex = -1;
    for (String event : new String[]{"mousedown", "focus", "mouseup", "click"}) {
      int index = text.indexOf(event);

      assertTrue(event + " did not fire at all", index != -1);
      assertTrue(event + " did not fire in the correct order", index > lastIndex);
    }
  }

  @JavascriptEnabled
  @Ignore(SELENESE)
  public void testsShouldIssueMouseDownEvents() {
    driver.get(javascriptPage);
    driver.findElement(By.id("mousedown")).click();

    String result = driver.findElement(By.id("result")).getText();
    assertThat(result, equalTo("mouse down"));
  }

  @JavascriptEnabled
  public void testShouldIssueClickEvents() {
    driver.get(javascriptPage);
    driver.findElement(By.id("mouseclick")).click();

    String result = driver.findElement(By.id("result")).getText();
    assertThat(result, equalTo("mouse click"));
  }

  @JavascriptEnabled
  @Ignore(SELENESE)
  public void testShouldIssueMouseUpEvents() {
    driver.get(javascriptPage);
    driver.findElement(By.id("mouseup")).click();

    String result = driver.findElement(By.id("result")).getText();
    assertThat(result, equalTo("mouse up"));
  }

  @JavascriptEnabled
  @Ignore(value = {IPHONE, SELENESE})
  public void testMouseEventsShouldBubbleUpToContainingElements() {
    driver.get(javascriptPage);
    driver.findElement(By.id("child")).click();

    String result = driver.findElement(By.id("result")).getText();
    assertThat(result, equalTo("mouse down"));
  }

  @JavascriptEnabled
  @Ignore(value = {IPHONE, SELENESE})
  public void testShouldEmitOnChangeEventsWhenSelectingElements() {
    driver.get(javascriptPage);
    //Intentionally not looking up the select tag.  See selenium r7937 for details.
    List<WebElement> allOptions = driver.findElements(By.xpath("//select[@id='selector']//option"));

    String initialTextValue = driver.findElement(By.id("result")).getText();

    WebElement foo = allOptions.get(0);
    WebElement bar = allOptions.get(1);

    foo.setSelected();
    assertThat(driver.findElement(By.id("result")).getText(),
               equalTo(initialTextValue));
    bar.setSelected();
    assertThat(driver.findElement(By.id("result")).getText(),
               equalTo("bar"));
  }

  @JavascriptEnabled
  @Ignore(SELENESE)
  public void testShouldEmitOnChangeEventsWhenChangingTheStateOfACheckbox() {
    driver.get(javascriptPage);
    WebElement checkbox = driver.findElement(By.id("checkbox"));

    checkbox.setSelected();
    assertThat(driver.findElement(By.id("result")).getText(),
               equalTo("checkbox thing"));
  }

  @JavascriptEnabled
  public void testShouldEmitClickEventWhenClickingOnATextInputElement() {
    driver.get(javascriptPage);

    WebElement clicker = driver.findElement(By.id("clickField"));
    clicker.click();

    assertThat(clicker.getValue(), equalTo("Clicked"));
  }

  @JavascriptEnabled
  public void testClearingAnElementShouldCauseTheOnChangeHandlerToFire() {
    driver.get(javascriptPage);

    WebElement element = driver.findElement(By.id("clearMe"));
    element.clear();

    WebElement result = driver.findElement(By.id("result"));
    assertThat(result.getText(), equalTo("Cleared"));
  }
  
  @JavascriptEnabled
  @Ignore(value = {SELENESE, IPHONE},
      reason = "Chrome: Non-native event firing is broken in .\n"
               + "  Selenese: Fails when running in firefox.\n"
               + "  iPhone: sendKeys implementation is incorrect")
  public void testSendingKeysToAnotherElementShouldCauseTheBlurEventToFire() {
    if (browserNeedsFocusOnThisOs(driver)) {
      System.out.println("Skipping this test because browser demands focus");
      return;
    }

    driver.get(javascriptPage);
  	WebElement element = driver.findElement(By.id("theworks"));
  	element.sendKeys("foo");
  	WebElement element2 = driver.findElement(By.id("changeable"));
  	element2.sendKeys("bar");
  	assertEventFired("blur");
  }
  
  @JavascriptEnabled
  @Ignore(value = {SELENESE, CHROME, IPHONE},
      reason = ": Non-native event firing is broken in Chrome.\n"
               + "  Selenese: Fails when running in firefox.\n"
               + "  iPhone: sendKeys implementation is incorrect")
  public void testSendingKeysToAnElementShouldCauseTheFocusEventToFire() {
    if (browserNeedsFocusOnThisOs(driver)) {
      System.out.println("Skipping this test because browser demands focus");
      return;
    }
    
  	driver.get(javascriptPage);
  	WebElement element = driver.findElement(By.id("theworks"));
  	element.sendKeys("foo");
  	assertEventFired("focus");
  }

  @JavascriptEnabled
  @Ignore(IE)
  public void testSubmittingFormFromFormElementShouldFireOnSubmitForThatForm() {
    driver.get(javascriptPage);
    WebElement formElement = driver.findElement(By.id("submitListeningForm"));
    formElement.submit();
    assertEventFired("form-onsubmit");
  }

  @JavascriptEnabled
  @Ignore(IE)
  public void testSubmittingFormFromFormInputSubmitElementShouldFireOnSubmitForThatForm() {
    driver.get(javascriptPage);
    WebElement submit = driver.findElement(By.id("submitListeningForm-submit"));
    submit.submit();
    assertEventFired("form-onsubmit");
  }

  @JavascriptEnabled 
  @Ignore(IE)
  public void testSubmittingFormFromFormInputTextElementShouldFireOnSubmitForThatFormAndNotClickOnThatInput() {
    driver.get(javascriptPage);
    WebElement submit = driver.findElement(By.id("submitListeningForm-submit"));
    submit.submit();
    assertEventFired("form-onsubmit");
    assertEventNotFired("text-onclick");
  }

  @JavascriptEnabled 
  @Ignore(value = {CHROME, SELENESE, IPHONE},
      reason = "Does not yet support file uploads")
  public void testUploadingFileShouldFireOnChangeEvent() throws IOException {
    driver.get(formPage);
    WebElement uploadElement = driver.findElement(By.id("upload"));
    WebElement result = driver.findElement(By.id("fileResults"));
    assertThat(result.getText(), equalTo(""));

    File file = File.createTempFile("test", "txt");
    file.deleteOnExit();

    uploadElement.sendKeys(file.getAbsolutePath());
    // Shift focus to something else because send key doesn't make the focus leave
    driver.findElement(By.tagName("body")).click();

    assertThat(result.getText(), equalTo("changed"));
  }

  private void clickOnElementWhichRecordsEvents() {
    driver.findElement(By.id("plainButton")).click();
  }

  private void assertEventFired(String eventName) {
    WebElement result = driver.findElement(By.id("result"));
    String text = result.getText();
    assertTrue("No " + eventName + " fired: " + text, text.contains(eventName));
  }
  
  private void assertEventNotFired(String eventName) {
    WebElement result = driver.findElement(By.id("result"));
    String text = result.getText();
    assertFalse(eventName + " fired: " + text, text.contains(eventName));
  }
}
