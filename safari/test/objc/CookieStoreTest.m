//
//  CookieStoreTest
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

#import <SenTestingKit/SenTestingKit.h>



@interface CookieStoreTest : SenTestCase
{
}
@end

@implementation CookieStoreTest

- (void)testCookieStoreShouldBePerProcess 
{
    NSHTTPCookieStorage* cookieStore = [NSHTTPCookieStorage sharedHTTPCookieStorage];
    NSString* pid = [NSString stringWithFormat:@"pid:%d",[[NSProcessInfo processInfo]processIdentifier]];
    
    NSURL* testCookieURL = [NSURL URLWithString:@"http://pid.unittest.safari.selenium.openqa.org/"]; 
    NSString* pidCookieName = @"safari_test_cookie_pid";
    
    // Make sure we either don't have the test cookie or it has our pid:
    // If you don't link in PerProcessHTTPCookieStore this assertion fails for the second round.
    // Unfortunately since this store is a global thing, and the way we override it is a hack,
    // there is no really nice way to test it - you really have to run 2 times for relaible results...
    NSArray* cookies = [cookieStore cookiesForURL:testCookieURL];
    if ([cookies count]) {
        for (NSHTTPCookie* cookie in cookies) {
            if ([[cookie name] isEqualToString:pidCookieName]) {
                STAssertEqualObjects([cookie value],pid,@"Cookie did not have the correct pid!");
            }
        }
    }

    NSHTTPCookie* cookie = [NSHTTPCookie cookieWithProperties:
        [NSDictionary dictionaryWithObjectsAndKeys:
            pidCookieName,NSHTTPCookieName,
            [testCookieURL host],NSHTTPCookieDomain,
            [testCookieURL path],NSHTTPCookiePath,
            @"FALSE",NSHTTPCookieDiscard,
            @"360009",NSHTTPCookieMaximumAge,
            pid,NSHTTPCookieValue,
            nil,nil
        ]
    ];
    STAssertNotNil(cookie,@"Cookie creation failed");
    [cookieStore setCookie:cookie];
    cookies = [cookieStore cookiesForURL:testCookieURL];
    STAssertEquals((int)1,(int)[cookies count],@"Storing cookie failed!");
}

@end
