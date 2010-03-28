//
//  PerProcessSafariFiles
//  SafariDriver
//
//  Copyright 2010 WebDriver committers
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

#import "PerProcessSafariFiles.h"
#import "SafariExtensionPaths.h"
#import "PerProcessHTTPCookieStore.h"

@implementation PerProcessSafariFiles

- (void)setupPerProcessURLCache {
  NSURLCache* origCache = [NSURLCache sharedURLCache];
  NSURLCache* cache = [[NSURLCache alloc] initWithMemoryCapacity:[origCache memoryCapacity]
            diskCapacity:[origCache diskCapacity] 
            diskPath:[paths cacheStoragePath]];
  [NSURLCache setSharedURLCache:cache];
}

- (void)_overrideDefaultsWithArgsTakingPrecedence:(NSDictionary*)overrideSettings  {
  NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
  NSMutableDictionary *newArgumentSettings = [NSMutableDictionary dictionaryWithDictionary:overrideSettings];
  [newArgumentSettings addEntriesFromDictionary:[defaults volatileDomainForName:NSArgumentDomain]];
  [defaults setVolatileDomain:newArgumentSettings 
        forName:NSArgumentDomain];
}

- (void)setupPerProcessDatabase {
    // See WebUtilites for more info
  NSDictionary* overrideSettings = [NSDictionary dictionaryWithObject:[paths databaseStoragePath] forKey:@"WebDatabaseDirectory"];
  [self _overrideDefaultsWithArgsTakingPrecedence:overrideSettings];
}

- (void)setupPerProcessCookies {
    // This does nothing it just do it to make sure that we've linked in 
    // the cateogy that hacks HTTPCookieStore
  [PerProcessHTTPCookieStore makeSurePerProcessHTTPCookieStoreLinkedIn];
}
 
- (id)initWithPaths:(SafariExtensionPaths*)paths_ {
  self = [super init];
  if (self != nil) {
    paths = paths_;
    [paths_ retain];
    [self setupPerProcessCookies];
    [self setupPerProcessURLCache];
    [self setupPerProcessDatabase];
  }
  return self;
}

+ (void)init {
  NSAssert([NSThread isMainThread],@"Should be called from the main therad!");
  PerProcessSafariFiles* instance = 0;
  if (!instance) {
    instance = [[PerProcessSafariFiles alloc] initWithPaths:[SafariExtensionPaths instance]];
  }
}

@end
