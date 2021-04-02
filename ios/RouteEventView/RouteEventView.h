//
//  RouteEventView.h
//  Navigine Example
//
//  Created by Dmitry Stavitsky on 01/07/2018.
//  Copyright © 2018 Navigine. All rights reserved.
//

#ifndef RouteEventView_h
#define RouteEventView_h

#import <UIKit/UIKit.h>

@class NCRouteEvent;
@interface RouteEventView : UIView
@property (nonatomic, strong) UIButton *cancelBtn; // Cancel route button
@property (nonatomic, strong) NCRouteEvent *event;
- (id) initWithFrame:(CGRect) rect;
- (void) setFinishTitle;
@end

#endif /* RouteEventView_h */
