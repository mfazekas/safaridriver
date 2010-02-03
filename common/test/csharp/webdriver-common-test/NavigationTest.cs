using System;
using NUnit.Framework;

namespace OpenQA.Selenium
{

    [TestFixture]
    public class NavigationTest : DriverTestFixture
    {

        [Test]
        public void ShouldNotHaveProblemNavigatingWithNoPagesBrowsed()
        {
            CreateFreshDriver();
            INavigation navigation;
            navigation = driver.Navigate();
            navigation.Back();
            navigation.Forward();
        }

        [Test]
        [IgnoreBrowser(Browser.Chrome, "Driver opens a new window for every navigation, rendering back/forward useless for Chrome.")]
        public void ShouldGoBackAndForward()
        {
            INavigation navigation;
            navigation = driver.Navigate();

            driver.Url = macbethPage;
            driver.Url = simpleTestPage;
            
            navigation.Back();
            Assert.AreEqual(macbethTitle, driver.Title);

            navigation.Forward();
            Assert.AreEqual(simpleTestTitle, driver.Title);
        }

        [Test]
        public void ShouldAcceptInvalidUrlsUsingStrings()
        {
            INavigation navigation;
            navigation = driver.Navigate();

            navigation.GoToUrl("isidsji30342��������");
            navigation.GoToUrl("");
        }

        [Test]
        [ExpectedException(typeof(ArgumentNullException))]
        public void ShouldAcceptInvalidUrlsUsingUris()
        {
            INavigation navigation;
            navigation = driver.Navigate();
            navigation.GoToUrl((Uri)null);
            // new Uri("") and new Uri("isidsji30342��������") 
            // throw an exception, so we needn't worry about them.
        }

        [Test]
        public void ShouldGoToUrlUsingString()
        {
            INavigation navigation;
            navigation = driver.Navigate();

            navigation.GoToUrl(macbethPage);
            Assert.AreEqual(macbethTitle, driver.Title);

            // We go to two pages to ensure that the browser wasn't
            // already at the desired page through a previous test.
            navigation.GoToUrl(simpleTestPage);
            Assert.AreEqual(simpleTestTitle, driver.Title);
        }

        [Test]
        public void ShouldGoToUrlUsingUri()
        {
            Uri macBeth = new Uri(macbethPage);
            Uri simpleTest = new Uri(simpleTestPage);
            INavigation navigation;
            navigation = driver.Navigate();

            navigation.GoToUrl(macBeth);
            Assert.AreEqual(driver.Title, macbethTitle);

            // We go to two pages to ensure that the browser wasn't
            // already at the desired page through a previous test.
            navigation.GoToUrl(simpleTest);
            Assert.AreEqual(simpleTestTitle, driver.Title);
        }

        [Test]
        [IgnoreBrowser(Browser.IE, "Refresh does not work for IE")]
        public void ShouldRefreshPage()
        {
            driver.Url = javascriptPage;
            IWebElement changedDiv = driver.FindElement(By.Id("dynamo"));
            driver.FindElement(By.Id("updatediv")).Click();

            Assert.AreEqual("Fish and chips!", changedDiv.Text);
            driver.Navigate().Refresh();

            changedDiv = driver.FindElement(By.Id("dynamo"));
            Assert.AreEqual("What's for dinner?", changedDiv.Text);
        }

    }
}
