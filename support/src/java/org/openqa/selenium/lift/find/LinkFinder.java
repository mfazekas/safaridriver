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

package org.openqa.selenium.lift.find;

/**
 * A {@link Finder} for HTML anchor tags, "links".
 * @author rchatley (Robert Chatley)
 *
 */

import org.hamcrest.Factory;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.match.TextMatcher.text;

public class LinkFinder extends HtmlTagFinder {
	
	private LinkFinder() {};

	@Override
	protected String tagName() {
		return "a";
	}
	
	@Override
	protected String tagDescription() {
		return "link";
	}
	
	@Factory
	public static HtmlTagFinder link() {
		return new LinkFinder();
	}
	
	@Factory
	public static HtmlTagFinder link(String linkText) {
		return new LinkFinder().with(text(equalTo(linkText)));
	}
	
	@Factory
	public static HtmlTagFinder links() {
		return link();
	}
}