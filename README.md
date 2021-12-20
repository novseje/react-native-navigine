# react-native-navigine

React Native module for Navigine indoor positioning system

# This module continues to evolve as a paid product

You can contact me for more detailed information.


## Installation

`$ npm install github:novseje/react-native-navigine`

### Install for iOS

Edit your `Podfile` and add `pod 'Navigine'` to your target:

```ruby
target 'TargetName' do
pod 'Navigine'
end
```

Then, run the following command:

```bash
$ pod install
```

Join NavigineFramework to node module in XCode.

![alt text](/extra/add-framework.png?raw=true "Xcode")

Note: After every "pod install" command, you need to set this checkbox again.

### Install for Android

Download `libnavigine` lib directory from Navigine repositories folder.
https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0

Put this directory in `android` directory in your project directory.

Add code in android/settings.gradle file:
```
include ':libnavigine'
```

Add code in android/app/build.gradle file:
```
android {
    ...
    packagingOptions {
        pickFirst '**/*.so'
    }
}
```

Declare a broadcast receiver and job scheduler service for scanning BLE devices in Android versions >= 26.
Add this code to file android/app/src/main/AndroidManifest.xml
```xml
<application>
  ...
    <service
        android:name="com.reactnativenavigine.NotificationService"
        android:process=":remote"
        android:enabled="true"
        android:exported="false"
        android:stopWithTask="false"
        android:foregroundServiceType="location">
    </service>
</application>
```

And set permissions:
```
      <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
      <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
      <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
      <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
      <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
      <uses-permission android:name="android.permission.BLUETOOTH"/>
      <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
      <uses-permission android:name="android.permission.INTERNET"/>
      <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
      <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
      <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## Usage
```js
import NaviginePlugin from 'react-native-navigine';

NaviginePlugin.init("XXXX-XXXX-XXXX-XXXX", 99999, async (param) => {
    
    // Start work with Navigine

    let floorImage_base64 = await NaviginePlugin.getFloorImage();

    let floorImageSizes = await NaviginePlugin.getFloorImageSizes();

    let floorImageScale = await NaviginePlugin.getZoomScale();

    setInterval(this.curPositionUpdate, 1500);

    setInterval(this.didEnterZonesCheck, 2000);

    setInterval(this.getRoutePoints, 2000);
});

curPositionUpdate = async () => {
    let curPosition = await NaviginePlugin.getCurPosition();

    let curAzimuth = await NaviginePlugin.getAzimuth();
  }

didEnterZonesCheck = async () => {
    let name = await NaviginePlugin.getLastZoneName();
}

/**
 * Get array of (only first) route points
 * point object format is {x: 0.0, y: 0.0}
 * @returns array of all points [{x: 0.0, y: 0.0}]
 */
getRoutePoints = async () => {
    let points = await NaviginePlugin.getRoutePoints();
}

/**
 * Set destionation point to routing
 * @param x floor position in pixels
 * @param y floor position in pixels
 * @returns 'OK' if OK
 */
setRouteDestination = async (x, y) => {
    NaviginePlugin.setRouteDestination(x, y);
}
```
