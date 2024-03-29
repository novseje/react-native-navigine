package com.reactnativenavigine;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.module.annotations.ReactModule;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactContext;
import com.navigine.idl.java.Point;

import android.Manifest;
import android.os.*;
import android.util.*;
import java.lang.*;
import java.util.Locale;
import android.app.Activity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import java.util.Base64;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import java.util.Objects;
import android.bluetooth.BluetoothAdapter;

import java.util.ArrayList;


@ReactModule(name = NavigineModule.NAME)
public class NavigineModule extends ReactContextBaseJavaModule {
    public static final String NAME = "Navigine";

    private final ReactApplicationContext mContext;

      private static final String   TAG                     = "NAVIGINE.Demo";
      private boolean DEBUG_LOG = true;
      private String API_KEY;
      private String API_SERVER = "https://api.navigine.com"; // API server
      private int LOCATION_ID;

      private Callback initCallback = null;

    public NavigineModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return "Navigine";
    }

    @ReactMethod
    public void init(String apiKey, int locationId, Callback callback) {
        API_KEY = apiKey;
        LOCATION_ID = locationId;

        initCallback = callback;

      final Activity activity = getCurrentActivity();

        ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);

      ///verifyBluetooth();

      String packageName = activity.getPackageName();
      PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
      if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivity(intent);
      }


      // Get a handler that can be used to post to the main thread
      Handler mainHandler = new Handler(mContext.getMainLooper());
      Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
          initNavigineSDK();
          NavigineApp.UserHash = apiKey;
          NavigineApp.LocationId = locationId;
          NavigineApp.initializeSdk(initCallback);
        }
      };
      mainHandler.post(myRunnable);

    }

    @ReactMethod
    public void getFloorImage(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getFloorImage()");

      byte[] image = NavigineApp.getMapImage();
      String encodedFile = new String(Base64.getEncoder().encode(image));
      callback.invoke(encodedFile);
    }

    @ReactMethod
    public void getFloorImageSizes(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getFloorImageSizes()");

      int width = NavigineApp.getMapImageWidth();
      int height = NavigineApp.getMapImageHeight();
      callback.invoke(width + "|" + height);
    }

    // Return current position in pixels
    @ReactMethod
    public void getCurPosition(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getCurPosition()");

      float posX = 0;
      float posY = 0;

      float mapWidth = NavigineApp.getCurSublocationWidth(); // location width in meters
      float mapHeight = NavigineApp.getCurSublocationHeight(); // location height in meters
      if (DEBUG_LOG) Log.d(TAG, "MapSize: : " + mapWidth + "/" + mapHeight);

      int imageWidth = NavigineApp.getMapImageWidth(); // map image width in pixels
      int imageHeight = NavigineApp.getMapImageHeight(); // map image height in pixels
      if (DEBUG_LOG) Log.d(TAG, "ImageSize: : " + imageWidth + "/" + imageHeight);

      if (mapWidth > 0 && mapHeight > 0 && imageWidth > 0 && imageHeight > 0) {
        Point point = NavigineApp.getCurPosition();

        float width1m = imageWidth / mapWidth; // pixels in 1 meter of width
        float height1m = imageHeight / mapHeight; // pixels in 1 meter of height
        if (DEBUG_LOG) Log.d(TAG, "pixelsIn1m: : " + width1m + "/" + height1m);

        posX = point.getX() * width1m;
        posY = imageHeight - (point.getY() * height1m);
      }

      callback.invoke( posX + "|" + posY);
      if (DEBUG_LOG) Log.d(TAG, "CurPosition: " + posX + "/" + posY);
    }

    // Return current azimuth in degrees
    @ReactMethod
    public void getAzimuth(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getAzimuth()");

      float azimuth = NavigineApp.getAzimuth();
      callback.invoke(azimuth);
      if (DEBUG_LOG) Log.d(TAG, "Azimuth: " + azimuth);
    }

    @ReactMethod
    public void getLastZoneId(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getLastZoneId()");

      int zoneId = NavigineApp.getLastZoneId();
      callback.invoke(zoneId);
    }

    @ReactMethod
    public void getLastZoneName(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getLastZoneName()");

      String zoneName = NavigineApp.getLastZoneName();
      callback.invoke(zoneName);
    }

    @ReactMethod
    public void getRoutePoints(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getRoutePoints()");

      ArrayList<Point> firstRoutePoints = NavigineApp.getFirstRoutePoints();

      int pointsSize = firstRoutePoints.size();
      if (pointsSize > 0) {
        float mapWidth = NavigineApp.getCurSublocationWidth(); // location width in meters
        float mapHeight = NavigineApp.getCurSublocationHeight(); // location height in meters
        if (DEBUG_LOG) Log.d(TAG, "MapSize: : " + mapWidth + "/" + mapHeight);

        int imageWidth = NavigineApp.getMapImageWidth(); // map image width in pixels
        int imageHeight = NavigineApp.getMapImageHeight(); // map image height in pixels
        if (DEBUG_LOG) Log.d(TAG, "ImageSize: : " + imageWidth + "/" + imageHeight);

        if (mapWidth > 0 && mapHeight > 0 && imageWidth > 0 && imageHeight > 0) {
          float width1m = imageWidth / mapWidth; // pixels in 1 meter of width
          float height1m = imageHeight / mapHeight; // pixels in 1 meter of height
          if (DEBUG_LOG) Log.d(TAG, "pixelsIn1m: : " + width1m + "/" + height1m);

          ArrayList<String> pointsArray = new ArrayList<String>();
          for (int j = 0; j < pointsSize; j++) {
            Point P = firstRoutePoints.get(j);
            pointsArray.add("{\"x\": " + String.valueOf(P.getX() * width1m) + ", \"y\": " + String.valueOf(imageHeight - P.getY() * height1m) + "}");
          }
          String pointsString = "[";
          for (String s : pointsArray) {
            pointsString += s + ", ";
          }
          pointsString = pointsString.replaceAll(",\\s*$", "");
          pointsString += "]";
          callback.invoke(pointsString);
          return;
        }
      }
      callback.invoke("[]");
    }

    @ReactMethod
    public void setRouteDestination(float x, float y, Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, String.format(Locale.ENGLISH, "setRouteDestination: x:%.2f, y:%.2f)", x, y));

      float mapWidth = NavigineApp.getCurSublocationWidth(); // location width in meters
      float mapHeight = NavigineApp.getCurSublocationHeight(); // location height in meters
      if (DEBUG_LOG) Log.d(TAG, "MapSize: : " + mapWidth + "/" + mapHeight);

      int imageWidth = NavigineApp.getMapImageWidth(); // map image width in pixels
      int imageHeight = NavigineApp.getMapImageHeight(); // map image height in pixels
      if (DEBUG_LOG) Log.d(TAG, "ImageSize: : " + imageWidth + "/" + imageHeight);

      if (mapWidth > 0 && mapHeight > 0 && imageWidth > 0 && imageHeight > 0) {
        float width1m = imageWidth / mapWidth; // pixels in 1 meter of width
        float height1m = imageHeight / mapHeight; // pixels in 1 meter of height
        if (DEBUG_LOG) Log.d(TAG, "pixelsIn1m: : " + width1m + "/" + height1m);

        if (DEBUG_LOG) Log.d(TAG, String.format(Locale.ENGLISH, "setRouteDestination: x:%.2fm, y:%.2fm)", x / width1m, mapHeight - y / height1m));
        NavigineApp.setRouteDestination(x / width1m, mapHeight - y / height1m);
      }

      callback.invoke("OK");
    }

    @ReactMethod
    public void uploadLogFile() {
      NavigineApp.uploadLogFile();
    }

    @ReactMethod
    public void getZoomScale(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getZoomScale()");
      callback.invoke("1");
    }


//-------------------- NATIVE FUNCTIONS --------------------

      private void initNavigineSDK() {
        if (DEBUG_LOG) Log.d(TAG, "initNavigineSDK()");

        NavigineApp.createInstance(this.mContext.getApplicationContext());
        Intent i = new Intent(this.mContext.getApplicationContext(), NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("NavigineSDK", "startForegroundService()>>");
          this.mContext.getApplicationContext().startForegroundService(i);
        } else {
            Log.d("NavigineSDK", "startService()>>");
          this.mContext.getApplicationContext().startService(i);
        }
      }

    private void verifyBluetooth() {
        try {
            ;
        }
        catch (RuntimeException e) {
            Log.d("NavigineSDK", "BLUETOOTH ERROR" + e.getMessage());
        }
    }

}
