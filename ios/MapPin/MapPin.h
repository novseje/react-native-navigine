//
//  MapPin.h
//  Navigine Example
//
//  Created by Dmitry Stavitsky on 01/07/2018.
//  Copyright © 2018 Navigine. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "../Helpers/UIView+Additions.h"
#define kColorFromHex(color)[UIColor colorWithRed:((float)((color & 0xFF0000) >> 16))/255.0 green:((float)((color & 0xFF00) >> 8))/255.0 blue:((float)(color & 0xFF))/255.0 alpha:1.0]

@class NCVenue;
@interface MapPin : UIButton 
@property (nonatomic) UIButton *popUp;
@property (nonatomic, weak) NCVenue *venue;

- (id)initWithVenue:(NCVenue *) venue;
- (void) resizeMapPinWithZoom: (CGFloat) zoom;
- (void) saveMapPinSize;

@end
