/*
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

/**
 * Logs a message to the console service.
 * @param {string} message The message to log.
 */
function log(message) {
  Components.classes['@mozilla.org/consoleservice;1'].
    getService(Components.interfaces.nsIConsoleService).
    logStringMessage(message);
}


/**
 * An active FirefoxDriver session.  
 * @constructor
 */
function wdSession() {
  /**
   * A wrapped self-reference for XPConnect.
   * @type {wdSession}
   */
  this.wrappedJSObject = this;
}


/**
 * This component's ID.
 * @type {nsIJSID}
 */
wdSession.CLASS_ID = Components.ID('{e193dc71-5b1d-4fea-b4c2-ec71f4557f0f}');


/**
 * This component's class name.
 * @type {string}
 */
wdSession.CLASS_NAME = 'wdSession';


/**
 * This component's contract ID.
 * @type {string}
 */
wdSession.CONTRACT_ID = '@googlecode.com/webdriver/wdsession;1';


/**
 * This session's ID.
 * @type {?string}
 * @private
 */
wdSession.prototype.id_ = null;


/**
 * The main chrome window that this is session is currently focused on. All
 * command's for this session will be directed at the current window, which
 * may be inside a [I]FRAME, within this window.
 * @type {?ChromeWindow}
 * @private
 */
wdSession.prototype.chromeWindow_ = null;


/**
 * The content window this session is currently focused on.
 * @type {?nsIDOMWindow}
 * @private
 */
wdSession.prototype.window_ = null;


/**
 * The current user input speed setting for this session.
 * @type {number}
 * @private
 */
wdSession.prototype.inputSpeed_ = 1;


/** @see nsISupports.QueryInterface */
wdSession.prototype.QueryInterface = function(aIID) {
  if (aIID.equals(Components.interfaces.nsISupports)) {
    return this;
  }
  throw Components.results.NS_ERROR_NO_INTERFACE;
};


/** @return {?string} This session's ID. */
wdSession.prototype.getId = function() {
  return this.id_;
};


/**
 * Sets this session's ID.
 * @param {string} id The session ID.
 */
wdSession.prototype.setId = function(id) {
  this.id_ = id;
};


/**
 * @return {browser|tabbrowser} The browser object for this session's current
 *     window.
 */
wdSession.prototype.getBrowser = function() {
  return this.chromeWindow_.getBrowser();
};


/** @return {?ChromeWindow} The chrome window for this session. */
wdSession.prototype.getChromeWindow = function() {
  return this.chromeWindow_;
};


/** @return {?nsIDOMWindow} This session's current window. */
wdSession.prototype.getWindow = function() {
  if (!this.window_.document) {
    // Uh-oh, we lost our DOM! Try to recover by changing focus to the
    // main content window.
    this.setWindow(this.chromeWindow_.getBrowser().contentWindow);
  }
  return this.window_;
};


/** @return {nsIDOMDocument} This session's current document. */
wdSession.prototype.getDocument = function() {
  return this.getWindow().document;
  return this.window_.document;
};


/**
 * Set the chrome window for this session; will also set the current window to
 * the main content window inside the chrome window.
 * @param {ChromeWindow} win The new chrome window.
 */
wdSession.prototype.setChromeWindow = function(win) {
  this.chromeWindow_ = win;
  this.setWindow(win.getBrowser().contentWindow);
};


/**
 * Set this session's current window. If the selected window is a frameset,
 * the current window will be adjusted to focus on the first frame.
 * @param {nsIDOMWindow} win The new window.
 */
wdSession.prototype.setWindow = function(win) {
  this.window_ = win;
  var frames = this.window_.frames;
  if (frames && frames.length && 'FRAME' == frames[0].frameElement.tagName) {
    this.window_ = frames[0];
  }
};


/**
 * @return {number} The user input speed for this session.
 */
wdSession.prototype.getInputSpeed = function() {
  return this.inputSpeed_;
};


/**
 * Sets the user input speed for this session.
 * @param {number} speed The new input speed.
 */
wdSession.prototype.setInputSpeed = function(speed) {
  this.inputSpeed_ = speed;
};


///////////////////////////////////////////////////////////////////
//
// nsIFactory functions
//
///////////////////////////////////////////////////////////////////

/** @constructor */
function wdSessionFactory() {
}


/** @see nsIFactory.createInstance */
wdSessionFactory.prototype.createInstance = function(aOuter, aIID) {
  if (aOuter != null) {
    throw Components.results.NS_ERROR_NO_AGGREGATION;
  }
  return new wdSession().QueryInterface(aIID);
};

///////////////////////////////////////////////////////////////////
//
// nsIModule functions
//
///////////////////////////////////////////////////////////////////

/** @constructor */
function wdSessionModule() {
}


/**
 * Whether this module has already been registered.
 * @type {!boolean}
 * @private
 */
wdSessionModule.prototype.hasRegistered_ = false;


/** @see nsIModule.registerSelf */
wdSessionModule.prototype.registerSelf = function(aCompMgr, aFileSpec, aLocation, aType) {
  if (this.hasRegistered_) {
    throw Components.results.NS_ERROR_FACTORY_REGISTER_AGAIN;
  }
  aCompMgr.QueryInterface(Components.interfaces.nsIComponentRegistrar).
      registerFactoryLocation(
          wdSession.CLASS_ID,
          wdSession.CLASS_NAME,
          wdSession.CONTRACT_ID,
          aFileSpec, aLocation, aType);
  this.hasRegistered_ = true;
};


/** @see nsIModule.unregisterSelf */ 
wdSessionModule.prototype.unregisterSelf = function(aCompMgr, aLocation) {
  aCompMgr.QueryInterface(Components.interfaces.nsIComponentRegistrar).
      unregisterFactoryLocation(wdSession.CLASS_ID, aLocation);
};


/** @see nsIModule.getClassObject */
wdSessionModule.prototype.getClassObject = function(aCompMgr, aCID, aIID) {
  if (!aIID.equals(Components.interfaces.nsIFactory)) {
    throw Components.results.NS_ERROR_NOT_IMPLEMENTED;
  } else if (!aCID.equals(wdSession.CLASS_ID)) {
    throw Components.results.NS_ERROR_NO_INTERFACE;
  }
  return new wdSessionFactory();
};


/** @see nsIModule.canUnload */
wdSessionModule.prototype.canUnload = function() {
  return true;
};



/**
 * Module initialization.
 */
function NSGetModule() {
  return new wdSessionModule();
}

