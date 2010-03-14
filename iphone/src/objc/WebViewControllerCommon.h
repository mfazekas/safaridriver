//
//  WebViewController.h
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

#import "WebViewDriver.h"

#if TARGET_OS_IPHONE
#include <UIKit/UIKit.h>
typedef UIWebView WebViewType;
#else
#include <WebKit/WebKit.h>
typedef WebView WebViewType;
#endif

@protocol WebViewControllerDelegate

- (void)describeLastAction:(NSString *)status;
- (ImageType *)screenshot;
- (void)clickOnPageElementAt:(CGPoint)point;
- (void)loadRequest:(NSURLRequest*)url;

@end


@interface WebViewControllerCommon : NSObject<WebViewDriver>
{
 @private
  // The spec states that the GET message shouldn't return until the new page
  // is loaded. We need to lock the main thread to implement that. That'll
  // happen by polling [view isLoaded] but we can break early if the delegate
  // methods are fired. Note that subframes may still be being loaded.
  NSCondition *loadLock_;
  
  NSString *lastJSResult_;
	
  NSURLRequestCachePolicy cachePolicy_;
  
  // This is nil if the last operation succeeded.
  NSError *lastError_;
  
  NSObject<WebViewControllerDelegate>* delegate_;
  WebViewType* webView_;
}

- (id)initWithWebView:(WebViewType*)webView;

- (void)webViewDidFinishLoad:(WebViewType *)webView;
- (void)webView:(WebViewType *)webView didFailLoadWithError:(NSError *)error;

@property (retain, readonly, nonatomic) WebViewType *webView;
@property (assign, nonatomic) NSObject<WebViewControllerDelegate>* delegate;

@end
