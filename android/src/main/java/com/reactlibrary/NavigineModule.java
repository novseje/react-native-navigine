package com.reactlibrary;

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

import com.navigine.naviginesdk.*;



public class NavigineModule extends ReactContextBaseJavaModule {
  private final ReactApplicationContext mContext;

      private static final String   TAG                     = "NAVIGINE.Demo";
      private static final String   NOTIFICATION_CHANNEL    = "NAVIGINE_DEMO_NOTIFICATION_CHANNEL";
      private static final int      UPDATE_TIMEOUT          = 100;  // milliseconds
      private static final int      ADJUST_TIMEOUT          = 5000; // milliseconds
      private static final int      ERROR_MESSAGE_TIMEOUT   = 5000; // milliseconds
      private static final boolean  ORIENTATION_ENABLED     = true; // Show device orientation?
      private static final boolean  NOTIFICATIONS_ENABLED   = true; // Show zone notifications?

      // NavigationThread instance
      private NavigationThread mNavigation            = null;

      // UI Parameters
      private LocationView  mLocationView             = null;
      private Button        mPrevFloorButton          = null;
      private Button        mNextFloorButton          = null;
      private View          mBackView                 = null;
      private View          mPrevFloorView            = null;
      private View          mNextFloorView            = null;
      private View          mZoomInView               = null;
      private View          mZoomOutView              = null;
      private View          mAdjustModeView           = null;
      private TextView      mCurrentFloorLabel        = null;
      private TextView      mErrorMessageLabel        = null;
      private float         mDisplayDensity           = 0.0f;

      private boolean       mAdjustMode               = false;
      private long          mAdjustTime               = 0;

      // Location parameters
      private Location      mLocation                 = null;
      private int           mCurrentSubLocationIndex  = -1;

      // Device parameters
      private DeviceInfo    mDeviceInfo               = null; // Current device
      private LocationPoint mPinPoint                 = null; // Potential device target
      private LocationPoint mTargetPoint              = null; // Current device target
      private RectF         mPinPointRect             = null;

      private RelativeLayout mDirectionLayout         = null;
      private TextView       mDirectionTextView       = null;
      private ImageView      mDirectionImageView      = null;

      private Bitmap  mVenueBitmap    = null;
      private Venue   mTargetVenue    = null;
      private Venue   mSelectedVenue  = null;
      private RectF   mSelectedVenueRect = null;
      private Zone    mSelectedZone   = null;

      private Callback       initCallback   = null;


    public NavigineModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
    }

    @Override
    public String getName() {
        return "Navigine";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void init(Callback callback) {
      final Activity activity = getCurrentActivity();

      // Setting up NavigineSDK parameters
      NavigineSDK.setParameter(mContext, "debug_level", 2);
      NavigineSDK.setParameter(mContext, "actions_updates_enabled",  false);
      NavigineSDK.setParameter(mContext, "location_updates_enabled", true);
      NavigineSDK.setParameter(mContext, "location_loader_timeout",  60);
      NavigineSDK.setParameter(mContext, "location_update_timeout",  300);
      NavigineSDK.setParameter(mContext, "location_retry_timeout",   300);
      NavigineSDK.setParameter(mContext, "post_beacons_enabled",     true);
      NavigineSDK.setParameter(mContext, "post_messages_enabled",    true);

      // Initializing location view
    //  LocationView mLocationView = new LocationView(mContext);

      mCurrentSubLocationIndex = 0;

      ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);


      /*
      if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions( //Method of Fragment
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                101
        );
      } else {
        Log.d(TAG, "Permissions already granted");
      }
      */

      initCallback = callback;

      Log.d(TAG, "NavigineSDK.initialize | START");
      if (NavigineSDK.initialize(mContext, "D536-A0D5-4BEE-25CE", "https://api.navigine.com"))
      {
        Log.d(TAG, "NavigineSDK.initialize | OK");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
               @Override
               public void run() {
                 boolean isLoaded = NavigineSDK.loadLocationInBackground(60019, 30,
                         new Location.LoadListener()
                         {
                           @Override public void onFinished()
                           {
                             Log.d(TAG, "onFinished");

                             mNavigation = NavigineSDK.getNavigation();
                             Log.d(TAG, "mNavigation = " + mNavigation.toString());

                             // Setting up device listener
                             if (mNavigation != null)
                             {
                               mNavigation.setDeviceListener
                               (
                                       new DeviceInfo.Listener()
                                       {
                                         @Override public void onUpdate(DeviceInfo info) { handleDeviceUpdate(info); }
                                       }
                               );
                             }

                             loadMap();

                             Log.d(TAG, "init() callback");
                             initCallback.invoke("init()");

                           }
                           @Override public void onFailed(int error)
                           {
                             Log.d(TAG, "Error downloading location 'Navigine Demo' (error " + error + ")! " +
                                     "Please, try again later or contact technical support");
                           }
                           @Override public void onUpdate(int progress)
                           {
                             Log.d(TAG, "Downloading location: " + progress + "%");
                           }
                         });
                 Log.d(TAG, "NavigineSDK.loadLocationInBackground() isLoaded? = " + isLoaded);
               }
        });


      }

      Log.d(TAG, "init() END");
    }

    @ReactMethod
    public void getLocationData(Callback callback) {
      Log.d(TAG, "getLocationData()");
      callback.invoke("getLocationData()");
    }

    @ReactMethod
    public void getFloorImage(Callback callback) {
      Log.d(TAG, "getFloorImage()");

      SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
      Log.d(TAG, "Image:" + subLoc.getImage());

      String encodedFile = new String(Base64.getEncoder().encode(subLoc.getImage().getData()));

      callback.invoke(encodedFile);
    }

    @ReactMethod
    public void getCurPosition(Callback callback) {
      Log.d(TAG, "getCurPosition()");
      callback.invoke("0|0");
    }

    @ReactMethod
    public void getFloorImageSizes(Callback callback) {
      Log.d(TAG, "getFloorImageSizes()");

      SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
      Log.d(TAG, String.format(Locale.ENGLISH, "Loading sublocation %s (%.2f x %.2f)", subLoc.getName(), subLoc.getWidth(), subLoc.getHeight()));

      callback.invoke(subLoc.getWidth() + "|" + subLoc.getHeight());
    }

    @ReactMethod
    public void getZoomScale(Callback callback) {
      Log.d(TAG, "getZoomScale()");
      callback.invoke("1");
    }

      protected void RENAMEDonCreate(Bundle savedInstanceState)
      {
         /*
              drawZones(canvas);
              drawPoints(canvas);
              drawVenues(canvas);
              drawDevice(canvas);
         */

        mNavigation = NavigineSDK.getNavigation();

        loadMap();

        // Setting up device listener
        if (mNavigation != null)
        {
          mNavigation.setDeviceListener
          (
            new DeviceInfo.Listener()
            {
              @Override public void onUpdate(DeviceInfo info) { handleDeviceUpdate(info); }
            }
          );
        }

        // Setting up zone listener
        if (mNavigation != null)
        {
          mNavigation.setZoneListener
          (
            new Zone.Listener()
            {
              @Override public void onEnterZone(Zone z) { handleEnterZone(z); }
              @Override public void onLeaveZone(Zone z) { handleLeaveZone(z); }
            }
          );
        }
      }

      public void RENAMEDonDestroy()
      {
        if (mNavigation != null)
        {
          NavigineSDK.finish();
          mNavigation = null;
        }
      }

      public void onBackPressed()
      {

      }

      public void toggleAdjustMode(View v)
      {

      }

      public void onNextFloor(View v)
      {
        if (loadNextSubLocation())
          mAdjustTime = System.currentTimeMillis() + ADJUST_TIMEOUT;
      }

      public void onPrevFloor(View v)
      {
        if (loadPrevSubLocation())
          mAdjustTime = System.currentTimeMillis() + ADJUST_TIMEOUT;
      }

      public void onZoomIn(View v)
      {
        mLocationView.zoomBy(1.25f);
      }

      public void onZoomOut(View v)
      {
        mLocationView.zoomBy(0.8f);
      }

      public void onMakeRoute(View v)
      {
        if (mNavigation == null)
          return;

        if (mPinPoint == null)
          return;

        mTargetPoint  = mPinPoint;
        mTargetVenue  = null;
        mPinPoint     = null;
        mPinPointRect = null;

        mNavigation.setTarget(mTargetPoint);
        mBackView.setVisibility(View.VISIBLE);
        mLocationView.redraw();
      }

      public void onCancelRoute(View v)
      {
        if (mNavigation == null)
          return;

        mTargetPoint  = null;
        mTargetVenue  = null;
        mPinPoint     = null;
        mPinPointRect = null;

        mNavigation.cancelTargets();
        mLocationView.redraw();
      }

      private void handleClick(float x, float y)
      {
        Log.d(TAG, String.format(Locale.ENGLISH, "Click at (%.2f, %.2f)", x, y));

        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
        if (subLoc == null)
          return;

        if (mPinPoint != null)
        {
          if (mPinPointRect != null && mPinPointRect.contains(x, y))
          {
            mTargetPoint  = mPinPoint;
            mTargetVenue  = null;
            mPinPoint     = null;
            mPinPointRect = null;
            mNavigation.setTarget(mTargetPoint);
            mBackView.setVisibility(View.VISIBLE);
            return;
          }
          cancelPin();
          return;
        }

        if (mSelectedVenue != null)
        {
          if (mSelectedVenueRect != null && mSelectedVenueRect.contains(x, y))
          {
            mTargetVenue = mSelectedVenue;
            mTargetPoint = null;
            mNavigation.setTarget(new LocationPoint(mLocation.getId(), subLoc.getId(), mTargetVenue.getX(), mTargetVenue.getY()));
            mBackView.setVisibility(View.VISIBLE);
          }
          cancelVenue();
          return;
        }

        // Check if we touched venue
        mSelectedVenue = getVenueAt(x, y);
        mSelectedVenueRect = new RectF();

        // Check if we touched zone
        if (mSelectedVenue == null)
        {
          Zone Z = getZoneAt(x, y);
          if (Z != null)
            mSelectedZone = (mSelectedZone == Z) ? null : Z;
        }

        mLocationView.redraw();
      }

      private void handleLongClick(float x, float y)
      {
        Log.d(TAG, String.format(Locale.ENGLISH, "Long click at (%.2f, %.2f)", x, y));
        makePin(mLocationView.getAbsCoordinates(x, y));
        cancelVenue();
      }

      private void handleScroll(float x, float y, boolean byTouchEvent)
      {
        if (byTouchEvent)
          mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
      }

      private void handleZoom(float ratio, boolean byTouchEvent)
      {
        if (byTouchEvent)
          mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
      }

      private void handleEnterZone(Zone z)
      {
        Log.d(TAG, "Enter zone " + z.getName());
      }

      private void handleLeaveZone(Zone z)
      {
        Log.d(TAG, "Leave zone " + z.getName());
        if (NOTIFICATIONS_ENABLED)
        {

        }
      }

      private void handleDeviceUpdate(DeviceInfo deviceInfo)
      {
        Log.d(TAG, "handleDeviceUpdate()");

        mDeviceInfo = deviceInfo;
        if (mDeviceInfo == null)
          return;

        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        if (mDeviceInfo.isValid() && mDeviceInfo.getPaths().size() != 0) {
          if (mDeviceInfo.getPaths().get(0).getEvents().size() >= 1)
            showDirections(mDeviceInfo.getPaths().get(0));
        }
        else{
        //mDirectionLayout.setVisibility(GONE);
        }


        if (mDeviceInfo.isValid())
        {
          if (mAdjustMode)
            adjustDevice();
        }
        else
        {

          switch (mDeviceInfo.getErrorCode())
          {
            case 4:
              Log.d(TAG, "You are out of navigation zone! Please, check that your bluetooth is enabled!");
              break;

            case 8:
            case 30:
              Log.d(TAG, "Not enough beacons on the location! Please, add more beacons!");
              break;

            default:
              Log.d(TAG, String.format(Locale.ENGLISH,
                              "Something is wrong with location '%s' (error code %d)! " +
                              "Please, contact technical support!",
                              mLocation.getName(), mDeviceInfo.getErrorCode()));
              break;
          }
        }

        // This causes map redrawing
       // mLocationView.redraw();
      }

      private void setErrorMessage(String message)
      {
        mErrorMessageLabel.setText(message);
        mErrorMessageLabel.setVisibility(View.VISIBLE);
      }

      private void cancelErrorMessage()
      {
        mErrorMessageLabel.setVisibility(View.GONE);
      }

      private boolean loadMap()
      {
        if (mNavigation == null)
        {
          Log.e(TAG, "Can't load map! Navigine SDK is not available!");
          return false;
        }

        mLocation = mNavigation.getLocation();
        mCurrentSubLocationIndex = -1;

        if (mLocation == null)
        {
          Log.e(TAG, "Loading map failed: no location");
          return false;
        }

        if (mLocation.getSubLocations().size() == 0)
        {
          Log.e(TAG, "Loading map failed: no sublocations");
          mLocation = null;
          return false;
        }

        if (!loadSubLocation(0))
        {
          Log.e(TAG, "Loading map failed: unable to load default sublocation");
          mLocation = null;
          return false;
        }

        mNavigation.setMode(NavigationThread.MODE_NORMAL);

        mNavigation.setLogFile(getLogFile("log"));

      //  mLocationView.redraw();
        return true;
      }

      private boolean loadSubLocation(int index)
      {
        if (mNavigation == null)
          return false;

        if (mLocation == null || index < 0 || index >= mLocation.getSubLocations().size())
          return false;

        SubLocation subLoc = mLocation.getSubLocations().get(index);
        Log.d(TAG, String.format(Locale.ENGLISH, "Loading sublocation %s (%.2f x %.2f)", subLoc.getName(), subLoc.getWidth(), subLoc.getHeight()));

        if (subLoc.getWidth() < 1.0f || subLoc.getHeight() < 1.0f)
        {
          Log.e(TAG, String.format(Locale.ENGLISH, "Loading sublocation failed: invalid size: %.2f x %.2f", subLoc.getWidth(), subLoc.getHeight()));
          return false;
        }
/*
        if (!mLocationView.loadSubLocation(subLoc))
        {
          Log.e(TAG, "Loading sublocation failed: invalid image");
          return false;
        }

        float viewWidth  = mLocationView.getWidth();
        float viewHeight = mLocationView.getHeight();
        float minZoomFactor = Math.min(viewWidth / subLoc.getWidth(), viewHeight / subLoc.getHeight());
        float maxZoomFactor = LocationView.ZOOM_FACTOR_MAX;
        mLocationView.setZoomRange(minZoomFactor, maxZoomFactor);
        mLocationView.setZoomFactor(minZoomFactor);
        Log.d(TAG, String.format(Locale.ENGLISH, "View size: %.1f x %.1f", viewWidth, viewHeight));
*/
        mAdjustTime = 0;
        mCurrentSubLocationIndex = index;

        cancelVenue();
      //  mLocationView.redraw();
        return true;
      }

      private boolean loadNextSubLocation()
      {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return false;
        return loadSubLocation(mCurrentSubLocationIndex + 1);
      }

      private boolean loadPrevSubLocation()
      {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return false;
        return loadSubLocation(mCurrentSubLocationIndex - 1);
      }

      private void makePin(PointF P)
      {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
        if (subLoc == null)
          return;

        if (P.x < 0.0f || P.x > subLoc.getWidth() ||
            P.y < 0.0f || P.y > subLoc.getHeight())
        {
          // Missing the map
          return;
        }

        if (mTargetPoint != null || mTargetVenue != null)
          return;

        if (mDeviceInfo == null || !mDeviceInfo.isValid())
          return;

        mPinPoint = new LocationPoint(mLocation.getId(), subLoc.getId(), P.x, P.y);
        mPinPointRect = new RectF();
        mLocationView.redraw();
      }

      private void cancelPin()
      {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
        if (subLoc == null)
          return;

        if (mTargetPoint != null || mTargetVenue != null || mPinPoint == null)
          return;

        mPinPoint = null;
        mPinPointRect = null;
        mLocationView.redraw();
      }

      private void cancelVenue()
      {
        mSelectedVenue = null;
    //    mLocationView.redraw();
      }

      private Venue getVenueAt(float x, float y)
      {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return null;

        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
        if (subLoc == null)
          return null;

        Venue v0 = null;
        float d0 = 1000.0f;

        for(int i = 0; i < subLoc.getVenues().size(); ++i)
        {
          Venue v = subLoc.getVenues().get(i);
          PointF P = mLocationView.getScreenCoordinates(v.getX(), v.getY());
          float d = Math.abs(x - P.x) + Math.abs(y - P.y);
          if (d < 30.0f * mDisplayDensity && d < d0)
          {
            v0 = v;
            d0 = d;
          }
        }

        return v0;
      }

      private Zone getZoneAt(float x, float y)
      {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return null;

        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
        if (subLoc == null)
          return null;

        PointF P = mLocationView.getAbsCoordinates(x, y);
        LocationPoint LP = new LocationPoint(mLocation.getId(), subLoc.getId(), P.x, P.y);

        for(int i = 0; i < subLoc.getZones().size(); ++i)
        {
          Zone Z = subLoc.getZones().get(i);
          if (Z.contains(LP))
            return Z;
        }
        return null;
      }

      private void drawPoints(Canvas canvas)
      {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);

        if (subLoc == null)
          return;

        final int solidColor  = Color.argb(255, 64, 163, 205);  // Light-blue color
        final int circleColor = Color.argb(127, 64, 163, 205);  // Semi-transparent light-blue color
        final int arrowColor  = Color.argb(255, 255, 255, 255); // White color
        final float dp        = mDisplayDensity;
        final float textSize  = 16 * dp;

        // Preparing paints
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Drawing pin point (if it exists and belongs to the current sublocation)
        if (mPinPoint != null && mPinPoint.subLocation == subLoc.getId())
        {
          final PointF T = mLocationView.getScreenCoordinates(mPinPoint);
          final float tRadius = 10 * dp;

          paint.setARGB(255, 0, 0, 0);
          paint.setStrokeWidth(4 * dp);
          canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

          paint.setColor(solidColor);
          paint.setStrokeWidth(0);
          canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);

          final String text = "Make route";
          final float textWidth = paint.measureText(text);
          final float h  = 50 * dp;
          final float w  = Math.max(120 * dp, textWidth + h/2);
          final float x0 = T.x;
          final float y0 = T.y - 75 * dp;

          mPinPointRect.set(x0 - w/2, y0 - h/2, x0 + w/2, y0 + h/2);

          paint.setColor(solidColor);
          canvas.drawRoundRect(mPinPointRect, h/2, h/2, paint);

          paint.setARGB(255, 255, 255, 255);
          canvas.drawText(text, x0 - textWidth/2, y0 + textSize/4, paint);
        }

        // Drawing target point (if it exists and belongs to the current sublocation)
        if (mTargetPoint != null && mTargetPoint.subLocation == subLoc.getId())
        {
          final PointF T = mLocationView.getScreenCoordinates(mTargetPoint);
          final float tRadius = 10 * dp;

          paint.setARGB(255, 0, 0, 0);
          paint.setStrokeWidth(4 * dp);
          canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

          paint.setColor(solidColor);
          canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);
        }
      }

      private void drawVenues(Canvas canvas)
      {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);

        final float dp = mDisplayDensity;
        final float textSize  = 16 * dp;
        final float venueSize = 30 * dp;
        final int   venueColor = Color.argb(255, 0xCD, 0x88, 0x50); // Venue color

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(0);
        paint.setColor(venueColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        for(int i = 0; i < subLoc.getVenues().size(); ++i)
        {
          Venue v = subLoc.getVenues().get(i);
          if (v.getSubLocationId() != subLoc.getId())
            continue;

          final PointF P = mLocationView.getScreenCoordinates(v.getX(), v.getY());
          final float x0 = P.x - venueSize/2;
          final float y0 = P.y - venueSize/2;
          final float x1 = P.x + venueSize/2;
          final float y1 = P.y + venueSize/2;
          canvas.drawBitmap(mVenueBitmap, null, new RectF(x0, y0, x1, y1), paint);
        }

        if (mSelectedVenue != null)
        {
          final PointF T = mLocationView.getScreenCoordinates(mSelectedVenue.getX(), mSelectedVenue.getY());
          final float textWidth = paint.measureText(mSelectedVenue.getName());

          final float h  = 50 * dp;
          final float w  = Math.max(120 * dp, textWidth + h/2);
          final float x0 = T.x;
          final float y0 = T.y - 50 * dp;
          mSelectedVenueRect.set(x0 - w/2, y0 - h/2, x0 + w/2, y0 + h/2);

          paint.setColor(venueColor);
          canvas.drawRoundRect(mSelectedVenueRect, h/2, h/2, paint);

          paint.setARGB(255, 255, 255, 255);
          canvas.drawText(mSelectedVenue.getName(), x0 - textWidth/2, y0 + textSize/4, paint);
        }
      }

      private void drawZones(Canvas canvas)
      {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
        if (subLoc == null)
          return;

        // Preparing paints
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        for(int i = 0; i < subLoc.getZones().size(); ++i)
        {
          Zone Z = subLoc.getZones().get(i);
          if (Z.getPoints().size() < 3)
            continue;

          boolean selected = (Z == mSelectedZone);

          Path path = new Path();
          final LocationPoint P0 = Z.getPoints().get(0);
          final PointF        Q0 = mLocationView.getScreenCoordinates(P0);
          path.moveTo(Q0.x, Q0.y);

          for(int j = 0; j < Z.getPoints().size(); ++j)
          {
            final LocationPoint P = Z.getPoints().get((j + 1) % Z.getPoints().size());
            final PointF        Q = mLocationView.getScreenCoordinates(P);
            path.lineTo(Q.x, Q.y);
          }

          int zoneColor = Color.parseColor(Z.getColor());
          int red       = (zoneColor >> 16) & 0xff;
          int green     = (zoneColor >> 8 ) & 0xff;
          int blue      = (zoneColor >> 0 ) & 0xff;
          paint.setColor(Color.argb(selected ? 200 : 100, red, green, blue));
          canvas.drawPath(path, paint);
        }
      }

      private void drawDevice(Canvas canvas)
      {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        // Check if navigation is available
        if (mDeviceInfo == null || !mDeviceInfo.isValid())
          return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);

        if (subLoc == null)
          return;

        final int solidColor  = Color.argb(255, 64,  163, 205); // Light-blue color
        final int circleColor = Color.argb(127, 64,  163, 205); // Semi-transparent light-blue color
        final int arrowColor  = Color.argb(255, 255, 255, 255); // White color
        final float dp = mDisplayDensity;

        // Preparing paints
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        /// Drawing device path (if it exists)
        if (mDeviceInfo.getPaths() != null && mDeviceInfo.getPaths().size() > 0)
        {
          RoutePath path = mDeviceInfo.getPaths().get(0);
          if (path.getPoints().size() >= 2)
          {
            paint.setColor(solidColor);

            for(int j = 1; j < path.getPoints().size(); ++j)
            {
              LocationPoint P = path.getPoints().get(j-1);
              LocationPoint Q = path.getPoints().get(j);
              if (P.subLocation == subLoc.getId() && Q.subLocation == subLoc.getId())
              {
                paint.setStrokeWidth(3 * dp);
                PointF P1 = mLocationView.getScreenCoordinates(P);
                PointF Q1 = mLocationView.getScreenCoordinates(Q);
                canvas.drawLine(P1.x, P1.y, Q1.x, Q1.y, paint);
              }
            }
          }
        }

        paint.setStrokeCap(Paint.Cap.BUTT);

        // Check if device belongs to the current sublocation
        if (mDeviceInfo.getSubLocationId() != subLoc.getId())
          return;

        final float x  = mDeviceInfo.getX();
        final float y  = mDeviceInfo.getY();
        final float r  = mDeviceInfo.getR();
        final float angle = mDeviceInfo.getAzimuth();
        final float sinA = (float)Math.sin(angle);
        final float cosA = (float)Math.cos(angle);
        final float radius  = mLocationView.getScreenLengthX(r);  // External radius: navigation-determined, transparent
        final float radius1 = 25 * dp;                            // Internal radius: fixed, solid

        PointF O = mLocationView.getScreenCoordinates(x, y);
        PointF P = new PointF(O.x - radius1 * sinA * 0.22f, O.y + radius1 * cosA * 0.22f);
        PointF Q = new PointF(O.x + radius1 * sinA * 0.55f, O.y - radius1 * cosA * 0.55f);
        PointF R = new PointF(O.x + radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y + radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);
        PointF S = new PointF(O.x - radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y - radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);

        // Drawing transparent circle
        paint.setStrokeWidth(0);
        paint.setColor(circleColor);
        canvas.drawCircle(O.x, O.y, radius, paint);

        // Drawing solid circle
        paint.setColor(solidColor);
        canvas.drawCircle(O.x, O.y, radius1, paint);

        if (ORIENTATION_ENABLED)
        {
          // Drawing arrow
          paint.setColor(arrowColor);
          Path path = new Path();
          path.moveTo(Q.x, Q.y);
          path.lineTo(R.x, R.y);
          path.lineTo(P.x, P.y);
          path.lineTo(S.x, S.y);
          path.lineTo(Q.x, Q.y);
          canvas.drawPath(path, paint);
        }
      }

      private void adjustDevice()
      {
        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        // Check if navigation is available
        if (mDeviceInfo == null || !mDeviceInfo.isValid())
          return;

        long timeNow = System.currentTimeMillis();

        // Adjust map, if necessary
        if (timeNow >= mAdjustTime)
        {
          // Firstly, set the correct sublocation
          SubLocation subLoc = mLocation.getSubLocations().get(mCurrentSubLocationIndex);
          if (mDeviceInfo.getSubLocationId() != subLoc.getId())
          {
            for(int i = 0; i < mLocation.getSubLocations().size(); ++i)
              if (mLocation.getSubLocations().get(i).getId() == mDeviceInfo.getSubLocationId())
                loadSubLocation(i);
          }

          // Secondly, adjust device to the center of the screen
          PointF center = mLocationView.getScreenCoordinates(mDeviceInfo.getX(), mDeviceInfo.getY());
          float deltaX  = mLocationView.getWidth()  / 2 - center.x;
          float deltaY  = mLocationView.getHeight() / 2 - center.y;
          mAdjustTime   = timeNow;
          mLocationView.scrollBy(deltaX, deltaY);
        }
      }

      private void showDirections(RoutePath path)
      {
        switch (path.getEvents().get(0).getType())
        {
          case RouteEvent.TURN_LEFT:
            //mDirectionImageView.setBackgroundResource(R.drawable.ic_left);
            break;
          case RouteEvent.TURN_RIGHT:
            //mDirectionImageView.setBackgroundResource(R.drawable.ic_right);
            break;
          case RouteEvent.TRANSITION:
            //mDirectionImageView.setBackgroundResource(R.drawable.ic_escalator);
            break;
        }
        float nextTurnDistance = Math.max(path.getEvents().get(0).getDistance(), 1);
        mDirectionTextView.setText(String.format(Locale.ENGLISH,"%.0f m", nextTurnDistance));
        mDirectionLayout.setVisibility(View.VISIBLE);
      }

      private String getLogFile(String extension)
      {
        try
        {
          final String extDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Navigine.Demo";
          (new File(extDir)).mkdirs();
          if (!(new File(extDir)).exists())
            return null;

          Calendar calendar = Calendar.getInstance();
          calendar.setTimeInMillis(System.currentTimeMillis());

          return String.format(Locale.ENGLISH, "%s/%04d%02d%02d_%02d%02d%02d.%s", extDir,
                               calendar.get(Calendar.YEAR),
                               calendar.get(Calendar.MONTH) + 1,
                               calendar.get(Calendar.DAY_OF_MONTH),
                               calendar.get(Calendar.HOUR_OF_DAY),
                               calendar.get(Calendar.MINUTE),
                               calendar.get(Calendar.SECOND),
                               extension);
        }
        catch (Throwable e)
        {
          return null;
        }
      }
}
