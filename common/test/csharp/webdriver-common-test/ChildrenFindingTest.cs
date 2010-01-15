using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using System.Collections.ObjectModel;
using OpenQA.Selenium.Internal;

namespace OpenQA.Selenium
{
    [TestFixture]
    public class ChildrenFindingTest : DriverTestFixture
    {
        [Test]
        public void FindElementByXPath()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("form2"));
            IWebElement child = element.FindElement(By.XPath("select"));
            Assert.AreEqual(child.GetAttribute("id"), "2");
        }

        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void FindElementByXPathWhenNoMatch()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("form2"));
            element.FindElement(By.XPath("select/x"));
        }

        [Test]
        public void FindElementsByXPath()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("form2"));
            ReadOnlyCollection<IWebElement> children = element.FindElements(By.XPath("select/option"));
            Assert.AreEqual(children.Count, 8);
            Assert.AreEqual(children[0].Text, "One");
            Assert.AreEqual(children[1].Text, "Two");
        }

        [Test]
        public void FindElementsByXPathWhenNoMatch()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("form2"));
            ReadOnlyCollection<IWebElement> children = element.FindElements(By.XPath("select/x"));
            Assert.AreEqual(0, children.Count);
        }

        [Test]
        [IgnoreBrowser(Browser.IE, "Multiple items of ID 1 exist in the page, returns subelements of *all* of them, not the one we selected. See issue 278.")]
        [IgnoreBrowser(Browser.Remote, "Multiple items of ID 1 exist in the page, returns subelements of *all* of them, not the one we selected. See issue 278.")]
        [IgnoreBrowser(Browser.HtmlUnit, "Multiple items of ID 1 exist in the page, returns subelements of *all* of them, not the one we selected. See issue 278.")]
        public void FindElementsByXPathWithMultipleParentElementsOfSameId()
        {
            driver.Url = nestedPage;
            IWebElement select = driver.FindElement(By.Id("1"));
            ReadOnlyCollection<IWebElement> elements = select.FindElements(By.XPath("//option"));
            Assert.AreEqual(4, elements.Count);
        }

        [Test]
        [IgnoreBrowser(Browser.IE, "Issue 278.")]
        [IgnoreBrowser(Browser.Remote, "Issue 278.")]
        [IgnoreBrowser(Browser.HtmlUnit, "Issue 278.")]
        public void FindsSubElementNotTopLevelElementWhenLookingUpSubElementByXPath()
        {
            driver.Url = simpleTestPage;
            IWebElement parent = driver.FindElement(By.Id("containsSomeDiv"));
            IWebElement child = parent.FindElement(By.XPath("//div[@name='someDiv']"));
            Assert.IsFalse(child.Text.Contains("Top level"), "Child should not contain text Top level");
            Assert.IsTrue(child.Text.Contains("Nested"), "Child should contain text Nested");
        }

        [Test]
        public void FindElementByName()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("form2"));
            IWebElement child = element.FindElement(By.Name("selectomatic"));
            Assert.AreEqual(child.GetAttribute("id"), "2");
        }

        [Test]
        public void FindElementsByName()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("form2"));
            ReadOnlyCollection<IWebElement> children = element.FindElements(By.Name("selectomatic"));
            Assert.AreEqual(children.Count, 2);
        }

        [Test]
        public void FindElementById()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("form2"));
            IWebElement child = element.FindElement(By.Id("2"));
            Assert.AreEqual(child.GetAttribute("name"), "selectomatic");
        }


        [Test]
        public void FindElementByIdWhenMultipleMatchesExist()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Id("test_id_div"));
            IWebElement child = element.FindElement(By.Id("test_id"));
            Assert.AreEqual(child.Text, "inside");
        }


        [Test]
        [ExpectedException(typeof(NoSuchElementException))]
        public void FindElementByIdWhenNoMatchInContext()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Id("test_id_div"));
            element.FindElement(By.Id("test_id_out"));
        }


        [Test]
        public void FindElementsById()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("form2"));
            ReadOnlyCollection<IWebElement> children = element.FindElements(By.Id("2"));
            Assert.AreEqual(children.Count, 2);
        }


        [Test]
        public void FindElementByLinkText()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("div1"));
            IWebElement child = element.FindElement(By.LinkText("hello world"));
            Assert.AreEqual(child.GetAttribute("name"), "link1");
        }


        [Test]
        public void FindElementsByLinkTest()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("div1"));
            ReadOnlyCollection<IWebElement> elements = element.FindElements(By.LinkText("hello world"));

            Assert.AreEqual(2, elements.Count);
            Assert.AreEqual(elements[0].GetAttribute("name"), "link1");
            Assert.AreEqual(elements[1].GetAttribute("name"), "link2");
        }


        [Test]
        public void FindElementsByLinkText()
        {
            driver.Url = nestedPage;
            IWebElement element = driver.FindElement(By.Name("div1"));
            ReadOnlyCollection<IWebElement> children = element.FindElements(By.LinkText("hello world"));
            Assert.AreEqual(children.Count, 2);
        }

        [Test]
        [IgnoreBrowser(Browser.IE)]
        public void ShouldFindChildElementsByClassName()
        {
            driver.Url = nestedPage;
            IWebElement parent = driver.FindElement(By.Name("classes"));

            IWebElement element = parent.FindElement(By.ClassName("one"));

            Assert.AreEqual("Find me", element.Text);
        }

        [Test]
        [IgnoreBrowser(Browser.IE)]
        public void ShouldFindChildrenByClassName()
        {
            driver.Url = nestedPage;
            IWebElement parent = driver.FindElement(By.Name("classes"));

            ReadOnlyCollection<IWebElement> elements = parent.FindElements(By.ClassName("one"));

            Assert.AreEqual(2, elements.Count);
        }


        [Test]
        public void ShouldFindChildElementsByTagName()
        {
            driver.Url = nestedPage;
            IWebElement parent = driver.FindElement(By.Name("div1"));

            IWebElement element = parent.FindElement(By.TagName("a"));

            Assert.AreEqual("link1", element.GetAttribute("name"));
        }


        [Test]
        public void ShouldFindChildrenByTagName()
        {
            driver.Url = nestedPage;
            IWebElement parent = driver.FindElement(By.Name("div1"));

            ReadOnlyCollection<IWebElement> elements = parent.FindElements(By.TagName("a"));

            Assert.AreEqual(2, elements.Count);
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

            driver.Url = nestedPage;
            IWebElement parent = driver.FindElement(By.Name("form2"));

            IWebElement element = parent.FindElement(By.CssSelector("*[name=\"selectomatic\"]"));

            Assert.AreEqual("2", element.GetAttribute("id"));
        }

        [Test]
        [Category("Javascript")]
        [IgnoreBrowser(Browser.Chrome, "Chrome doesn't handle the many-pages situation well")]
        public void ShouldBeAbleToFindAnElementsByCssSelector()
        {
            if (!SupportsSelectorApi())
            {
                Console.WriteLine("Skipping test: selector API not supported");
                return;
            }

            driver.Url = nestedPage;
            IWebElement parent = driver.FindElement(By.Name("form2"));

            ReadOnlyCollection<IWebElement> elements = parent.FindElements(By.CssSelector("*[name=\"selectomatic\"]"));

            Assert.AreEqual(2, elements.Count);
        }

        private bool SupportsSelectorApi()
        {
            return driver is IFindsByCssSelector &&
                (bool)((IJavaScriptExecutor)driver).ExecuteScript("return document['querySelector'] !== undefined;");
        }
    }
}
