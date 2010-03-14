package org.openqa.selenium.iphone;

import java.net.URL;

import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.HttpCommandExecutor;

/**
 * @author jmleyba@gmail.com (Jason Leyba)
 */
public class IPhoneSimulatorDriver extends IPhoneDriver {

  public IPhoneSimulatorDriver(URL iWebDriverUrl, IPhoneSimulatorBinary iphoneSimulator)
      throws Exception {
    super(commandExecutor(iWebDriverUrl, iphoneSimulator));
  }

  static CommandExecutor commandExecutor(URL iWebDriverUrl, IPhoneSimulatorBinary iphoneSimulator)
       throws Exception {
    boolean isDev = Boolean.getBoolean("webdriver.iphone.useExisting");
    if (isDev) {
      return new HttpCommandExecutor(new URL(DEFAULT_IWEBDRIVER_URL));
    } else {
      return new IPhoneSimulatorCommandExecutor(iWebDriverUrl, iphoneSimulator);
    }
  }

  public IPhoneSimulatorDriver(String iWebDriverUrl, IPhoneSimulatorBinary iphoneSimulator)
      throws Exception {
    this(new URL(iWebDriverUrl), iphoneSimulator);
  }

  public IPhoneSimulatorDriver(IPhoneSimulatorBinary iphoneSimulator) throws Exception {
    this(DEFAULT_IWEBDRIVER_URL, iphoneSimulator);
  }

  @Override
  protected void startClient() {
    boolean isDev = Boolean.getBoolean("webdriver.iphone.useExisting");
    if (!isDev) {
      ((IPhoneSimulatorCommandExecutor) getCommandExecutor()).startClient();
    }
  }

  @Override
  protected void stopClient() {
    boolean isDev = Boolean.getBoolean("webdriver.iphone.useExisting");
    if (!isDev) {
      ((IPhoneSimulatorCommandExecutor) getCommandExecutor()).stopClient();
    }
  }
}
