//
//  WebViewController.m
//  iWebDriver
//
//  Copyright 2009 Google Inc.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
#import "WebViewControllerCommon.h"
#import "NSException+WebDriver.h"
#import "NSURLRequest+IgnoreSSL.h"
#import "WebDriverPreferences.h"
#import "WebDriverRequestFetcher.h"
#import "WebDriverUtilities.h"
#import <objc/runtime.h>
#import <QuartzCore/QuartzCore.h>
#import <QuartzCore/CATransaction.h>

@implementation WebViewControllerCommon

@dynamic webView;
@synthesize delegate=delegate_;

- (void)assertCalledFromUIThread {
    NSAssert([NSThread isMainThread],@"Should be called from main thread!");
}

- (id)initWithWebView:(WebViewType*)webView {
    self = [super init];
    if (self != nil) {
        webView_ = webView;
        [webView_ retain];
        loadLock_ = [[NSCondition alloc] init];
        lastJSResult_ = nil;
    }
    return self;
}

- (void)dealloc {
  [loadLock_ release];
  [lastJSResult_ release];
  [webView_ release];
  webView_ = 0;
  [super dealloc];
}

- (WebViewType *)webView {
    return webView_;
}

- (void)webViewDidFinishLoad:(WebViewType *)webView {
  NSLog(@"finished loading");
  [loadLock_ signal];
}

- (void)webView:(WebViewType *)webView didFailLoadWithError:(NSError *)error {
  // This is a very troubled method. It can be called multiple times (for each
  // frame of webpage). It is sometimes called even when the page seems to have
  // loaded correctly.
  
  // Page loading errors are ignored because that's what WebDriver expects.
  NSLog(@"*** WebView failed to load URL with error %@", error);
  [loadLock_ signal];
}

#pragma mark Web view controls

- (void)performSelectorOnWebView:(SEL)selector withObject:(id)obj {
  [self assertCalledFromUIThread];
  [[self webView] performSelector:selector withObject:obj];
}

- (BOOL)_isWebViewLoading {
  [self assertCalledFromUIThread];
  return [[self webView] isLoading];
}

- (void)isWebViewLoadingOnMT:(NSMutableArray*)result {
    [result replaceObjectAtIndex:0 withObject:[NSNumber numberWithBool:[self _isWebViewLoading]]];
}
- (BOOL)isWebViewLoading {
  NSMutableArray* isLoading = [NSMutableArray arrayWithObject:[NSNumber numberWithBool:NO]];
  [self performSelectorOnMainThread:@selector(isWebViewLoadingOnMT:) withObject:isLoading waitUntilDone:YES];
  return [[isLoading objectAtIndex:0] boolValue];
}

- (void)waitForLoad {
  // TODO(josephg): Test sleep intervals on the device.
  // This delay should be long enough that the webview has isLoading
  // set correctly (but as short as possible - these delays slow down testing.)
  
  // - The problem with [view isLoading] is that it gets set in a separate
  // worker thread. So, right after asking the webpage to load a URL we need to
  // wait an unspecified amount of time before isLoading will correctly tell us
  // whether the page is loading content.
  
  [NSThread sleepForTimeInterval:0.2f];
  while ([self isWebViewLoading]) {
    // Yield.
    [NSThread sleepForTimeInterval:0.01f];
  }  
}

// All method calls on the view need to be done from the main thread to avoid
// synchronization errors. This method calls a given selector in this class
// optionally with an argument.
//
// If called with waitUntilLoad:YES, we wait for a web page to be loaded in the
// view before returning.
- (void)performSelectorOnView:(SEL)selector
                   withObject:(id)value
                waitUntilLoad:(BOOL)wait {

  /* The problem with this method is that the UIWebView never gives us any clear
   * indication of whether or not it's loading and if so, when its done. Asking
   * it to load causes it to begin loading sometime later (isLoading returns NO
   * for awhile.) Even the |webViewDidFinishLoad:| method isn't a sure sign of
   * anything - it will be called multiple times, once for each frame of the
   * loaded page.
   * 
   * The result: The only effective method I can think of is nasty polling.
   */
  while ([self isWebViewLoading])
    [NSThread sleepForTimeInterval:0.01f];
  
  [[self webView] performSelectorOnMainThread:selector
                                   withObject:value
                                waitUntilDone:YES];

  NSLog(@"loading %d", [self isWebViewLoading]);
  
  if (wait)
    [self waitForLoad];
}

// Get the specified URL and block until it's finished loading.
- (void)setURL:(NSDictionary *)urlMap {
  NSString *urlString = (NSString*) [urlMap objectForKey:@"url"];
  NSURLRequest *url = [NSURLRequest requestWithURL:[NSURL URLWithString:urlString]
                                       cachePolicy:cachePolicy_
                                   timeoutInterval:60];
  
  [delegate_ loadRequest:url];
  [self waitForLoad];
}

- (void)back {
  [self describeLastAction:@"back"];
  [self performSelectorOnView:@selector(goBack)
                   withObject:nil
                waitUntilLoad:YES];
}

- (void)forward {
  [self describeLastAction:@"forward"];
  [self performSelectorOnView:@selector(goForward)
                   withObject:nil
                waitUntilLoad:YES];
}

- (void)refresh {
  [self describeLastAction:@"refresh"];
  [self performSelectorOnView:@selector(reload)
                   withObject:nil
                waitUntilLoad:YES];
}

- (id)visible {
  // The WebView is always visible.
  return [NSNumber numberWithBool:YES];  
}

// Ignored.
- (void)setVisible:(NSNumber *)target {
}

// Execute js in the main thread and set lastJSResult_ appropriately.
// This function must be executed on the main thread. Its designed to be called
// using performSelectorOnMainThread:... which doesn't return a value - so
// the return value is passed back through a class parameter.
- (void)jsEvalInternal:(NSString *)script {
  [self assertCalledFromUIThread];
  // We wrap the eval command in a CATransaction so that we can explicitly
  // force any UI updates that might occur as a side effect of executing the
  // javascript to finish rendering before we return control back to the HTTP
  // server thread. We actually found some cases where the rendering was
  // finishing before control returned and so the core animation framework would
  // defer committing its implicit transaction until the next iteration of the
  // HTTP server thread's run loop. However, because you're only allowed to
  // update the UI on the main application thread, committing it on the HTTP
  // server thread would cause the whole application to crash.
  // This feels like it shouldn't be necessary but it was the only way we could
  // find to avoid the problem.
  [CATransaction begin];
  [lastJSResult_ release];
  lastJSResult_ = [[[self webView]
                   stringByEvaluatingJavaScriptFromString:script] retain];
  [CATransaction commit];

  NSLog(@"jsEval: %@ -> %@", script, lastJSResult_);
}

// Evaluate the given JS format string & arguments. Argument list is the same
// as [NSString stringWithFormat:...].
- (NSString *)jsEval:(NSString *)format, ... {
  if (format == nil) {
    [NSException raise:@"invalidArguments" format:@"Invalid arguments for jsEval"];
  }
  
  va_list argList;
  va_start(argList, format);
  NSString *script = [[[NSString alloc] initWithFormat:format
                                             arguments:argList]
                      autorelease];
  va_end(argList);

  [self performSelectorOnMainThread:@selector(jsEvalInternal:)
                         withObject:script
                      waitUntilDone:YES];
  
  return [[lastJSResult_ copy] autorelease];
}

- (NSString *)jsEvalAndBlock:(NSString *)format, ... {
  if (format == nil) {
    [NSException raise:@"invalidArguments" format:@"Invalid arguments for jsEval"];
  }
  
  va_list argList;
  va_start(argList, format);
  NSString *script = [[[NSString alloc] initWithFormat:format
                                             arguments:argList]
                      autorelease];
  va_end(argList);
  
  NSString *result = [self jsEval:@"%@", script];
  
  [self waitForLoad];
  
  return result;
}

- (BOOL)testJsExpression:(NSString *)format, ... {
  if (format == nil) {
    [NSException raise:@"invalidArguments" format:@"Invalid arguments for jsEval"];
  }
  
  va_list argList;
  va_start(argList, format);
  NSString *script = [[[NSString alloc] initWithFormat:format
                                             arguments:argList]
                      autorelease];
  va_end(argList);
  
  return [[self jsEval:@"!!(%@)", script] isEqualToString:@"true"];
}

- (float)floatProperty:(NSString *)property ofObject:(NSString *)jsObject {
  return [[self jsEval:@"%@.%@", jsObject, property] floatValue];
}

- (BOOL)jsElementIsNullOrUndefined:(NSString *)expression {
  NSString *isNull = [self jsEval:@"%@ === null || %@ === undefined",
                                   expression, expression];
  return [isNull isEqualToString:@"true"];
}

- (NSString *)currentTitle {
  return [self jsEval:@"document.title"];
}

- (NSString *)source {
  return [self jsEval:@"(function() {\n"
                       "  var div = document.createElement('div');\n"
                       "  div.appendChild(document.documentElement.cloneNode(true));\n"
                       "  return div.innerHTML;\n"
                       "})();"];
}

// Takes a screenshot.
- (ImageType *)screenshot {
    return [delegate_ screenshot];
}

- (NSString *)URL {
  return [self jsEval:@"window.location.href"];
}

- (void)describeLastAction:(NSString *)status {
    [delegate_ describeLastAction:status];
}

- (CGRect)viewableArea {
  CGRect area;
  area.origin.x = [[self jsEval:@"window.pageXOffset"] intValue];
  area.origin.y = [[self jsEval:@"window.pageYOffset"] intValue];
  area.size.width = [[self jsEval:@"window.innerWidth"] intValue];
  area.size.height = [[self jsEval:@"window.innerHeight"] intValue];
  return area;
}

- (BOOL)pointIsViewable:(CGPoint)point {
//  NSLog(@"bounds: %@", NSStringFromCGRect([[self webView] bounds]));
  return CGRectContainsPoint([self viewableArea], point);
}

// Scroll to make the given point centered on the screen (if possible).
- (void)scrollIntoView:(CGPoint)point {
  // Webkit will clip the given point if it lies outside the window.
  // It may be necessary at some stage to do this using touches.
  [self jsEval:@"window.scroll(%f - window.innerWidth / 2, %f - window.innerHeight / 2);", point.x, point.y];
}

- (void)clickOnPageElementAt:(CGPoint)point {
  if (![self pointIsViewable:point]) {
    [self scrollIntoView:point];
  }
  
  [delegate_ clickOnPageElementAt:point];
}

// I don't know why, but this doesn't work in the current version of
// mobile safari. (2.2 firmware)
- (void)addFirebug {
  // This is the http://getfirebug.com/lite.html bookmarklet
  [self jsEval:
  @"var firebug=document.createElement('script');\r"
   "firebug.setAttribute('src','http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js');\r"
   "document.body.appendChild(firebug);\r"
   "(function() {\r"
   "  if(window.firebug.version) {\r"
   "    firebug.init();\r"
   "  } else {\r"
   "  setTimeout(arguments.callee);\r"
   "  }\r"
   "})();"];
}

@end
