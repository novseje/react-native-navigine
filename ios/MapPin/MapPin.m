//
//  MapPin.m
//  Navigine Example
//
//  Created by Dmitry Stavitsky on 01/07/2018.
//  Copyright © 2018 Navigine. All rights reserved.
//

#import "MapPin.h"
#import "Navigine/Navigine.h"

@implementation MapPin

- (id)initWithVenue:(NCVenue *)venue {
  self = [super initWithFrame:CGRectZero];
  if (self) {
    self.venue = venue;
    // Init map pin with venue's centre
    CGPoint ctrVenue = CGPointMake(venue.x.floatValue, venue.y.floatValue);
    self.center = ctrVenue;
    
    // Set image to venue
    UIImage *venImg = [UIImage imageNamed:@"elmVenueIcon"];
    [self setImage:venImg forState: UIControlStateNormal];
    self.layer.zPosition = 5.; // For overlap path curve
    
    // Add venue's popup title
    _popUp = [[UIButton alloc] initWithFrame:CGRectZero];
    [_popUp setTitle:self.venue.name forState:UIControlStateNormal];
    _popUp.titleLabel.font = [UIFont fontWithName:@"AppleSDGothicNeo-Bold" size:30.]; ;
    _popUp.titleLabel.textColor = UIColor.whiteColor;
    _popUp.titleLabel.textAlignment = NSTextAlignmentCenter;
    _popUp.backgroundColor = kColorFromHex(0xCE8951);
    _popUp.clipsToBounds = NO;
    _popUp.layer.cornerRadius = 10.;
    [_popUp sizeToFit];
    _popUp.width += 20.;
    
    // Add down arrow to label
    UIImageView *labelCaret = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"elmBubbleArrowOrange"]];
    labelCaret.centerX = _popUp.width / 2.;
    
    // Hide gap between label and arrow
    labelCaret.centerY = _popUp.height + labelCaret.height / 2. - .5;
    [_popUp addSubview:labelCaret];
    _popUp.hidden = YES;
  }
  return self;
}

- (void) resizeMapPinWithZoom: (CGFloat) zoom {
  const float imgHeight = self.imageView.frame.size.height/2;
  self.transform = CGAffineTransformMakeScale(1.0f / zoom, 1.0f / zoom);
  _popUp.transform = CGAffineTransformMakeScale(1.0f / zoom, 1.0f / zoom);
  _popUp.bottom = self.top - (imgHeight) / zoom;
}

- (void) saveMapPinSize {
}
@end
