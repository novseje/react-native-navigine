# react-native-navigine

React Native module for Navigine indoor positioning system

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
Declare a broadcast receiver and job scheduler service for scanning BLE devices in Android versions >= 26.
Add this code to file android/app/src/main/AndroidManifest.xml
```xml
<application>
  ...
    <service android:name="com.navigine.naviginesdk.NavigineJobService"
        android:permission="android.permission.BIND_JOB_SERVICE"
        android:exported="false"
        android:enabled="true"/>

    <receiver android:name="com.navigine.naviginesdk.BLEBroadcastReceiver"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED"/>
        </intent-filter>
    </receiver>
</application>
```

And set permissions:
```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
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
