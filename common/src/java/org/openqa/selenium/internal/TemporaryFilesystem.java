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

package org.openqa.selenium.internal;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.internal.FileHandler;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A wrapper around temporary filesystem behaviour.
 *
 * @author gblock@google.com (Gregory Block)
 */
public class TemporaryFilesystem {
  private static final Set<File> temporaryFiles = new CopyOnWriteArraySet<File>();
  private static final File baseDir = new File(System.getProperty("java.io.tmpdir"));
  private static final Thread shutdownHook = new Thread() {
    @Override public void run() {
      deleteTemporaryFiles();
    }
  };

  /**
   * Add the static initialization hook; verify java.io.tmpdir is writable.
   */
  static {
    Runtime.getRuntime().addShutdownHook(shutdownHook);

    if (!baseDir.exists()) {
      throw new WebDriverException("Unable to find tmp dir: " + baseDir.getAbsolutePath());
    }
    if (!baseDir.canWrite()) {
      throw new WebDriverException("Unable to write to tmp dir: " + baseDir.getAbsolutePath());
    }
  }

  private TemporaryFilesystem() {
    // Static utility class, no public constructor.
  }

  /**
   * Create a temporary directory, and track it for deletion.
   *
   * @param prefix the prefix to use when creating the temporary directory
   * @param suffix the suffix to use when creating the temporary directory
   * @return the temporary directory to create
   */
  public static File createTempDir(String prefix, String suffix) {
    try {
      // Create a tempfile, and delete it.
      File file = File.createTempFile(prefix, suffix, baseDir);
      file.delete();

      // Create it as a directory.
      File dir = new File(file.getAbsolutePath());
      if (dir == null || !dir.mkdirs()) {
        throw new WebDriverException("Cannot create profile directory at " + dir.getAbsolutePath());
      }

      // Create the directory and mark it writable.
      FileHandler.createDir(dir);

      temporaryFiles.add(dir);
      return dir;
    } catch (IOException e) {
      throw new WebDriverException(
          "Unable to create temporary file at " + baseDir.getAbsolutePath());
    }
  }

  /**
   * Delete a temporary directory that we were responsible for creating.
   *
   * @param file the file to delete
   * @throws WebDriverException if interrupted
   */
  public static void deleteTempDir(File file) {
    if (!shouldReap()) {
      return;
    }

    // If the tempfile can be removed, delete it.  If not, it wasn't created by us.
    if (temporaryFiles.remove(file)) {
      FileHandler.delete(file);
    }
  }

  /**
   * Perform the operation that a shutdown hook would have.
   */
  public static void deleteTemporaryFiles() {
    if (!shouldReap()) {
      return;
    }

    for (File file : temporaryFiles) {
      try {
        FileHandler.delete(file);
      } catch (WebDriverException e) {
        // ignore; an interrupt will already have been logged.
      }
    }
  }

  /**
   * Returns true if we should be reaping profiles.  Used to control tempfile deletion.
   *
   * @return true if reaping is enabled.
   */
  static boolean shouldReap() {
    String reap = System.getProperty("webdriver.reap_profile", "true");
    return Boolean.valueOf(reap);
  }
}
