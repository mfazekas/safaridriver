//
//  WebViewController.m
//  SafariDriver
//
//  Created by Andrian Kurniady on 10/13/09.
//  Adapted from WebViewController.m from iPhone Driver by josephg.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "WebViewController.h"


@implementation WebViewController
@synthesize webView = webView_;

+ (id)sharedInstance {
  static id instance = nil;
  if (instance == nil) {
    instance = [[WebViewController alloc] init];
  }
  return instance;
}
- (id)init {
  if (self = [super init]) {
    WebDriverPreferences* preferences = [WebDriverPreferences sharedInstance];
    cachePolicy_ = [preferences cache_policy];
    
    // Kicks off the HTTP Server
    NSLog(@"Starting HTTP Server...");
    [HTTPServerController sharedInstance];
    NSLog(@"HTTP Server started...");
  }
  return self;
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
  
  while ([webView_ isLoading]) {
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
- (void)performSelectorOnWebView:(SEL)selector
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
   
  while ([webView_ isLoading])
    [NSThread sleepForTimeInterval:0.01f];
   
  [webView_ performSelectorOnMainThread:selector
                                   withObject:value
                                waitUntilDone:YES];
   
  NSLog(@"loading %d", [[self webView] isLoading]);
   
  if (wait) {
    [self waitForLoad];
  }
}

// Get the specified URL and block until it's finished loading.
- (void)setURL:(NSString *)urlString {
  
  [self performSelectorOnWebView:@selector(setMainFrameURL:)
                      withObject:urlString
                   waitUntilLoad:YES];
}

- (void)back {
  [self performSelectorOnWebView:@selector(goBack)
                      withObject:nil
                   waitUntilLoad:YES];
}

- (void)forward {
  [self performSelectorOnWebView:@selector(goForward)
                      withObject:nil
                   waitUntilLoad:YES];
}

- (void)reload {
  [self performSelectorOnWebView:@selector(reload:)
                      withObject:self
                   waitUntilLoad:YES];
}

- (NSString *)URL {
  return [webView_ mainFrameURL];
}

- (NSString*)currentTitle {
  return [webView_ mainFrameTitle];
}

- (NSString *)source {
  return [self jsEval:@"document.documentElement.innerHTML"];
}

// Execute js in the main thread and set lastJSResult_ appropriately.
// This function must be executed on the main thread. Its designed to be called
// using performSelectorOnMainThread:... which doesn't return a value - so
// the return value is passed back through a class parameter.
- (void)jsEvalInternal:(NSString *)script {
  [lastJSResult_ release];
  lastJSResult_ = [[[self webView]
                    stringByEvaluatingJavaScriptFromString:script] retain];
  
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

- (BOOL)jsElementIsNullOrUndefined:(NSString *)expression {
  NSString *isNull = [self jsEval:@"%@ === null || %@ === undefined",
                      expression, expression];
  return [isNull isEqualToString:@"true"];
}

@end

/* The old code from iPhone driver, just for reference
  - (void)didReceiveMemoryWarning {
 NSLog(@"Memory warning recieved.");
 // TODO(josephg): How can we send this warning to the user? Maybe set the
 // displayed text; though that could be overwritten basically straight away.
 [super didReceiveMemoryWarning];
 }
 
 - (void)dealloc {
 [[self webView] setDelegate:nil];
 [loadLock_ release];
 [lastJSResult_ release];
 [super dealloc];
 }
 
 - (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
 NSLog(@"shouldStartLoadWithRequest");
 return YES;
 }
 
 - (void)webViewDidStartLoad:(UIWebView *)webView {
 NSLog(@"webViewDidStartLoad");
 }
 
 - (void)webViewDidFinishLoad:(UIWebView *)webView {
 NSLog(@"finished loading");
 [loadLock_ signal];
 }
 
 - (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error {
 // This is a very troubled method. It can be called multiple times (for each
 // frame of webpage). It is sometimes called even when the page seems to have
 // loaded correctly.
 
 // Page loading errors are ignored because that's what WebDriver expects.
 NSLog(@"*** WebView failed to load URL with error %@", error);
 [loadLock_ signal];
 }
 
 #pragma mark Web view controls
 
 - (void)performSelectorOnWebView:(SEL)selector withObject:(id)obj {
 [[self webView] performSelector:selector withObject:obj];
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
 
 while ([[self webView] isLoading]) {
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
 

while ([[self webView] isLoading])
[NSThread sleepForTimeInterval:0.01f];

[[self webView] performSelectorOnMainThread:selector
                                 withObject:value
                              waitUntilDone:YES];

NSLog(@"loading %d", [[self webView] isLoading]);

if (wait)
[self waitForLoad];
}

// Get the specified URL and block until it's finished loading.
- (void)setURL:(NSString *)urlString {
  NSURLRequest *url = [NSURLRequest requestWithURL:[NSURL URLWithString:urlString]
                                       cachePolicy:cachePolicy_
                                   timeoutInterval:60];
  
  [self performSelectorOnView:@selector(loadRequest:)
                   withObject:url
                waitUntilLoad:YES];
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

- (id)visible {
  // The WebView is always visible.
  return [NSNumber numberWithBool:YES];  
}


// Ignored.
- (void)setVisible:(NSNumber *)target {
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



- (NSString *)currentTitle {
  return [self jsEval:@"document.title"];
}

- (NSString *)source {
  return [self jsEval:@"document.documentElement.innerHTML"];
}

// Takes a screenshot.
- (UIImage *)screenshot {
  UIGraphicsBeginImageContext([[self webView] bounds].size);
  [[self webView].layer renderInContext:UIGraphicsGetCurrentContext()];
  UIImage *viewImage = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
  
  // dump the screenshot into a file for debugging
  //NSString *path = [[[NSSearchPathForDirectoriesInDomains
  //   (NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0]
  //  stringByAppendingPathComponent:@"screenshot.png"] retain];
  //[UIImagePNGRepresentation(viewImage) writeToFile:path atomically:YES];
  
  return viewImage;
}

- (NSString *)URL {
  return [self jsEval:@"window.location.href"];
}

- (void)describeLastAction:(NSString *)status {
  [statusLabel_ setText:status];
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

// Translate pixels in webpage-space to pixels in view space.
- (CGPoint)translatePageCoordinateToView:(CGPoint)point {
  CGRect viewBounds = [[self webView] bounds];
  CGRect pageBounds = [self viewableArea];
  
  // ... And then its just a linear transformation.
  float scale = viewBounds.size.width / pageBounds.size.width;
  CGPoint transformedPoint;
  transformedPoint.x = (point.x - pageBounds.origin.x) * scale;
  transformedPoint.y = (point.y - pageBounds.origin.y) * scale;
  
  NSLog(@"%@ -> %@",
        NSStringFromCGPoint(point),
        NSStringFromCGPoint(transformedPoint));
  
  return transformedPoint;
}

- (void)clickOnPageElementAt:(CGPoint)point {
  if (![self pointIsViewable:point]) {
    [self scrollIntoView:point];
  }
  
  CGPoint pointInViewSpace = [self translatePageCoordinateToView:point];
  
  NSLog(@"simulating a click at %@", NSStringFromCGPoint(pointInViewSpace));
  [[self webView] simulateTapAt:pointInViewSpace];
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

*/
