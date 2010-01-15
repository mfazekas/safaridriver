using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using OpenQA.Selenium.Environment;
using System.Text.RegularExpressions;

namespace OpenQA.Selenium
{
    [TestFixture]
    public class TextHandlingTest : DriverTestFixture
    {
        private string newLine = "\r\n";

        [Test]
        public void ShouldReturnTheTextContentOfASingleElementWithNoChildren()
        {
            driver.Url = simpleTestPage;
            string selectText = driver.FindElement(By.Id("oneline")).Text;
            Assert.AreEqual(selectText, "A single line of text");

            string getText = driver.FindElement(By.Id("oneline")).Text;
            Assert.AreEqual(getText, "A single line of text");
        }

        [Test]
        public void ShouldReturnTheEntireTextContentOfChildElements()
        {
            driver.Url = (simpleTestPage);
            string text = driver.FindElement(By.Id("multiline")).Text;

            Assert.IsTrue(text.Contains("A div containing"));
            Assert.IsTrue(text.Contains("More than one line of text"));
            Assert.IsTrue(text.Contains("and block level elements"));
        }

        [Test]
        public void ShouldIgnoreScriptElements()
        {
            driver.Url = javascriptEnhancedForm;
            IWebElement labelForUsername = driver.FindElement(By.Id("labelforusername"));
            string text = labelForUsername.Text;

            Assert.AreEqual(labelForUsername.FindElements(By.TagName("script")).Count, 1);
            Assert.IsFalse(text.Contains("document.getElementById"));
            Assert.AreEqual(text, "Username:");
        }

        [Test]
        public void ShouldRepresentABlockLevelElementAsANewline()
        {
            driver.Url = (simpleTestPage);
            string text = driver.FindElement(By.Id("multiline")).Text;

            Assert.IsTrue(text.StartsWith("A div containing" + newLine));
            Assert.IsTrue(text.Contains("More than one line of text" + newLine));
            Assert.IsTrue(text.EndsWith("and block level elements"));
        }

        [Test]
        public void ShouldCollapseMultipleWhitespaceCharactersIntoASingleSpace()
        {
            driver.Url = (simpleTestPage);
            string text = driver.FindElement(By.Id("lotsofspaces")).Text;

            Assert.AreEqual(text, "This line has lots of spaces.");
        }

        [Test]
        public void ShouldTrimText()
        {
            driver.Url = (simpleTestPage);
            string text = driver.FindElement(By.Id("multiline")).Text;

            Assert.IsTrue(text.StartsWith("A div containing"));
            Assert.IsTrue(text.EndsWith("block level elements"));
        }

        [Test]
        public void ShouldConvertANonBreakingSpaceIntoANormalSpaceCharacter()
        {
            driver.Url = (simpleTestPage);
            string text = driver.FindElement(By.Id("nbsp")).Text;

            Assert.AreEqual(text, "This line has a non-breaking space");
        }

        [Test]
        public void ShouldTreatANonBreakingSpaceAsAnyOtherWhitespaceCharacterWhenCollapsingWhitespace()
        {
            driver.Url = (simpleTestPage);
            IWebElement element = driver.FindElement(By.Id("nbspandspaces"));
            string text = element.Text;

            Assert.AreEqual(text, "This line has a non-breaking space and spaces");
        }

        [Test]
        public void HavingInlineElementsShouldNotAffectHowTextIsReturned()
        {
            driver.Url = (simpleTestPage);
            string text = driver.FindElement(By.Id("inline")).Text;

            Assert.AreEqual(text, "This line has text within elements that are meant to be displayed inline");
        }

        [Test]
        public void ShouldReturnTheEntireTextOfInlineElements()
        {
            driver.Url = (simpleTestPage);
            string text = driver.FindElement(By.Id("span")).Text;

            Assert.AreEqual(text, "An inline element");
        }

        //[Test]
        //public void ShouldRetainTheFormatingOfTextWithinAPreElement()
        //{
        //    driver.Url = simpleTestPage;
        //    string text = driver.FindElement(By.Id("preformatted")).Text;

        //    Assert.AreEqual(text, "This section has a\npreformatted\n   text block\n" +
        //            "  within in\n" +
        //            "        ");
        //}

        [Test]
        public void ShouldBeAbleToSetMoreThanOneLineOfTextInATextArea()
        {
            driver.Url = formsPage;
            IWebElement textarea = driver.FindElement(By.Id("withText"));
            textarea.Clear();
            string expectedText = "I like cheese" + newLine + newLine + "It's really nice";
            textarea.SendKeys(expectedText);

            string seenText = textarea.Value;
            Assert.AreEqual(seenText, expectedText);
        }

        [Test]
        public void ShouldBeAbleToEnterDatesAfterFillingInOtherValuesFirst()
        {
            driver.Url = formsPage;
            IWebElement input = driver.FindElement(By.Id("working"));
            string expectedValue = "10/03/2007 to 30/07/1993";
            input.SendKeys(expectedValue);
            string seenValue = input.Value;

            Assert.AreEqual(seenValue, expectedValue);
        }

        [Test]
        public void ShouldReturnEmptystringWhenTextIsOnlySpaces()
        {
            driver.Url = (xhtmlTestPage);

            string text = driver.FindElement(By.Id("spaces")).Text;
            Assert.AreEqual(text, string.Empty);
        }

        [Test]
        public void ShouldReturnEmptystringWhenTextIsEmpty()
        {
            driver.Url = (xhtmlTestPage);

            string text = driver.FindElement(By.Id("empty")).Text;
            Assert.AreEqual(text, string.Empty);
        }

        [Test]
        public void ShouldReturnEmptystringWhenTagIsSelfClosing()
        {
            driver.Url = (xhtmlTestPage);

            string text = driver.FindElement(By.Id("self-closed")).Text;
            Assert.AreEqual(text, string.Empty);
        }

        [Test]
        public void ShouldHandleSiblingBlockLevelElements()
        {
            driver.Url = (simpleTestPage);

            string text = driver.FindElement(By.Id("twoblocks")).Text;

            Assert.AreEqual(text, "Some text" + newLine + "Some more text");
        }

        [Test]
        [IgnoreBrowser(Browser.Firefox)]
        [IgnoreBrowser(Browser.HtmlUnit)]
        [IgnoreBrowser(Browser.IE)]
        [IgnoreBrowser(Browser.Chrome)]
        public void ShouldHandleNestedBlockLevelElements()
        {
            driver.Url = (simpleTestPage);

            string text = driver.FindElement(By.Id("nestedblocks")).Text;

            Assert.AreEqual(text, "Cheese" + newLine + "Some text" + newLine + "Some more text" + newLine
                                + "and also" + newLine + "Brie");
        }

        [Test]
        public void ShouldHandleWhitespaceInInlineElements()
        {
            driver.Url = (simpleTestPage);

            string text = driver.FindElement(By.Id("inlinespan")).Text;

            Assert.AreEqual(text, "line has text");
        }

        [Test]
        public void ReadALargeAmountOfData()
        {
            driver.Url = EnvironmentManager.Instance.UrlBuilder.WhereIs("macbeth.html");
            string source = driver.PageSource.Trim().ToLower();

            Assert.IsTrue(source.EndsWith("</html>"));
        }

        [Test]
        public void GetTextWithLineBreakForInlineElement()
        {
            driver.Url = (simpleTestPage);

            IWebElement label = driver.FindElement(By.Id("label1"));
            string labelText = label.Text;

            Assert.IsTrue(new Regex("foo[\\n\\r]+bar").IsMatch(labelText));
        }

        [Test]
        [Category("Javascript")]
        public void ShouldOnlyIncludeVisibleText()
        {
            driver.Url = javascriptPage;

            string empty = driver.FindElement(By.Id("suppressedParagraph")).Text;
            string explicitText = driver.FindElement(By.Id("outer")).Text;

            Assert.AreEqual(string.Empty, empty);
            Assert.AreEqual("sub-element that is explicitly visible", explicitText);
        }
    }
}
