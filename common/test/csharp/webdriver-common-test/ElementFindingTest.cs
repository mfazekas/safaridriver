using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using System.Collections.ObjectModel;
using OpenQA.Selenium.Internal;

namespace OpenQA.Selenium
{
    [TestFixture]
    public class ElementFindingTest : DriverTestFixture
    {
        [Test]
        public void ShouldReturnTitleOfPageIfSet()
        {
            driver.Url = xhtmlTestPage;
            Assert.AreEqual(driver.Title, "XHTML Test Page");

            driver.Url = simpleTestPage;
            Assert.AreEqual(driver.Title, "Hello WebDriver");
        }

        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void ShouldNotBeAbleToLocateASingleElementThatDoesNotExist()
        {
            driver.Url = formsPage;

            driver.FindElement(By.Id("nonExistantButton"));
        }

        [Test]
        public void ShouldBeAbleToClickOnLinkIdentifiedByText()
        {
            driver.Url = xhtmlTestPage;
            driver.FindElement(By.LinkText("click me")).Click();
            //TODO (jimevan): this is an ugly sleep. Remove when implicit waiting is implemented.
            System.Threading.Thread.Sleep(500);
            Assert.AreEqual(driver.Title, "We Arrive Here");
        }

        [Test]
        public void DriverShouldBeAbleToFindElementsAfterLoadingMoreThanOnePageAtATime()
        {
            driver.Url = formsPage;
            driver.Url = xhtmlTestPage;
            driver.FindElement(By.LinkText("click me")).Click();
            //TODO (jimevan): this is an ugly sleep. Remove when implicit waiting is implemented.
            System.Threading.Thread.Sleep(500);
            Assert.AreEqual(driver.Title, "We Arrive Here");
        }

        [Test]
        public void ShouldBeAbleToClickOnLinkIdentifiedById()
        {
            driver.Url = xhtmlTestPage;
            driver.FindElement(By.Id("linkId")).Click();
            //TODO (jimevan): this is an ugly sleep. Remove when implicit waiting is implemented.
            System.Threading.Thread.Sleep(500);
            Assert.AreEqual(driver.Title, "We Arrive Here");
        }

        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void ShouldThrowAnExceptionWhenThereIsNoLinkToClickAndItIsFoundWithLinkText()
        {
            driver.Url = xhtmlTestPage;

            driver.FindElement(By.LinkText("Not here either"));
        }

        [Test]
        public void ShouldFindAnElementBasedOnId()
        {
            driver.Url = formsPage;

            IWebElement element = driver.FindElement(By.Id("checky"));

            Assert.IsFalse(element.Selected);
        }

        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void ShouldNotBeAbleTofindElementsBasedOnIdIfTheElementIsNotThere()
        {
            driver.Url = formsPage;

            driver.FindElement(By.Id("notThere"));
        }

        [Test]
        public void ShouldBeAbleToFindChildrenOfANode()
        {
            driver.Url = xhtmlTestPage;
            ReadOnlyCollection<IWebElement> elements = driver.FindElements(By.XPath("/html/head"));
            IWebElement head = elements[0];
            ReadOnlyCollection<IWebElement> importedScripts = head.FindElements(By.TagName("script"));
            Assert.AreEqual(importedScripts.Count, 2);
        }

        [Test]
        public void ReturnAnEmptyListWhenThereAreNoChildrenOfANode()
        {
            driver.Url = xhtmlTestPage;
            IWebElement table = driver.FindElement(By.Id("table"));
            ReadOnlyCollection<IWebElement> rows = table.FindElements(By.TagName("tr"));

            Assert.AreEqual(rows.Count, 0);
        }

        [Test]
        public void ShouldFindElementsByName()
        {
            driver.Url = formsPage;

            IWebElement element = driver.FindElement(By.Name("checky"));

            Assert.AreEqual(element.Value, "furrfu");
        }

        [Test]
        public void ShouldFindElementsByClass()
        {
            driver.Url = xhtmlTestPage;

            IWebElement element = driver.FindElement(By.ClassName("extraDiv"));
            Assert.IsTrue(element.Text.StartsWith("Another div starts here."));
        }

        [Test]
        public void ShouldFindElementsByClassWhenItIsTheFirstNameAmongMany()
        {
            driver.Url = xhtmlTestPage;

            IWebElement element = driver.FindElement(By.ClassName("nameA"));
            Assert.AreEqual(element.Text, "An H2 title");
        }

        [Test]
        public void ShouldFindElementsByClassWhenItIsTheLastNameAmongMany()
        {
            driver.Url = xhtmlTestPage;

            IWebElement element = driver.FindElement(By.ClassName("nameC"));
            Assert.AreEqual(element.Text, "An H2 title");
        }

        [Test]
        public void ShouldFindElementsByClassWhenItIsInTheMiddleAmongMany()
        {
            driver.Url = xhtmlTestPage;

            IWebElement element = driver.FindElement(By.ClassName("nameBnoise"));
            Assert.AreEqual(element.Text, "An H2 title");
        }

        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void ShouldNotFindElementsByClassWhenTheNameQueriedIsShorterThanCandidateName()
        {
            driver.Url = xhtmlTestPage;

            driver.FindElement(By.ClassName("nameB"));
        }

        [Test]
        public void ShouldBeAbleToFindMultipleElementsByXPath()
        {
            driver.Url = xhtmlTestPage;

            ReadOnlyCollection<IWebElement> elements = driver.FindElements(By.XPath("//div"));

            Assert.IsTrue(elements.Count > 1);
        }

        [Test]
        public void ShouldBeAbleToFindMultipleElementsByLinkText()
        {
            driver.Url = xhtmlTestPage;

            ReadOnlyCollection<IWebElement> elements = driver.FindElements(By.LinkText("click me"));

            Assert.IsTrue(elements.Count == 2, "Expected 2 links, got " + elements.Count);
        }

        [Test]
        public void ShouldBeAbleToFindMultipleElementsByPartialLinkText()
        {
            driver.Url = xhtmlTestPage;

            ReadOnlyCollection<IWebElement> elements = driver.FindElements(By.PartialLinkText("ick me"));

            Assert.IsTrue(elements.Count == 2);
        }

        [Test]
        public void ShouldBeAbleToFindElementByPartialLinkText()
        {
            driver.Url = xhtmlTestPage;

            driver.FindElement(By.PartialLinkText("anon"));
        }

        [Test]
        public void ShouldBeAbleToFindMultipleElementsByName()
        {
            driver.Url = nestedPage;

            ReadOnlyCollection<IWebElement> elements = driver.FindElements(By.Name("checky"));

            Assert.IsTrue(elements.Count > 1);
        }

        [Test]
        public void ShouldBeAbleToFindMultipleElementsById()
        {
            driver.Url = nestedPage;

            ReadOnlyCollection<IWebElement> elements = driver.FindElements(By.Id("2"));

            Assert.AreEqual(8, elements.Count);
        }

        [Test]
        public void ShouldBeAbleToFindMultipleElementsByClassName()
        {
            driver.Url = xhtmlTestPage;

            ReadOnlyCollection<IWebElement> elements = driver.FindElements(By.ClassName("nameC"));

            Assert.IsTrue(elements.Count > 1);
        }

        [Test]
        // You don't want to ask why this is here
        public void WhenFindingByNameShouldNotReturnById()
        {
            driver.Url = formsPage;

            IWebElement element = driver.FindElement(By.Name("id-name1"));
            Assert.AreEqual(element.Value, "name");

            element = driver.FindElement(By.Id("id-name1"));
            Assert.AreEqual(element.Value, "id");

            element = driver.FindElement(By.Name("id-name2"));
            Assert.AreEqual(element.Value, "name");

            element = driver.FindElement(By.Id("id-name2"));
            Assert.AreEqual(element.Value, "id");
        }

        [Test]
        public void ShouldFindGrandChildren()
        {
            driver.Url = formsPage;
            IWebElement form = driver.FindElement(By.Id("nested_form"));
            form.FindElement(By.Name("x"));
        }

        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void ShouldNotFindElementOutSideTree()
        {
            driver.Url = formsPage;
            IWebElement element = driver.FindElement(By.Name("login"));
            element.FindElement(By.Name("x"));
        }

        [Test]
        public void ShouldReturnElementsThatDoNotSupportTheNameProperty()
        {
            driver.Url = nestedPage;

            driver.FindElement(By.Name("div1"));
            // If this works, we're all good
        }

        [Test]
        public void ShouldFindHiddenElementsByName()
        {
            driver.Url = formsPage;

            driver.FindElement(By.Name("hidden"));
        }

        [Test]
        public void ShouldfindAnElementBasedOnTagName()
        {
            driver.Url = formsPage;

            IWebElement element = driver.FindElement(By.TagName("input"));

            Assert.IsNotNull(element);
        }

        [Test]
        public void ShouldfindElementsBasedOnTagName()
        {
            driver.Url = formsPage;

            ReadOnlyCollection<IWebElement> elements = driver.FindElements(By.TagName("input"));

            Assert.IsNotNull(elements);
        }

        [Test]
        [ExpectedException(typeof(IllegalLocatorException))]
        public void FindingElementByCompoundClassNameIsAnError()
        {
            driver.Url = xhtmlTestPage;

            driver.FindElement(By.ClassName("a b"));
        }

        [Test]
        [ExpectedException(typeof(IllegalLocatorException))]
        public void FindingElementCollectionByCompoundClassNameIsAnError()
        {
            driver.FindElements(By.ClassName("a b"));
        }

        [Test]
        [Category("Javascript")]
        public void ShouldBeAbleToClickOnLinksWithNoHrefAttribute()
        {
            driver.Url = javascriptPage;

            IWebElement element = driver.FindElement(By.LinkText("No href"));
            element.Click();

            // if any exception is thrown, we won't get this far. Sanity check
            Assert.AreEqual("Changed", driver.Title);
        }

        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void ShouldNotBeAbleToFindAnElementOnABlankPage()
        {
            driver.Url = "about:blank";
            driver.FindElement(By.TagName("a"));
        }

        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void ShouldNotBeAbleToLocateASingleElementOnABlankPage()
        {
            // Note we're on the default start page for the browser at this point.
            CreateFreshDriver();
            driver.FindElement(By.Id("nonExistantButton"));
        }

        [Test]
        [Category("Javascript")]
        [ExpectedException(typeof(StaleElementReferenceException))]
        public void RemovingAnElementDynamicallyFromTheDomShouldCauseAStaleRefException()
        {
            driver.Url = javascriptPage;

            IRenderedWebElement toBeDeleted = (IRenderedWebElement)driver.FindElement(By.Id("deleted"));
            Assert.IsTrue(toBeDeleted.Displayed);

            driver.FindElement(By.Id("delete")).Click();
            //TODO (jimevan): this is an ugly sleep. Remove when implicit waiting is implemented.
            System.Threading.Thread.Sleep(500);
            bool displayedAfterDelete = toBeDeleted.Displayed;
        }

        [Test]
        public void FindingALinkByXpathUsingContainsKeywordShouldWork()
        {
            driver.Url = nestedPage;

            driver.FindElement(By.XPath("//a[contains(.,'hello world')]"));
        }

        [Test]
        [Category("Javascript")]
        public void ShouldBeAbleToFindAnElementByCssSelector()
        {
            if (!SupportsSelectorApi())
            {
                Console.WriteLine("Skipping test: selector API not supported");
                return;
            }

            driver.Url = xhtmlTestPage;

            driver.FindElement(By.CssSelector("div.content"));
        }

        [Test]
        [Category("Javascript")]
        public void ShouldBeAbleToFindAnElementsByCssSelector()
        {
            if (!SupportsSelectorApi())
            {
                Console.WriteLine("Skipping test: selector API not supported");
                return;
            }

            driver.Url = xhtmlTestPage;

            driver.FindElements(By.CssSelector("p"));
        }

        private bool SupportsSelectorApi()
        {
            return driver is IFindsByCssSelector &&
                (bool)((IJavaScriptExecutor)driver).ExecuteScript("return document['querySelector'] !== undefined;");
        }
    }
}
