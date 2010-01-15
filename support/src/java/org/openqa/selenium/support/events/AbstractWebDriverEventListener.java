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

package org.openqa.selenium.support.events;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Use this class as base class, if you want to implement
 * a {@link WebDriverEventListener} and are only interested
 * in some events. All methods provided by this class have
 * an empty method body.
 *
 * @author Michael Tamm
 */
public abstract class AbstractWebDriverEventListener implements WebDriverEventListener {

    public void beforeNavigateTo(String url, WebDriver driver) {
        // Do nothing.
    }

    public void afterNavigateTo(String url, WebDriver driver) {
        // Do nothing.
    }

    public void beforeNavigateBack(WebDriver driver) {
        // Do nothing.
    }

    public void afterNavigateBack(WebDriver driver) {
        // Do nothing.
    }

    public void beforeNavigateForward(WebDriver driver) {
        // Do nothing.
    }

    public void afterNavigateForward(WebDriver driver) {
        // Do nothing.
    }

    public void beforeFindBy(By by, WebElement element, WebDriver driver) {
        // Do nothing.
    }

    public void afterFindBy(By by, WebElement element, WebDriver driver) {
        // Do nothing.
    }

    public void beforeClickOn(WebElement element, WebDriver driver) {
        // Do nothing.
    }

    public void afterClickOn(WebElement element, WebDriver driver) {
        // Do nothing.
    }

    public void beforeChangeValueOf(WebElement element, WebDriver driver) {
        // Do nothing.
    }

    public void afterChangeValueOf(WebElement element, WebDriver driver) {
        // Do nothing.
    }

    public void beforeScript(String script, WebDriver driver) {
        // Do nothing
    }

    public void afterScript(String script, WebDriver driver) {
        // Do nothing
    }

    public void onException(Throwable throwable, WebDriver driver) {
        // Do nothing
    }
}
