//
//  WebViewController.m
//  SafariDriver
//
//  Created by Miklós Fazekas on 1/5/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "WebViewController.h"
#import "WebViewControllerCommon.h"
@class WebView;

@interface WebViewController : NSObject<WebViewControllerDelegate>
{
  WebView* webView_;
  WebViewControllerCommon* webViewControllerCommon_;
}

+ (id)sharedInstance;
+ (id)createSharedInstance:(WebView*)webView;

- (id) initWithWebView:(WebView*)webView;
- (id<WebViewDriver>) webViewDriver;

@end
