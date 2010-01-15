//
//  WebViewController.h
//  SafariDriver
//
//  Created by Andrian Kurniady on 10/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <WebKit/WebKit.h>
#import "HTTPServerController.h"
#import "WebDriverPreferences.h"

@interface WebViewController : NSObject {
  WebView *webView_;
  
  NSString *lastJSResult_;

  NSURLRequestCachePolicy cachePolicy_;
  
  HTTPServerController *httpServerController;
}

@property (assign, nonatomic) WebView *webView;

- (void)setURL:(NSString *)urlString;
- (NSString *)URL;
- (NSString *)currentTitle;
- (void)back;
- (void)forward;
- (NSString *)source;
- (NSString *)jsEval:(NSString *)format, ...;

+ (id)sharedInstance;

@end
