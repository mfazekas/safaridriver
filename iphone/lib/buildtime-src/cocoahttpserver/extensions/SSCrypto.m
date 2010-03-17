/* this is a hack so that we can conditionally (only on mac but not in iphone) 
 * include SSCrypto.m into the project
 * also we're adding some include that was missing from SSCrypto.m.
 */
#import <Foundation/Foundation.h>

#if TARGET_OS_IPHONE || TARGET_IPHONE_SIMULATOR
// we don't need SSCrypto on iphone
#else
#include <openssl/md5.h>
#include "../project/SSCrypto.m"
#endif
