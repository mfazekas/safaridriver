package org.openqa.selenium.safari;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.internal.Base64Encoder;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

/**
 * An implementation of the {#link WebDriver} interface that drives Safari.
 * This works through a safari extension (in ObjC) which is plugged into
 * safari through some hook mechanism.
 * 
 * @author kurniady@google.com (Andrian Kurniady)
 */
public class SafariDriver extends RemoteWebDriver implements TakesScreenshot  {
  private SafariBinary safariBinary;
  
  public SafariDriver(SafariBinary safariBinary) throws Exception {
	super(new SafariCommandExecutor(safariBinary),DesiredCapabilities.iphone());
	this.safariBinary = safariBinary;
  }

  public SafariDriver() throws Exception {
	this(new SafariBinary());
  }
  
  @Override
  protected void startClient() {
	boolean isDev = Boolean.getBoolean("webdriver.safari.useExisting");
	if (!isDev) {
	  SafariCommandExecutor safariCommandExecutor = (SafariCommandExecutor)this.getCommandExecutor();
	  safariCommandExecutor.binary().startSafari();
	}
  }
  
  @Override
  protected void stopClient() {
	this.safariBinary.quit();
  }

  public byte[] getScreenshot() {
    return (byte[]) execute(DriverCommand.SCREENSHOT).getValue();
  }

  public <X> X getScreenshotAs(OutputType<X> target) {
    byte[] rawPng = getScreenshot();
    String base64Png = new Base64Encoder().encode(rawPng);
    // ... and convert it.
    return target.convertFromBase64Png(base64Png);
  }
}
