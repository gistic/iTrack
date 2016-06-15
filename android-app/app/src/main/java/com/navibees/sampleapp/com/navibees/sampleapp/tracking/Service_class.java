package com.navibees.sampleapp.com.navibees.sampleapp.tracking;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;



import com.navibees.sdk.AppManager;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.postioning.IndoorLocationListener;

import com.navibees.sdk.model.postioning.NaviBeesBeaconManager;
import com.navibees.sdk.model.postioning.PositionManager;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.Calendar;

public class Service_class extends Service implements AppManager.OnInitializedListener, IndoorLocationListener  {

    private PositionManager positionManager;
    private int nullCount = 0;

final static String TAG = "Service_class";

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: " + startId + " " + positionManager);

        if (positionManager != null){
            positionManager.getNaviBeesBeaconManager().bindBeaconManager();
            positionManager.startTracking();
        }else if (startId >1){
            AppManager.getInstance().initialize(this, this);
        }

        checkAndEnableBluetooth();
        return START_NOT_STICKY;
    }

    private void checkAndEnableBluetooth(){
        //turning bluetooth on during working hours
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 8 && hour <= 17 && !mBluetoothAdapter.isEnabled()){
            Log.i(TAG, "onCreate: Enabling Bluetooth ...");
            mBluetoothAdapter.enable();
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        positionManager.getNaviBeesBeaconManager().unbind();
        //AppManager.getInstance().getNaviBeesBeaconManager(getApplicationContext()).unbind();

        //Intent intent = new Intent(getApplicationContext(), Service_class.class);
//        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, intent,
//                0);
//        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//
//        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 30000,
//                pintent);

        super.onDestroy();

    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
        try {
            String parseAppId = "pey8pjhhotyDIusYHgSEF6SUI84ugMPVe6rqF2Ah";
            String parseClientKey = "ZD6C8F4bhChdi0FiotmdbWjNDIvmlL5hbtVfHvsx";
            Parse.enableLocalDatastore(getApplicationContext());
            Parse.initialize(this, parseAppId, parseClientKey);
            ParseInstallation.getCurrentInstallation().saveInBackground();
            AppManager.getInstance().initialize(this, this);
        }catch (Exception  e){
            Log.w(TAG, "onCreate: ",  e);
        }
    }

    @Override
    public void onInitialized(boolean success) {
        Log.i(TAG, "onInitialized: " + success);
        if (success){
            positionManager = AppManager.getInstance().getPositionManager(this, this);
        }else{
            stopSelf();
        }

    }

    @Override
    public void locationCallback(IndoorLocation currentLocationWithoutSmoothing, IndoorLocation currentLocationAfterSmoothing, int numOfValidBeacons) {
        Log.i(TAG, "locationCallback: " + currentLocationAfterSmoothing);

        if (currentLocationAfterSmoothing != null || nullCount >= 5) {
            nullCount = 0;
            positionManager.disableReportingLocation();
            positionManager.getNaviBeesBeaconManager().unbind();

            if (currentLocationAfterSmoothing == null){
                Log.i(TAG, "locationCallback: Stop scanning after receiving many null callbacks");
                return;
            }

            final ParseObject testObject = new ParseObject("Positions");
            testObject.put("X", currentLocationAfterSmoothing.getX());
            testObject.put("Y", currentLocationAfterSmoothing.getY());
            testObject.put("floor", currentLocationAfterSmoothing.getFloor());
            testObject.put("type", "point");
            testObject.put("timeStamp", Calendar.getInstance().getTime());
            testObject.put("installation_id", ParseInstallation.getCurrentInstallation().getInstallationId());
            testObject.saveEventually();

        } else {
            nullCount ++;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Log.i(TAG, "finalize: "+ positionManager.getNaviBeesBeaconManager());
        positionManager.getNaviBeesBeaconManager().unbind();
        super.finalize();
    }
}
