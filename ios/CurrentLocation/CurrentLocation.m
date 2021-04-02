//
//  CurrentLocation.m
//  Navigine Example
//
//  Created by Dmitry Stavitsky on 01/07/2018.
//  Copyright © 2018 Navigine. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CurrentLocation.h"

@interface CurrentLocation ()
@property (nonatomic, strong) UIView *presisCircle; // Accurancy of your position
@end

@implementation CurrentLocation

- (id) init {
  self = [self initWithColor: UIColor.redColor];
  return self;
}

- (id) initWithColor: (UIColor *)color {
  self = [super initWithFrame:CGRectMake(0, 0, 20, 20)];
  if (self) {
    // Init position point
    self.backgroundColor = color;
    self.layer.borderColor = [UIColor colorWithWhite:1. alpha:1.].CGColor;
    self.layer.borderWidth = 2.;
    self.layer.cornerRadius = self.frame.size.height / 2.f;
    // Init presision circle
    _presisCircle = [[UIView alloc] initWithFrame:CGRectZero];
    _presisCircle.clipsToBounds = YES;
    _presisCircle.backgroundColor = [UIColor.grayColor colorWithAlphaComponent: .5];
    _radius = 0.;
    [self addSubview:_presisCircle];
    self.hidden = YES;
  }
  return self;
}

- (void) setRadius:(float)radius {
  CGRect newFrame = CGRectMake(self.frame.size.width / 2. - radius / 2., self.frame.size.height / 2. - radius / 2., radius, radius);
  [self.presisCircle setFrame:newFrame];
  self.presisCircle.layer.cornerRadius = radius / 2.;
}

- (void)resizeLocationPinWithZoom:(CGFloat) zoom {
  self.transform = CGAffineTransformMakeScale(1. / zoom, 1. / zoom);
}

@end
