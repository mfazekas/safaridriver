//
//  SafariExtension.h
//  SafariExtension
//
//  Created by Andrian Kurniady on 10/1/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <Webkit/Webkit.h>
#import "WebViewController.h"

@interface SafariExtension : NSObject {
  bool loaded_;
}

- (void) onSafariLoaded;
- (void) onWebViewLoaded:(NSNotification*) n;

// This method is to be called by the loader hook.
+ (void) load;

+ (id) sharedInstance;

@end