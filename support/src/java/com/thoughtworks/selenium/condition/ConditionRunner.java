/*
 * Copyright 2008 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.thoughtworks.selenium.condition;

import com.thoughtworks.selenium.Selenium;

/**
 * A ConditionRunner is a class that can execute a {@link Condition}, which
 * need certain basic pieces that it needs to execute (e.g. an instance
 * of {@link Selenium}). This is achieved through the {@link Context} interface.
 */
public interface ConditionRunner {

    /**
     * This method will, every so often, evaluate the given {@code condition}'s
     * {@link Condition#isTrue(ConditionRunner.Context)} method, until:
     * <p/>
     * <ul>
     * <li>it becomes true, in which  case it simply returns
     * <li>a certain amount of time is passed, in which case it fails by throwing
     * an failure exception tailored to a given test framework -- e.g.
     * {@link junit.framework.AssertionFailedError} in the case of JUnit
     * <li>it throws an exception, in which case that is wrapped inside a
     * {@link RuntimeException} and rethrown
     * </ul>
     * <p/>
     * How often if "every so often" and how long is the "certain amount of time"
     * is left to the specific implementations of this interface.
     */
    void waitFor(Condition condition);

    /**
     * As above but with an additonal 'should' phrase narrative used in the
     * event of the condition failing to become true
     */    
    void waitFor(String narrative, Condition condition);

    /**
     * Used by implementations of {@link ConditionRunner#waitFor(Condition)} to
     * provide context to the
     * {@link Condition#isTrue(com.google.testing.selenium.condition.ConditionRunner.Context)}
     * method
     */
    public interface Context {

        /**
         * Returns the condition runner inside which this condition is being run.
         * <p/>
         * <p>This allows for a condition to chain to other conditions.
         */
        ConditionRunner getConditionRunner();

        /**
         * Returns the {@link Selenium} associated with this instance. This method
         * will almost always be called by any
         * {@link Condition#isTrue(ConditionRunner.Context)}.
         */
        Selenium getSelenium();

        /**
         * A {@link Condition#isTrue(ConditionRunner.Context)} can call this method
         * to set extra information to be displayed upon a failure.
         */
        void info(String string);

        /**
         * Returns the amount of time elapsed since the {@link #waitFor(Condition)}
         * method for this context was called.
         */
        long elapsed();
    }
}
