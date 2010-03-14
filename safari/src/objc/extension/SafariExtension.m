//
//  SafariExtension.m
//  SafariExtension
//
//  Created by Andrian Kurniady on 10/1/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "SafariExtension.h"
#import "HTTPServerController.h"
#import <WebKit/WebKit.h>

@interface NSDocument(BrowserDocumentProtocol)
- (WebView*)currentWebView;
@end

@implementation SafariExtension

- (id)init {
  if (self = [super init]) {
    loaded_ = false;
  }
  return self;
}

- (void)gotWebView:(WebView*)webView {
  if (!loaded_) {
    loaded_ = true;
    NSLog(@"SafariDriver init");
    [WebViewController createSharedInstance:webView];
    NSLog(@"WebView located!");
  }
}

- (void)onWebViewLoaded:(NSNotification*)n {
  [self gotWebView:(WebView*)[n object]];
}

- (WebView*)viewViewFromDocument {
  NSDocumentController* documentController = [NSDocumentController sharedDocumentController];
  
  NSDocument* document = [documentController currentDocument];
  if (!document) {
    if ([[documentController documents] count] == 0) {
        [documentController newDocument:nil];
    }
    document = [[documentController documents] objectAtIndex:0];
  }
  return [document currentWebView];
}

- (void)onSafariLoaded {
  NSLog(@"Safari loaded!");
  WebView* webView = 0;
  if (webView = [self viewViewFromDocument]) {
    [self gotWebView:webView];
  }
  [HTTPServerController sharedInstance];
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
