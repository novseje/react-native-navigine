//
//  ErrorView.m
//  Navigine
//
//  Created by Dmitry Stavitsky on 01/07/2018.
//  Copyright © 2018 Navigine. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ErrorView.h"

@implementation ErrorView

- (id) initWithFrame:(CGRect) rect {
  if(self = [super initWithFrame:rect]) {
    [self setNumberOfLines: 0];
    [self setTextColor:[UIColor whiteColor]];
    [self setBackgroundColor:[UIColor.redColor colorWithAlphaComponent: .7]];
    [self setFont:[UIFont fontWithName: @"AppleSDGothicNeo-Bold" size: 24]];
    [self setTextAlignment:NSTextAlignmentCenter];
    [self adjustsFontSizeToFitWidth];
    [self setHidden:YES];
  }
  return self;
}

- (void) setError:(NSError*) error {
  if(_error != error) {
    _error = error;
    NSString *defautError = [NSString stringWithFormat:@"%@\n%@ %@", @"Navigation unavailable!", @"Error", @(error.code)];
    switch(error.code) {
      case 1:
        [self setText:[defautError stringByAppendingString:@": Incorrect Client"]];
        break;
      case 4:
        [self setText:[defautError stringByAppendingString:@": No solution"]];
        break;
      case 8:
        [self setText:[defautError stringByAppendingString:@": No beacons found"]];
        break;
      case 10:
        [self setText:[defautError stringByAppendingString:@": Incorrect BMP"]];
        break;
      case 20:
        [self setText:[defautError stringByAppendingString:@": Incorrect GP"]];
        break;
      case 21:
        [self setText:[defautError stringByAppendingString:@": Incorrect XML Params"]];
        break;
      default:
        [self setText:[defautError stringByAppendingString:@": Unknown error"]];
        break;
    }
  }
}

@end
