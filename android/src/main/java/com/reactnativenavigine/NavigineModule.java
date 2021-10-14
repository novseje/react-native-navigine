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

import java.util.ArrayList;


@ReactModule(name = NavigineModule.NAME)
public class NavigineModule extends ReactContextBaseJavaModule {
  public static final String NAME = "Navigine";

  private final ReactApplicationContext mContext;

      private static final String   TAG                     = "NAVIGINE.Demo";
      private static final String   NOTIFICATION_CHANNEL    = "NAVIGINE_DEMO_NOTIFICATION_CHANNEL";
      private static final int      UPDATE_TIMEOUT          = 100;  // milliseconds
      private static final int      ADJUST_TIMEOUT          = 5000; // milliseconds
      private static final int      ERROR_MESSAGE_TIMEOUT   = 5000; // milliseconds
      private static final boolean  ORIENTATION_ENABLED     = true; // Show device orientation?
      private static final boolean  NOTIFICATIONS_ENABLED   = true; // Show zone notifications?

      private View          mBackView                 = null;
      private TextView      mErrorMessageLabel        = null;
      private float         mDisplayDensity           = 0.0f;

      private boolean       mAdjustMode               = false;
      private long          mAdjustTime               = 0;

      // Location parameters
      private int           mCurrentSubLocationIndex  = -1;

      // Device parameters
      private RectF         mPinPointRect             = null;

      private RelativeLayout mDirectionLayout         = null;
      private TextView       mDirectionTextView       = null;
      private ImageView      mDirectionImageView      = null;

      private Bitmap  mVenueBitmap    = null;
      private RectF   mSelectedVenueRect = null;

      private boolean DEBUG_LOG = false;
      private String API_KEY;
      private String API_SERVER = "https://api.navigine.com"; // API server
      private int LOCATION_ID;
      private Callback       initCallback   = null;

      //private ArrayList<Zone> zonesCollect = new ArrayList<Zone>();


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

      final Activity activity = getCurrentActivity();

      if (NavigineApp.mNavigineSdk == null)
      {
        return;
      }

      NavigineApp.UserHash = apiKey;
      NavigineApp.Settings.edit();

      initNavigineSDK();

      mCurrentSubLocationIndex = 0;

      ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);


      initCallback = callback;
    }

    @ReactMethod
    public void getFloorImage(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getFloorImage()");

      callback.invoke("");
      return;
/*
      SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
      if (DEBUG_LOG) Log.d(TAG, "Image:" + subLoc.getImage());

      String encodedFile = new String(Base64.getEncoder().encode(subLoc.getImage().getData()));

      callback.invoke(encodedFile);
 */
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
    public void getFloorImageSizes(Callback callback) {
      if (DEBUG_LOG) Log.d(TAG, "getFloorImageSizes()");

      callback.invoke(100 + "|" + 200);
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
        Intent i = new Intent(this.mContext.getApplicationContext(), com.reactnativenavigine.NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          this.mContext.getApplicationContext().startForegroundService(i);
        } else {
          this.mContext.getApplicationContext().startService(i);
        }
      }

}
