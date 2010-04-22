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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.internal.FileHandler;
import org.openqa.selenium.internal.TemporaryFilesystem;

public class SafariExtension {

  public static final String SAFARI_EXTENSION_DIRECTORY_PROPERTY = "webdriver.safari.extensiondir";
  public static final String SAFARI_EXTENSION_NAME = "SafariExtension.bundle";
  private File extensionDir = null;
  private List<File> dirsToDelete = new ArrayList<File>();
  
  public void addListenPortEnv(Map<String, String> envs, URL remoteURL) {
    String port = String.valueOf(remoteURL.getPort());
    envs.put("WEBDRIVER_SAFARI_PORT", port);
  }
  
  private File findSafariExtensionDir() {
	if (extensionDir == null) {
	  extensionDir = loadExtension();
	}
	return extensionDir;
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
  
  static private String bundleInjectorLibrary(File extensionDir) {
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

  private File loadExtension() {
    try {
      File extensionDir;
      String directory = System.getProperty(SAFARI_EXTENSION_DIRECTORY_PROPERTY);
      if (directory != null && !"".equals(directory)) {
        extensionDir = new File(directory);
      } else {
        InputStream stream = SafariExtension.class.getResourceAsStream(DEFAULT_EXTENSION_PATH);
        File tempExtensionDir = FileHandler.unzip(stream);
        dirsToDelete.add(tempExtensionDir);
        extensionDir = TemporaryFilesystem.createTempDir("webdriver", "extension");
        dirsToDelete.add(extensionDir);
        if (extensionDir.exists() && !FileHandler.delete(extensionDir)) {
          throw new IOException("Unable to delete existing extension directory: " + extensionDir);
        }

        FileHandler.createDir(extensionDir);
        FileHandler.makeWritable(extensionDir);
        FileHandler.copy(tempExtensionDir, extensionDir);
      }
      return checkExtensionContent(extensionDir);
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
  }

  public void clean() {
	for (File file: dirsToDelete) {
	  TemporaryFilesystem.deleteTempDir(file);
	}
  }
}
