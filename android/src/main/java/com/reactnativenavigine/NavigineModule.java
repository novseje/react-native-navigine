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

import android.Manifest;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.util.*;
import java.io.*;
import java.lang.*;
import java.util.Locale;
import android.icu.util.Calendar;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

    @ReactMethod
    public void getCurPosition(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getCurPosition()");

      callback.invoke(10 + "|" + 20);
    }

    @ReactMethod
    public void getAzimuth(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getAzimuth()");

      callback.invoke(0);
    }

    @ReactMethod
    public void didEnterZones(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "didEnterZones()");

      callback.invoke("");
    }

    @ReactMethod
    public void getRoutePoints(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getRoutePoints()");

      callback.invoke("[]");
    }

    @ReactMethod
    public void setRouteDestination(float x, float y, Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, String.format(Locale.ENGLISH, "setRouteDestination: x:%.2f, y:%.2f)", x, y));

      callback.invoke("OK");
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
