﻿using System.Collections.Generic;
using System.Collections.ObjectModel;
using OpenQA.Selenium.Internal;

namespace OpenQA.Selenium.Remote
{
    /// <summary>
    /// RemoteWebElement allows you to have access to specific items that are found on the page
    /// </summary>
    /// <seealso cref="IRenderedWebElement"/>
    /// <seealso cref="ILocatable"/>
    public class RemoteWebElement : IWebElement, IFindsByLinkText, IFindsById, IFindsByName, IFindsByTagName, IFindsByClassName, IFindsByXPath, IFindsByPartialLinkText, IWrapsDriver
    {
        #region Private members
        private RemoteWebDriver driver;
        private string elementId;
        #endregion

        #region Constructor
        /// <summary>
        /// Initializes a new instance of the <see cref="RemoteWebElement"/> class.
        /// </summary>
        /// <param name="parentDriver">The <see cref="RemoteWebDriver"/> instance hosting this element.</param>
        /// <param name="id">The ID assigned to the element.</param>
        internal RemoteWebElement(RemoteWebDriver parentDriver, string id)
        {
            driver = parentDriver;
            elementId = id;
        }
        #endregion

        #region IWrapsDriver Members
        /// <summary>
        /// Gets the <see cref="IWebDriver"/> used to find this element.
        /// </summary>
        public IWebDriver WrappedDriver
        {
            get { return driver; }
        }
        #endregion

        #region IWebElement properties
        /// <summary>
        /// Gets the DOM Tag of element
        /// </summary>
        public string TagName
        {
            get
            {
                Dictionary<string, object> parameters = new Dictionary<string, object>();
                parameters.Add("id", elementId);
                Response commandResponse = Execute(DriverCommand.GetElementTagName, parameters);
                return commandResponse.Value.ToString();
            }
        }

        /// <summary>
        /// Gets the text from the element
        /// </summary>
        public string Text
        {
            get
            {
                Dictionary<string, object> parameters = new Dictionary<string, object>();
                parameters.Add("id", elementId);
                Response commandResponse = Execute(DriverCommand.GetElementText, parameters);
                return commandResponse.Value.ToString();
            }
        }

        /// <summary>
        /// Gets the value of the element's "value" attribute. If this value has been modified after the page has loaded (for example, through javascript) then this will reflect the current value of the "value" attribute.
        /// </summary>
        public string Value
        {
            get
            {
                Dictionary<string, object> parameters = new Dictionary<string, object>();
                parameters.Add("id", elementId);
                Response commandResponse = Execute(DriverCommand.GetElementValue, parameters);
                return commandResponse.Value.ToString();
            }
        }

        /// <summary>
        /// Gets a value indicating whether an element is currently enabled
        /// </summary>
        public bool Enabled
        {
            get
            {
                Dictionary<string, object> parameters = new Dictionary<string, object>();
                parameters.Add("id", elementId);
                Response commandResponse = Execute(DriverCommand.IsElementEnabled, parameters);
                return (bool)commandResponse.Value;
            }
        }

        /// <summary>
        /// Gets a value indicating whether this element is selected or not. This operation only applies to input elements such as checkboxes, options in a select and radio buttons.
        /// </summary>
        public bool Selected
        {
            get
            {
                Dictionary<string, object> parameters = new Dictionary<string, object>();
                parameters.Add("id", elementId);
                Response commandResponse = Execute(DriverCommand.IsElementSelected, parameters);
                return (bool)commandResponse.Value;
            }
        }
        #endregion

        #region Internal Properties
        /// <summary>
        /// Gets the ID of the element.
        /// </summary>
        /// <remarks>This property is internal to the WebDriver instance, and is
        /// not intended to be used in your code. The element's ID has no meaning
        /// outside of internal WebDriver usage, so it would be improper to scope
        /// it as public. However, both subclasses of <see cref="RemoteWebElement"/>
        /// and the parent driver hosting the element have a need to access the
        /// internal element ID. Therefore, we have two properties returning the
        /// same value, one scoped as internal, the other as protected.</remarks>
        internal string InternalElementId
        {
            get { return elementId; }
        }
        #endregion

        #region Protected properties
        /// <summary>
        /// Gets the ID of the element
        /// </summary>
        /// <remarks>This property is internal to the WebDriver instance, and is
        /// not intended to be used in your code. The element's ID has no meaning
        /// outside of internal WebDriver usage, so it would be improper to scope
        /// it as public. However, both subclasses of <see cref="RemoteWebElement"/>
        /// and the parent driver hosting the element have a need to access the
        /// internal element ID. Therefore, we have two properties returning the
        /// same value, one scoped as internal, the other as protected.</remarks>
        protected string Id
        {
            get { return elementId; }
        }
        #endregion

        #region IWebElement methods
        /// <summary>
        /// Select or unselect element. This operation only applies to input elements such as checkboxes, options in a select and radio buttons.
        /// </summary>
        public void Select()
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            Execute(DriverCommand.SetElementSelected, parameters);
        }

        /// <summary>
        /// Method to clear the text out of an Input element
        /// </summary>
        public void Clear()
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            Execute(DriverCommand.ClearElement, parameters);
        }

        /// <summary>
        /// Method for sending native key strokes to the browser
        /// </summary>
        /// <param name="text">String containing what you would like to type onto the screen</param>
        public void SendKeys(string text)
        {
            // N.B. The Java remote server expects a CharSequence as the value input to
            // SendKeys. In JSON, these are serialized as an array of strings, with a
            // single character to each element of the array. Thus, we must use ToCharArray()
            // to get the same effect.
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            parameters.Add("value", text.ToCharArray());
            Execute(DriverCommand.SendKeysToElement, parameters);
        }

        /// <summary>
        /// If this current element is a form, or an element within a form, then this will be submitted to the remote server. 
        /// If this causes the current page to change, then this method will block until the new page is loaded.
        /// </summary>
        public void Submit()
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            Execute(DriverCommand.SubmitElement, parameters);
        }

        /// <summary>
        /// Click this element. If this causes a new page to load, this method will block until the page has loaded. At this point, you should discard all references to this element and any further operations performed on this element 
        /// will have undefined behaviour unless you know that the element and the page will still be present. If this element is not clickable, then this operation is a no-op since it's pretty common for someone to accidentally miss 
        /// the target when clicking in Real Life
        /// </summary>
        public void Click()
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            Execute(DriverCommand.ClickElement, parameters);
        }

        /// <summary>
        /// If this current element is a form, or an element within a form, then this will be submitted to the remote server. If this causes the current page to change, then this method will block until the new page is loaded.
        /// </summary>
        /// <param name="attributeName">Attribute you wish to get details of</param>
        /// <returns>The attribute's current value or null if the value is not set.</returns>
        public string GetAttribute(string attributeName)
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            parameters.Add("name", attributeName);
            Response commandResponse = Execute(DriverCommand.GetElementAttribute, parameters);
            string attributeValue = string.Empty;
            if (commandResponse.Value == null)
            {
                attributeValue = null;
            }
            else
            {
                attributeValue = commandResponse.Value.ToString();

                // Normalize string values of boolean results as lowercase.
                if (commandResponse.Value is bool)
                {
                    attributeValue = attributeValue.ToLowerInvariant();
                }
            }

            return attributeValue;
        }

        /// <summary>
        /// If the element is a checkbox this will toggle the elements state from selected to not selected, or from not selected to selected
        /// </summary>
        /// <returns>Whether the toggled element is selected (true) or not (false) after this toggle is complete</returns>
        public bool Toggle()
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            Response commandResponse = Execute(DriverCommand.ToggleElement, parameters);
            return (bool)commandResponse.Value;
        }

        /// <summary>
        /// Finds the elements on the page by using the <see cref="By"/> object and returns a ReadOnlyCollection of the Elements on the page
        /// </summary>
        /// <param name="by">By mechanism to find the element</param>
        /// <returns>ReadOnlyCollection of IWebElement</returns>
        public ReadOnlyCollection<IWebElement> FindElements(By by)
        {
            return by.FindElements(this);
        }

        /// <summary>
        /// Finds the first element in the page that matches the <see cref="By"/> object
        /// </summary>
        /// <param name="by">By mechanism to find the element</param>
        /// <returns>IWebElement object so that you can interction that object</returns>
        public IWebElement FindElement(By by)
        {
            return by.FindElement(this);
        }
        #endregion

        #region IFindsByLinkText Members
        /// <summary>
        /// Finds the first of elements that match the link text supplied
        /// </summary>
        /// <param name="linkText">Link text of element </param>
        /// <returns>IWebElement object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// IWebElement elem = driver.FindElementByLinkText("linktext")
        /// </code>
        /// </example>
        public IWebElement FindElementByLinkText(string linkText)
        {
            return FindElement("link text", linkText);
        }

        /// <summary>
        /// Finds the first of elements that match the link text supplied
        /// </summary>
        /// <param name="linkText">Link text of element </param>
        /// <returns>IWebElement object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// ReadOnlyCollection<![CDATA[<IWebElement>]]> elem = driver.FindElementsByLinkText("linktext")
        /// </code>
        /// </example>
        public ReadOnlyCollection<IWebElement> FindElementsByLinkText(string linkText)
        {
            return FindElements("link text", linkText);
        }

        #endregion

        #region IFindsById Members
        /// <summary>
        /// Finds the first element in the page that matches the ID supplied
        /// </summary>
        /// <param name="id">ID of the element</param>
        /// <returns>IWebElement object so that you can interction that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// IWebElement elem = driver.FindElementById("id")
        /// </code>
        /// </example>
        public IWebElement FindElementById(string id)
        {
            return FindElement("id", id);
        }

        /// <summary>
        /// Finds the first element in the page that matches the ID supplied
        /// </summary>
        /// <param name="id">ID of the Element</param>
        /// <returns>ReadOnlyCollection of Elements that match the object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// ReadOnlyCollection<![CDATA[<IWebElement>]]> elem = driver.FindElementsById("id")
        /// </code>
        /// </example>
        public ReadOnlyCollection<IWebElement> FindElementsById(string id)
        {
            return FindElements("id", id);
        }

        #endregion

        #region IFindsByName Members
        /// <summary>
        /// Finds the first of elements that match the name supplied
        /// </summary>
        /// <param name="name">Name of the element</param>
        /// <returns>IWebElement object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// elem = driver.FindElementsByName("name")
        /// </code>
        /// </example>
        public IWebElement FindElementByName(string name)
        {
            return FindElement("name", name);
        }

        /// <summary>
        /// Finds a list of elements that match the name supplied
        /// </summary>
        /// <param name="name">Name of element</param>
        /// <returns>ReadOnlyCollect of IWebElement objects so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// ReadOnlyCollection<![CDATA[<IWebElement>]]> elem = driver.FindElementsByName("name")
        /// </code>
        /// </example>
        public ReadOnlyCollection<IWebElement> FindElementsByName(string name)
        {
            return FindElements("name", name);
        }

        #endregion

        #region IFindsByTagName Members
        /// <summary>
        /// Finds the first of elements that match the DOM Tag supplied
        /// </summary>
        /// <param name="tagName">tag name of the element</param>
        /// <returns>IWebElement object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// IWebElement elem = driver.FindElementsByTagName("tag")
        /// </code>
        /// </example>
        public IWebElement FindElementByTagName(string tagName)
        {
            return FindElement("tag name", tagName);
        }

        /// <summary>
        /// Finds a list of elements that match the DOM Tag supplied
        /// </summary>
        /// <param name="tagName">DOM Tag of the element on the page</param>
        /// <returns>IWebElement object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// ReadOnlyCollection<![CDATA[<IWebElement>]]> elem = driver.FindElementsByTagName("tag")
        /// </code>
        /// </example>
        public ReadOnlyCollection<IWebElement> FindElementsByTagName(string tagName)
        {
            return FindElements("tag name", tagName);
        }
        #endregion

        #region IFindsByClassName Members
        /// <summary>
        /// Finds the first element in the page that matches the CSS Class supplied
        /// </summary>
        /// <param name="className">className of the</param>
        /// <returns>IWebElement object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// IWebElement elem = driver.FindElementByClassName("classname")
        /// </code>
        /// </example>
        public IWebElement FindElementByClassName(string className)
        {
            return FindElement("class name", className);
        }

        /// <summary>
        /// Finds a list of elements that match the classname supplied
        /// </summary>
        /// <param name="className">CSS class name of the elements on the page</param>
        /// <returns>ReadOnlyCollection of IWebElement object so that you can interact with those objects</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// ReadOnlyCollection<![CDATA[<IWebElement>]]> elem = driver.FindElementsByClassName("classname")
        /// </code>
        /// </example>
        public ReadOnlyCollection<IWebElement> FindElementsByClassName(string className)
        {
            return FindElements("class name", className);
        }
        #endregion

        #region IFindsByXPath Members
        /// <summary>
        /// Finds the first of elements that match the XPath supplied
        /// </summary>
        /// <param name="xpath">xpath to the element</param>
        /// <returns>IWebElement object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// IWebElement elem = driver.FindElementsByXPath("//table/tbody/tr/td/a");
        /// </code>
        /// </example>
        public IWebElement FindElementByXPath(string xpath)
        {
            return FindElement("xpath", xpath);
        }

        /// <summary>
        /// Finds a list of elements that match the XPath supplied
        /// </summary>
        /// <param name="xpath">xpath to element on the page</param>
        /// <returns>ReadOnlyCollection of IWebElement objects so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// ReadOnlyCollection<![CDATA[<IWebElement>]]> elem = driver.FindElementsByXpath("//tr/td/a")
        /// </code>
        /// </example>
        public ReadOnlyCollection<IWebElement> FindElementsByXPath(string xpath)
        {
            return FindElements("xpath", xpath);
        }
        #endregion

        #region IFindsByPartialLinkText Members
        /// <summary>
        /// Finds the first of elements that match the part of the link text supplied
        /// </summary>
        /// <param name="partialLinkText">part of the link text</param>
        /// <returns>IWebElement object so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// IWebElement elem = driver.FindElementsByPartialLinkText("partOfLink")
        /// </code>
        /// </example>
        public IWebElement FindElementByPartialLinkText(string partialLinkText)
        {
            return FindElement("partial link text", partialLinkText);
        }

        /// <summary>
        /// Finds a list of elements that match the classname supplied
        /// </summary>
        /// <param name="partialLinkText">part of the link text</param>
        /// <returns>ReadOnlyCollection<![CDATA[<IWebElement>]]> objects so that you can interact that object</returns>
        /// <example>
        /// <code>
        /// IWebDriver driver = new RemoteWebDriver(DesiredCapabilities.Firefox());
        /// ReadOnlyCollection<![CDATA[<IWebElement>]]> elem = driver.FindElementsByPartialLinkText("partOfTheLink")
        /// </code>
        /// </example>
        public ReadOnlyCollection<IWebElement> FindElementsByPartialLinkText(string partialLinkText)
        {
            return FindElements("partial link text", partialLinkText);
        }
        #endregion

        #region Overrides
        /// <summary>
        /// Method to get the hash code of the element
        /// </summary>
        /// <returns>Interger of the hash code for the element</returns>
        public override int GetHashCode()
        {
            return elementId.GetHashCode();
        }

        /// <summary>
        /// Compares if two elements are equal
        /// </summary>
        /// <param name="obj">Object to compare against</param>
        /// <returns>A boolean if it is equal or not</returns>
        public override bool Equals(object obj)
        {
            IWebElement other = obj as IWebElement;
            if (other == null)
            {
                return false;
            }

            IWrapsElement objAsWrapsElement = obj as IWrapsElement;
            if (objAsWrapsElement != null)
            {
                other = objAsWrapsElement.WrappedElement;
            }

            RemoteWebElement otherAsElement = other as RemoteWebElement;
            if (otherAsElement == null)
            {
                return false;
            }

            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            parameters.Add("other", otherAsElement.Id);

            Response response = Execute(DriverCommand.ElementEquals, parameters);
            object value = response.Value;
            return value != null && value is bool && (bool)value;
        }
        #endregion

        #region Protected support methods
        /// <summary>
        /// Finds a child element matching the given mechanism and value.
        /// </summary>
        /// <param name="mechanism">The mechanism by which to find the element.</param>
        /// <param name="value">The value to use to search for the element.</param>
        /// <returns>The first <see cref="IWebElement"/> matching the given criteria.</returns>
        protected IWebElement FindElement(string mechanism, string value)
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            parameters.Add("using", mechanism);
            parameters.Add("value", value);
            Response commandResponse = Execute(DriverCommand.FindChildElement, parameters);
            return driver.GetElementFromResponse(commandResponse);
        }

        /// <summary>
        /// Finds all child elements matching the given mechanism and value.
        /// </summary>
        /// <param name="mechanism">The mechanism by which to find the elements.</param>
        /// <param name="value">The value to use to search for the elements.</param>
        /// <returns>A collection of all of the <see cref="IWebElement">IWebElements</see> matchings the given criteria.</returns>
        protected ReadOnlyCollection<IWebElement> FindElements(string mechanism, string value)
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("id", elementId);
            parameters.Add("using", mechanism);
            parameters.Add("value", value);
            Response commandResponse = Execute(DriverCommand.FindChildElements, parameters);
            return driver.GetElementsFromResponse(commandResponse);
        }

        /// <summary>
        /// Executes a command on this element using the specified parameters.
        /// </summary>
        /// <param name="commandToExecute">The <see cref="DriverCommand"/> to execute against this element.</param>
        /// <param name="parameters">A <see cref="Dictionary{K, V}"/> containing names and values of the parameters for the command.</param>
        /// <returns>The <see cref="Response"/> object containing the result of the command execution.</returns>
        protected Response Execute(DriverCommand commandToExecute, Dictionary<string, object> parameters)
        {
            return driver.InternalExecute(commandToExecute, parameters);
        }
        #endregion
    }
}
