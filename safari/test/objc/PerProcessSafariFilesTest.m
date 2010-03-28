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
#import "PerProcessSafariFiles.h"
#import "SafariExtensionPaths.h"

@interface PerProcessSafariFilesTest : SenTestCase
{
}
@end

@implementation PerProcessSafariFilesTest

- (void)testDatabasePathIsRewrittenInDefaults
{
  [PerProcessSafariFiles init];
  NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
  NSString* webdbDir = [defaults objectForKey:@"WebDatabaseDirectory"];
  STAssertEqualObjects([[SafariExtensionPaths instance] databaseStoragePath], webdbDir,@"webdbdir is not dbpath");
}

@end
