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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.lift.find.Finder;

/**
 * Unit test for {@link HamcrestWebDriverTestCase}.
 * @author rchatley (Robert Chatley)
 *
 */
@SuppressWarnings("unchecked")
public class HamcrestWebdriverTestCaseTest extends MockObjectTestCase {

	final String text = "abcde";
	final String url = "http://www.example.com";
	Finder<WebElement, WebDriver> something = mock(Finder.class);
	Matcher<Integer> someNumberOf = mock(Matcher.class);

	HamcrestWebDriverTestCase testcase = createTestCase();
	
	public void testDelegatesAllCallsToItsTestContext() {
		
		final TestContext testContext = mock(TestContext.class);
		testcase.setContext(testContext);
		
		final Sequence given = sequence("given here");
		
		checking(new Expectations() {{ 
			one(testContext).goTo(url); inSequence(given);
			one(testContext).clickOn(something); inSequence(given);
			one(testContext).type(text, something); inSequence(given);
			one(testContext).assertPresenceOf(something); inSequence(given);
			one(testContext).assertPresenceOf(someNumberOf, something); inSequence(given);
		}});
		
		testcase.goTo(url);
		testcase.clickOn(something);
		testcase.type(text, something);
		testcase.assertPresenceOf(something);
		testcase.assertPresenceOf(someNumberOf, something);
	}
	
	public void testProvidesSyntacticSugarMethodNamedInto() throws Exception {
		
		Finder<WebElement, WebDriver> result = testcase.into(something);
		assertThat(result, is(something));
	}

	private HamcrestWebDriverTestCase createTestCase() {
		HamcrestWebDriverTestCase testcase = new HamcrestWebDriverTestCase() {

			@Override
			protected WebDriver createDriver() {
				return mock(WebDriver.class);
			}
		};
		return testcase;
	}
	
}
