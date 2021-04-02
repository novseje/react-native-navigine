//
//  ErrorView.h
//  Navigine Example
//
//  Created by Dmitry Stavitsky on 01/07/2018.
//  Copyright © 2018 Navigine. All rights reserved.
//

#ifndef ErrorView_h
#define ErrorView_h

#import <UIKit/UIKit.h>

@interface ErrorView : UILabel
@property (nonatomic, strong) NSError *error;
- (id) initWithFrame:(CGRect) rect;
@end

#endif /* ErrorView_h */
