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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.openqa.jetty.log.LogFactory;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.browserlaunchers.WindowsUtils.WindowsRegistryException;
import org.openqa.selenium.server.log.AntJettyLoggerBuildListener;

public class WindowsProxyManager {
    static Log log = LogFactory.getLog(WindowsProxyManager.class);
    protected static final String REG_KEY_BACKUP_READY = "BackupReady";
    
    // All Cookies end in ".txt"
    protected static final String COOKIE_SUFFIX = ".txt";
    
    // All cookies hidden by Selenium RC will go here.
    protected static final File HIDDEN_COOKIE_DIR = new File(
        System.getenv("USERPROFILE") 
          + File.separator + "CookiesHiddenBySeleniumRC");
    
    protected static String REG_KEY_BASE = "HKEY_CURRENT_USER";
    private static final Pattern HUDSUCKR_LINE = Pattern.compile("^([^=]+)=(.*)$");
    private HudsuckrSettings oldSettings;
    private boolean customPACappropriate;
    private File customProxyPACDir;
    private int port;
    private int portDriversShouldContact;
    private boolean changeMaxConnections;
    private static final Preferences prefs = Preferences.userNodeForPackage(WindowsProxyManager.class);

    public WindowsProxyManager(boolean customPACappropriate, String sessionId, int port, int portDriversShouldContact) {
        this.portDriversShouldContact = portDriversShouldContact;
        this.customPACappropriate = customPACappropriate;
        this.port = port;
        customProxyPACDir = LauncherUtils.createCustomProfileDir(sessionId);
        if (customProxyPACDir.exists()) {
            LauncherUtils.recursivelyDeleteDir(customProxyPACDir);
        }
        customProxyPACDir.mkdir();
        init();
    }
    
    public void setChangeMaxConnections(boolean changeMaxConnections) {
        this.changeMaxConnections = changeMaxConnections;
    }
    
    public boolean getChangeMaxConnections() {
        return changeMaxConnections;
    }
    
    public File getCustomProxyPACDir() {
        return customProxyPACDir;
    }

    protected void init() {
    	handleEvilPopupMgrBackup();
    }
    
    // IE7 changed the type of the popup mgr key to DWORD (int/boolean) from String (which could be "yes" or "no")
    protected void handleEvilPopupMgrBackup() {
    	if (RegKey.POPUP_MGR.type != null) return;
        // this will return String (REG_SZ), int (REG_DWORD), or null if the key is missing
        RegKey.POPUP_MGR.type = WindowsUtils.discoverRegistryKeyType(RegKey.POPUP_MGR.key);
        Class<?> backupPopupMgrType = discoverPrefKeyType(RegKey.POPUP_MGR.name());
        if (RegKey.POPUP_MGR.type == null) { // if official PopupMgr key is missing
            if (backupPopupMgrType == null) {
                // we don't know which type it should be; let's take a guess
                // IE6 can deal with a DWORD 0
            	RegKey.POPUP_MGR.type = boolean.class;
                return;
            }
            // non-null backup type is our best guess
            RegKey.POPUP_MGR.type = backupPopupMgrType;
            return;
        }
        if (RegKey.POPUP_MGR.type.equals(backupPopupMgrType)) return;
        
        // if we're here, we know the current type of pop-up manager,
        // and the backup has a different (wrong) type
        if (backupPopupMgrType != null) {
            WindowsUtils.deleteRegistryValue(RegKey.POPUP_MGR.key);
        }
        if (!backupIsReady()) {
            return;
        }
        
        // assume they originally wanted it off, set backup pref to false
        String value = "no";
        if (RegKey.POPUP_MGR.type.equals(boolean.class)) {
        	value = "false";
        }
        prefs.put(RegKey.POPUP_MGR.name(), value);
    }
    
    private static boolean prefNodeExists(String key) {
        return null != prefs.get(key, null);
    }
    
    private Class<?> discoverPrefKeyType(String key) {
        String data = prefs.get(key, null);
        if (data == null) return null;
        if ("true".equals(data) || "false".equals(data)) {
            return boolean.class;
        }
        try {
            Integer.parseInt(data);
            return int.class;
        } catch (NumberFormatException e) {
            return String.class;
        }
    }

    public static void setBaseRegKey(String base) {
        REG_KEY_BASE = base;
    }

    protected void changeRegistrySettings(boolean ensureCleanSession, boolean avoidProxy) throws IOException {
    	log.info("Modifying registry settings...");
    	HudsuckrSettings settings;
    	if (oldSettings == null) {
    		backupHudsuckrSettings();
    	}
    	if (!customPACappropriate) {
    		String proxyServer = "127.0.0.1:" + portDriversShouldContact;
            settings = new HudsuckrSettings(oldSettings.connection, true, true, false, false, proxyServer, "(null)", "(null)");
        } else {
            File proxyPAC = LauncherUtils.makeProxyPAC(customProxyPACDir, port, avoidProxy);

            String newURL = "file://" + proxyPAC.getAbsolutePath().replace('\\', '/');
            settings = new HudsuckrSettings(oldSettings.connection, true, false, true, false, "(null)", "(null)", newURL);
        }
    	runHudsuckr(settings.toStringArray());

        // Disabling automatic proxy caching
        // http://support.microsoft.com/?kbid=271361
        // Otherwise, *all* requests will go through our proxy, rather than just */selenium-server/* requests
        try {
            WindowsUtils.writeBooleanRegistryValue(RegKey.AUTOPROXY_RESULT_CACHE.key, false);
        } catch (WindowsRegistryException ex) {
            log.debug("Couldn't modify autoproxy result cache; this often fails on Vista, but it's merely a nice-to-have", ex);
        }
        
        // Disable caching of html
        try {
            WindowsUtils.writeStringRegistryValue(RegKey.MIME_EXCLUSION_LIST_FOR_CACHE.key, "multipart/mixed multipart/x-mixed-replace multipart/x-byteranges text/html");
        } catch (WindowsRegistryException ex) {
            log.debug("Couldn't disable caching of html; this often fails on Vista, but it's merely a nice-to-have", ex);
        }
        
        WindowsUtils.writeBooleanRegistryValue(RegKey.USERNAME_PASSWORD_DISABLE.key, false);

        // Disable pop-up blocking
        if (WindowsUtils.doesRegistryValueExist(RegKey.POPUP_MGR.key)) {
		    WindowsUtils.deleteRegistryValue(RegKey.POPUP_MGR.key);
		}
		if (RegKey.POPUP_MGR.type.equals(String.class)) {
		    WindowsUtils.writeStringRegistryValue(RegKey.POPUP_MGR.key, "no");
		} else {
		    WindowsUtils.writeBooleanRegistryValue(RegKey.POPUP_MGR.key, false);
		}

        WindowsUtils.writeBooleanRegistryValue(RegKey.WARN_ON_FORM_SUBMIT.key, false);

        // DGF Don't manage proxy settings the IE4 way; use hudsuckr instead
//        if (WindowsUtils.doesRegistryValueExist(RegKey.PROXY_OVERRIDE.key)) {
//            WindowsUtils.deleteRegistryValue(RegKey.PROXY_OVERRIDE.key);
//        }

        if (changeMaxConnections) {
            // need at least 1 xmlHttp connection per frame/window
            WindowsUtils.writeIntRegistryValue(RegKey.MAX_CONNECTIONS_PER_1_0_SVR.key, 256);
            WindowsUtils.writeIntRegistryValue(RegKey.MAX_CONNECTIONS_PER_1_1_SVR.key, 256);
        }
        
        // Hide pre-existing user cookies if -ensureCleanSession is set
        if (ensureCleanSession) {
          hidePreexistingCookies();
          deleteTemporaryInternetFiles();
        }

        // TODO Do we want to make these preferences configurable somehow?
        // TODO Disable security warnings
        // TODO Disable "do you want to remember this password?"
    }

    private static void deleteTemporaryInternetFiles() {
        String cachePath = WindowsUtils.readStringRegistryValue(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\\Cache");
        File globalCacheDir = new File(cachePath);
        File iexploreCacheDir = new File(globalCacheDir, "Content.IE5");
        if (iexploreCacheDir.exists()) {
            try {
                LauncherUtils.recursivelyDeleteDir(iexploreCacheDir);
            } catch (BuildException e) {
            } // Errors are expected here; the index.dat file is undeletable
        }
    }

    public void backupRegistrySettings() {
        // Don't clobber our old backup if we 
        // never got the chance to restore for some reason 
        if (backupIsReady()) return;
        log.info("Backing up registry settings...");
        for (RegKey key : RegKey.values()) {
            key.backup();
        }
        backupHudsuckrSettings();
        backupReady(true);
    }

    public void restoreRegistrySettings(boolean ensureCleanSession) {
        
        // restore pre-existing user cookies if -ensureCleanSession is set
        if (ensureCleanSession) {
          restorePreexistingCookies();
        }
      
        // Backup really should be ready, but if not, skip it 
        if (!backupIsReady()) return;
        log.info("Restoring registry settings (won't affect running browsers)...");
        for (RegKey key : RegKey.values()) {
            key.restore();
        }
        restoreHudsuckrSettings();
        backupReady(false);
    }
    
    /**
     * Hides pre-existing cookies, if any.  If no cookies can be found
     * then just exit.
     */
    private static void hidePreexistingCookies() {
      boolean done = false;
      File cookieDir = getCookieDir();
      done = hideCookies(cookieDir, COOKIE_SUFFIX, HIDDEN_COOKIE_DIR);
      if (!done) {
        log.warn("Could not hide pre-existing cookies using either the" +
      	  "WinXP directory structure or the Vista directory structure");
      }
    }
    
    /**
     * Hides all previously existing user cookies, found in the
     * WinXP directory structure, by moving them to a different directory.
     */
    protected static boolean hideCookies(File cookieDir, 
        String cookieSuffix, File hiddenCookieDir) {
      boolean result = false;
      LauncherUtils.recursivelyDeleteDir(hiddenCookieDir);
      if (cookieDir.exists()) {
        log.info("Copying cookies from " + cookieDir.getAbsolutePath() +
            " to " + hiddenCookieDir.getAbsolutePath());
        LauncherUtils.copyDirectory(cookieDir, cookieSuffix, hiddenCookieDir);
        log.info("Deleting original cookies...");
        deleteFlatDirContents(cookieDir, cookieSuffix);
        result = true;
      }
      return result;
    }
    
    private static File getCookieDir() {
        String cookiePath = WindowsUtils.readStringRegistryValue(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\\Cookies");
        File cookieDir = new File(cookiePath);
        return cookieDir;
    }
    
    private static void restorePreexistingCookies() {
      boolean done = false;
      File cookieDir = getCookieDir();
      done = restoreCookies(cookieDir, COOKIE_SUFFIX, HIDDEN_COOKIE_DIR);
      if (!done) {
        log.warn("Could not restore pre-existing cookies, using either the" +
          "WinXp directory structure or the Vista directory structure");
      }
    }
    
    /**
     * Restores previously hidden user cookies, if any.
     */
    protected static boolean restoreCookies(File cookieDir, 
        String cookieSuffix, File hiddenCookieDir) {
      boolean result = false;
      if (cookieDir.exists()) {
        log.info("Deleting cookies created during session from " 
            + cookieDir.getAbsolutePath());
        deleteFlatDirContents(cookieDir, cookieSuffix);
      }
      if (hiddenCookieDir.exists()) {
        log.info("Copying cookies from " + hiddenCookieDir.getAbsolutePath() +
            " to " + cookieDir.getAbsolutePath());
        LauncherUtils.copyDirectory(hiddenCookieDir, cookieDir);
        LauncherUtils.recursivelyDeleteDir(hiddenCookieDir);
        result = true;
      }
      return result;
    }
    
    /**
     * Deletes all files contained by the given directory.
     * 
     * @param dir the directory to delete the contents of
     * @param suffix if not null, only files with this suffix will be deleted.
     */
    protected static void deleteFlatDirContents(File dir, String suffix) {
      if (dir.exists()) {
        log.info("looking for files ending with: " + suffix);
        File[] list = dir.listFiles(new SuffixFilter(suffix));
        if (null != list) {
          for (File file : list) {
            boolean success = file.delete();
            if (!success) {
              log.warn("Could not delete file " + file.getAbsolutePath());
            }
          }
        } else {
          log.info("...no matching files");
        }
      }
    }
   
    private boolean backupIsReady() {
        if (!prefNodeExists(REG_KEY_BACKUP_READY)) return false;
        return prefs.getBoolean(REG_KEY_BACKUP_READY, false);
    }

    private void backupReady(boolean backupReady) {
        prefs.putBoolean(REG_KEY_BACKUP_READY, backupReady);
    }
    
    private enum RegKey {
    	POPUP_MGR(REG_KEY_BASE + "\\Software\\Microsoft\\Internet Explorer\\New Windows\\PopupMgr", null), // In IE7 it's a DWORD; in IE6 a string "yes"/"no"
    	USERNAME_PASSWORD_DISABLE(REG_KEY_BASE + "\\Software\\Microsoft\\Internet Explorer\\Main\\FeatureControl\\FEATURE_HTTP_USERNAME_PASSWORD_DISABLE\\iexplore.exe", boolean.class),
    	MAX_CONNECTIONS_PER_1_0_SVR(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\MaxConnectionsPer1_0Server", int.class),
    	MAX_CONNECTIONS_PER_1_1_SVR(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\MaxConnectionsPerServer", int.class),
    	AUTOPROXY_RESULT_CACHE(REG_KEY_BASE + "\\Software\\Policies\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\EnableAutoproxyResultCache", boolean.class),
    	MIME_EXCLUSION_LIST_FOR_CACHE(REG_KEY_BASE + "\\Software\\Policies\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\MimeExclusionListForCache", String.class),
    	WARN_ON_FORM_SUBMIT(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Zones\\3\\1601", boolean.class),
    	//DGF Don't manage proxy settings the IE4 way; use hudsuckr instead
    	//AUTOCONFIG_URL(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\AutoConfigURL", String.class),
        //PROXY_ENABLE(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\ProxyEnable", boolean.class),
        //PROXY_OVERRIDE(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\ProxyOverride", String.class),
        //PROXY_SERVER(REG_KEY_BASE + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\ProxyServer", String.class),

    	;
    	
    	RegKey(String key, Class<?> type) {
			this.key = key;
			this.type = type;
		}
    	String key;
    	Class<?> type;
    	
    	private boolean backupExists() {
            return prefNodeExists(name());
        }

        private boolean originalExists() {
            return WindowsUtils.doesRegistryValueExist(key);
        }

        private void backup() {
            if (originalExists()) {
                if (type.equals(String.class)) {
                    String data = WindowsUtils.readStringRegistryValue(key);
                    prefs.put(name(), data);
                    return;
                } else if (type.equals(boolean.class)) {
                    boolean data = WindowsUtils.readBooleanRegistryValue(key);
                    prefs.putBoolean(name(), data);
                    return;
                } else if (type.equals(int.class)) {
                    int data = WindowsUtils.readIntRegistryValue(key);
                    prefs.putInt(name(), data);
                    return;
                }
                throw new RuntimeException("Bad type: " + type.getName());
            }
            prefs.remove(name());
        }

        private void restore() {
            if (backupExists()) {
                if (type.equals(String.class)) {
                    String data = prefs.get(name(), null);
                    WindowsUtils.writeStringRegistryValue(key, data);
                    return;
                } else if (type.equals(boolean.class)) {
                    boolean data = prefs.getBoolean(name(), false);
                    WindowsUtils.writeBooleanRegistryValue(key, data);
                    return;
                } else if (type.equals(int.class)) {
                    int data = prefs.getInt(name(), 0);
                    WindowsUtils.writeIntRegistryValue(key, data);
                    return;
                }
                throw new RuntimeException("Bad type: " + type.getName());
            }
            if (WindowsUtils.doesRegistryValueExist(key)) {
                WindowsUtils.deleteRegistryValue(key);
            }
        }

    }
    
    private File extractHudsuckr() {
    	File hudsuckr = new File(customProxyPACDir, "hudsuckr.exe");
    	if (hudsuckr.exists()) return hudsuckr;
        try {
			ResourceExtractor.extractResourcePath(WindowsProxyManager.class, "/hudsuckr/hudsuckr.exe", hudsuckr);
		} catch (IOException e) {
			throw new RuntimeException("Bug extracting hudsuckr", e);
		}
        return hudsuckr;
    }
    
    private String runHudsuckr(String... args) {
    	Project p = new Project();
        p.addBuildListener(new AntJettyLoggerBuildListener(log));
        ExecTask exec = new ExecTask();
        exec.setProject(p);
        exec.setTaskType("hudsuckr");
        exec.setExecutable(extractHudsuckr().getAbsolutePath());
        exec.setFailonerror(false);
        exec.setResultProperty("result");
        exec.setOutputproperty("output");
        for (Object arg : args) {
            exec.createArg().setValue(String.valueOf(arg));
        }
        exec.execute();
        String output = p.getProperty("output");
        String result = p.getProperty("result");
        if (!"0".equals(result)) {
            throw new RuntimeException("exec return code " + result + ": " + output);
        }
        return output;
    }
    
    private HudsuckrSettings parseHudsuckrSettings(String hudsuckrOutput) {
    	Map<String, String> settings = LauncherUtils.parseDictionary(hudsuckrOutput, HUDSUCKR_LINE);
    	String connection, server, bypass, pacUrl;
    	boolean direct, proxy, pac, wpad;
    	for (HudsuckrKey key : HudsuckrKey.values()) {
    		if (!settings.containsKey(key.name())) {
    			throw new RuntimeException("Bug! Hudsuckr settings didn't include " + key + ": " + hudsuckrOutput);
    		}
    	}
    	connection = settings.get(HudsuckrKey.ACTIVE_CONNECTION.name());
    	direct = "true".equals(settings.get(HudsuckrKey.PROXY_TYPE_DIRECT.name()));
    	proxy = "true".equals(settings.get(HudsuckrKey.PROXY_TYPE_PROXY.name()));
    	pac = "true".equals(settings.get(HudsuckrKey.PROXY_TYPE_AUTO_PROXY_URL.name()));
    	wpad = "true".equals(settings.get(HudsuckrKey.PROXY_TYPE_AUTO_DETECT.name()));
    	server = settings.get(HudsuckrKey.INTERNET_PER_CONN_PROXY_SERVER.name());
    	bypass = settings.get(HudsuckrKey.INTERNET_PER_CONN_PROXY_BYPASS.name());
    	pacUrl = settings.get(HudsuckrKey.INTERNET_PER_CONN_AUTOCONFIG_URL.name());
    	return new HudsuckrSettings(connection, direct, proxy, pac, wpad, server, bypass, pacUrl);
    }
    
    private void backupHudsuckrSettings() {
    	String output = runHudsuckr();
    	HudsuckrSettings settings = parseHudsuckrSettings(output);
    	oldSettings = settings;
    	prefs.put(HudsuckrKey.ACTIVE_CONNECTION.name(), settings.connection);
    	prefs.putBoolean(HudsuckrKey.PROXY_TYPE_DIRECT.name(), settings.direct);
    	prefs.putBoolean(HudsuckrKey.PROXY_TYPE_PROXY.name(), settings.proxy);
    	prefs.putBoolean(HudsuckrKey.PROXY_TYPE_AUTO_PROXY_URL.name(), settings.pac);
    	prefs.putBoolean(HudsuckrKey.PROXY_TYPE_AUTO_DETECT.name(), settings.wpad);
    	prefs.put(HudsuckrKey.INTERNET_PER_CONN_PROXY_SERVER.name(), settings.server);
    	prefs.put(HudsuckrKey.INTERNET_PER_CONN_PROXY_BYPASS.name(), settings.bypass);
    	prefs.put(HudsuckrKey.INTERNET_PER_CONN_AUTOCONFIG_URL.name(), settings.pacUrl);
    }
    
    
    private void restoreHudsuckrSettings() {
    	String connection, server, bypass, pacUrl;
    	boolean direct, proxy, pac, wpad;
    	for (HudsuckrKey key : HudsuckrKey.values()) {
    		if (!prefNodeExists(key.name())) {
    			throw new RuntimeException("Bug!  Prefs don't contain " + key);
    		}
    	}
    	connection = prefs.get(HudsuckrKey.ACTIVE_CONNECTION.name(), null);
    	direct = prefs.getBoolean(HudsuckrKey.PROXY_TYPE_DIRECT.name(), false);
    	proxy = prefs.getBoolean(HudsuckrKey.PROXY_TYPE_PROXY.name(), false);
    	pac = prefs.getBoolean(HudsuckrKey.PROXY_TYPE_AUTO_PROXY_URL.name(), false);
    	wpad = prefs.getBoolean(HudsuckrKey.PROXY_TYPE_AUTO_DETECT.name(), false);
    	server = prefs.get(HudsuckrKey.INTERNET_PER_CONN_PROXY_SERVER.name(), null);
    	bypass = prefs.get(HudsuckrKey.INTERNET_PER_CONN_PROXY_BYPASS.name(), null);
    	pacUrl = prefs.get(HudsuckrKey.INTERNET_PER_CONN_AUTOCONFIG_URL.name(), null);
    	HudsuckrSettings settings = new HudsuckrSettings(connection, direct, proxy, pac, wpad, server, bypass, pacUrl);
    	runHudsuckr(settings.toStringArray());
    }
    
    private enum HudsuckrKey {
    	ACTIVE_CONNECTION,
    	PROXY_TYPE_DIRECT,
    	PROXY_TYPE_PROXY,
    	PROXY_TYPE_AUTO_PROXY_URL,
    	PROXY_TYPE_AUTO_DETECT,
    	INTERNET_PER_CONN_PROXY_SERVER,
    	INTERNET_PER_CONN_PROXY_BYPASS,
    	INTERNET_PER_CONN_AUTOCONFIG_URL;
    }
    
    private static class HudsuckrSettings {
    	final String connection, server, bypass, pacUrl;
    	final boolean direct, proxy, pac, wpad;
    	public HudsuckrSettings(String connection, boolean direct,
				boolean proxy, boolean pac, boolean wpad, String server,
				String bypass, String pacUrl) {
			this.connection = connection;
			this.server = server;
			this.bypass = bypass;
			this.pacUrl = pacUrl;
			this.direct = direct;
			this.proxy = proxy;
			this.pac = pac;
			this.wpad = wpad;
		}
    	
    	public String[] toStringArray() {
    		String[] result = new String[8];
    		result[0] = connection;
    		result[1] = Boolean.toString(direct);
    		result[2] = Boolean.toString(proxy);
    		result[3] = Boolean.toString(pac);
    		result[4] = Boolean.toString(wpad);
    		result[5] = server;
    		result[6] = bypass;
    		result[7] = pacUrl;
    		return result;
    	}
    	
    	@Override
    	public String toString() {
    		return Arrays.toString(toStringArray());
    	}
    }
    
    private static class SuffixFilter implements FileFilter {
      
      private final String suffix;
      
      public SuffixFilter(String suffix) {
        this.suffix = suffix;
      }
      
      public boolean accept(File pathname) {
        boolean result = false;
        if (null == suffix) {
          result = true;
        } else if (pathname.getName().endsWith(suffix)) {
          result = true;
        }
        return result;
      }
    }

}
