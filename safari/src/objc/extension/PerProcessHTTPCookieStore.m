//
//  PerProcessHTTPCookieStore
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
#import "PerProcessHTTPCookieStore.h"
#import "SafariExtensionPaths.h"

#import <objc/objc.h>
#import <objc/objc-class.h>

@interface NSObject(NSHTTPCookieStoreInetrnal) 
- (id)initWithStorageLocation:(NSURL*)url;
@end

static id cookieStoreInitImpl(id self_) {
  NSURL* storageURL = NULL;
  NSString* pidPath = [[SafariExtensionPaths instance] cookieStorePath];
  storageURL = [NSURL fileURLWithPath:pidPath];
  return [self_ initWithStorageLocation:storageURL];
}

#if __OBJC2__

static id new_cookieStoreInternalInitIMP(id self_, SEL sel_) 
{
  return cookieStoreInitImpl(self_);
}


@implementation PerProcessHTTPCookieStore

+ (void)overrideHTTPCookieStoreInternal {
  Class class = objc_getClass("NSHTTPCookieStorageInternal");
  class_replaceMethod(class,@selector(init),(IMP)&new_cookieStoreInternalInitIMP,"@:");
}

+ (void)initialize {
  if(self == [PerProcessHTTPCookieStore class]) {
    [self overrideHTTPCookieStoreInternal];
  }
}

+ (void)makeSurePerProcessHTTPCookieStoreLinkedIn {
}

@end
#else

@interface NSHTTPCookieStorageInternal : NSObject {
}
- (id)initWithStorageLocation:(NSURL*)storageFileURL;
@end

@interface NSHTTPCookieStorageInternal(PerProcessHTTPCookieStore)

- (id)init;
+ (void)makeSurePerProcessHTTPCookieStoreLinkedIn;

@end

@implementation NSHTTPCookieStorageInternal(OverrideCookieURL)

- (id)init
{
  return cookieStoreInitImpl(self);
}

@end

@implementation PerProcessHTTPCookieStore

+ (void)makeSurePerProcessHTTPCookieStoreLinkedIn
{
  // dummy, just so that we can call it to make sure it's linked in
}

@end

#endif

