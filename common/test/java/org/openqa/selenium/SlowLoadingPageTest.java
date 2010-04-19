package org.openqa.selenium;

import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.Ignore.Driver.CHROME;
import static org.openqa.selenium.Ignore.Driver.IE;
import static org.openqa.selenium.Ignore.Driver.IPHONE;
import static org.openqa.selenium.Ignore.Driver.SELENESE;

public class SlowLoadingPageTest extends AbstractDriverTestCase {

  private static final long LOAD_TIME_IN_SECONDS = 3;

  @Ignore(value = {IE, IPHONE, SELENESE}, reason = "Untested browsers")
  public void testShouldBlockUnitlPageLoads() {
    long start = System.currentTimeMillis();
    driver.get(pages.sleepingPage + "?time=" + LOAD_TIME_IN_SECONDS);
    long now = System.currentTimeMillis();
    assertEllapsed(LOAD_TIME_IN_SECONDS * 1000, now - start);
  }

  @Ignore(value = {IE, IPHONE, SELENESE, CHROME}, reason = "Chrome: doesn't work; Others: untested")
  public void testRefreshShouldBlockUntilPageLoads() {
    long start = System.currentTimeMillis();
    driver.get(pages.sleepingPage + "?time=" + LOAD_TIME_IN_SECONDS);
    assertEllapsed(LOAD_TIME_IN_SECONDS * 1000, System.currentTimeMillis() - start);
    long refreshed = System.currentTimeMillis();
    driver.navigate().refresh();
    assertEllapsed(LOAD_TIME_IN_SECONDS * 1000, System.currentTimeMillis() - refreshed);
  }

  private static void assertEllapsed(long expected, long actual) {
    assertTrue(expected + "ms should have ellapsed, but was: " + actual, expected <= actual);
  }
}
