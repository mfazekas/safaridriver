using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using System.Collections.ObjectModel;

namespace OpenQA.Selenium
{
    [TestFixture]
    public class PartialLinkTextMatchTest : DriverTestFixture
    {
        [Test]
        [IgnoreBrowser(Browser.Remote)]
        [IgnoreBrowser(Browser.IE)]
        public void LinkWithFormattingTags()
        {
            driver.Url = simpleTestPage;
            IWebElement elem = driver.FindElement(By.Id("links"));

            IWebElement res =
                elem.FindElement(By.PartialLinkText("link with formatting tags"));
            Assert.IsNotNull(res);
            Assert.AreEqual("link with formatting tags", res.Text);
        }

        [Test]
        [IgnoreBrowser(Browser.Remote)]
        [IgnoreBrowser(Browser.IE)]
        public void LinkWithLeadingSpaces()
        {
            driver.Url = simpleTestPage;
            IWebElement elem = driver.FindElement(By.Id("links"));

            IWebElement res = elem.FindElement(By.PartialLinkText("link with leading space"));
            Assert.IsNotNull(res);
            Assert.AreEqual("link with leading space", res.Text);
        }

        [Test]
        [IgnoreBrowser(Browser.Remote)]
        [IgnoreBrowser(Browser.IE)]
        public void LinkWithTrailingSpace()
        {
            driver.Url = simpleTestPage;
            IWebElement elem = driver.FindElement(By.Id("links"));

            IWebElement res =
                elem.FindElement(By.PartialLinkText("link with trailing space"));
            Assert.IsNotNull(res);
            Assert.AreEqual("link with trailing space", res.Text);
        }

        [Test]
        [IgnoreBrowser(Browser.Remote)]
        [IgnoreBrowser(Browser.IE)]
        public void FindMultipleElements()
        {
            driver.Url = simpleTestPage;
            IWebElement elem = driver.FindElement(By.Id("links"));

            ReadOnlyCollection<IWebElement> elements = elem.FindElements(By.PartialLinkText("link"));
            Assert.IsNotNull(elements);
            Assert.AreEqual(3, elements.Count);
        }
        
        [Test]
        public void CanGetLinkByLinkTestIgnoringTrailingWhitespace()
        {
            driver.Url = simpleTestPage;
            IWebElement link = null;
            link = driver.FindElement(By.LinkText("link with trailing space"));
            Assert.AreEqual("linkWithTrailingSpace", link.GetAttribute("id"));
        }
    }
}
