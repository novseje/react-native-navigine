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
import com.navigine.idl.java.LocationPoint;
import com.navigine.idl.java.Point;
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
import com.navigine.idl.java.RouteListener;
import com.navigine.idl.java.RoutePath;
import com.navigine.idl.java.ZoneManager;
import com.navigine.idl.java.ZoneListener;
import com.navigine.idl.java.Zone;
import com.navigine.idl.java.ResourceUploadListener;
import com.navigine.sdk.Navigine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public static ZoneManager         ZoneManager         = null;

    private static Location CurrentLocation = null;
    private static Sublocation CurrentSublocation = null;
    private static Image CurrentImage = null;
    private static Position CurrentPosition = null;
    private static ArrayList<RoutePath> CurrentRoutePaths = null;
    private static Zone LastZone = null;
    private static ArrayList<Zone> zonesCollect = new ArrayList<Zone>();

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

            NavigationManager.startLogRecording();

            MeasurementManager = mNavigineSdk.getMeasurementManager();
            RouteManager = mNavigineSdk.getRouteManager(LocationManager, NavigationManager);
            NotificationManager = mNavigineSdk.getNotificationManager(LocationManager);
            ZoneManager = mNavigineSdk.getZoneManager(LocationManager, NavigationManager);

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
                CurrentPosition = position;
              }

              @Override
              public void onPositionError(Error error) {
                Log.d("NavigineApp", "onPositionError(). Message: " + error.getMessage());
              }
            });

            RouteManager.addRouteListener(new RouteListener() {
              @Override
              public void onPathsUpdated(ArrayList<RoutePath> routePaths){
                Log.d("NavigineApp", "onPathsUpdated().");
                CurrentRoutePaths = routePaths;
              }
            });

            ZoneManager.addZoneListener(new ZoneListener() {
              @Override
              public void onEnterZone(Zone z){
                Log.d("NavigineApp", "onEnterZone()");
                ///zonesCollect.add(z);
                LastZone = z;
                Log.d("NavigineApp", "ZONE: " + z.getName());
              }
              @Override
              public void onLeaveZone(Zone z){
                Log.d("NavigineApp", "onLeaveZone()");
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

  public static float getAzimuth()
  {
    if (CurrentSublocation != null) {
      // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-Sublocation#function-getazimuth
      return CurrentSublocation.getAzimuth();
    }
    return 0;
  }

  public static Point getCurPosition()
  {
    if (CurrentPosition != null) {
      Log.d("NavigineApp", "getCurPosition(): " + String.valueOf(CurrentPosition.getPoint()));
      // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-Position#function-getpoint
      return CurrentPosition.getPoint();
    }
    return new Point(0,0);
  }

  public static float getCurSublocationWidth()
  {
    if (CurrentSublocation != null) {
      // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-Sublocation#function-getwidth
      return CurrentSublocation.getWidth();
    }
    return 0;
  }

  public static float getCurSublocationHeight()
  {
    if (CurrentSublocation != null) {
      // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-Sublocation#function-getheight
      return CurrentSublocation.getHeight();
    }
    return 0;
  }

  public static void setRouteDestination(float x, float y)
  {
    if (CurrentLocation != null && CurrentSublocation != null) {
      RouteManager.clearTargets();

      Point targetPoint = new Point(x, y);
      int currentLocationId = CurrentLocation.getId();
      int currentSublocationId = CurrentSublocation.getId();

      Log.d("NavigineApp", String.format(Locale.ENGLISH, "setRouteDestination: x:%.2f, y:%.2f ON %d / %d)", x, y, currentLocationId, currentSublocationId));
      LocationPoint targetLPoint = new LocationPoint(targetPoint, currentLocationId, currentSublocationId);
      // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-RouteManager#function-settarget
      RouteManager.setTarget(targetLPoint);
    }
  }

  public static ArrayList<Point> getFirstRoutePoints()
  {
    Log.d("NavigineApp", "getFirstRoutePoints()");

    ArrayList<Point> firstRoutePoints = new ArrayList<>();

    if (CurrentRoutePaths != null) {
      for (int j = 0; j < CurrentRoutePaths.size(); j++)
      {
        // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-RoutePath
        RoutePath CurrentRoutePath = CurrentRoutePaths.get(j);
        ArrayList<LocationPoint> routePoints = CurrentRoutePath.getPoints();
        Log.d("NavigineApp", "==DEBUG== RoutePath: " + String.valueOf(CurrentRoutePath.getPoints()));
        Log.d("NavigineApp", "==DEBUG== RouteLength: " + String.valueOf(CurrentRoutePath.getLength()));
        for (int r = 0; r < routePoints.size(); r++)
        {
          Point lPoint = routePoints.get(r).getPoint();
          // https://github.com/Navigine/Indoor-Navigation-Android-Mobile-SDK-2.0/wiki/Class-LocationPoint
          firstRoutePoints.add(lPoint);
          Log.d("NavigineApp", "==DEBUG== routePoint: " + String.valueOf(lPoint));
          Log.d("NavigineApp", String.format(Locale.ENGLISH, "==DEBUG== Location: %s, Sublocation: %s", routePoints.get(r).getLocationId(), routePoints.get(r).getSublocationId()));

        }
        break; // (!) only first route
      }
    }

    return firstRoutePoints;
  }

  private static Zone getLastZone()
  {
    Log.d("NavigineApp", "getLastZone()");
    return LastZone;
  }

  public static void clearZonesCollect()
  {
    Log.d("NavigineApp", "clearZonesCollect()");
    zonesCollect.clear();
  }

  public static int getLastZoneId()
  {
    Log.d("NavigineApp", "getLastZoneId()");

    Zone z = LastZone;
    if (z != null) {
      return z.getId();
    } else {
      return 0;
    }
  }

  public static String getLastZoneName()
  {
    Log.d("NavigineApp", "getLastZoneName()");

    Zone z = LastZone;
    if (z != null) {
      return z.getName();
    } else {
      return "";
    }
  }

  public static void uploadLogFile()
  {
    Log.d("NavigineApp", "uploadLogFile()");

    NavigationManager.stopLogRecording();

    List<String> mLogList = new ArrayList<>();
    mLogList.addAll(NavigineApp.ResourceManager.getLogsList());
    String item = mLogList.get(0);
    Log.d("NavigineApp", "===LOG===\n" + item);

    NavigineApp.ResourceManager.uploadLogFile(item, new ResourceUploadListener() {
      @Override
      public void onUploaded() {
        Log.d("NavigineApp", "Log uploaded");
      }

      @Override
      public void onFailed(Error error) {
        Log.d("NavigineApp", "Logfile uploading failed");
      }
    });
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
