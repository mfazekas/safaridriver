//
//  WebViewDriverSingleton.m
//  SafariDriver
//
//  Created by Miklós Fazekas on 3/6/10.
//  Copyright 2010 Apple Inc. All rights reserved.
//

#import "WebViewDriverSingleton.h"
#import "WebViewController.h"

@implementation WebViewDriverSingleton

+ (id<WebViewDriver>)instance
{
    return [[WebViewController sharedInstance] webViewDriver];
}

@end
