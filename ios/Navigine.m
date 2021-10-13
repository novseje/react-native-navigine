#import "Navigine.h"

#import "CurrentLocation/CurrentLocation.h"
#import "RouteEventView/RouteEventView.h"
#import "ErrorView/ErrorView.h"

#define DEBUG_LOG YES

@implementation Navigine

static NSString* API_KEY; // personal security key in the profile
static NSString* API_SERVER = @"https://api.navigine.com"; // API server

static NSMutableArray* zonesCollect;
static NSMutableArray* routePathPoints;

RCT_EXPORT_MODULE(Navigine)

RCT_EXPORT_METHOD(setApiKey:(NSString *)apiKey callback:(RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] setApiKey: %@", apiKey );
    API_KEY = apiKey;
    callback(@"OK");
}

RCT_EXPORT_METHOD(init:(NSString *)apiKey locationId:(NSInteger)locationId callback:(RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] init: locationId=%d", locationId );
    API_KEY = apiKey;
    _locationId = locationId;

    zonesCollect = [[NSMutableArray alloc] init];
    routePathPoints = [[NSMutableArray alloc] init];

    _curPosition = [[CurrentLocation alloc] init];
    _curPosition.hidden = NO;

    [self initCore];

    BOOL forced = YES;
    // If YES, the content data would be loaded even if the same version has been downloaded already earlier.
    // If NO, the download process compares the current downloaded version with the last version on the server.
    // If server version equals to the current downloaded version, the re-downloading is not done.

    [_navigineCore downloadLocationById:_locationId forceReload:forced
        processBlock:^(NSInteger loadProcess) {
            if(DEBUG_LOG) NSLog( @"[CALL] processBlock" );
        } successBlock:^(NSDictionary *userInfo) {
            if(DEBUG_LOG) NSLog( @"[CALL] successBlock" );

            [self->_navigineCore startNavigine];
            [self setupFloor: self.floor];

            callback(@[[NSString stringWithFormat: @"numberArgument: %ld stringArgument: %@", (long)_navigineCore.location.id, _navigineCore.location.name]]);
        } failBlock:^(NSError *error) {
            if(DEBUG_LOG) NSLog( @"[CALL] failBlock" );
            NSLog(@"%@",error);
        }];
}

RCT_EXPORT_METHOD(getFloorImage: (RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] getFloorImage" );

    _location = _navigineCore.location;
    _sublocation = _navigineCore.location.sublocations[_floor];
    UIImage *floorImg = [UIImage imageWithData: _sublocation.sublocationImage.imageData];

    callback(@[[NSString stringWithFormat: @"%@", [self encodeToBase64String:floorImg]]]);
}

RCT_EXPORT_METHOD(getAzimuth: (RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] getAzimuth: azimuth=%f", _azimuth);

    callback(@[[NSString stringWithFormat: @"%f", _azimuth]]);
}

RCT_EXPORT_METHOD(getCurPosition: (RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] getCurPosition" );
    if(DEBUG_LOG) NSLog( @"curPosition: %f, %f, zoom: %f", _curPosition.center.x, _curPosition.center.y, _zoomScale);

    callback(@[[NSString stringWithFormat: @"%f|%f", _curPosition.center.x * _zoomScale, _curPosition.center.y * _zoomScale]]);
}

RCT_EXPORT_METHOD(getFloorImageSizes: (RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] getFloorImageSizes" );
    if(DEBUG_LOG) NSLog( @"floorImageSizes: %f|%f", _floorImageWidth, _floorImageHeight);

    callback(@[[NSString stringWithFormat: @"%f|%f", _floorImageWidth, _floorImageHeight]]);
}

RCT_EXPORT_METHOD(getZoomScale: (RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] getZoomScale" );
    if(DEBUG_LOG) NSLog( @"zoomScale: %f", _zoomScale);

    callback(@[[NSString stringWithFormat: @"%f", _zoomScale]]);
}

RCT_EXPORT_METHOD(didEnterZones:(RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] didEnterZones" );

    if ([zonesCollect count] < 1) {
        callback(@[[NSString stringWithFormat: @""]]);
        return;
    }

    // loop through every element (dynamic typing)
    /*for (NCZone *zone in zonesCollect) {
        if(DEBUG_LOG) NSLog(@"Single element: %@", zone);
        if(DEBUG_LOG) NSLog(@"Zone name: %@", zone.name);
    }*/

    NCZone *zone = [zonesCollect lastObject];

    if (zone) {
        callback(@[[NSString stringWithFormat: @"%@", zone.name]]);
    }
    [zonesCollect removeAllObjects];
}

RCT_EXPORT_METHOD(getRoutePoints:(RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] getRoutePoints" );

    NSMutableArray *pointsArray = [[NSMutableArray alloc] init];
    for (NSValue *point in routePathPoints) {
        if(DEBUG_LOG) NSLog( @"NSValue: %@", point );
        if(DEBUG_LOG) NSLog( @"NSValue.x: %f", [point CGPointValue].x );
        [pointsArray addObject:[NSString stringWithFormat: @"{\"x\": %f, \"y\": %f}", [point CGPointValue].x * _zoomScale, [point CGPointValue].y * _zoomScale]];
/*
        const CGFloat dstX = (_floorImageWidth / _zoomScale) * [point CGPointValue].x / _sublocation.dimensions.width;
        const CGFloat dstY = (_floorImageHeight / _zoomScale) * (1. - [point CGPointValue].y / _sublocation.dimensions.height);
        [pointsArray addObject:[NSString stringWithFormat: @"{\"x\": %f, \"y\": %f}", dstX, dstY]];
*/
        if(DEBUG_LOG) NSLog( @"addObject: %@", @[[NSString stringWithFormat: @"{x: %f, y: %f}", [point CGPointValue].x, [point CGPointValue].y]] );
    }
    if(DEBUG_LOG) NSLog( @"pointsArray: %@", pointsArray );

    NSString *pointsString = [NSString stringWithFormat: @"[%@]", [pointsArray componentsJoinedByString: @", "]];
    if(DEBUG_LOG) NSLog( @"pointsString: %@", pointsString );

    callback(@[pointsString]);
}

RCT_EXPORT_METHOD(setRouteDestination:(float)x yParameter:(float)y callback:(RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] setRouteDestination: x: %f, y: %f", x, y );
    CGPoint touchPtInM = [self convertPixelsToMeters: x:y withScale:1]; // Touch point in meters
    NCLocationPoint *targetPt = [NCLocationPoint pointWithLocation: _location.id
                                                       sublocation: _sublocation.id
                                                                 x: @(touchPtInM.x)
                                                                 y: @(touchPtInM.y)];
    if(DEBUG_LOG) NSLog( @"NCLocationPoint: x: %f, y: %f", touchPtInM.x, touchPtInM.y );
    [_navigineCore cancelTargets];
    [_navigineCore setTarget:targetPt];
    _isRouting = YES;
    callback(@[[NSString stringWithFormat: @"OK"]]);
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (void) initCore {
    if(DEBUG_LOG) NSLog( @"[CALL] initCore");

    _floor = 0;
    //_locationId = 60019; // location id from web site
    _zoomScale = 1;

    // Initialize navigation core
    NSString *userHash = API_KEY; // your personal security key in the profile
    NSString *server = API_SERVER; // your API server
    _navigineCore = [[NavigineCore alloc] initWithUserHash:userHash server:server];
    _navigineCore.delegate = self;
    _navigineCore.locationDelegate = self;
    _navigineCore.navigationDelegate = self;
    _navigineCore.bluetoothDelegate = self;
}

- (void) setupFloor:(NSInteger) floor {
    if(DEBUG_LOG) NSLog( @"[CALL] setupFloor");

//  [self removeVenuesFromMap]; // Remove venues from map
//  [self removeZonesFromMap];  // Remove zones from map
    _location = _navigineCore.location;
    _sublocation = _navigineCore.location.sublocations[_floor];
    UIImage *floorImg = [UIImage imageWithData: _sublocation.sublocationImage.imageData];

    const CGSize imgSize = floorImg.size;

    if(DEBUG_LOG) NSLog( @"imgSize_width: %f, imgSize_height: %f", imgSize.width, imgSize.height );

   _floorImageWidth = imgSize.width;
   _floorImageHeight = imgSize.height;

    const CGSize viewSize = CGSizeMake(400, 400);
    float scale = 1.f;
    if ((imgSize.width / imgSize.height) > (viewSize.width / viewSize.height))
        scale = viewSize.height / imgSize.height;
    else
        scale = imgSize.width / imgSize.width;

    _floorViewWidth = imgSize.width * scale;
    _floorViewHeight = imgSize.height * scale;
    _zoomScale = scale;
//  _scrollView.contentSize = CGSizeMake(imgSize.width * scale, imgSize.height * scale);

//  [self drawZones];
//  [self drawVenues];
}

#pragma mark Handlers

- (IBAction) btnIncreaseFloorPressed:(id)sender {
  NSUInteger sublocCnt = _location.sublocations.count - 1;
  if (_floor == sublocCnt)
    return;
  else
    [self setupFloor: ++_floor];
  }

- (IBAction) btnDecreaseFloorPressed:(id)sender {
  if (_floor == 0)
    return;
  else
    [self setupFloor: --_floor];
}

#pragma mark Helper functions

// Convert from pixels to meters
- (CGPoint) convertPixelsToMeters:(float)srcX :(float)srcY withScale :(float)scale {
  const CGFloat dstX = srcX / (_floorImageWidth / scale) * _sublocation.dimensions.width;
  const CGFloat dstY = (1. - srcY / (_floorImageHeight / scale)) * _sublocation.dimensions.height;
  return CGPointMake(dstX, dstY);
}

// Convert from meters to pixels
- (CGPoint) convertMetersToPixels:(float)srcX :(float)srcY withScale :(float)scale {
  const CGFloat dstX = (_floorImageWidth / scale) * srcX / _sublocation.dimensions.width;
  const CGFloat dstY = (_floorImageHeight / scale) * (1. - srcY / _sublocation.dimensions.height);
  return CGPointMake(dstX, dstY);
}

- (void) drawRouteWithPath: (NSArray *)path andDistance: (float)distance {
    if(DEBUG_LOG) NSLog( @"[CALL] drawRouteWithPath");

  if (distance <= 3.) { // Check that we are close to the finish point of the route
    [self stopRoute];
  }
  else {
    [_routePath removeAllPoints];
    _routePath = [[UIBezierPath alloc] init];

    [routePathPoints removeAllObjects];

    for (NCLocationPoint *point in path) {
      if(DEBUG_LOG) NSLog( @"NCLocationPoint: %@", point);
      if (point.sublocation != _sublocation.id) // If path between different sublocations
      {
        if(DEBUG_LOG) NSLog( @"path between different sublocations");
        continue;
      }
      else {
        CGPoint cgPoint = [self convertMetersToPixels:point.x.floatValue:point.y.floatValue withScale:_zoomScale];
          if(DEBUG_LOG) NSLog( @"CGPoint: x: %f, y: %f", cgPoint.x, cgPoint.y);
          [routePathPoints addObject:[NSValue valueWithCGPoint:cgPoint]];
        if (_routePath.empty) {
          [_routePath moveToPoint:cgPoint];
          if(DEBUG_LOG) NSLog( @"moveToPoint" );
        }
        else {
          [_routePath addLineToPoint:cgPoint];
          if(DEBUG_LOG) NSLog( @"addLineToPoint" );
        }

      }
    }
  }
}

- (void) stopRoute {
    if(DEBUG_LOG) NSLog( @"[CALL] stopRoute");

  [[_imageView viewWithTag:1] removeFromSuperview]; // Remove current pin from map
  [_routePath removeAllPoints];
  [_navigineCore cancelTargets];
  _isRouting = NO;
}

- (uint) stringToHex: (NSString *) srcStr {
  uint hexStr = 0;
  NSScanner *strScanner = [NSScanner scannerWithString: srcStr];
  [strScanner setScanLocation: 1];
  [strScanner scanHexInt: &hexStr];
  return hexStr;
}

// Converting Objective-C object to a string
- (NSString *)encodeToBase64String:(UIImage *)image {
 return [UIImagePNGRepresentation(image) base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
}

# pragma mark NavigineCoreDelegate methods

- (void) navigineCore: (NavigineCore *)navigineCore didUpdateDeviceInfo: (NCDeviceInfo *)deviceInfo {
    if(DEBUG_LOG) NSLog( @"[CALL] didUpdateDeviceInfo");

  NSError *navError = deviceInfo.error;
  if (navError == nil) {
    _curPosition.hidden = deviceInfo.sublocation != _sublocation.id; // Hide current position pin
    if(!_curPosition.hidden) {
      //const float radScale = _imageView.width / _sublocation.dimensions.width;
      _curPosition.center = [self convertMetersToPixels: [deviceInfo.x floatValue]: [deviceInfo.y floatValue] withScale: _zoomScale];
      //_curPosition.radius = deviceInfo.r * radScale;
        if(DEBUG_LOG) NSLog( @"curPosition: %f, %f", _curPosition.center.x, _curPosition.center.y);
      _azimuth = deviceInfo.azimuth;
        if(DEBUG_LOG) NSLog( @"azimuth: %f", deviceInfo.azimuth);
    }
  }
  else {
    _curPosition.hidden = YES;
  }
  if (_isRouting) {
    if(DEBUG_LOG) NSLog( @"ROUTING: YES");
    NCRoutePath *devicePath = deviceInfo.paths.firstObject;
    if (devicePath) {
      NCLocalPoint *lastPoint = devicePath.points.lastObject;
      NSArray *path = devicePath.points;
      NSArray *eventsArr = devicePath.events;
      float distance = devicePath.lenght;
      [self drawRouteWithPath:path andDistance:distance];
    }
  }
}

- (void)navigineCore:(NavigineCore *)navigineCore didEnterZone:(NCZone *)zone {
    if(DEBUG_LOG) NSLog( @"[CALL] didEnterZone");

    if(DEBUG_LOG) NSLog(@"Enter zone: %@", zone);

    [zonesCollect addObject:zone];
}

- (void)navigineCore:(NavigineCore *)navigineCore didExitZone:(NCZone *)zone {
    if(DEBUG_LOG) NSLog( @"[CALL] didExitZone");

    if(DEBUG_LOG) NSLog(@"Leave zone: %@", zone);
}

#pragma mark UIScrollViewDelegate methods

- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView {
  return _imageView;
}

#pragma mark NavigineCoreDelegate methods

- (void) didRangePushWithTitle:(NSString *)title
                       content:(NSString *)content
                         image:(NSString *)image
                            id:(NSInteger)id {
  // Your code
}

#pragma mark NavigineCoreLocationDelegate methods

- (void)navigineCore:(NavigineCore *)navigineCore didUpdateLocations:(NSArray<CLLocation *> *)locations {
  NSLog(@"Locations: %@", locations);
}

- (void)navigineCore:(NavigineCore *)navigineCore didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
  NSLog(@"AuthorizationStatus: %d", status);
}

- (void)navigineCore:(NavigineCore *)navigineCore didFailWithError:(NSError *)error {
  NSLog(@"Error: %@", error);
}

#pragma mark NavigineCoreBluetoothDelegate methods

- (void)navigineCore:(NavigineCore *)navigineCore didUpdateBluetoothState:(CBManagerState)status {
  NSLog(@"Bluetooth status: %zd", status);
}

@end
