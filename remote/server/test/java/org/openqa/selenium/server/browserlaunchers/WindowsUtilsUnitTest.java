/*
 * Copyright 2006 ThoughtWorks, Inc.
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
package org.openqa.selenium.server.browserlaunchers;

import junit.framework.TestCase;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowsUtilsUnitTest extends TestCase {

    private int majorVersion;
    private int minorVersion;
    private Pattern WIN_OS_VERSION = Pattern.compile("^(\\d)+\\.(\\d)+$");
    
    public void setUp() {
        if (!WindowsUtils.thisIsWindows()) return;
        String osVersion = System.getProperty("os.version");
        Matcher m = WIN_OS_VERSION.matcher(osVersion);
        if (!m.find()) fail("osVersion doesn't look right: " + osVersion);
        majorVersion = Integer.parseInt(m.group(1));
        minorVersion = Integer.parseInt(m.group(2));
    }
    
    private boolean isXpOrHigher() {
        return majorVersion >= 5 && minorVersion >= 1;
    }
    
    public void testLoadEnvironment() {
        if (!WindowsUtils.thisIsWindows()) return;
        Map p =WindowsUtils.loadEnvironment();
        assertFalse("Environment appears to be empty!", p.isEmpty());
//            for (Iterator i = p.keySet().iterator(); i.hasNext();) {
//                String key = (String) i.next();
//                String value = (String) p.get(key);
//                System.out.print(key);
//                System.out.print('=');
//                System.out.println(value);
//            }
        assertNotNull("SystemRoot env var apparently not set on Windows!", WindowsUtils.findSystemRoot());
    }
    public void testWMIC() {
        if (!WindowsUtils.thisIsWindows()) return;
        if (!isXpOrHigher()) return;
        assertTrue("wmic should be found", "wmic" != WindowsUtils.findWMIC());
    }
    public void testTaskKill() {
        if (!WindowsUtils.thisIsWindows()) return;
        if (!isXpOrHigher()) return;
        assertTrue("taskkill should be found", "taskkill" != WindowsUtils.findTaskKill());
    }
    public void testRegistry() {
        if (!WindowsUtils.thisIsWindows()) return;
        String keyCurrentVersion = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\CurrentVersion";
        String keyProxyEnable = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\ProxyEnable";
        String keySeleniumFoo = "HKEY_CURRENT_USER\\Software\\Selenium\\RemoteControl\\foo";
        assertTrue("Standard Windows reg key CurrentVersion doesn't exist", WindowsUtils.doesRegistryValueExist(keyCurrentVersion));
        System.out.println("CurrentVersion: " + WindowsUtils.readStringRegistryValue(keyCurrentVersion));
        assertTrue("Standard Windows reg key ProxyEnable doesn't exist", WindowsUtils.doesRegistryValueExist(keyProxyEnable));
        System.out.println("ProxyEnable: " + WindowsUtils.readIntRegistryValue(keyProxyEnable));
        WindowsUtils.writeStringRegistryValue(keySeleniumFoo, "bar");
        assertEquals("Didn't set Foo string key correctly", "bar", WindowsUtils.readStringRegistryValue(keySeleniumFoo));
        WindowsUtils.writeStringRegistryValue(keySeleniumFoo, "baz");
        assertEquals("Didn't modify Foo string key correctly", "baz", WindowsUtils.readStringRegistryValue(keySeleniumFoo));
        WindowsUtils.deleteRegistryValue(keySeleniumFoo);
        assertFalse("Didn't delete Foo key correctly", WindowsUtils.doesRegistryValueExist(keySeleniumFoo));
        WindowsUtils.writeBooleanRegistryValue(keySeleniumFoo, true);
        assertTrue("Didn't set Foo boolean key correctly", WindowsUtils.readBooleanRegistryValue(keySeleniumFoo));
        WindowsUtils.deleteRegistryValue(keySeleniumFoo);
        assertFalse("Didn't delete Foo key correctly", WindowsUtils.doesRegistryValueExist(keySeleniumFoo));
    }
    public void testVersion1() {
    	if (!WindowsUtils.thisIsWindows()) return;
    	System.out.println("Version 1: " + WindowsUtils.isRegExeVersion1());
    }
}
