//
//  RouteEventView.m
//  Navigine Example
//
//  Created by Dmitry Stavitsky on 01/07/2018.
//  Copyright © 2018 Navigine. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RouteEventView.h"
#import "Navigine/Navigine.h"

@interface RouteEventView()
@property (nonatomic, strong) UILabel *eventTitle;
@property (nonatomic, strong) UILabel *eventSubTitle;
@end

@implementation RouteEventView

- (id) initWithFrame:(CGRect) rect  {
  if( self = [super initWithFrame:rect]) {
    _event = [[NCRouteEvent alloc] init];
    self.backgroundColor = UIColor.whiteColor;
    [self addBottomBorderWithColor: UIColor.lightGrayColor andWidth: 1.];
    // Setup button
    _cancelBtn = [[UIButton alloc] init];
    _cancelBtn.frame = CGRectMake(self.frame.size.width - 50, self.frame.size.height / 4., 40, 40);
    _cancelBtn.backgroundColor = [UIColor.grayColor colorWithAlphaComponent: 0.1];
    _cancelBtn.layer.cornerRadius = _cancelBtn.frame.size.height / 2.;
    // Set image for cancel button
    UIImage *btnImg = [UIImage imageNamed:@"cancelRouteBtn"];
    [_cancelBtn setImage:btnImg forState: UIControlStateNormal];
    // Setup title
    _eventTitle = [[UILabel alloc] initWithFrame: CGRectMake(0.,
                                                             0.,
                                                             self.frame.size.width,
                                                             self.frame.size.height / 2.)];
    [_eventTitle setTextColor:[UIColor blackColor]];
    [_eventTitle setBackgroundColor:[UIColor clearColor]];
    [_eventTitle setFont:[UIFont fontWithName: @"AppleSDGothicNeo-Bold" size: 24.0f]];
    [_eventTitle setTextAlignment:NSTextAlignmentCenter];
    [_eventTitle adjustsFontSizeToFitWidth];
    // Setup subtitle
    _eventSubTitle = [[UILabel alloc] initWithFrame: CGRectMake(0.,
                                                                self.frame.size.height / 2., // Status bar
                                                                self.frame.size.width,
                                                                self.frame.size.height / 3.)];
    [_eventSubTitle setTextColor:[UIColor grayColor]];
    [_eventSubTitle setBackgroundColor:[UIColor clearColor]];
    [_eventSubTitle setFont:[UIFont fontWithName: @"AppleSDGothicNeo-Bold" size: 16.0f]];
    [_eventSubTitle setTextAlignment:NSTextAlignmentCenter];
    [_eventSubTitle adjustsFontSizeToFitWidth];
    [self addSubview: _eventTitle];
    [self addSubview: _eventSubTitle];
    [self addSubview: _cancelBtn];
    self.hidden = YES;
  }
  return self;
}

- (void) setFinishTitle {
  [_eventTitle setText: @"Less than 1 meter"];
  [_eventSubTitle setText: @"You are reached your place!"];
}

- (void) setEvent:(NCRouteEvent *)event {
  if (_event != event) {
    _event = event;
    [_eventTitle setText: [[NSString stringWithFormat:@"%.1f", _event.distance] stringByAppendingString:@"m"]];
    switch(_event.type) {
      case NCRouteEventTypeUndefined:
        [_eventSubTitle setText:@"Undefined event!"];
        break;
      case NCRouteEventTypeTurnLeft:
        [_eventSubTitle setText:@"Turn left"];
        break;
      case NCRouteEventTypeTurnRight:
        [_eventSubTitle setText:@"Turn right"];
        break;
      case NCRouteEventTypeTransition:
        [_eventSubTitle setText:@"Cange floor"];
        break;
      default:
        [_eventSubTitle setText:@"Unknown event!"];
        break;
    }
  }
}

// Add buttom border to general view
- (void) addBottomBorderWithColor:(UIColor *)color andWidth:(CGFloat) borderWidth {
  UIView *border = [UIView new];
  border.backgroundColor = color;
  [border setAutoresizingMask:UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin];
  border.frame = CGRectMake(0, self.frame.size.height - borderWidth, self.frame.size.width, borderWidth);
  [self addSubview:border];
}

@end
