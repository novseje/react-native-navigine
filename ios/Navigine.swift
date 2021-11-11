import UIKit

@objc(Navigine)
class Navigine: NSObject {
    
    var mNavigineSdk: NCNavigineSdk?
    var mLocationManager: NCLocationManager?
    var mNavigationManager: NCNavigationManager?
    var mResourceManager: NCResourceManager?

    static var initCallback: RCTResponseSenderBlock? // initSDK() callback
    static var currentLocation: NCLocation?
    static var currentSublocation: NCSublocation?
    static var currentImage: NCImage?


    @objc(init:locationId:callback:)
    func initSDK(apiKey: String, locationId: Int32, callback:@escaping RCTResponseSenderBlock) -> Void {
        print("(Navigine.swift) initSDK()")
        
        Navigine.initCallback = callback
        
        NCNavigineSdk.setUserHash(apiKey)
        NCNavigineSdk.setServer("https://api.navigine.com")

        mNavigineSdk = NCNavigineSdk.getInstance()!

        mLocationManager = mNavigineSdk?.getLocationManager()!
        mLocationManager?.add(self)
        mLocationManager?.setLocation(Int32(locationId))

        mResourceManager = mNavigineSdk?.getResourceManager(mLocationManager)!
        
        mNavigationManager = mNavigineSdk?.getNavigationManager(mLocationManager)!
        mNavigationManager?.add(self)
        
        
        
    }

    @objc(getFloorImage:)
    func getFloorImage(callback:RCTResponseSenderBlock) -> Void {
        print("(Navigine.swift) getFloorImage()")
        
        let image = Navigine.currentImage
        let imgData = Data(image!.data).base64EncodedString()
        print(imgData)
        
        callback([imgData])
    }
    
    @objc(getFloorImageSizes:)
    func getFloorImageSizes(callback:RCTResponseSenderBlock) -> Void {
        print("(Navigine.swift) getFloorImageSizes()")
        
        let image = Navigine.currentImage
        let width: Int32 = image!.width
        let height: Int32 = image!.height
        
        let result: String = "\(width)|\(height)"
        print(result)
        callback([result])
    }

    @objc(getAzimuth:)
    func getAzimuth(callback:RCTResponseSenderBlock) -> Void {
        print("(Navigine.swift) getAzimuth()")
        
        let azimuth = Navigine.currentSublocation?.azimuth
        
        let result: String = "\(azimuth)"
        print(result)
        callback([result])
    }


//--------------- NATIVE FUNCTIONS ---------------
    
    
}

//--------------- LISTENERS ---------------

extension Navigine: NCLocationListener {
    func onLocationLoaded(_ location: NCLocation?) {
        print("NCLocationListener onLocationLoaded()")

        Navigine.currentLocation = location
        
        for sublocation in location!.sublocations {
            print(sublocation.id)
            Navigine.currentSublocation = sublocation
            break
        }
        
        //guard var imageId = currentSublocation?.imageId else { return }
        if (Navigine.currentSublocation != nil) {
            let imageId: String = Navigine.currentSublocation?.imageId ?? ""
            
            mResourceManager!.loadImage(imageId, listener: self)
        }
        
        
        
    }
    
    func onDownloadProgress(_ received: Int32, total: Int32) {
        // do smth with download progress
        print("NCLocationListener onDownloadProgress()")
    }
    
    func onLocationFailed(_ error: Error?) {
        // do smth with error
        print("NCLocationListener onLocationFailed()")
    }
}

extension Navigine: NCPositionListener {
    func onPositionUpdated(_ position: NCPosition) {
        // do smth with position
        print("NCPositionListener onPositionUpdated()")
    }

    func onPositionError(_ error: Error?) {
        // do smth with error
        print("NCPositionListener onPositionError()")
    }
}

extension Navigine: NCResourceListener {
    func onLoaded(_ imageId: String, image: NCImage?) {
        print("NCResourceListener onLoaded()")
        
        Navigine.currentImage = image
        
        Navigine.initCallback!(["OK"])
    }
    
    func onFailed(_ imageId: String, error: Error?) {
        print("NCResourceListener onFailed()")
    }
}


