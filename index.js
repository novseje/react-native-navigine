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

    getFloorImage222: async function () {
        Navigine.getFloorImage(callbackFn);
    },
}

export default NaviginePlugin;
