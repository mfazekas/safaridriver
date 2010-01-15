//
//  SafariExtension.m
//  SafariExtension
//
//  Created by Andrian Kurniady on 10/1/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "SafariExtension.h"
#import <WebKit/WebKit.h>

@implementation SafariExtension

- (id)init {
  if (self = [super init]) {
    loaded_ = false;
  }
  return self;
}

- (void)onSafariLoaded {
  NSLog(@"Safari loaded!");
}

- (void)onWebViewLoaded:(NSNotification*)n {
  if (!loaded_) {
    loaded_ = true;
    NSLog(@"SafariDriver init");
  }
  [[WebViewController sharedInstance] setWebView:(WebView*)[n object]];
  NSLog(@"WebView located!");
}

+ (void)load {  
  NSLog(@"Safari extension loading...");
  
  SafariExtension* extension = [SafariExtension sharedInstance];
  
  [[NSNotificationCenter defaultCenter] addObserver:extension
                                           selector:@selector(onSafariLoaded)
                                               name:NSApplicationDidFinishLaunchingNotification 
                                             object:NSApp];
  
  [[NSNotificationCenter defaultCenter] addObserver:extension 
                                           selector:@selector(onWebViewLoaded:)
                                               name:WebViewProgressFinishedNotification
                                             object:nil];
}

+ (id)sharedInstance {
  static id instance = nil;
  if (instance == nil) {
    instance = [[SafariExtension alloc] init];
  }
  return instance;
}

@end
