/*
 Copyright 2007-2009 WebDriver committers
 Copyright 2007-2009 Google Inc.
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
 * @fileOverview Contains a Javascript implementation for
 *     nsICommandProcessor.idl. The implemented XPCOM component is exposed to
 *     the content page as a global property so that it can be used from
 *     unpriviledged code.
 */


/**
 * When this component is loaded, load the necessary subscripts.
 */
(function() {
  var scripts = [
    'errorcode.js',
    'utils.js'
  ];

  // Firefox 3.5+ has native JSON support; prefer that over our script from
  // www.json.org, which may be slower.
  var appInfo = Components.classes['@mozilla.org/xre/app-info;1'].
      getService(Components.interfaces.nsIXULAppInfo);
  var versionChecker = Components.classes['@mozilla.org/xpcom/version-comparator;1'].
      getService(Components.interfaces.nsIVersionComparator);
  if (versionChecker.compare(appInfo.version, '3.5') < 0) {
    scripts.push('json2.js');
  }

  var fileProtocolHandler = Components.
      classes['@mozilla.org/network/protocol;1?name=file'].
      createInstance(Components.interfaces.nsIFileProtocolHandler);
  var loader = Components.classes['@mozilla.org/moz/jssubscript-loader;1'].
      createInstance(Components.interfaces.mozIJSSubScriptLoader);

  for (var script in scripts) {
    var file = __LOCATION__.parent.clone();
    file.append(scripts[script]);

    var fileName = fileProtocolHandler.getURLSpecFromFile(file);
    loader.loadSubScript(fileName);
  }
})();


/**
 * Encapsulates the result of a command to the {@code nsCommandProcessor}.
 * @param {Object} command JSON object describing the command to execute.
 * @param {nsIResponseHandler} responseHandler The handler to send the response
 *     to.
 * @constructor
 */
var Response = function(command, responseHandler) {
  this.statusBarLabel_ = null;
  this.responseHandler_ = responseHandler;
  this.json_ = {
    name: command ? command.name : 'Unknown command',
    sessionId: command['sessionId'],
    status: ErrorCode.SUCCESS,
    value: ''
  };
  if (this.json_['sessionId'] && this.json_['sessionId']['value']) {
    this.json_['sessionId'] = this.json_['sessionId']['value'];
  }
  this.session = null;
};

Response.prototype = {

  /**
   * Updates the extension status label to indicate we are about to execute a
   * command.
   * @param {window} win The content window that the command will be executed on.
   */
  startCommand: function(win) {
    this.statusBarLabel_ = win.document.getElementById("fxdriver-label");
    if (this.statusBarLabel_) {
      this.statusBarLabel_.style.color = "red";
    }
  },

  /**
   * Sends the encapsulated response to the registered callback.
   */
  send: function() {
    if (this.responseSent_) {
      // We shouldn't ever send the same response twice.
      return;
    }
    // Indicate that we are no longer executing a command.
    if (this.statusBarLabel_) {
      this.statusBarLabel_.style.color = 'black';
    }

    this.responseHandler_.handleResponse(JSON.stringify(this.json_));

    // Neuter ourselves
    this.responseSent_ = true;
  },

  /**
   * Sends a WebDriver error response.
   * @param {WebDriverError} e The error to send.
   */
  sendError: function(e) {
    // if (e instanceof WebDriverError) won't work here since
    // WebDriverError is defined in the utils.js subscript which is
    // loaded independently in this component and in the main driver
    // component.
    this.status = e.isWebDriverError ? e.code : ErrorCode.UNHANDLED_ERROR;
    this.value = ErrorCode.toJSON(e);
    this.send();
  },

  set name(name) { this.json_.name = name; },
  get name()     { return this.json_.name; },
  set status(newStatus) { this.json_.status = newStatus; },
  get status()          { return this.json_.status; },
  set value(val)     { this.json_.value = val; },
  get value()        { return this.json_.value; },
};


/**
 * Handles executing a command from the {@code CommandProcessor} once the window
 * has fully loaded.
 * @param {FirefoxDriver} driver The FirefoxDriver instance to execute the
 *     command with.
 * @param {Object} command JSON object describing the command to execute.
 * @param {Response} response The response object to send the command response
 *     in.
 * @param {Number} opt_sleepDelay The amount of time to wait before attempting
 *     the command again if the window is not ready.
 * @constructor
 */
var DelayedCommand = function(driver, command, response, opt_sleepDelay) {
  this.driver_ = driver;
  this.command_ = command;
  this.response_ = response;
  this.onBlank_ = false;
  this.sleepDelay_ = opt_sleepDelay || DelayedCommand.DEFAULT_SLEEP_DELAY;

  var activeWindow = response.session.getWindow();
  try {
    var webNav = activeWindow.
        QueryInterface(Components.interfaces.nsIInterfaceRequestor).
        getInterface(Components.interfaces.nsIWebNavigation);
    this.loadGroup_ = webNav.
        QueryInterface(Components.interfaces.nsIInterfaceRequestor).
        getInterface(Components.interfaces.nsILoadGroup);
  } catch (ex) {
    // Well this sucks. This can happen if the DOM gets trashed or if the window
    // is unexpectedly closed. We need to report this error to the user so they
    // can let us (webdriver-eng) know that the FirefoxDriver is busted.
    response.sendError(ex);
    // Re-throw the error so the command will be aborted.
    throw ex;
  }
};


/**
 * Default amount of time, in milliseconds, to wait before (re)attempting a
 * {@code DelayedCommand}.
 * @type {Number}
 */
DelayedCommand.DEFAULT_SLEEP_DELAY = 100;


/**
 * Executes the command after the specified delay.
 * @param {Number} ms The delay in milliseconds.
 */
DelayedCommand.prototype.execute = function(ms) {
  var self = this;
  this.driver_.window.setTimeout(function() {
    self.executeInternal_();
  }, ms);
};


/**
 * @return {boolean} Whether this instance should delay execution of its
 *     command for a pending request in the current window's nsILoadGroup.
 */
DelayedCommand.prototype.shouldDelayExecutionForPendingRequest_ = function() {
  try {
    if (this.loadGroup_.isPending()) {
      var hasOnLoadBlocker = false;
      var numPending = 0;
      var requests = this.loadGroup_.requests;
      while (requests.hasMoreElements()) {
        var request =
            requests.getNext().QueryInterface(Components.interfaces.nsIRequest);
        if (request.isPending()) {
          numPending += 1;
          hasOnLoadBlocker = hasOnLoadBlocker ||
                             (request.name == 'about:document-onload-blocker');

          if (numPending > 1) {
            // More than one pending request, need to wait.
            return true;
          }
        }
      }

      if (numPending && !hasOnLoadBlocker) {
        Utils.dumpn('Ignoring pending about:document-onload-blocker request');
        // If we only have one pending request and it is not a
        // document-onload-blocker, we need to wait.  We do not wait for
        // document-onload-blocker requests since these are created when
        // one of document.[open|write|writeln] is called. If document.close is
        // never called, the document-onload-blocker request will not be
        // completed.
        return true;
      }
    }
  } catch(e) {
    Utils.dumpn('Problem while checking if we should delay execution: ' + e);
    return true;
  }

  return false;
};


/**
 * Attempts to execute the command.  If the window is not ready for the command
 * to execute, will set a timeout to try again.
 * @private
 */
DelayedCommand.prototype.executeInternal_ = function() {
  if (this.shouldDelayExecutionForPendingRequest_()) {
    return this.execute(this.sleepDelay_);
  }

  // Ugh! New windows open on "about:blank" before going to their
  // destination URL. This check attempts to tell the difference between a
  // newly opened window and someone actually wanting to do something on
  // about:blank.
  if (this.driver_.window.location == 'about:blank' && !this.onBlank_) {
    this.onBlank_ = true;
    return this.execute(this.sleepDelay_);
  } else {
    try {
      this.response_.name = this.command_.name;
      // TODO(simon): This is rampantly ugly, but allows an alert to kill the command
      // TODO(simon): This is never cleared, but _should_ be okay, because send wipes itself
      this.driver_.response_ = this.response_;

      this.driver_[this.command_.name](
          this.response_, this.command_.parameters);
    } catch (e) {
      if (!e.isWebDriverError) {
        Utils.dumpn(
            'Exception caught by driver: ' + this.command_.name +
            '(' + this.command_.parameters + ')\n' + e);
      }
      this.response_.sendError(e);
    }
  }
};


/**
 * Class for dispatching WebDriver requests.  Handles window locating commands
 * (e.g. switching, searching, etc.), all other commands are executed with the
 * {@code FirefoxDriver} through reflection.  Note this is a singleton class.
 * @constructor
 */
var nsCommandProcessor = function() {
  this.wrappedJSObject = this;
  this.wm = Components.classes['@mozilla.org/appshell/window-mediator;1'].
      getService(Components.interfaces.nsIWindowMediator);
};

/**
 * Flags for the {@code nsIClassInfo} interface.
 * @type {Number}
 */
nsCommandProcessor.prototype.flags =
    Components.interfaces.nsIClassInfo.DOM_OBJECT;

/**
 * Implementaiton language detail for the {@code nsIClassInfo} interface.
 * @type {String}
 */
nsCommandProcessor.prototype.implementationLanguage =
    Components.interfaces.nsIProgrammingLanguage.JAVASCRIPT;


/**
 * Logs a message to the Console Service and then throws an error.
 * @param {String} message The message to log.
 * @throws {Components.results.NS_ERROR_FAILURE}
 */
nsCommandProcessor.logError = function(message) {
  // TODO(jleyba): This should log an error and not a generic message.
  Utils.dumpn(message);
  throw Components.results.NS_ERROR_FAILURE;
};


/**
 * Processes a command request for the {@code FirefoxDriver}.
 * @param {string} jsonCommandString The command to execute, specified in a
 *     JSON string.
 * @param {nsIResponseHandler} responseHandler The callback to send the response
 *     to.
 */
nsCommandProcessor.prototype.execute = function(jsonCommandString,
                                                responseHandler) {
  var command, response;
  try {
    command = JSON.parse(jsonCommandString);
  } catch (ex) {
    response = JSON.stringify({
      'status': ErrorCode.UNHANDLED_ERROR,
      'value': 'Error parsing command: "' + jsonCommandString + '"'
    });
    responseHandler.handleResponse(response);
    return;
  }

  response = new Response(command, responseHandler);

  // These commands do not require a session.
  if (command.name == 'newSession' ||
      command.name == 'quit' ||
      command.name == 'getWindowHandles') {
    try {
      this[command.name](response, command.parameters);
    } catch (ex) {
      response.sendError(ex);
    }
    return;
  }

  var sessionId = command.sessionId;
  if (!sessionId) {
    response.sendError(new WebDriverError(ErrorCode.UNHANDLED_ERROR,
        'No session ID specified'));
    return;
  }

  sessionId = sessionId.value;
  try {
    response.session = Components.
      classes['@googlecode.com/webdriver/wdsessionstoreservice;1'].
      getService(Components.interfaces.nsISupports).
      wrappedJSObject.
      getSession(sessionId).
      wrappedJSObject;
  } catch (ex) {
    response.sendError(new WebDriverError(ErrorCode.UNHANDLED_ERROR,
        'Session not found: ' + sessionId));
    return;
  }

  if (command.name == 'deleteSession' ||
      command.name == 'getSessionCapabilities' ||
      command.name == 'switchToWindow') {
    return this[command.name](response, command.parameters);
  }

  var sessionWindow = response.session.getChromeWindow();
  var driver = sessionWindow.fxdriver;  // TODO(jmleyba): We only need to store an ID on the window!
  if (!driver) {
    response.sendError(new WebDriverError(ErrorCode.UNHANDLED_ERROR,
        'Session has no driver: ' + response.session.getId()));
    return;
  }

  if (typeof driver[command.name] != 'function') {
    response.sendError(new WebDriverError(ErrorCode.UNKNOWN_COMMAND,
        'Unrecognised command: ' + command.name));
    return;
  }

  response.startCommand(sessionWindow);
  new DelayedCommand(driver, command, response).execute(0);
};


/**
 * Changes the context of the caller to the specified window.
 * @param {Response} response The response object to send the command response
 *     in.
 * @param {{name: string}} parameters The command parameters.
 * @param {number} opt_searchAttempt Which attempt this is at finding the
 *     window to switch to.
 */
nsCommandProcessor.prototype.switchToWindow = function(response, parameters,
                                                       opt_searchAttempt) {
  var lookFor = parameters.name;
  var matches = function(win, lookFor) {
    return !win.closed &&
           (win.content && win.content.name == lookFor) ||
           (win.top && win.top.fxdriver && win.top.fxdriver.id == lookFor);
  };

  var windowFound = this.searchWindows_('navigator:browser', function(win) {
    if (matches(win, lookFor)) {
      // Create a switch indicator file so the native events library
      // will know a window switch is in progress and will indeed
      // switch focus.
      createSwitchFile("switch:" + win.fxdriver.id);

      win.focus();
      if (win.top.fxdriver) {
        response.session.setChromeWindow(win.top);
        response.value = response.session.getId();
        response.send();
      } else {
        response.sendError(new WebDriverError(ErrorCode.UNHANDLED_ERROR,
            'No driver found attached to top window!'));
      }
      // Found the desired window, stop the search.
      return true;
    }
  });

  // It is possible that the window won't be found on the first attempt. This is
  // typically true for anchors with a target attribute set. This search could
  // execute before the target window has finished loaded, meaning the content
  // window won't have a name or FirefoxDriver instance yet (see matches above).
  // If we don't find the window, set a timeout and try again.
  if (!windowFound) {
    // TODO(jmleyba): We should be sniffing the current windows to detect if
    // one is still loading vs. a brute force "try again"
    var searchAttempt = opt_searchAttempt || 0;
    if (searchAttempt > 3) {
      response.sendError(new WebDriverError(ErrorCode.NO_SUCH_WINDOW,
          'Unable to locate window "' + lookFor + '"'));
    } else {
      var self = this;
      this.wm.getMostRecentWindow('navigator:browser').
          setTimeout(function() {
            self.switchToWindow(response, parameters, (searchAttempt + 1));
          }, 500);
    }
  }
};


/**
 * Retrieves a list of all known FirefoxDriver windows.
 * @param {Response} response The response object to send the command response
 *     in.
 */
nsCommandProcessor.prototype.getWindowHandles = function(response) {
  var res = [];
  this.searchWindows_('navigator:browser', function(win) {
    if (win.top && win.top.fxdriver) {
      res.push(win.top.fxdriver.id);
    } else if (win.content) {
      res.push(win.content.name);
    }
  });
  response.value = res;
  response.send();
};


/**
 * Searches over a selection of windows, calling a visitor function on each
 * window found in the search.
 * @param {?string} search_criteria The category of windows to search or
 *     {@code null} to search all windows.
 * @param {function} visitor_fn A visitor function to call with each window. The
 *     function may return true to indicate that the window search should abort
 *     early.
 * @return {boolean} Whether the visitor function short circuited the search.
 */
nsCommandProcessor.prototype.searchWindows_ = function(search_criteria,
                                                       visitor_fn) {
  var allWindows = this.wm.getEnumerator(search_criteria);
  while (allWindows.hasMoreElements()) {
    var win = allWindows.getNext();
    if (visitor_fn(win)) {
      return true;
    }
  }
  return false;
};


/**
 * Locates the most recently used FirefoxDriver window.
 * @param {Response} response The object to send the command response in.
 */
nsCommandProcessor.prototype.newSession = function(response) {
  var win = this.wm.getMostRecentWindow("navigator:browser");
  var driver = win.fxdriver;
  if (!driver) {
    response.sendError(new WebDriverError(ErrorCode.UNHANDLED_ERROR,
        'No drivers associated with the window'));
  } else {
    var sessionStore = Components.
        classes['@googlecode.com/webdriver/wdsessionstoreservice;1'].
        getService(Components.interfaces.nsISupports);

    var session = sessionStore.wrappedJSObject.createSession();
    session = session.wrappedJSObject;  // XPConnect...
    session.setChromeWindow(win);

    response.session = session;
    response.value = session.getId();
  }
  response.send();
};


/**
 * Describes a session.
 * @param {Response} response The object to send the command response in.
 */
nsCommandProcessor.prototype.getSessionCapabilities = function(response) {
  var appInfo = Components.classes['@mozilla.org/xre/app-info;1'].
      getService(Components.interfaces.nsIXULAppInfo);
  var xulRuntime = Components.classes['@mozilla.org/xre/app-info;1'].
      getService(Components.interfaces.nsIXULRuntime);
  response.value = {
    'browserName': 'firefox',
    'version': appInfo.version,
    'javascriptEnabled': true,
    'platform': xulRuntime.OS          // same as Platform.valueOf("name");
  };
  response.send();
};


/**
 * Deletes the session associated with the current request.
 * @param {Response} response The object to send the command response in.
 */
nsCommandProcessor.prototype.deleteSession = function(response) {
  var sessionStore = Components.
      classes['@googlecode.com/webdriver/wdsessionstoreservice;1'].
      getService(Components.interfaces.nsISupports);
  sessionStore.wrappedJSObject.deleteSession(response.session.getId());
  response.send();
};


/**
 * Forcefully shuts down the Firefox application.
 * @param {Response} response The object to send the command response in.
 */
nsCommandProcessor.prototype.quit = function(response) {
  // Go ahead and respond to the command request to acknowledge that we are
  // shutting down. We do this because once we force a quit, there's no way
  // to respond.  Clients will just have to trust that this shutdown didn't
  // fail.  Or they could monitor the PID. Either way, not much we can do about
  // it in here.
  response.send();

  // Use an nsITimer to give the response time to go out.
  var event = {
    notify: function(timer) {
      // Create a switch file so the native events library will
      // let all events through in case of a close.
      createSwitchFile("close:<ALL>");
      Components.classes['@mozilla.org/toolkit/app-startup;1'].
          getService(Components.interfaces.nsIAppStartup).
          quit(Components.interfaces.nsIAppStartup.eForceQuit);
    }
  };

  var timer = Components.classes['@mozilla.org/timer;1'].
      createInstance(Components.interfaces.nsITimer);
  timer.initWithCallback(event, 500,  // milliseconds
      Components.interfaces.nsITimer.TYPE_ONE_SHOT);
};


nsCommandProcessor.prototype.getInterfaces = function(count) {
  var ifaces = [
    Components.interfaces.nsICommandProcessor,
    Components.interfaces.nsISupports
  ];
  count.value = ifaces.length;
  return ifaces;
};


nsCommandProcessor.prototype.QueryInterface = function (aIID) {
  if (!aIID.equals(Components.interfaces.nsICommandProcessor) &&
      !aIID.equals(Components.interfaces.nsISupports)) {
    throw Components.results.NS_ERROR_NO_INTERFACE;
  }
  return this;
};


nsCommandProcessor.CLASS_ID =
    Components.ID('{692e5117-a4a2-4b00-99f7-0685285b4db5}');
nsCommandProcessor.CLASS_NAME = 'Firefox WebDriver CommandProcessor';
nsCommandProcessor.CONTRACT_ID =
    '@googlecode.com/webdriver/command-processor;1';


/**
 * Factory object for obtaining a reference to the singleton instance of
 * {@code CommandProcessor}.
 */
nsCommandProcessor.Factory = {
  instance_ : null,

  createInstance: function(aOuter, aIID) {
    if (aOuter != null) {
      throw Components.results.NS_ERROR_NO_AGGREGATION;
    }
    if (!this.instance_) {
      this.instance_ = new nsCommandProcessor();
    }
    return this.instance_.QueryInterface(aIID);
  }
};


/**
 * Module definition for registering this XPCOM component.
 */
nsCommandProcessor.Module = {
  firstTime_: true,

  registerSelf: function(aCompMgr, aFileSpec, aLocation, aType) {
    if (this.firstTime_) {
      this.firstTime_ = false;
      throw Components.results.NS_ERROR_FACTORY_REGISTER_AGAIN;
    }
    aCompMgr.QueryInterface(Components.interfaces.nsIComponentRegistrar).
        registerFactoryLocation(
            nsCommandProcessor.CLASS_ID,
            nsCommandProcessor.CLASS_NAME,
            nsCommandProcessor.CONTRACT_ID,
            aFileSpec, aLocation, aType);
  },

  unregisterSelf: function(aCompMgr, aLocation) {
    aCompMgr.QueryInterface(Components.interfaces.nsIComponentRegistrar).
        unregisterFactoryLocation(nsCommandProcessor.CLASS_ID, aLocation);
  },

  getClassObject: function(aCompMgr, aCID, aIID) {
    if (!aIID.equals(Components.interfaces.nsIFactory)) {
      throw Components.results.NS_ERROR_NOT_IMPLEMENTED;
    } else if (!aCID.equals(nsCommandProcessor.CLASS_ID)) {
      throw Components.results.NS_ERROR_NO_INTERFACE;
    }
    return nsCommandProcessor.Factory;
  },

  canUnload: function() {
    return true;
  }
};


/**
 * Module initialization.
 */
function NSGetModule() {
  return nsCommandProcessor.Module;
}
