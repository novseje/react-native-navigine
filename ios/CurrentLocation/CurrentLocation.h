//
//  CurrentLocation.h
//  Navigine Example
//
//  Created by Dmitry Stavitsky on 01/07/2018.
//  Copyright © 2018 Navigine. All rights reserved.
//

#ifndef CurrentLocation_h
#define CurrentLocation_h

#import <UIKit/UIView.h>

@interface CurrentLocation : UIView
@property (nonatomic, assign) float radius; // Accurancy of your position
- (id) initWithColor: (UIColor *)color;
- (id) init; // Default constructor
- (void)resizeLocationPinWithZoom:(CGFloat)zoom;
@end
#endif /* CurrentLocation_h */
