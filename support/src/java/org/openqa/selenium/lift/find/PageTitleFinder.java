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

/**
 * A {@link Finder} for title tags.
 */
package org.openqa.selenium.lift.find;

import org.hamcrest.Factory;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.match.TextMatcher.text;

/**
 * A {@link Finder} for HTML title tags.
 * @author rchatley (Robert Chatley)
 *
 */
public class PageTitleFinder extends HtmlTagFinder {
	
	private PageTitleFinder() {};

	@Override
	protected String tagName() {
		return "title";
	}

	@Override
	protected String tagDescription() {
		return "page title";
	}

	@Factory
	public static HtmlTagFinder title() {
		return new PageTitleFinder();
	}
	
	@Factory
	public static HtmlTagFinder title(String title) {
		return new PageTitleFinder().with(text(equalTo(title)));
	}
	
	@Factory
	public static HtmlTagFinder titles() {
		return new PageTitleFinder();
	}
}