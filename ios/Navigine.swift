import UIKit

@objc(Navigine)
class Navigine: NSObject {
    
    var mNavigineSdk: NCNavigineSdk?
    var mLocationManager: NCLocationManager?
    var mNavigationManager: NCNavigationManager?

    var currentLocation: NCLocation?
    var currentSublocation: NCSublocation?


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

    @objc(getFloorImage:)
    func getFloorImage(callback:RCTResponseSenderBlock) -> Void {
        
        
        
        callback(["OK"])
    }


//--------------- NATIVE FUNCTIONS ---------------
    
    
}

extension Navigine: NCLocationListener {
    func onLocationLoaded(_ location: NCLocation?) {
        print("onLocationLoaded()")

        currentLocation = location
        
        for sublocation in location!.sublocations {
            print(sublocation.id)
            currentSublocation = sublocation
            break
        }
        
        var imageId = currentSublocation?.imageId
        
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

extension Navigine: NCResourceListener {
    func onLoaded(_ imageId: String, image: NCImage?) {
        print("onLoaded()")
    }
    
    func onFailed(_ imageId: String, error: Error?) {
        print("onFailed()")
    }
}


