//
//  WebViewDriver.h
//  iWebDriver
//
//  Created by Miklós Fazekas on 1/16/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#if TARGET_OS_IPHONE
#include <UIKit/UIKit.h>
typedef UIImage ImageType;
#else
#include <WebKit/WebKit.h>
typedef NSImage ImageType;
#endif

@protocol WebViewDriver

- (CGRect)viewableArea;
- (BOOL)pointIsViewable:(CGPoint)point;

// Some webdriver stuff.
- (id)visible;
- (void)setVisible:(NSNumber *)target;

// Get the current page title
- (NSString *)currentTitle;

// Get the URL of the page we're looking at
- (NSString *)URL;

// Navigate to a URL
- (void)setURL:(NSDictionary *)urlMap;

- (void)forward;
- (void)back;
- (void)refresh;

// Evaluate a javascript string and return the result.
// Arguments can be passed in in NSFormatter (printf) style.
//
// Variables declared with var are kept between script calls. However, they are
// lost when the page reloads. Check before using any variables which were
// defined during previous events.
- (NSString *)jsEval:(NSString *)format, ...;

// Evaluate a javascript string and return the result. Block if the evaluation
// results in a page reload.
// Arguments can be passed in in NSFormatter (printf) style.
- (NSString *)jsEvalAndBlock:(NSString *)format, ...;

// Test if a JS expression evaluates to true
- (BOOL)testJsExpression:(NSString *)format, ...;

// Get a float property of a javascript object
- (float)floatProperty:(NSString *)property ofObject:(NSString *)jsObject;

// Test if a JS object is equal to null
- (BOOL)jsElementIsNullOrUndefined:(NSString *)expression;

// Get the HTML source of the page we've loaded
- (NSString *)source;

// Get a screenshot of the page we've loaded
- (ImageType *)screenshot;

- (void)clickOnPageElementAt:(CGPoint)point;

- (void)addFirebug;

// Calls the same on the main view controller.
- (void)describeLastAction:(NSString *)status;

@end
