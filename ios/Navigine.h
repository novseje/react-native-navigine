#import <React/RCTBridgeModule.h>
#import <React/RCTViewManager.h>
#import <React/RCTView.h>
#import <Navigine/Navigine.h>

@class ErrorView, RouteEventView, CurrentLocation, MapPin;

@interface Navigine : NSObject
<
RCTBridgeModule,
NavigineCoreDelegate,
NavigineCoreNavigationDelegate,
NavigineCoreLocationDelegate,
NavigineCoreBluetoothDelegate
>
@property (weak, nonatomic) IBOutlet UIImageView *imageView;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
// Buttons
@property (weak, nonatomic) IBOutlet UILabel *lblCurrentFloor;
@property (weak, nonatomic) IBOutlet UIStackView *btnStackFloor;
@property (nonatomic, strong) MapPin *pressedPin; // Selected venue
@property (nonatomic, strong) CurrentLocation *curPosition;
@property (nonatomic, assign) int floor; // Selected sublocation
@property (nonatomic, assign) int locationId;
// Constraints
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *imageViewHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *imageViewWidthConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *imageViewTopConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *imageViewLeadingConstraint;
// Path
@property (nonatomic, strong) UIBezierPath *routePath;
@property (nonatomic, strong) CAShapeLayer *routeLayer;
@property (nonatomic, assign) BOOL isRouting;
@property (nonatomic, strong) RouteEventView *eventView;
@property (nonatomic, strong) ErrorView *errorView;
// NCore attributes
@property (nonatomic, strong) NavigineCore *navigineCore;
@property (nonatomic, strong) NCLocation *location;
@property (nonatomic, strong) NCSublocation *sublocation;
@end
