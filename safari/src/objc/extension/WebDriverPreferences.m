//
//  WebDriverPreferences.mm
//  SafariDriver
//
//  Created by Miklós Fazekas on 1/3/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "WebDriverPreferences.h"


@implementation WebDriverPreferences

static WebDriverPreferences *singleton = nil;

+ (WebDriverPreferences*) sharedInstance {
  if (singleton == nil) {
    singleton = [[WebDriverPreferences alloc] init];
  }	
  return singleton;
}

- (void) setDefaultServerPortNumber
{
    const char* port_env = getenv("WEBDRIVER_SAFARI_PORT");
    if ((port_env) != 0) 
    {
        portNumber = [[NSString stringWithUTF8String:port_env] intValue];
    }
    else 
    {
        portNumber = 0;
    }
}

- (id) init
{
    self = [super init];
    if (self != nil) {
        [self setDefaultServerPortNumber];
    }
    return self;
}

- (UInt16) serverPortNumber
{
    return portNumber;
}

- (void) setServerPortNumber:(UInt16)portNumber_
{
    portNumber = portNumber_;
}

@end
