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
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.Ignore.Driver.CHROME;
import static org.openqa.selenium.Ignore.Driver.IE;
import static org.openqa.selenium.Ignore.Driver.IPHONE;
import static org.openqa.selenium.Ignore.Driver.SELENESE;
import org.openqa.selenium.environment.GlobalTestEnvironment;

public class PageLoadingTest extends AbstractDriverTestCase {

  public void testShouldWaitForDocumentToBeLoaded() {
    driver.get(simpleTestPage);

    assertThat(driver.getTitle(), equalTo("Hello WebDriver"));
  }

  public void testShouldFollowRedirectsSentInTheHttpResponseHeaders() {
    driver.get(redirectPage);

    assertThat(driver.getTitle(), equalTo("We Arrive Here"));
  }

  public void testShouldFollowMetaRedirects() throws Exception {
    driver.get(metaRedirectPage);
    assertThat(driver.getTitle(), equalTo("We Arrive Here"));
  }

  @Ignore(SELENESE)
  public void testShouldBeAbleToGetAFragmentOnTheCurrentPage() {
    driver.get(xhtmlTestPage);
    driver.get(xhtmlTestPage + "#text");
    driver.findElement(By.id("id1"));
  }

  @Ignore(SELENESE)
  public void testShouldReturnWhenGettingAUrlThatDoesNotResolve() {
    try {
      // Of course, we're up the creek if this ever does get registered
      driver.get("http://www.thisurldoesnotexist.comx/");
    } catch (IllegalStateException e) {
      if (!isIeDriverTimedOutException(e)) {
        throw e;
      }
    }
  }

  @Ignore({IPHONE, SELENESE})
  public void testShouldReturnWhenGettingAUrlThatDoesNotConnect() {
    // Here's hoping that there's nothing here. There shouldn't be
    driver.get("http://localhost:3001");
  }

  @Ignore({IPHONE, SELENESE})
  public void testShouldBeAbleToLoadAPageWithFramesetsAndWaitUntilAllFramesAreLoaded() {
    driver.get(framesetPage);

    driver.switchTo().frame(0);
    WebElement pageNumber = driver.findElement(By.xpath("//span[@id='pageNumber']"));
    assertThat(pageNumber.getText().trim(), equalTo("1"));

    driver.switchTo().frame(1);
    pageNumber = driver.findElement(By.xpath("//span[@id='pageNumber']"));
    assertThat(pageNumber.getText().trim(), equalTo("2"));
  }

  @Ignore({IPHONE, SELENESE})
  @NeedsFreshDriver
  public void testSouldDoNothingIfThereIsNothingToGoBackTo() {
    String originalTitle = driver.getTitle();
    driver.get(formPage);

    driver.navigate().back();
    // We may have returned to the browser's home page
    assertThat(driver.getTitle(), anyOf(equalTo(originalTitle), equalTo("We Leave From Here")));
  }

  @Ignore(SELENESE)
  public void testShouldBeAbleToNavigateBackInTheBrowserHistory() {
    driver.get(formPage);

    driver.findElement(By.id("imageButton")).submit();
    assertThat(driver.getTitle(), equalTo("We Arrive Here"));

    driver.navigate().back();
    assertThat(driver.getTitle(), equalTo("We Leave From Here"));
  }

  @Ignore(SELENESE)
  public void testShouldBeAbleToNavigateBackInTheBrowserHistoryInPresenceOfIframes() {
    driver.get(xhtmlTestPage);

    driver.findElement(By.name("sameWindow")).click();
    assertThat(driver.getTitle(), equalTo("This page has iframes"));

    driver.navigate().back();
    assertThat(driver.getTitle(), equalTo("XHTML Test Page"));
  }

  @Ignore(SELENESE)
  public void testShouldBeAbleToNavigateForwardsInTheBrowserHistory() {
    driver.get(formPage);

    driver.findElement(By.id("imageButton")).submit();
    assertThat(driver.getTitle(), equalTo("We Arrive Here"));

    driver.navigate().back();
    assertThat(driver.getTitle(), equalTo("We Leave From Here"));

    driver.navigate().forward();
    assertThat(driver.getTitle(), equalTo("We Arrive Here"));
  }

  @Ignore({IE, CHROME, SELENESE, IPHONE})
  public void testShouldBeAbleToAccessPagesWithAnInsecureSslCertificate() {
    String url = GlobalTestEnvironment.get().getAppServer().whereIsSecure("simpleTest.html");
    driver.get(url);

    // This should work
    assertThat(driver.getTitle(), equalTo("Hello WebDriver"));
  }

  @Ignore({IE, SELENESE})
  public void testShouldBeAbleToRefreshAPage() {
    driver.get(xhtmlTestPage);

    driver.navigate().refresh();

    assertThat(driver.getTitle(), equalTo("XHTML Test Page"));
  }

  /**
   * @throws Exception If the test fails.
   * @see <a href="http://code.google.com/p/selenium/issues/detail?id=208">
   *     Issue 208</a>
   */
  @Ignore(value = {IE, SELENESE, CHROME, IPHONE}, reason = "Untested user-agents")
  @JavascriptEnabled
  public void testShouldNotHangIfDocumentOpenCallIsNeverFollowedByDocumentCloseCall()
      throws Exception {
    driver.get(documentWrite);

    // If this command succeeds, then all is well.
    driver.findElement(By.xpath("//body"));
  }
}
