//
//  WebDriverPreferences.h
//  SafariDriver
//
//  Created by Miklós Fazekas on 1/3/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@interface WebDriverPreferences : NSObject {
    UInt16 portNumber;
}

+ (WebDriverPreferences *)sharedInstance;

- (UInt16) serverPortNumber;
- (void) setServerPortNumber:(UInt16) portNumber;

@end
