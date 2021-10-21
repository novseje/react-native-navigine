#import <React/RCTBridgeModule.h>

#define DEBUG_LOG YES

@implementation Navigine

static NSString* API_KEY; // personal security key in the profile
static NSString* API_SERVER = @"https://api.navigine.com"; // API server

RCT_EXPORT_MODULE(Navigine)

RCT_EXPORT_METHOD(setApiKey:(NSString *)apiKey callback:(RCTResponseSenderBlock)callback)
{
    if(DEBUG_LOG) NSLog( @"[CALL] setApiKey: %@", apiKey );

    callback(@"OK");
}

RCT_EXPORT_METHOD(init:(NSString *)apiKey locationId:(NSInteger)locationId callback:(RCTResponseSenderBlock)callback)
{

}

RCT_EXPORT_METHOD(getFloorImage: (RCTResponseSenderBlock)callback)
{

}

RCT_EXPORT_METHOD(getAzimuth: (RCTResponseSenderBlock)callback)
{

}

RCT_EXPORT_METHOD(getCurPosition: (RCTResponseSenderBlock)callback)
{

}

RCT_EXPORT_METHOD(getFloorImageSizes: (RCTResponseSenderBlock)callback)
{

}

RCT_EXPORT_METHOD(getZoomScale: (RCTResponseSenderBlock)callback)
{

}

RCT_EXPORT_METHOD(didEnterZones:(RCTResponseSenderBlock)callback)
{

}

RCT_EXPORT_METHOD(getRoutePoints:(RCTResponseSenderBlock)callback)
{

}

RCT_EXPORT_METHOD(setRouteDestination:(float)x yParameter:(float)y callback:(RCTResponseSenderBlock)callback)
{

}


@end
