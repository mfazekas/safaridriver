package com.thoughtworks.selenium;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.BeanToJsonConverter;
import org.openqa.selenium.remote.JsonToBeanConverter;
import org.openqa.selenium.remote.ProxyPac;

/**
 * Contains parameters for a single Selenium browser session.
 * 
 * BrowserConfigurationOptions is used as an argument to {@code Selenium.start()}.
 * The parameters set within will override any command-line parameters set for the same option.
 * 
 * @author jbevan, chandrap
 *
 */
public class BrowserConfigurationOptions {
	public static final String PROXY_CONFIG = "proxy";
	public static final String PROFILE_NAME = "profile";
	public static final String SINGLE_WINDOW = "singleWindow";
	public static final String MULTI_WINDOW = "multiWindow";
	public static final String BROWSER_EXECUTABLE_PATH = "executablePath";
	public static final String TIMEOUT_IN_SECONDS = "timeoutInSeconds";
	public static final String BROWSER_MODE = "mode";
        public static final String COMMAND_LINE_FLAGS = "commandLineFlags";
	
	public static final int DEFAULT_TIMEOUT_IN_SECONDS = 30 * 60; // identical to RemoteControlConfiguration;

    private Map<String,String> options = new HashMap<String,String>();
    
    /**
     * Instantiate a blank BrowserConfigurationOptions instance.
     */
    public BrowserConfigurationOptions() {}
    
    /**
     * Returns true if any options are set in this instance.
     * 
     * @return true if any options are set in this instance.
     */
    public boolean hasOptions() {
      return options.size() > 0;
    }
    
    /**
     * Serializes to the format "name=value;name=value".
     * 
     * @return String with the above format.
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : options.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append(key).append('=').append(options.get(key));
        }
        return sb.toString();
    }
    
    /**
     * Sets the name of the profile, which must exist in the -profilesLocation directory,
     * to use for this browser session.
     * 
     * @param profile the name of the profile.
     * @return this BrowserConfigurationOptions object.
     */
    public BrowserConfigurationOptions setProfile(String profile) {
    	put(PROFILE_NAME, profile);
    	return this;
    }

    protected String getProfile() {
        return options.get(PROFILE_NAME);
    }

    
    /**
     * Returns true if the {@code SINGLE_WINDOW} field is set.
     * 
     * @return true if {@code SINGLE_WINDOW} is set.
     */
    protected boolean isSingleWindow() {
        if (isSet(SINGLE_WINDOW)) {
        	return true;
        }
        return false;
    }
    
    /**
     * Returns true if the {@code MULTI_WINDOW} field is set.
     * 
     * @return true if {@code MULTI_WINDOW} is set.
     */
    protected boolean isMultiWindow() {
        if (isSet(MULTI_WINDOW)) {
        	return true;
        }
        return false;
    }
    
    /**
     * Sets {@code SINGLE_WINDOW} and unsets {@code MULTI_WINDOW}.
     */
    public BrowserConfigurationOptions setSingleWindow() {
    	synchronized (options) {
	        options.put(SINGLE_WINDOW, "true");  // "true" string used for serialization
	        options.remove(MULTI_WINDOW);
    	}
    	return this;
    }
    
    /**
     * Sets {@code MULTI_WINDOW} and unsets {@code SINGLE_WINDOW}
     */
    public BrowserConfigurationOptions setMultiWindow() {
    	synchronized (options) {
    		options.put(MULTI_WINDOW, "true");  // "true" string used for serialization
    		options.remove(SINGLE_WINDOW);
    	}
    	return this;
    }
    
    protected String getBrowserExecutablePath() {
        return options.get(BROWSER_EXECUTABLE_PATH);
    }
    
    
    /**
     * Sets the full path for the browser executable.
     * 
     * @param executablePath the full path for the browser executable.
     */
    public BrowserConfigurationOptions setBrowserExecutablePath(String executablePath) {
        put(BROWSER_EXECUTABLE_PATH, executablePath);
        return this;
    }
    
    /**
     * Sets the timeout, in seconds, for all commands.
     * 
     * @param timeout the timeout for all commands
     * @return this BrowserConfigurationOptions instance.
     */
    public BrowserConfigurationOptions setTimeoutInSeconds(int timeout) {
    	put(TIMEOUT_IN_SECONDS, String.valueOf(timeout));
    	return this;
    }
    
    protected int getTimeoutInSeconds() {
        String value = options.get(TIMEOUT_IN_SECONDS);
        if (value == null) return DEFAULT_TIMEOUT_IN_SECONDS;
        return Integer.parseInt(value);
    }
    
    /**
     * Sets the "mode" for the browser.
     * 
     * Historically, the 'browser' argument for getNewBrowserSession implied the mode for the browser.  For
     * example, *iehta indicated HTA mode for IE, whereas *iexplore indicated the default user mode.  Using
     * this method allows a browser mode to be specified independently of the base browser, eg. "HTA" or
     * "PROXY".
     * 
     * Note that absolutely no publication nor synchronization of these hard-coded strings such as "HTA" has
     * yet been done.  Use at your own risk until this is rectified.
     * 
     * @param mode
     */
    public BrowserConfigurationOptions setBrowserMode(String mode) {
    	put(BROWSER_MODE, mode);
    	return this;
    }
    
    protected String getBrowserMode() {
    	return options.get(BROWSER_MODE);
    }

    public BrowserConfigurationOptions setCommandLineFlags(String cmdLineFlags) {
      put(COMMAND_LINE_FLAGS, cmdLineFlags);
      return this;
    }
    
    public String getCommandLineFlags() {
      return get(COMMAND_LINE_FLAGS);
    }

    public BrowserConfigurationOptions setProxyConfig(ProxyPac pac) {
      put(PROXY_CONFIG, new BeanToJsonConverter().convert(pac));
      return this;
    }

    public ProxyPac getProxyConfig() {
      String raw = get(PROXY_CONFIG);
      if (raw == null) {
        return null;
      }

      try {
        return new JsonToBeanConverter().convert(ProxyPac.class, raw);
      } catch (Exception e) {
        throw new SeleniumException("Unable to retrieve proxy configuration", e);
      }
    }

    protected boolean canUse(String value) {
    	return (value != null && !"".equals(value));
    }
    
    private void put(String key, String value) {
    	if (canUse(value)) {
    		options.put(key, value);
    	}
    }
    
    public boolean isSet(String key) {
    	boolean result = false;
    	synchronized (options) {
    		result = (null != options.get(key));
    	}
        return result;
    }
    
    public String get(String key) {
        return options.get(key);
    }
    
    
    /**
     * Sets the given key to the given value unless the value is null.
     * In that case, no entry for the key is made.
     * 
     * @param key the name of the key
     * @param value the value for the key
     */
    public BrowserConfigurationOptions set(String key, String value) {
        if (value != null) {
            options.put(key, value);
        }
        return this;
    }
    
    /**
     * Returns the serialization of this object, as defined by the serialize()
     * method.
     */
    @Override public String toString() {
        return serialize();
    }
}
