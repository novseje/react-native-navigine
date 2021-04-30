import { NativeModules } from 'react-native';

const { Navigine } = NativeModules;

if (!Navigine) {
    throw new Error('Navigine module: NativeModule is null');
}

let callbackFn = function (data) {
    alert(data);
}

let locationDataPromise = new Promise((resolve, reject) => {
    Navigine.getLocationData((data) => {
        resolve(data);
    });
});

const NaviginePlugin = {
    init: function (callback) {
        Navigine.init(callback);
    },

    setApiKey: function (apiKey: string) {

    },

    getLocationData: async function () {
        let response = await locationDataPromise;
        return response;
    },

    getFloorImage: async function () {
        let getFloorImagePromise = new Promise((resolve, reject) => {
            Navigine.getFloorImage(function (data) {
                resolve(data);
            });
        });
        let response = await getFloorImagePromise;
        return response;
    },

    getCurPosition: async function () {
        let getCurPositionPromise = new Promise((resolve, reject) => {
            Navigine.getCurPosition(function (data) {
                resolve(data);
            });
        });
        let response = await getCurPositionPromise;
        let coords = response.split('|');
        return {x: parseFloat(coords[0]), y: parseFloat(coords[1]) };
    },

    getAzimuth: async function () {
        let getAzimuthPromise = new Promise((resolve, reject) => {
            Navigine.getAzimuth(function (data) {
                resolve(data);
            });
        });
        let response = await getAzimuthPromise;

        return parseFloat(response);
    },

    getFloorImageSizes: async function () {
        let getFloorImageSizesPromise = new Promise((resolve, reject) => {
            Navigine.getFloorImageSizes(function (data) {
                resolve(data);
            });
        });
        let response = await getFloorImageSizesPromise;
        let coords = response.split('|');
        return {x: parseFloat(coords[0]), y: parseFloat(coords[1]) };
    },

    getZoomScale: async function () {
        let getZoomScalePromise = new Promise((resolve, reject) => {
            Navigine.getZoomScale(function (data) {
                resolve(data);
            });
        });
        let response = await getZoomScalePromise;
        return parseFloat(response);
    },

    didEnterZones: async function () {
        let didEnterZonesPromise = new Promise((resolve, reject) => {
            Navigine.didEnterZones(function (data) {
                resolve(data);
            });
        });
        let response = await didEnterZonesPromise;
        console.log('didEnterZonesPromise: '+ response);
        if (response) {
            let name = response.toString();
            let zones = [{name: name}];
            return zones;
        } else {
            return null;
        }
    },

    // TEMPORARY
    getLastZoneName: async function () {
        let zones = await this.didEnterZones();
        console.log('getLastZoneName/zones: '+ zones);
        if (zones) {
            var last_element = zones[zones.length - 1];
            console.log('getLastZoneName/last_element: '+ last_element);
            console.log('getLastZoneName/last_element.name: '+ last_element.name);
            return last_element.name;
        } else {
            return false;
        }
    },

    getRoutePoints: async function () {
        let getRoutePointsPromise = new Promise((resolve, reject) => {
            Navigine.getRoutePoints(function (data) {
                resolve(data);
            });
        });
        let response = await getRoutePointsPromise;
        console.log('getRoutePoints: '+ response);
        if (response) {
            let json = JSON.parse(response);
            if (json) {
                return json;
            } else {
                return null
            }
        } else {
            return null;
        }
    },

    getFloorImage222: async function () {
        Navigine.getFloorImage(callbackFn);
    },

    test: async function () {
        Navigine.test(callbackFn);
    },
}

export default NaviginePlugin;
