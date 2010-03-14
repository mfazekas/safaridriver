//
//  WebViewDriverSingleton.m
//  SafariDriver
//
//  Created by Miklós Fazekas on 3/6/10.
//  Copyright 2010 Apple Inc. All rights reserved.
//

#import "WebViewDriverSingleton.h"
#import "HTTPServerController.h"
#import "WebDriverPreferences.h"
#import "WebDriverRequestFetcher.h"
#import "WebViewController.h"

@implementation WebViewDriverSingleton

+ (id<WebViewDriver>)instance
{
  NSString* mode = [[WebDriverPreferences sharedInstance] mode];
  if ([mode isEqualToString:@"Server"]) {
    return [[[HTTPServerController sharedInstance] viewController] webViewDriver];
  } else {
    return [[[WebDriverRequestFetcher sharedInstance] viewController] webViewDriver];
  }
}

@end
