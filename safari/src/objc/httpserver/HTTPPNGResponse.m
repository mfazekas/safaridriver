//
//  HTTPPNGResponse.m
//  SafariDriver
//
//  Created by Miklós Fazekas on 1/17/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "HTTPPNGResponse.h"


@implementation HTTPPNGResponse

- (id)initWithImage:(ImageType *)image
{
    NSBitmapImageRep* bits = [[image representations] objectAtIndex:0];
    NSData* imageData = [bits representationUsingType: NSPNGFileType properties: nil];
    NSLog(@"Sending PNG image of size %d bytes", [imageData length]);
    return [super initWithData:imageData];
}

- (NSString *) contentType {
  return @"image/png";
}

@end
