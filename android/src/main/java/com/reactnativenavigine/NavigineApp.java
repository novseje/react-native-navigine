package com.reactnativenavigine;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.facebook.react.bridge.Callback;
import com.navigine.idl.java.Image;
import com.navigine.idl.java.LocationListManager;
import com.navigine.idl.java.LocationManager;
import com.navigine.idl.java.LocationListener;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.Sublocation;
import com.navigine.idl.java.MeasurementManager;
import com.navigine.idl.java.NavigationManager;
import com.navigine.idl.java.NotificationManager;
import com.navigine.idl.java.NavigineSdk;
import com.navigine.idl.java.Position;
import com.navigine.idl.java.PositionListener;
import com.navigine.idl.java.ResourceManager;
import com.navigine.idl.java.ResourceListener;
import com.navigine.idl.java.RouteManager;
import com.navigine.sdk.Navigine;

import java.util.ArrayList;

public class NavigineApp extends Application implements LifecycleObserver {
    private String TAG = "DEMO";

    public static final String      DEFAULT_SERVER_URL = "https://api.navigine.com";
    public static final String      DEFAULT_USER_HASH  = "0000-0000-0000-0000";

    @SuppressLint("StaticFieldLeak")
    public static Context     Context     = null;
    public static NavigineSdk mNavigineSdk = null;

    // Display settings
    public static float DisplayWidthPx  = 0.0f;
    public static float DisplayHeightPx = 0.0f;
    public static float DisplayWidthDp  = 0.0f;
    public static float DisplayHeightDp = 0.0f;
    public static float DisplayDensity  = 0.0f;

    public static String LocationServer = null;
    public static String UserHash = null;

    public static SharedPreferences Settings = null;

    // managers

    public static LocationListManager LocationListManager = null;
    public static LocationManager     LocationManager     = null;
    public static ResourceManager     ResourceManager     = null;
    public static NavigationManager   NavigationManager   = null;
    public static NotificationManager NotificationManager = null;
    public static MeasurementManager  MeasurementManager  = null;
    public static RouteManager        RouteManager        = null;

    public static Location CurrentLocation = null;
    public static Sublocation CurrentSublocation = null;
    public static Image CurrentImage = null;
    //private ArrayList<Zone> zonesCollect = new ArrayList<Zone>();

    public static int LocationId = 0;

    public synchronized static void createInstance(Context context)
    {
        Log.d("NavigineSDK", "createInstance()");
        Context = context;
        Navigine.initialize(Context);
        Navigine.setMode(Navigine.Mode.NORMAL);

        Settings = context.getSharedPreferences("Navigine", 0);
        LocationServer = Settings.getString ("location_server", DEFAULT_SERVER_URL);
        UserHash = Settings.getString ("user_hash", DEFAULT_USER_HASH);

        // Initializing display parameters
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        DisplayWidthPx  = displayMetrics.widthPixels;
        DisplayHeightPx = displayMetrics.heightPixels;
        DisplayDensity  = displayMetrics.density;
        DisplayWidthDp  = DisplayWidthPx  / DisplayDensity;
        DisplayHeightDp = DisplayHeightPx / DisplayDensity;
    }

    public synchronized static boolean initializeSdk(Callback callback)
    {
        Log.d("NavigineApp", "initializeSdk()");
        Log.d("NavigineApp", "UserHash: " + UserHash);

        Callback initCallback = callback;

        try {
            NavigineSdk.setUserHash(UserHash);
            NavigineSdk.setServer(LocationServer);
            NavigineApp.mNavigineSdk = NavigineSdk.getInstance();

            if (NavigineApp.mNavigineSdk == null)
            {
              Log.d("NavigineApp", "NavigineApp.mNavigineSdk is null ");
              return false;
            }

            LocationListManager = mNavigineSdk.getLocationListManager();
            LocationManager = mNavigineSdk.getLocationManager();
            ResourceManager = mNavigineSdk.getResourceManager(LocationManager);
            NavigationManager = mNavigineSdk.getNavigationManager(LocationManager);
            MeasurementManager = mNavigineSdk.getMeasurementManager();
            RouteManager = mNavigineSdk.getRouteManager(LocationManager, NavigationManager);
            NotificationManager = mNavigineSdk.getNotificationManager(LocationManager);

            LocationManager.addLocationListener(new LocationListener() {
              @Override
              public void onLocationLoaded(Location location) {
                Log.d("NavigineApp", "onLocationLoaded()");
                CurrentLocation = location;

                ArrayList<Sublocation> SublocationsList = CurrentLocation.getSublocations();

                for(int j = 0; j < SublocationsList.size(); j++)
                {
                  Log.d("==DEBUG==", "Sublocation: " + String.valueOf(SublocationsList.get(j).getName()));

                  //
                  // TODO: Get true current sublocation
                  //
                  CurrentSublocation = SublocationsList.get(j);
                  break;
                }

                if (CurrentSublocation != null) {
                  String imageId = CurrentSublocation.getImageId();

                  ResourceManager.loadImage(imageId, new ResourceListener() {
                    @Override
                    public void onLoaded(String imageId, Image image) {
                      Log.d("NavigineApp", "onLoaded()");
                      CurrentImage = image;

                      Log.d("NavigineApp", "init() callback");
                      initCallback.invoke("init()");
                    }
                    @Override
                    public void onFailed(String imageId, Error error) {
                      Log.d("NavigineApp", "onFailed(). Message:" + error.getMessage());
                    }
                  });
                }
              }

              @Override
              public void onDownloadProgress(int progress, int total) {
                Log.d("NavigineApp", "onDownloadProgress [ "+progress+"/"+total+" ]");
              }

              @Override
              public void onLocationFailed(Error error) {
                Log.d("NavigineApp", "onLocationFailed()");
              }
            });
            LocationManager.setLocationId(LocationId);

            NavigationManager.addPositionListener(new PositionListener() {
              @Override
              public void onPositionUpdated(Position position) {
                Log.d("NavigineApp", "onPositionUpdated()");
              }

              @Override
              public void onPositionError(Error error) {
                Log.d("NavigineApp", "onPositionError(). Message:" + error.getMessage());
              }
            });

        } catch (Exception e) {
            Log.d("NavigineApp", "ERROR: " + e.getMessage());
            return false;
        }
        Log.d("NavigineApp", "initializeSdk() ==END==");

        return true;
    }

  public static byte[] getMapImage()
  {
    if (CurrentImage != null) {
      // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-Image#function-getdata
      return CurrentImage.getData();
    }
    return new byte[0];
  }

  public static int getMapImageWidth()
  {
    if (CurrentImage != null) {
      // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-Image#function-getwidth
      return CurrentImage.getWidth();
    }
    return 0;
  }

  public static int getMapImageHeight()
  {
    if (CurrentImage != null) {
      // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-Image#function-getheight
      return CurrentImage.getHeight();
    }
    return 0;
  }

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Navigine.setMode(Navigine.Mode.NORMAL);
        Log.d(TAG, "Lifecycle.Event.ON_START onAppForegrounded!");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        Navigine.setMode(Navigine.Mode.NORMAL);
        Log.d(TAG, "Lifecycle.Event.ON_RESUME");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Navigine.setMode(Navigine.Mode.BACKGROUND);
        Log.d(TAG, "Lifecycle.Event.ON_PAUSE");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Navigine.setMode(Navigine.Mode.BACKGROUND);
        Log.d(TAG, "Lifecycle.Event.ON_STOP onAppBackgrounded!");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        Navigine.setMode(Navigine.Mode.BACKGROUND);
        Log.d(TAG, "Lifecycle.Event.ON_DESTROY");
    }
}
