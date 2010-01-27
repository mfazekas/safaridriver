/*
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.

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

package org.openqa.selenium;

import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;

import java.util.List;

/**
 * Mechanism used to locate elements within a document. In order to create
 * your own locating mechanisms, it is possible to subclass this class and
 * override the protected methods as required, though it is expected that
 * that all subclasses rely on the basic finding mechanisms provided through
 * static methods of this class:
 *
 * <code>
 * public WebElement findElement(WebDriver driver) {
 *     WebElement element = driver.findElement(By.id(getSelector()));
 *     if (element == null)
 *       element = driver.findElement(By.name(getSelector());
 *     return element;
 * }
 * </code>
 */
public abstract class By {
  /**
   * @param id The value of the "id" attribute to search for
   * @return a By which locates elements by the value of the "id" attribute.
   */
    public static By id(final String id) {
      if (id == null)
        throw new IllegalArgumentException("Cannot find elements with a null id attribute.");

      return new By() {
        @Override
        public List<WebElement> findElements(SearchContext context) {
          if (context instanceof FindsById)
              return ((FindsById) context).findElementsById(id);
          return ((FindsByXPath) context).findElementsByXPath("*[@id = '" + id + "']");
        }

        @Override
        public WebElement findElement(SearchContext context) {
          if (context instanceof FindsById)
            return ((FindsById) context).findElementById(id);
          return ((FindsByXPath) context).findElementByXPath("*[@id = '" + id + "']");
        }
       
        @Override
        public String toString() {
          return "By.id: " + id;
        }
      };
    }

  /**
   * @param linkText The exact text to match against
   * @return a By which locates A elements by the exact text it displays
   */
    public static By linkText(final String linkText) {
      if (linkText == null)
        throw new IllegalArgumentException("Cannot find elements when link text is null.");

      return new By() {
        @Override
        public List<WebElement> findElements(SearchContext context) {
          return ((FindsByLinkText) context).findElementsByLinkText(linkText);
        }

        @Override
        public WebElement findElement(SearchContext context) {
          return ((FindsByLinkText) context).findElementByLinkText(linkText);
        }
        
        @Override
        public String toString() {
          return "By.linkText: " + linkText;
        }
      };
    }

  /**
   * @param linkText The text to match against
   * @return a By which locates A elements that contain the given link text
   */
    public static By partialLinkText(final String linkText) {
      if (linkText == null)
        throw new IllegalArgumentException("Cannot find elements when link text is null.");

      return new By() {
        @Override
        public List<WebElement> findElements(SearchContext context) {
          return ((FindsByLinkText) context).findElementsByPartialLinkText(linkText);
        }

        @Override
        public WebElement findElement(SearchContext context) {
          return ((FindsByLinkText) context).findElementByPartialLinkText(linkText);
        }
        
        @Override
        public String toString() {
          return "By.linkText: " + linkText;
        }
      };
    }

  /**
   * @param name The value of the "name" attribute to search for
   * @return a By which locates elements by the value of the "name" attribute.
   */
    public static By name(final String name) {
      if (name == null)
        throw new IllegalArgumentException("Cannot find elements when name text is null.");

      return new By() {
        @Override
        public List<WebElement> findElements(SearchContext context) {
            if (context instanceof FindsByName)
              return ((FindsByName) context).findElementsByName(name);
            return ((FindsByXPath) context).findElementsByXPath(".//*[@name = '" + name + "']");
        }

        @Override
        public WebElement findElement(SearchContext context) {
          if (context instanceof FindsByName)
            return ((FindsByName) context).findElementByName(name);
          return ((FindsByXPath) context).findElementByXPath(".//*[@name = '" + name + "']");
        }
        
        @Override
        public String toString() {
          return "By.name: " + name;
        }
      };
    }

  /**
   * @param name The element's tagName
   * @return a By which locates elements by their tag name
   */
    public static By tagName(final String name) {
      if (name == null)
        throw new IllegalArgumentException("Cannot find elements when name tag name is null.");

      return new By() {
        @Override
        public List<WebElement> findElements(SearchContext context) {
            if (context instanceof FindsByTagName)
              return ((FindsByTagName) context).findElementsByTagName(name);
            return ((FindsByXPath) context).findElementsByXPath(".//" + name);
        }

        @Override
        public WebElement findElement(SearchContext context) {
          if (context instanceof FindsByTagName)
            return ((FindsByTagName) context).findElementByTagName(name);
          return ((FindsByXPath) context).findElementByXPath(".//" + name);
        }
        
        @Override
        public String toString() {
          return "By.tagName: " + name;
        }
      };
    }

  /**
   * @param xpathExpression The xpath to use
   * @return a By which locates elements via XPath
   */
    public static By xpath(final String xpathExpression) {
       if (xpathExpression == null)
        throw new IllegalArgumentException("Cannot find elements when the XPath expression is null.");

      return new By() {
        @Override
        public List<WebElement> findElements(SearchContext context) {
          return ((FindsByXPath) context).findElementsByXPath(xpathExpression);
        }

        @Override
        public WebElement findElement(SearchContext context) {
          return ((FindsByXPath) context).findElementByXPath(xpathExpression);
        }
     
        @Override
        public String toString() {
          return "By.xpath: " + xpathExpression;
        }
      };
    }

  /**
   * Finds elements based on the value of the "class" attribute. If an element has many classes
   * then this will match against each of them. For example if the value is "one two onone", then the
   * following "className"s will match: "one" and "two"
   *
   * @param className The value of the "class" attribute to search for
   * @return a By which locates elements by the value of the "class" attribute.
   */
    public static By className(final String className) {
        if (className == null)
         throw new IllegalArgumentException("Cannot find elements when the class name expression is null.");

        if (className.matches(".*\\s+.*")) {
          throw new IllegalLocatorException(
              "Compound class names are not supported. Consider searching for one class name and filtering the results.");
        }


       return new By() {
         @Override
         public List<WebElement> findElements(SearchContext context) {
             if (context instanceof FindsByClassName)
               return ((FindsByClassName) context).findElementsByClassName(className);
             return ((FindsByXPath) context).findElementsByXPath(".//*[" + containingWord("class", className) + "]");
         }

         @Override
         public WebElement findElement(SearchContext context) {
             if (context instanceof FindsByClassName)
               return ((FindsByClassName) context).findElementByClassName(className);
             return ((FindsByXPath) context).findElementByXPath(".//*[" + containingWord("class", className) + "]");
         }

         /**
          * Generates a partial xpath expression that matches an element whose specified attribute
          * contains the given CSS word. So to match &lt;div class='foo bar'&gt; you would
          * say "//div[" + containingWord("class", "foo") + "]".
          *
          * @param attribute name
          * @param word name
          * @return XPath fragment
          */
         private String containingWord(String attribute, String word) {
           return "contains(concat(' ',normalize-space(@" + attribute + "),' '),' " + word + " ')";
         }
         
         @Override
         public String toString() {
           return "By.className: " + className;
         }
       };
     }

      /**
       * Finds elements via the driver's underlying W3 Selector engine. If the browser does not
       * implement the Selector API an exception will be thrown.
       */
      public static By cssSelector(final String selector) {
        if (selector == null)
          throw new IllegalArgumentException("Cannot find elements when the selector is null");

        return new By() {
          @Override
          public WebElement findElement(SearchContext context) {
            if (context instanceof FindsByCssSelector) {
              return ((FindsByCssSelector) context).findElementByCssSelector(selector);
            }

            throw new WebDriverException(
                "Driver does not support finding an element by selector: " + selector);
          }

          @Override
          public List<WebElement> findElements(SearchContext context) {
            if (context instanceof FindsByCssSelector) {
              return ((FindsByCssSelector) context).findElementsByCssSelector(selector);
            }

            throw new WebDriverException(
                "Driver does not support finding elements by selector: " + selector);
          }

          @Override
          public String toString() {
            return "By.selector: " + selector;
          }
        };

      }


    /**
     * Find a single element. Override this method if necessary.
     * @param context A context to use to find the element
     * @return The WebElement that matches the selector
     */
    public WebElement findElement(SearchContext context) {
        List<WebElement> allElements = findElements(context);
        if (allElements == null || allElements.size() == 0)
            throw new NoSuchElementException("Cannot locate an element using " + toString());
        return allElements.get(0);
    }

    /**
     * Find many elements.
     *
     * @param context A context to use to find the element
     * @return A list of WebElements matching the selector
     */
    public abstract List<WebElement> findElements(SearchContext context);
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        By by = (By) o;

        return toString().equals(by.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
