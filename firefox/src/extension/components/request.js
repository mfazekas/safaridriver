/*
 Copyright 2007-2010 WebDriver committers
 Copyright 2007-2010 Google Inc.

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
 * Encapsulates information describing an HTTP request.
 * @param {Request.Method} method The HTTP method used to make the request.
 * @param {nsIURL} requestUrl URL for the requested resource.
 * @param {Object} headers Map of request headers;.
 * @param {?string} body The request body, or null, if the request method does
 *     not permit a message body.
 * @constructor
 */
function Request(method, requestUrl, headers, body) {

  /**
   * A wrapped self-reference for XPConnect.
   * @type {Request}
   */
  this.wrappedJSObject = this;

  /**
   * The HTTP method used to make the request.
   * @type {Request.Method}
   * @private
   */
  this.method_ = method;


  /**
   * URL for the requested resource.
   * @type {?nsIURL}
   * @private
   */
  this.requestUrl_ = requestUrl;

  /**
   * The request path, minus the segments mapped to the servlet.
   * @type {?string}
   * @private
   */
  this.pathInfo_ = this.requestUrl_.path;

  /**
   * Map of request headers. All header names are specified as lowercase
   * strings.
   * @type {Object}
   * @private
   */
  this.headers_ = {};
  for (var name in headers) {
    this.headers_[name] = headers[name]
  }

  /**
   * The request body, if there was one.
   * @type {?string}
   * @private
   */
  this.body_ = body;

  /**
   * Map of custom request attributes.
   * @type {Object}
   * @private
   */
  this.attributes_ = {};
}


/**
 * The set of valid HTTP methods.
 * @enum {string}
 */
Request.Method = {
  'DELETE': 'DELETE',
  'GET': 'GET',
  'HEAD': 'HEAD',
  'OPTIONS': 'OPTIONS',
  'POST': 'POST',
  'PUT': 'PUT',
  'TRACE': 'TRACE'
};


/**
 * Path to the servlet servicing this request.
 * @type {string}
 * @private
 */
Request.prototype.servletPath_ = '/';


/** @return {Request.Method} The HTTP method used to make the request. */
Request.prototype.getMethod = function() {
  return this.method_;
};


/** @return {?nsIURL} The full request URL. */
Request.prototype.getRequestUrl = function() {
  return this.requestUrl_;
};


/**
 * Retrieves the named header value.
 * @param {string} name The name of the header to lookup.
 * @return {string} The header value, if it was included in this request.
 */
Request.prototype.getHeader = function(name) {
  return this.headers_[name.toLowerCase()];
};


/**
 * Sets the servlet path for this request; that is the path prefix that is
 * mapped to the receiving servlet. The remainder of the path, excluding query
 * and param data, is available via Request.getPathInfo().
 * @param {string} servletPath The receiving servlet's path.
 */
Request.prototype.setServletPath = function(servletPath) {
  if (servletPath[0] != '/') {
    servletPath = '/' + servletPath;
  }
  var length = servletPath.length;
  if (length > 1 && servletPath[length - 1] == '/') {
    servletPath = servletPath.substring(0, length - 1);
  }

  this.servletPath_ = servletPath;
  this.pathInfo_ = this.requestUrl_.path;
  if (this.servletPath_ != '/') {
    this.pathInfo_ = this.pathInfo_.substring(this.servletPath_.length);
  }

  if (this.pathInfo_.length > 1 &&
      this.pathInfo_.charAt(this.pathInfo_.length - 1) == '/') {
    this.pathInfo_ = this.pathInfo_.substring(0, this.pathInfo_.length - 1);
  }
};


/** @return {string} The servlet path for this request. */
Request.prototype.getServletPath = function() {
  return this.servletPath_;
};


/** @return {string} The path info for this request. */
Request.prototype.getPathInfo = function() {
  return this.pathInfo_;
};


/** @return {?string} The request body if there was one. */
Request.prototype.getBody = function() {
  return this.body_;
};


/** @return {string} The value of the named attribute if it exists. */
Request.prototype.getAttribute = function(name) {
  return this.attributes_[name];
};


/** @return {Array.<string>} A list of names for all set attributes. */
Request.prototype.getAttributeNames = function() {
  var names = [];
  for (var name in this.attributes_) {
    names.push(name);
  }
  return names;
};


/**
 * Sets an attribute on this request, replacing any previously set attribute
 * value.
 * @param {string} name The name of the attribute, or null to delete it.
 * @param {*} value The attribute value.
 */
Request.prototype.setAttribute = function(name, value) {
  if (value === null || value === undefined) {
    delete this.attributes_[name];
  } else {
    this.attributes_[name] = value;
  }
};


/**
 * @return {string} This request as a string for debugging.
 */
Request.prototype.toDebugString = function() {
  var message = this.method_ + ' ' + this.requestUrl_.path + ' HTTP/1.1\r\n';
  for (var name in this.headers_) {
    message += name + ':' + this.headers_[name] + '\r\n';
  }
  message += '\r\n';
  if (this.body_) {
    message += this.body_;
  }
  return message;
};
