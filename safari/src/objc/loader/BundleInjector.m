// The purpose of this code is to load a bundle when a dyld is initialized. This is
// usefull as we only can inject dylibs but not bundles with DYLD_INSERT_LIBRARIES.
//
// To use this dyld you should load it with DYLD_INSERT_LIBRARIES, the it will load
// bundle specified by the XCInjectBundle in the environment variable. This is a 
// mostly compatible replacement for DevToolsBundleInjection.

#import <Foundation/Foundation.h>

@interface BundleInjection : NSObject 

{
}
+ (void)initialize;
+ (void)ensureInitialized;

@end

@implementation BundleInjection

+ (void)initialize
{
    NSAutoreleasePool *ap = [[NSAutoreleasePool alloc] init];
    NSDictionary *environments = [[NSProcessInfo processInfo] environment];
    NSString* loadbundlepath = [environments objectForKey:@"XCInjectBundle"];
	if (loadbundlepath) {
		NSBundle* bundle = [NSBundle bundleWithPath: loadbundlepath];
        if (!bundle) {
            NSLog(@"Could not read the bundle at: %@, please revise XCInjectBundle\n",loadbundlepath);
            return;
        }
        [bundle load];
	} else {
        NSLog(@"Please define environment: XCInjectBundle\n");
    }
	[ap release];
}

+ (void)ensureInitialized
{
    // dummy function to make sure initialize is called by the runtime
}

@end


void BundleLoaderDyldMain(void) __attribute__ ((constructor));

void BundleLoaderDyldMain(void) 
{
	[BundleInjection ensureInitialized];
}
