﻿using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using OpenQA.Selenium.Environment;
using System.Collections.ObjectModel;

namespace OpenQA.Selenium
{
    [TestFixture]
    public class WindowSwitchingTest : DriverTestFixture
    {
        [Test]
        public void ShouldSwitchFocusToANewWindowWhenItIsOpenedAndNotStopFutureOperations()
        {
            driver.Url = xhtmlTestPage;
            String current = driver.GetWindowHandle();

            driver.FindElement(By.LinkText("Open new window")).Click();
            Assert.AreEqual("XHTML Test Page", driver.Title);

            //TODO (jimevan): this is an ugly sleep. Remove when implicit waiting is implemented.
            System.Threading.Thread.Sleep(500);
            driver.SwitchTo().Window("result");
            Assert.AreEqual("We Arrive Here", driver.Title);

            driver.Url = iframesPage;
            string handle = driver.GetWindowHandle();
            driver.FindElement(By.Id("iframe_page_heading"));
            driver.SwitchTo().Frame("iframe1");
            Assert.AreEqual(driver.GetWindowHandle(), handle);

            driver.SwitchTo().Window(current);
            //Assert.AreEqual("XHTML Test Page", driver.Title);
        }

        [Test]
        public void ShouldGetBrowserHandles()
        {
            driver.Url = xhtmlTestPage;
            driver.FindElement(By.LinkText("Open new window")).Click();

            string handle1, handle2;
            handle1 = driver.GetWindowHandle();

            driver.SwitchTo().Window("result");
            handle2 = driver.GetWindowHandle();

            ReadOnlyCollection<string> handles = driver.GetWindowHandles();

            // At least the two handles we want should be there.
            Assert.Contains(handle1, handles, "Should have contained current handle");
            Assert.Contains(handle2, handles, "Should have contained result handle");

            // Some (semi-)clean up..
            driver.Url = macbethPage;
            driver.SwitchTo().Window(handle1);
        }

        [Test]
        [IgnoreBrowser(Browser.IE,"Can't close handle and use it afterwards in IE driver")]
        public void CloseShouldCloseCurrentHandleOnly()
        {
            driver.Url = xhtmlTestPage;
            driver.FindElement(By.LinkText("Open new window")).Click();

            string handle1, handle2;
            handle1 = driver.GetWindowHandle();

            driver.SwitchTo().Window("result");
            handle2 = driver.GetWindowHandle();
           
            driver.Close();
            // TODO(andre.nogueira): IE: Safe handles don't allow this, as the
            // handle has already been closed! (Throws exception)
            ReadOnlyCollection<string> handles = driver.GetWindowHandles();

            Assert.IsFalse(handles.Contains(handle2), "Invalid handle still in handle list");
            Assert.IsTrue(handles.Contains(handle1), "Valid handle not in handle list");

            // Clean up after ourselves
            EnvironmentManager.Instance.CreateFreshDriver();
        }

        [Test]
        public void ShouldThrowNoSuchWindowException() {
            driver.Url = xhtmlTestPage;
            String current = driver.GetWindowHandle();
            try
            {
                driver.SwitchTo().Window("invalid name");
            }
            catch (NoSuchWindowException)
            {
                // This is expected.
            }

            driver.SwitchTo().Window(current);
        }


        [Test]
        [IgnoreBrowser(Browser.IE)]
        [IgnoreBrowser(Browser.Firefox)]
        [IgnoreBrowser(Browser.Remote)]
        public void ShouldBeAbleToIterateOverAllOpenWindows()
        {
            CreateFreshDriver();

            driver.Url = xhtmlTestPage;
            driver.FindElement(By.Name("windowOne")).Click();
            driver.FindElement(By.Name("windowTwo")).Click();

            ReadOnlyCollection<string> allWindowHandles = driver.GetWindowHandles();

            // There should be three windows. We should also see each of the window titles at least once.
            List<string> seenHandles = new List<string>();
            foreach (string handle in allWindowHandles)
            {
                Assert.IsFalse(seenHandles.Contains(handle));
                driver.SwitchTo().Window(handle);
                seenHandles.Add(handle);
            }

            Assert.AreEqual(3, allWindowHandles.Count);
            CreateFreshDriver();
        }

        [Test]
        [IgnoreBrowser(Browser.IE)]
        public void ClickingOnAButtonThatClosesAnOpenWindowDoesNotCauseTheBrowserToHang()
        {
            driver.Url = xhtmlTestPage;

            String currentHandle = driver.GetWindowHandle();

            driver.FindElement(By.Name("windowThree")).Click();

            driver.SwitchTo().Window("result");

            try
            {
                driver.FindElement(By.Id("close")).Click();
                // If we make it this far, we're all good.
            }
            finally
            {
                driver.SwitchTo().Window(currentHandle);
                driver.FindElement(By.Id("linkId"));
            }
        }

        [Test]
        [Category("Javascript")]
        [IgnoreBrowser(Browser.IE)]
        public void CanCallGetWindowHandlesAfterClosingAWindow()
        {
            driver.Url = xhtmlTestPage;

            String currentHandle = driver.GetWindowHandle();

            driver.FindElement(By.Name("windowThree")).Click();

            driver.SwitchTo().Window("result");

            try
            {
                driver.FindElement(By.Id("close")).Click();
                driver.GetWindowHandles();
                // If we make it this far, we're all good.
            }
            finally
            {
                driver.SwitchTo().Window(currentHandle);
            }
        }

        [Test]
        public void CanObtainAWindowHandle()
        {
            driver.Url = xhtmlTestPage;

            String currentHandle = driver.GetWindowHandle();

            Assert.IsNotNull(currentHandle);
        }

        [Test]
        public void FailingToSwitchToAWindowLeavesTheCurrentWindowAsIs()
        {
            driver.Url = xhtmlTestPage;
            String current = driver.GetWindowHandle();

            try
            {
                driver.SwitchTo().Window("i will never exist");
                Assert.Fail("Should not be ablt to change to a non-existant window");
            }
            catch (NoSuchWindowException)
            {
                // expected
            }

            String newHandle = driver.GetWindowHandle();

            Assert.AreEqual(current, newHandle);
        }

        [Test]
        [IgnoreBrowser(Browser.ChromeNonWindows, "Chrome failing on OS X")]
        [IgnoreBrowser(Browser.IE)]
        public void CanCloseWindowWhenMultipleWindowsAreOpen()
        {
            CreateFreshDriver();
            driver.Url = xhtmlTestPage;
            driver.FindElement(By.Name("windowOne")).Click();

            SleepBecauseWindowsTakeTimeToOpen();

            ReadOnlyCollection<string> allWindowHandles = driver.GetWindowHandles();

            // There should be two windows. We should also see each of the window titles at least once.
            Assert.AreEqual(2, allWindowHandles.Count);
            string handle1 = allWindowHandles[1];
            driver.SwitchTo().Window(handle1);
            driver.Close();
            allWindowHandles = driver.GetWindowHandles();
            Assert.AreEqual(1, allWindowHandles.Count);
            CreateFreshDriver();
        }

        [Test]
        public void ClosingOnlyWindowShouldNotCauseTheBrowserToHang()
        {
            CreateFreshDriver();
            driver.Url = xhtmlTestPage;
            driver.Close();
            CreateFreshDriver();
        }

        private void SleepBecauseWindowsTakeTimeToOpen()
        {
            try
            {
                System.Threading.Thread.Sleep(1000);
            }
            catch (Exception)
            {
                Assert.Fail("Interrupted");
            }
        }
    }
}
