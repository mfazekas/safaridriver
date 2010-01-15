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

package org.openqa.selenium.support.pagefactory;

import java.lang.reflect.Field;

/**
 * Allows the PageFactory to decorate fields.
 */
public interface FieldDecorator {
  /**
   * This method is called by PageFactory on all fields to decide how to
   * decorate the field.
   * @param loader The class loader that was used for the page object
   * @param field The field that may be decorated.
   * @return Value to decorate the field with or null if it shouldn't be
   * decorated.  If non-null, must be assignable to the field.
   */
  Object decorate(ClassLoader loader, Field field);
}
