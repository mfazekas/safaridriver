package org.openqa.selenium.safari;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the {#link WebDriver} interface that drives Safari.
 * This works through a safari extension (in ObjC) which is plugged into
 * safari through some hook mechanism.
 * 
 * @author kurniady@google.com (Andrian Kurniady)
 */
public class SafariDriver implements WebDriver {
  private SafariBinary safariBinary;
  private RemoteWebDriver rwd;
  
  public SafariDriver() throws Exception {
    SafariBinary safariBinary = new SafariBinary();
    safariBinary.startSafari();
    try {
      this.rwd = new RemoteWebDriver(new URL(safariBinary.getUrl()), DesiredCapabilities.iphone());
      this.safariBinary = safariBinary;
    } catch (Exception e) {
      safariBinary.quit();
    }
  }

  @Override
  public void close() {
    safariBinary.quit();
  }

  @Override
  public WebElement findElement(By by) {
    return rwd.findElement(by);
  }

  @Override
  public List<WebElement> findElements(By by) {
    return rwd.findElements(by);
  }

  @Override
  public void get(String url) {
    rwd.get(url);
  }

  @Override
  public String getCurrentUrl() {
    return rwd.getCurrentUrl();
  }

  @Override
  public String getPageSource() {
    return rwd.getPageSource();
  }

  @Override
  public String getTitle() {
    return rwd.getTitle();
  }

  @Override
  public String getWindowHandle() {
    return rwd.getWindowHandle();
  }

  @Override
  public Set<String> getWindowHandles() {
    return rwd.getWindowHandles();
  }

  @Override
  public Options manage() {
    return rwd.manage();
  }

  @Override
  public Navigation navigate() {
    return rwd.navigate();
  }

  @Override
  public void quit() {
    safariBinary.quit();
  }

  @Override
  public TargetLocator switchTo() {
    return rwd.switchTo();
  }
}
