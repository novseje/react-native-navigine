# react-native-navigine

## Getting started

`$ npm install react-native-navigine --save`

### Install for iOS

Join NavigineFramework to node module in XCode.

![alt text](/extra/add-framework.png?raw=true "Xcode")

Note: After every "pod install" command, you need to set this checkbox again.

### Install for Android
Declare a broadcast receiver and job scheduler service for scanning BLE devices in Android versions >= 26.
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

## Usage
```javascript
import NaviginePlugin from 'react-native-navigine';

NaviginePlugin.init(async (param) => {
    // Start work with Navigine

    let floorImage_base64 = await NaviginePlugin.getFloorImage();

    let floorImageSizes = await NaviginePlugin.getFloorImageSizes();

    let floorImageScale = await NaviginePlugin.getZoomScale();

    setInterval(this.curPositionUpdate, 2000);
});

curPositionUpdate = async () => {
    let curPosition = await NaviginePlugin.getCurPosition();
    if (this.state.floorImageSizes.x > 0 && this.state.floorImageSizes.y > 0) {
      let relPosition = {
        x: curPosition.x / this.state.floorImageSizes.x,
        y: curPosition.y / this.state.floorImageSizes.y,
      }
      this.setState({relPosition: relPosition});
    }

  }
```
