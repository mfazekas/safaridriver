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

package org.openqa.selenium.lift;

import org.hamcrest.Matcher;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.lift.find.Finder;

/**
 * Interface for objects that provide a context (maintaining any state) for web tests.
 * @author rchatley (Robert Chatley)
 *
 */
public interface TestContext {

	public abstract void goTo(String url);

	public abstract void assertPresenceOf(Finder<WebElement, WebDriver> finder);

	public abstract void assertPresenceOf(
			Matcher<Integer> cardinalityConstraint,
			Finder<WebElement, WebDriver> finder);

	public abstract void type(String input, Finder<WebElement, WebDriver> finder);

	public abstract void clickOn(Finder<WebElement, WebDriver> finder);

	public abstract void waitFor(Finder<WebElement, WebDriver> finder, long timeout);

	public abstract void quit();

}