﻿using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;

namespace OpenQA.Selenium
{
    [TestFixture]
    public class I18Test : DriverTestFixture
    {
        // The Hebrew word shalom (peace) encoded in order Shin (sh) Lamed (L) Vav (O) final-Mem (M).
        private string shalom = "\u05E9\u05DC\u05D5\u05DD";

        // The Hebrew word tmunot (images) encoded in order Taf (t) Mem (m) Vav (u) Nun (n) Vav (o) Taf (t).
        private string tmunot = "\u05EA\u05DE\u05D5\u05E0\u05D5\u05EA";

        // This is the Chinese link text
        private string linkText = "\u4E2D\u56FD\u4E4B\u58F0";

        [Test]
        [IgnoreBrowser(Browser.HtmlUnit)]
        [IgnoreBrowser(Browser.IE)]
        [IgnoreBrowser(Browser.Firefox)]
        [IgnoreBrowser(Browser.Remote)]
        public void ShouldBeAbleToReadChinese()
        {
            driver.Url = chinesePage;
            driver.FindElement(By.LinkText(linkText)).Click();
        }

        [Test]
        [IgnoreBrowser(Browser.IE, "Test will run, but requires the Chinese language pack which may hang the test")]
        [IgnoreBrowser(Browser.Remote)]
        public void ShouldBeAbleToEnterHebrewTextFromLeftToRight()
        {
            driver.Url = chinesePage;
            IWebElement input = driver.FindElement(By.Name("i18n"));

            input.SendKeys(shalom);

            Assert.AreEqual(shalom, input.Value);
        }

        [Test]
        [IgnoreBrowser(Browser.IE, "Test will run, but requires the Chinese language pack which may hang the test")]
        [IgnoreBrowser(Browser.Remote)]
        public void ShouldBeAbleToEnterHebrewTextFromRightToLeft()
        {
            driver.Url = chinesePage;
            IWebElement input = driver.FindElement(By.Name("i18n"));

            input.SendKeys(tmunot);

            Assert.AreEqual(tmunot, input.Value);
        }
    }
}
