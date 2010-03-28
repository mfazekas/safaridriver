//
//  SafariExtensionPaths
//  SafariDriver
//
//  Copyright 2009 WebDriver committers
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

#import "SafariExtensionPaths.h"

@implementation SafariExtensionPaths

- (id) initWithPid:(int)pid
{
  self = [super init];
  if (self != nil) {
    NSString *perProcessTempDir =
    [[NSTemporaryDirectory() 
        stringByAppendingPathComponent:@"SafariWebDriver"]
        stringByAppendingPathComponent:[NSString stringWithFormat:@"Pid%d",pid]];
    baseDir = [perProcessTempDir retain];
  }
  return self;
}

- (void) dealloc
{
  [baseDir release];
  [super dealloc];
}

- (NSString*)cookieStorePath {
  return [baseDir stringByAppendingPathComponent:@"Cookies"];
}

- (NSString*)cacheStoragePath {
  return [baseDir stringByAppendingPathComponent:@"Caches"];
}

- (NSString*)databaseStoragePath {
  return [baseDir stringByAppendingPathComponent:@"Databases"];
}


+ (SafariExtensionPaths*)instance {
  static SafariExtensionPaths* result = 0;
  @synchronized([SafariExtensionPaths class]) {
    if (!result) {
      int pid = [[NSProcessInfo processInfo] processIdentifier];
      result = [[SafariExtensionPaths alloc] initWithPid:pid];
    }
  }
  return result; 
}

@end
