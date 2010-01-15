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

package org.openqa.selenium.firefox.internal;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import java.util.concurrent.TimeUnit;

/**
 * Tests for the {@link SocketLock} to make sure I'm not batshit crazy.
 * 
 * @author gregory.block@gmail.com (Gregory Block)
 */
public class SocketLockTest extends TestCase {

  @Test
  public void wellKnownLockLocation() {
    Lock lock = new SocketLock(12345);
    lock.lock(TimeUnit.SECONDS.toMillis(1));
    lock.unlock();
  }
  
  @Test
  public void serialLockOnSamePort() {
    for (int i = 0; i < 20; i++) {
      Lock lock = new SocketLock(34567);
      lock.lock(TimeUnit.SECONDS.toMillis(1));
      lock.unlock();
    }
  }
  
  @Test
  public void attemptToReuseLocksFails() {
    Lock lock = new SocketLock(23456);
    lock.lock(TimeUnit.SECONDS.toMillis(1));
    lock.unlock();
    try {
      lock.lock(TimeUnit.SECONDS.toMillis(1));
      Assert.fail("Expected a SocketException to be thrown when reused");
    } catch (WebDriverException e) {
      
      // Lock reuse not permitted; expected.
    }
  }
}
