import { NativeModules } from 'react-native';

const { Navigine } = NativeModules;

if (!Navigine) {
    throw new Error('Navigine module: NativeModule is null');
}

let callbackFn = function (data) {
    return data;
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

    getFloorImage222: async function () {
        Navigine.getFloorImage(callbackFn);
    },
}

export default NaviginePlugin;
