﻿using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using OpenQA.Selenium.Firefox.Internal;
using System.Threading;
using System.Collections.ObjectModel;
using OpenQA.Selenium.Environment;

namespace OpenQA.Selenium.Firefox.Test
{
    [TestFixture]
    public class FirefoxDriverTest : DriverTestFixture
    {
        [Test]
        public void ShouldContinueToWorkIfUnableToFindElementById()
        {
            driver.Url = formsPage;

            try
            {
                driver.FindElement(By.Id("notThere"));
                Assert.Fail("Should not be able to select element by id here");
            }
            catch (NoSuchElementException)
            {
                // This is expected
            }

            // Is this works, then we're golden
            driver.Url = xhtmlTestPage;
        }

        [Test]
        //[IgnoreBrowser(Browser.Firefox, "Need to figure out how to open a new browser instance mid-test")]
        public void ShouldWaitUntilBrowserHasClosedProperly()
        {
            driver.Url = simpleTestPage;
            driver.Close();

            CreateFreshDriver();

            driver.Url = formsPage;
            IWebElement textarea = driver.FindElement(By.Id("withText"));
            string expectedText = "I like cheese" + System.Environment.NewLine 
                + System.Environment.NewLine + "It's really nice";
            textarea.Clear();
            textarea.SendKeys(expectedText);

            string seenText = textarea.Value;
            Assert.AreEqual(expectedText, seenText);
        }

        [Test]
        public void ShouldBeAbleToStartMoreThanOneInstanceOfTheFirefoxDriverSimultaneously()
        {
            IWebDriver secondDriver = new FirefoxDriver();

            driver.Url = xhtmlTestPage;
            secondDriver.Url = formsPage;

            Assert.AreEqual("XHTML Test Page", driver.Title);
            Assert.AreEqual("We Leave From Here", secondDriver.Title);

            // We only need to quit the second driver if the test passes
            secondDriver.Quit();
        }

        [Test]
        [Ignore]
        public void ShouldBeAbleToStartANamedProfile()
        {
            FirefoxProfile profile = new FirefoxProfileManager().GetProfile("default");

            if (profile != null)
            {
                IWebDriver firefox = new FirefoxDriver(profile);
                firefox.Quit();
            }
            else
            {
                Assert.Ignore("Skipping test: No profile named \"default\" found.");
            }
        }

        [Test]
        [NeedsFreshDriver(BeforeTest = true, AfterTest = true)]
        public void FocusRemainsInOriginalWindowWhenOpeningNewWindow()
        {
            if (PlatformHasNativeEvents() == false)
            {
                return;
            }
            // Scenario: Open a new window, make sure the current window still gets
            // native events (keyboard events in this case).
            driver.Url = xhtmlTestPage;

            driver.FindElement(By.Name("windowOne")).Click();

            SleepBecauseWindowsTakeTimeToOpen();

            driver.Url = javascriptPage;

            IWebElement keyReporter = driver.FindElement(By.Id("keyReporter"));
            keyReporter.SendKeys("ABC DEF");

            Assert.AreEqual("ABC DEF", keyReporter.Value);
        }

        [Test]
        [NeedsFreshDriver(BeforeTest = true, AfterTest = true)]
        public void SwitchingWindowShouldSwitchFocus()
        {
            if (PlatformHasNativeEvents() == false)
            {
                return;
            }
            // Scenario: Open a new window, switch to it, make sure it gets native events.
            // Then switch back to the original window, make sure it gets native events.
            driver.Url = xhtmlTestPage;

            string originalWinHandle = driver.GetWindowHandle();

            driver.FindElement(By.Name("windowOne")).Click();

            SleepBecauseWindowsTakeTimeToOpen();

            List<string> allWindowHandles = new List<string>(driver.GetWindowHandles());

            // There should be two windows. We should also see each of the window titles at least once.
            Assert.AreEqual(2, allWindowHandles.Count);

            allWindowHandles.Remove(originalWinHandle);
            string newWinHandle = (string)allWindowHandles[0];

            // Key events in new window.
            driver.SwitchTo().Window(newWinHandle);
            SleepBecauseWindowsTakeTimeToOpen();
            driver.Url = javascriptPage;

            IWebElement keyReporter = driver.FindElement(By.Id("keyReporter"));
            keyReporter.SendKeys("ABC DEF");
            Assert.AreEqual("ABC DEF", keyReporter.Value);

            // Key events in original window.
            driver.SwitchTo().Window(originalWinHandle);
            SleepBecauseWindowsTakeTimeToOpen();
            driver.Url = javascriptPage;

            IWebElement keyReporter2 = driver.FindElement(By.Id("keyReporter"));
            keyReporter2.SendKeys("QWERTY");
            Assert.AreEqual("QWERTY", keyReporter2.Value);
        }

        [Test]
        public void ShouldBeAbleToStartDriverMultipleTimes()
        {
            for (int i = 0; i < 5; i++)
            {
                CreateFreshDriver();
                driver.Url = simpleTestPage;
                Assert.AreEqual(simpleTestTitle, driver.Title);
                
            }
            CreateFreshDriver();
        }

        [Test]
        [NeedsFreshDriver(BeforeTest = true, AfterTest = true)]
        public void ClosingWindowAndSwitchingToOriginalSwitchesFocus()
        {
            if (PlatformHasNativeEvents() == false)
            {
                return;
            }
            // Scenario: Open a new window, switch to it, close it, switch back to the
            // original window - make sure it gets native events.
            driver.Url = xhtmlTestPage;
            string originalWinHandle = driver.GetWindowHandle();

            driver.FindElement(By.Name("windowOne")).Click();

            SleepBecauseWindowsTakeTimeToOpen();
            List<string> allWindowHandles = new List<string>(driver.GetWindowHandles());
            // There should be two windows. We should also see each of the window titles at least once.
            Assert.AreEqual(2, allWindowHandles.Count);

            allWindowHandles.Remove(originalWinHandle);
            string newWinHandle = (string)allWindowHandles[0];
            // Switch to the new window.
            driver.SwitchTo().Window(newWinHandle);
            SleepBecauseWindowsTakeTimeToOpen();
            // Close new window.
            driver.Close();

            // Switch back to old window.
            driver.SwitchTo().Window(originalWinHandle);
            SleepBecauseWindowsTakeTimeToOpen();

            // Send events to the new window.
            driver.Url = javascriptPage;
            IWebElement keyReporter = driver.FindElement(By.Id("keyReporter"));
            keyReporter.SendKeys("ABC DEF");
            Assert.AreEqual("ABC DEF", keyReporter.Value);
        }

        //[Test]
        public void CanBlockInvalidSslCertificates()
        {
            FirefoxProfile profile = new FirefoxProfile();
            profile.AcceptUntrustedCertificates = false;
            string url = EnvironmentManager.Instance.UrlBuilder.WhereIsSecure("simpleTest.html");

            IWebDriver secondDriver = null;
            try
            {
                secondDriver = new FirefoxDriver(profile);
                secondDriver.Url = url;
                string gotTitle = secondDriver.Title;
                Assert.AreNotEqual("Hello IWebDriver", gotTitle);
            }
            catch (Exception)
            {
                Assert.Fail("Creating driver with untrusted certificates set to false failed.");
            }
            finally
            {
                if (secondDriver != null)
                {
                    secondDriver.Quit();
                }
            }
        }

        [Test]
        public void ShouldAllowUserToSuccessfullyOverrideTheHomePage()
        {
            FirefoxProfile profile = new FirefoxProfile();
            profile.SetPreference("browser.startup.page", "1");
            profile.SetPreference("browser.startup.homepage", javascriptPage);

            IWebDriver driver2 = new FirefoxDriver(profile);

            try
            {
                Assert.AreEqual(javascriptPage, driver2.Url);
            }
            finally
            {
                driver2.Quit();
            }
        }

        private static bool PlatformHasNativeEvents()
        {
            return FirefoxDriver.DefaultEnableNativeEvents;
        }

        private void SleepBecauseWindowsTakeTimeToOpen()
        {
            try
            {
                Thread.Sleep(1000);
            }
            catch (ThreadInterruptedException)
            {
                Assert.Fail("Interrupted");
            }
        }
    }
}
