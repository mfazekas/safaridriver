/* Copyright notice and license
Copyright 2007-2010 WebDriver committers
Copyright 2007-2010 Google Inc.
Portions copyright 2007 ThoughtWorks, Inc

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

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Text;

namespace OpenQA.Selenium
{
    /// <summary>
    /// Defines the interface through which the user controls the browser.
    /// </summary>
    /// <remarks>
    /// The <see cref="IWebDriver"/> interface is the main interface to use for testing, which
    /// represents an idealised web browser. The methods in this class fall into three categories: 
    /// <list type="bullet">
    /// <item><description>Control of the browser itself</description></item>
    /// <item><description>Selection of <see cref="IWebElement">IWebElements</see></description></item>
    /// <item><description>Debugging aids</description></item>
    /// </list>
    /// <para>
    /// Key properties and methods are <see cref="IWebDriver.Url"/>, which is used to 
    /// load a new web page by setting the property, and the various methods similar 
    /// to <see cref="FindElement"/>, which is used to find <see cref="IWebElement">IWebElements</see>.
    /// </para>
    /// <para>
    /// You use the interface by instantiate drivers that implement of this interface.
    /// You should write your tests against this interface so that you may "swap in" a 
    /// more fully featured browser when there is a requirement for one.
    /// </para>
    /// </remarks>
    public interface IWebDriver : IDisposable
    {
        /// <summary>
        /// Gets or sets the URL the browser is currently displaying.
        /// </summary>
        /// <remarks>
        /// Setting the <see cref="Url"/> property will load a new web page in the current browser window. 
        /// This is done using an HTTP GET operation, and the method will block until the 
        /// load is complete. This will follow redirects issued either by the server or 
        /// as a meta-redirect from within the returned HTML. Should a meta-redirect "rest"
        /// for any duration of time, it is best to wait until this timeout is over, since 
        /// should the underlying page change while your test is executing the results of 
        /// future calls against this interface will be against the freshly loaded page. 
        /// </remarks>
        /// <seealso cref="INavigation.GoToUrl(System.String)"/>
        /// <seealso cref="INavigation.GoToUrl(System.Uri)"/>
        string Url { get; set; }

        /// <summary>
        /// Gets the title of the current browser window.
        /// </summary>
        string Title { get; }

        /// <summary>
        /// Gets the source of the page last loaded by the browser.
        /// </summary>
        /// <remarks>
        /// If the page has been modified after loading (for example, by JavaScript) 
        /// there is no guarentee that the returned text is that of the modified page. 
        /// Please consult the documentation of the particular driver being used to 
        /// determine whether the returned text reflects the current state of the page 
        /// or the text last sent by the web server. The page source returned is a 
        /// representation of the underlying DOM: do not expect it to be formatted 
        /// or escaped in the same way as the response sent from the web server. 
        /// </remarks>
        string PageSource { get; }

        /// <summary>
        /// Finds the first <see cref="IWebElement"/> on the current page using the specified mechanism.
        /// </summary>
        /// <param name="mechanism">A <see cref="By"/> object describing the mechanism used to find the element.</param>
        /// <returns>The first <see cref="IWebElement"/> on the current page matching the criteria.</returns>
        /// <exception cref="NoSuchElementException">If no matching elements are found.</exception>
        IWebElement FindElement(By mechanism);

        /// <summary>
        /// Finds all <see cref="IWebElement"/> on the current page using the specified mechanism.
        /// </summary>
        /// <param name="mechanism">A <see cref="By"/> object describing the mechanism used to find the element.</param>
        /// <returns>A <see cref="ReadOnlyCollection{T}"/> containing the <see cref="IWebElement">IWebElements</see>
        /// found on the page.</returns>
        ReadOnlyCollection<IWebElement> FindElements(By mechanism);

        /// <summary>
        /// Close the current window, quitting the browser if it is the last window currently open.
        /// </summary>
        void Close();

        /// <summary>
        /// Quits this driver, closing every associated window.
        /// </summary>
        void Quit();

        /// <summary>
        /// Instructs the driver to change its settings.
        /// </summary>
        /// <returns>An <see cref="IOptions"/> object allowing the user to change
        /// the settings of the driver.</returns>
        IOptions Manage();

        /// <summary>
        /// Instructs the driver to navigate the browser to another location.
        /// </summary>
        /// <returns>An <see cref="INavigation"/> object allowing the user to access 
        /// the browser's history and to navigate to a given URL.</returns>
        INavigation Navigate();

        /// <summary>
        /// Instructs the driver to send future commands to a different frame or window.
        /// </summary>
        /// <returns>An <see cref="ITargetLocator"/> object which can be used to select
        /// a frame or window.</returns>
        ITargetLocator SwitchTo();

        /// <summary>
        /// Get the window handles of open browser windows.
        /// </summary>
        /// <returns>A <see cref="ReadOnlyCollection{T}"/> containing all window handles
        /// of windows belonging to this driver instance.</returns>
        /// <remarks>The set of window handles returned by this method can be used to 
        /// iterate over all open windows of this <see cref="IWebDriver"/> instance by 
        /// passing them to <c>SwitchTo().Window(string)</c></remarks>
        ReadOnlyCollection<string> GetWindowHandles();

        /// <summary>
        /// Get the current window handle.
        /// </summary>
        /// <returns>An opaque handle to this window that uniquely identifies it 
        /// within this driver instance.</returns>
        string GetWindowHandle();
    }
}
