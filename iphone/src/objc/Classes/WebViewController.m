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
#import "WebViewController.h"
#import "HTTPServerController.h"
#import "NSException+WebDriver.h"
#import "NSURLRequest+IgnoreSSL.h"
#import "UIResponder+SimulateTouch.h"
#import "WebDriverPreferences.h"
#import "WebDriverRequestFetcher.h"
#import "WebDriverUtilities.h"
#import <objc/runtime.h>
#import "RootViewController.h"
#import <QuartzCore/QuartzCore.h>
#import <QuartzCore/CATransaction.h>

@implementation WebViewController

@dynamic webView;

- (void)assertCalledFromUIThread {
  NSAssert([NSThread isMainThread],@"Should be called from main thread!");
}

// Executed after the nib loads the interface.
// Configure the webview to match the mobile safari app.
- (void)viewDidLoad {
  [super viewDidLoad];
  if (webViewControllerCommon_) {
    [webViewControllerCommon_ release];
    webViewControllerCommon_ = 0;
  }
  webViewControllerCommon_ = [[WebViewControllerCommon alloc] initWithWebView:[self webView]];
  webViewControllerCommon_.delegate = self;
  [[self webView] setScalesPageToFit:NO];
  [[self webView] setDelegate:self];

  // Creating a new session if auto-create is enabled
  if ([[RootViewController sharedInstance] isAutoCreateSession]) {
    [[HTTPServerController sharedInstance]
      httpResponseForQuery:@"/hub/session"
                    method:@"POST"
                  withData:[@"{\"browserName\":\"firefox\",\"platform\":\"ANY\","
                            "\"javascriptEnabled\":false,\"version\":\"\"}"
                            dataUsingEncoding:NSASCIIStringEncoding]];
  }

  WebDriverPreferences *preferences = [WebDriverPreferences sharedInstance];

  cachePolicy_ = [preferences cache_policy];
  NSURLCache *sharedCache = [NSURLCache sharedURLCache];
  [sharedCache setDiskCapacity:[preferences diskCacheCapacity]];
  [sharedCache setMemoryCapacity:[preferences memoryCacheCapacity]];

  if ([[preferences mode] isEqualToString: @"Server"]) {
    HTTPServerController* serverController = [HTTPServerController sharedInstance];
    [serverController setViewController:self];
    [self describeLastAction:[serverController status]];		
  } else {
    WebDriverRequestFetcher* fetcher = [WebDriverRequestFetcher sharedInstance]; 
    [fetcher setViewController:self];
    [self describeLastAction:[fetcher status]];		
  }
}

- (void)viewDidUnload {
  [[self webView] setDelegate:nil];
  if (webViewControllerCommon_) {
    [webViewControllerCommon_ release];
    webViewControllerCommon_ = 0;
  }
  [super viewDidUnload];
}

- (void)didReceiveMemoryWarning {
  NSLog(@"Memory warning recieved.");
  // TODO(josephg): How can we send this warning to the user? Maybe set the
  // displayed text; though that could be overwritten basically straight away.
  [super didReceiveMemoryWarning];
}

- (void)dealloc {
  if (webViewControllerCommon_) {
    [webViewControllerCommon_ release];
    webViewControllerCommon_ = 0;
  }
  [[self webView] setDelegate:nil];
  [super dealloc];
}

- (UIWebView *)webView {
  if (![[self view] isKindOfClass:[UIWebView class]]) {
    NSLog(@"NIB error: WebViewController's view is not a UIWebView.");
    return nil;
  }
  return (UIWebView *)[self view];
}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
  NSLog(@"shouldStartLoadWithRequest");
  return YES;
}

- (void)webViewDidStartLoad:(UIWebView *)webView {
  NSLog(@"webViewDidStartLoad");
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
  [webViewControllerCommon_ webViewDidFinishLoad:webView];
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error {
  [webViewControllerCommon_ webView:webView didFailLoadWithError:error];
}

- (void)loadRequest:(NSURLRequest*)url {
  [[self webView] performSelectorOnMainThread:@selector(loadRequest:) 
    withObject:url 
    waitUntilDone:YES];
}

- (void)screenshotSafe:(NSMutableArray*)result
{
  [self assertCalledFromUIThread];
  UIGraphicsBeginImageContext([[self webView] bounds].size);
  [[self webView].layer renderInContext:UIGraphicsGetCurrentContext()];
  UIImage *viewImage = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
  
  // dump the screenshot into a file for debugging
  //NSString *path = [[[NSSearchPathForDirectoriesInDomains
  //   (NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0]
  //  stringByAppendingPathComponent:@"screenshot.png"] retain];
  //[UIImagePNGRepresentation(viewImage) writeToFile:path atomically:YES];
  
  [result replaceObjectAtIndex:0 withObject:viewImage];
}

// Takes a screenshot.
- (UIImage *)screenshot {
  NSMutableArray* result = [NSMutableArray arrayWithObject:[NSNull null]];
  [self performSelectorOnMainThread:@selector(screenshotSafe:) withObject:result waitUntilDone:YES];
  return [result objectAtIndex:0];
}

- (void)describeLastAction:(NSString *)status {
  [statusLabel_ performSelectorOnMainThread:@selector(setText:) withObject:status waitUntilDone:NO];
}

// Translate pixels in webpage-space to pixels in view space.
- (CGPoint)translatePageCoordinateToView:(CGPoint)point {
  [self assertCalledFromUIThread];
  CGRect viewBounds = [[self webView] bounds];
  CGRect pageBounds = [[self webViewDriver] viewableArea];
  
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
  [self assertCalledFromUIThread];
  CGPoint pointInViewSpace = [self translatePageCoordinateToView:point];
  
  NSLog(@"simulating a click at %@", NSStringFromCGPoint(pointInViewSpace));
  [[self webView] simulateTapAt:pointInViewSpace];
}

- (id<WebViewDriver>)webViewDriver {
    return webViewControllerCommon_;
}

@end
