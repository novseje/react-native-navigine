#import <React/RCTBridgeModule.h>
#import <React/RCTViewManager.h>
#import <React/RCTView.h>
#import <Navigine/Navigine.h>
#import "CurrentLocation/CurrentLocation.h"

@class ErrorView, RouteEventView, CurrentLocation;

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

@property (weak, nonatomic) IBOutlet UILabel *lblCurrentFloor;
@property (weak, nonatomic) IBOutlet UIStackView *btnStackFloor;
@property (nonatomic, strong) CurrentLocation *curPosition;

@property (nonatomic, assign) int floor; // Selected sublocation
@property (nonatomic, assign) int locationId;
@property (nonatomic, assign) float zoomScale;
@property (nonatomic, assign) float floorImageHeight;
@property (nonatomic, assign) float floorImageWidth;
@property (nonatomic, assign) float floorViewHeight;
@property (nonatomic, assign) float floorViewWidth;
@property (nonatomic, assign) float azimuth;

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
