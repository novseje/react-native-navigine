# react-native-navigine

## Getting started

`$ npm install react-native-navigine --save`

Join NavigineFramework to node module in XCode.

![alt text](/extra/add-framework.png?raw=true "Xcode")

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
