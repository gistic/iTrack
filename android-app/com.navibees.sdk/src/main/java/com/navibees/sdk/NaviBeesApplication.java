package com.navibees.sdk;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.esri.android.runtime.ArcGISRuntime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.postioning.NaviBeesBeaconParser;
import com.navibees.sdk.util.Log;
import com.parse.Parse;
import com.parse.ParseInstallation;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.List;

/**
 * Created by nabilnoaman on 6/22/15.
 */
//http://stackoverflow.com/questions/25906453/altbeacon-library-sample-doesnt-seem-to-work-in-the-background
public class NaviBeesApplication extends Application implements BootstrapNotifier {

    private static final String TAG = "NaviBeesApplication";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    private String uuid;



    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App started up");


        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();


        // Setup Parse
//        String parseAppId = "pey8pjhhotyDIusYHgSEF6SUI84ugMPVe6rqF2Ah";
//        String parseClientKey = "ZD6C8F4bhChdi0FiotmdbWjNDIvmlL5hbtVfHvsx";
//        Parse.initialize(this, parseAppId, parseClientKey);
//        ParseInstallation.getCurrentInstallation().saveInBackground();



        handlelicense();

        //Layout for iBeacon
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new NaviBeesBeaconParser());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundRegionsJSON = sp.getString(ApplicationConstants.BACKGROUND_MONITORED_REGIONS_KEY,"-");
        Log.i(TAG , "backgroundRegionsJSON:"+ backgroundRegionsJSON);

        if(!backgroundRegionsJSON.equalsIgnoreCase("-") ){
            Log.i(TAG , "backgroundRegionsJSON:"+ backgroundRegionsJSON);

            List<Region> backgroundRegions = new Gson().fromJson(backgroundRegionsJSON, new TypeToken<List<Region>>() {
            }.getType());

            Log.i(TAG , "(backgroundRegions != null):"+(backgroundRegions != null));

            Log.i(TAG , "backgroundRegions.size():"+backgroundRegions.size());

            /*
            if(backgroundRegions != null) {
                for (Region region : backgroundRegions) {

                    Log.i(TAG, "region.getUniqueId():" + region.getUniqueId() + " , region.getId1(): " + region.getId1() + " , region.getId2():" + region.getId2() + " , region.getId3():" + region.getId3());
                }
            }
            */
            //TODO we should check if Monitoring exist in license or no , we should put license (Plan and AccessToken) in Manifest file of app module
            regionBootstrap = new RegionBootstrap(this , backgroundRegions);
        }


        // set the duration of the scan to be 1.1 seconds
        beaconManager.setBackgroundScanPeriod(1100l);
        //beaconManager.setForegroundScanPeriod(1100l);
        // set the time between each scan to be 1 minute (5 seconds == 5000l milliSeconds)
        beaconManager.setBackgroundBetweenScanPeriod(5000l);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);

    }


    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "Got a didEnterRegion call");

        Intent in = new Intent(ApplicationConstants.MONITORED_REGION_ACTION);
        in.putExtra(ApplicationConstants.MONITORED_REGION_UNIQUE_IDENTIFIER_KEY, region.getUniqueId());
        in.putExtra(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_KEY, ApplicationConstants.MONITORED_REGION_ACTION_TYPE_ENTER_VALUE);
        // Fire the broadcast with intent packaged
        //LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        // or sendBroadcast(in) for a normal broadcast;
        sendBroadcast(in);
    }

    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "Got a didExitRegion call");

        Intent in = new Intent(ApplicationConstants.MONITORED_REGION_ACTION);
        in.putExtra(ApplicationConstants.MONITORED_REGION_UNIQUE_IDENTIFIER_KEY, region.getUniqueId());
        in.putExtra(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_KEY, ApplicationConstants.MONITORED_REGION_ACTION_TYPE_EXIT_VALUE);
        sendBroadcast(in);

    }


    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // Don't care
        Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
    }

    final public void disableRegionBootstrap(){
        if(regionBootstrap != null) {
            regionBootstrap.disable();
        }
    }


    final public void enableRegionBootstrap(List<Region> regions) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        //// wake up the app when any beacon defined in regions is seen
        if(regions != null) {
            Log.e(TAG, "enableBackgroundMonitoring:regions.size():" + regions.size());
            AppManager.getInstance().getLicenseManager().verify(this, NaviBeesFeature.Location_Based_Notifications);
            regionBootstrap = new RegionBootstrap(this, regions);
        }else {
            regionBootstrap = null;
        }
    }


    public void setAppInForeground(Boolean isAppInForeground) {
        Log.e(TAG , "NaviBeesApplication:setAppInForeground:isAppInForeground:"+isAppInForeground);

        editor.putBoolean(ApplicationConstants.IS_APP_IN_FOREGROUND_KEY, isAppInForeground);
        editor.commit();

    }

    private void handlelicense() {
        try {
            String clientID = getClientID();
            String licenseCode = getLicenseCode();

            if(TextUtils.isEmpty(clientID) || TextUtils.isEmpty(licenseCode)){
                return;
            }
            // set the Client ID first
            ArcGISRuntime.setClientId(clientID);
            // example license code; obtain an actual license code from customer service or your Esri distributor
            //String licenseCode = "runtimestandard,101,rux00000,none,XXXXXXX";
            // enable Standard level functionality in your app using your license code
            ArcGISRuntime.License.setLicense(licenseCode);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    protected String getClientID(){
        return "";
    }


    protected String getLicenseCode() {
        return "";
    }


}