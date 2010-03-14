//
//  WebViewController.m
//  SafariDriver
//
//  Created by Miklós Fazekas on 1/5/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "WebViewController.h"
#import <WebKit/WebKit.h>

NSString* NSStringFromCGPoint(CGPoint point)
{
  return [NSString stringWithFormat:@"%f %f",point.x,point.y];
}

@implementation WebViewController

- (id) initWithWebView:(WebView*)webView
{
  self = [super init];
  if (self != nil) {
    webView_ = [webView retain];
    webViewControllerCommon_ = [[WebViewControllerCommon alloc] initWithWebView:webView];
    webViewControllerCommon_.delegate = self;
  }
  return self;
}

- (void)dealloc {
  [webView_ release];
  [webViewControllerCommon_ release];
  [super dealloc];
}

- (WebView *)webView {

  return webView_;
}

- (void)webViewDidStartLoad:(WebView *)webView {
  NSLog(@"webViewDidStartLoad");
}

- (void)webViewDidFinishLoad:(WebView *)webView {
  [webViewControllerCommon_ webViewDidFinishLoad:webView];
}

- (void)webView:(WebView *)webView didFailLoadWithError:(NSError *)error {
  [webViewControllerCommon_ webView:webView didFailLoadWithError:error];
}

- (void)loadRequest:(NSURLRequest*)url
{
  [[webView_ mainFrame] performSelectorOnMainThread:@selector(loadRequest:) 
    withObject:url 
    waitUntilDone:YES];
}

- (void)describeLastAction_:(NSString*)status {
  [[webView_ UIDelegate] webView:webView_ setStatusText:status];
  NSLog(@"describeLastAction_:%@\n",status);
}

- (void)describeLastAction:(NSString *)status {
  [self performSelectorOnMainThread:@selector(describeLastAction_:) withObject:status waitUntilDone:NO];
}

// Translate pixels in webpage-space to pixels in view space.
- (CGPoint)translatePageCoordinateToView:(CGPoint)point {
  CGRect viewBounds = NSRectToCGRect([[self webView] bounds]);
  CGRect pageBounds = [webViewControllerCommon_ viewableArea];
  
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

- (void)simulateTapAt:(CGPoint)pointInViewSpace {
  NSLog(@"simulating a click at %@", NSStringFromCGPoint(pointInViewSpace));
}

- (NSEvent*)keyEvent:(NSString*)character shift:(BOOL)shift down:(BOOL)down
{
    //NSString *character = @"f";
        unsigned int modifiers = 0;
        if (shift)
            modifiers |= NSShiftKeyMask;
 
	// make the event
	NSEvent * e = [NSEvent keyEventWithType: down ? NSKeyDown : NSKeyUp
		location: NSMakePoint(0,0) 
		modifierFlags: modifiers 
		timestamp: 0
		windowNumber: 0
		context: [NSGraphicsContext currentContext] 
		characters: shift ? [character uppercaseString] : character
		charactersIgnoringModifiers: [character lowercaseString]
		isARepeat: NO
		keyCode: 0];
	return e;
}

- (void)sendKeys:(NSDictionary*)keys
{
  NSLog(@"sendKeys:%@\n",keys);
    
  NSArray* value = [keys objectForKey:@"value"];
  for (NSString* keys in value) {
    for (int i = 0; i < [keys length]; ++i) {
      unichar chars[2] = {[keys characterAtIndex:i],0};
      NSString* str = [NSString stringWithCharacters:chars length:1];
        
      NSEvent* eventDown = [self keyEvent:str shift:NO down:YES];
      NSWindow* window = [[self webView] window];
      [window performSelectorOnMainThread:@selector(sendEvent:) withObject:eventDown waitUntilDone:NO];
      NSEvent* eventUp = [self keyEvent:str shift:NO down:NO];
      [window performSelectorOnMainThread:@selector(sendEvent:) withObject:eventUp waitUntilDone:NO];
    }
  }
}

- (void)clickOnPageElementAt:(CGPoint)point {
  CGPoint pointInViewSpace = [self translatePageCoordinateToView:point];
  
  NSLog(@"simulating a click at %@", NSStringFromCGPoint(pointInViewSpace));
  [self simulateTapAt:pointInViewSpace];
}

- (id<WebViewDriver>) webViewDriver {
  return webViewControllerCommon_;
}

- (NSImage *)screenshot {
  [webView_ lockFocus];
  NSBitmapImageRep* rep = [[[NSBitmapImageRep alloc] initWithFocusedViewRect:[webView_ bounds]] autorelease];
  [webView_ unlockFocus];
  NSImage* result = [[[NSImage alloc] initWithSize:[rep size]] autorelease];
  [result addRepresentation:rep];
  return result;
}

#pragma mark "singleton"

static WebViewController* instance = 0;

+ (id)sharedInstance {
  NSAssert(instance != 0,@"no createSharedInstance was called!");
  return instance;
}

+ (id)createSharedInstance:(WebView*)webView {
  if (!instance || [instance webView] != webView) {
    [instance release]; instance = 0;
    instance = [[WebViewController alloc] initWithWebView:webView];
  }
}

@end
