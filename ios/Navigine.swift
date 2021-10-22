@objc(Navigine)
class Navigine: NSObject {
    
    var mNavigineSdk: NCNavigineSdk?
    var mLocationManager: NCLocationManager?
    var mNavigationManager: NCNavigationManager?


    @objc(init:locationId:callback:)
    func initSDK(apiKey: String, locationId: Int32, callback:RCTResponseSenderBlock) -> Void {
        
        NCNavigineSdk.setUserHash(apiKey)
        NCNavigineSdk.setServer("https://api.navigine.com")

        mNavigineSdk = NCNavigineSdk.getInstance()!

        mLocationManager = mNavigineSdk?.getLocationManager()!
        mLocationManager?.add(self)
        mLocationManager?.setLocation(Int32(locationId))

        mNavigationManager = mNavigineSdk?.getNavigationManager(mLocationManager)!
        mNavigationManager?.add(self)
        
        callback(["OK"])
    }
    
//--------------- NATIVE FUNCTIONS ---------------
    
    
}

extension Navigine: NCLocationListener {
    func onLocationLoaded(_ location: NCLocation?) {
        // do smth with location
        print("onLocationLoaded()")

    }
    
    func onDownloadProgress(_ received: Int32, total: Int32) {
        // do smth with download progress
        print("onDownloadProgress()")
    }
    
    func onLocationFailed(_ error: Error?) {
        // do smth with error
        print("onLocationFailed()")
    }
}

extension Navigine: NCPositionListener {
    func onPositionUpdated(_ position: NCPosition) {
        // do smth with position
        print("onPositionUpdated()")
    }

    func onPositionError(_ error: Error?) {
        // do smth with error
        print("onPositionError()")
    }
}
