import { NativeModules } from 'react-native';

const { Navigine } = NativeModules;

if (!Navigine) {
    throw new Error('Navigine module: NativeModule is null');
}

let callbackFn = function (data) {
    alert(data);
}

const NaviginePlugin = {
    init: function (apiKey, locationId, callback) {
        Navigine.init(apiKey, locationId, callback);
    },

    /**
     * Set Navigine API key (userHash)
     * @param apiKey
     */
    setApiKey: async function (apiKey) {
        let setApiKeyPromise = new Promise((resolve, reject) => {
            Navigine.setApiKey(apiKey, function (data) {
                resolve(data);
            });
        });
        let response = await setApiKeyPromise;
        return response;
    },

    /**
     * Get active floor image data encoded in Base64
     * @returns {String} Image data in Base64
     */
    getFloorImage: async function () {
        let getFloorImagePromise = new Promise((resolve, reject) => {
            Navigine.getFloorImage(function (data) {
                resolve(data);
            });
        });
        let response = await getFloorImagePromise;
        return response;
    },

    /**
     * Get current device position
     * @returns {Object} Positin in floor {x: 0.0, y: 0.0} in pixels
     */
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

    /**
     * Get Azimuth (device orientation)
     * @returns Azimuth value in radians
     */
    getAzimuth: async function () {
        let getAzimuthPromise = new Promise((resolve, reject) => {
            Navigine.getAzimuth(function (data) {
                resolve(data);
            });
        });
        let response = await getAzimuthPromise;

        return parseFloat(response);
    },

    /**
     * Get active floor image sizes (width and height) in pixels
     * @returns {Object} Image size {x: 0, y: 0}
     */
    getFloorImageSizes: async function () {
        let getFloorImageSizesPromise = new Promise((resolve, reject) => {
            Navigine.getFloorImageSizes(function (data) {
                resolve(data);
            });
        });
        let response = await getFloorImageSizesPromise;
        let coords = response.split('|');
        return {x: parseInt(coords[0]), y: parseInt(coords[1]) };
    },

    /**
     * Get floor image scale (relatively device width)
     * @returns {float} scale
     */
    getZoomScale: async function () {
        let getZoomScalePromise = new Promise((resolve, reject) => {
            Navigine.getZoomScale(function (data) {
                resolve(data);
            });
        });
        let response = await getZoomScalePromise;
        return parseFloat(response);
    },

    /**
     * Return array of all entered zones
     * @returns array of zones [{name: 'ZONE_NAME'}]
     */
    didEnterZones: async function () {
        let didEnterZonesPromise = new Promise((resolve, reject) => {
            Navigine.didEnterZones(function (data) {
                resolve(data);
            });
        });
        let response = await didEnterZonesPromise;
        if (response) {
            let name = response.toString();
            let zones = [{name: name}];
            return zones;
        } else {
            return null;
        }
    },

    /**
     * Return name of last entered zone
     * TEMPORARY FUNCTION
     * @returns {String} zone name
     */
    getLastZoneName: async function () {
        let zones = await this.didEnterZones();
        if (zones) {
            var last_element = zones[zones.length - 1];
            return last_element.name;
        } else {
            return false;
        }
    },

    /**
     * Get array of (only first) route points
     * point object format is {x: 0.0, y: 0.0}
     * @returns array of all points [{x: 0.0, y: 0.0}]
     */
    getRoutePoints: async function () {
        let getRoutePointsPromise = new Promise((resolve, reject) => {
            Navigine.getRoutePoints(function (data) {
                resolve(data);
            });
        });
        let response = await getRoutePointsPromise;
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

    /**
     * Set destionation point to routing
     * @param x floor position in pixels
     * @param y floor position in pixels
     * @returns 'OK' if OK
     */
    setRouteDestination: async function (x, y) {
        let setRouteDestinationPromise = new Promise((resolve, reject) => {
            Navigine.setRouteDestination(x, y, function (data) {
                resolve(data);
            });
        });
        let response = await setRouteDestinationPromise;
        return response;
    },

    getFloorImage222: async function () {
        Navigine.getFloorImage(callbackFn);
    },

    test: async function () {
        Navigine.test(callbackFn);
    },
}

export default NaviginePlugin;
