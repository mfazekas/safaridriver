//
//  WebViewDriverSingleton.h
//  SafariDriver
//
//  Created by Miklós Fazekas on 3/6/10.
//  Copyright 2010 Apple Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol WebViewDriver;

@interface WebViewDriverSingleton : NSObject {

}

+ (id<WebViewDriver>)instance;

@end
