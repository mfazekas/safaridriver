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

package org.openqa.selenium.safari;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.internal.FileHandler;

public class SafariExtension {

  public static final String SAFARI_EXTENSION_DIRECTORY_PROPERTY = "webdriver.safari.extensiondir";
  public static final String SAFARI_EXTENSION_NAME = "SafariExtension.bundle";
  
  private static volatile File defaultExtensionDir;
  
  public void addListenPortEnv(Map<String, String> envs, URL remoteURL) {
    String port = String.valueOf(remoteURL.getPort());
    envs.put("WEBDRIVER_SAFARI_PORT", port);
  }
  
  private String getBundlePath() {
    return findSafariExtensionDir().getAbsolutePath();
  }
  
  public void addLoadExtensionEnvs(Map<String, String> envs,String binaryPath) {
    File safariExtensionDir = findSafariExtensionDir();
    envs.put("DYLD_INSERT_LIBRARIES",bundleInjectorLibrary(safariExtensionDir));
    envs.put("XCInjectBundle", getBundlePath());
    envs.put("XCInjectBundleInto", binaryPath);
  }
  
  private static final String DEFAULT_EXTENSION_PATH = "/safari-extension.zip";
  
  public static File findSafariExtensionDir() {
    File directory = defaultExtensionDir;
    if (directory == null) {
      synchronized (SafariExtension.class) {
        directory = defaultExtensionDir;
        if (directory == null) {
          directory = defaultExtensionDir = loadExtension();
        }
      }
    }
    return directory;
  } 
  
  static private String bundleInjectorLibrary(File extensionDir) {
    //return "/Developer/Library/PrivateFrameworks/DevToolsBundleInjection.framework/DevToolsBundleInjection";
    File contentsDir = new File(extensionDir,"Contents");
    File sharedSupportDir = new File(contentsDir,"SharedSupport");
    File bundleInjectionDylib = new File(sharedSupportDir,"BundleInjector.dylib");
    return bundleInjectionDylib.getAbsolutePath();
  }
	
  private static File checkExtensionContent(File baseDir) throws IOException {
    File extensionDir = new File(baseDir,SAFARI_EXTENSION_NAME);
    if (!extensionDir.isDirectory()) {
      throw new FileNotFoundException(String.format(
                  "The specified directory is not a Safari Extension directory: %s; Try setting %s",
                  extensionDir.getAbsolutePath(), SAFARI_EXTENSION_DIRECTORY_PROPERTY));
    }
    
    File contentsDir = new File(extensionDir,"Contents");
    File infoPlistFile = new File(contentsDir,"Info.plist");
    if (!infoPlistFile.exists()) {
      throw new FileNotFoundException(String.format("The specified extension has no Info.plist at:%s!",infoPlistFile.getAbsolutePath()));
    }
    
    File bundleInjectorLibrary = new File(bundleInjectorLibrary(extensionDir));
    if (!bundleInjectorLibrary.exists()) {
      throw new FileNotFoundException(String.format("The specified extension has no BunndleInjection.dylib at:%s!",bundleInjectorLibrary.getAbsolutePath()));
    }
    
    return extensionDir;
  }

  private static File loadExtension() {
    try {
      File extensionDir;
      String directory = System.getProperty(SAFARI_EXTENSION_DIRECTORY_PROPERTY);
      if (directory != null && !"".equals(directory)) {
        extensionDir = new File(directory);
      } else {
        InputStream stream = SafariExtension.class.getResourceAsStream(DEFAULT_EXTENSION_PATH);
        extensionDir = FileHandler.unzip(stream);
      }
      return checkExtensionContent(extensionDir);
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
  }
}
