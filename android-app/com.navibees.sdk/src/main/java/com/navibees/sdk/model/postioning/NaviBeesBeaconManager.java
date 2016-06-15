package com.navibees.sdk.model.postioning;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.NaviBeesApplication;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.BeaconNode;
import com.navibees.sdk.model.metadata.BeaconNodeState;
import com.navibees.sdk.model.metadata.MetaDataManager;
import com.navibees.sdk.model.metadata.NaviBeesBeacon;
import com.navibees.sdk.model.metadata.json.BeaconNodeConfigurtion;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.metadata.json.MonitoredRegion;
import com.navibees.sdk.util.CommonUtils;
import com.navibees.sdk.util.Log;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


final public class NaviBeesBeaconManager implements BeaconConsumer , RangeNotifier , MonitorNotifier  {

    static final String TAG = "NaviBeesBeaconManager";

    private Context context;
    private BeaconNodeListener beaconNodeListener;

    private BeaconManager beaconManager;

    private String UUID;// = "D3ACCFE2-E95D-433C-BCF4-643BECC5D217";
    private String RegionIdentifer = "NaviBees";
    private Region rangingRegion ;
    private List<Region> backgroundMonitoredRegions;
    private List<Region> foregroundMonitoredRegions;

    private MetaDataManager metaDataManager;
    //update battery status for beacon each one days
    final static long BATTERY_STATUS_THRESHOLD =  1 * 24 * 60 * 60 * 1000L;

    private Activity activity;

    private List<ParseObject> beaconsToBeUpadtedOnServer = new ArrayList<ParseObject>();//Due to Battery Status

    public NaviBeesBeaconManager(/*Context context */Activity activity, BeaconNodeListener beaconNodeListener) {
        this.activity = activity;
        this.context = activity.getApplicationContext();

        try {
            AppManager.getInstance().getLicenseManager().verify(activity.getApplicationContext() , NaviBeesFeature.Positioning);
            this.beaconNodeListener = beaconNodeListener;
        } catch (NaviBeesLicenseNotAuthorithedException e) {
            e.printStackTrace();
        } catch (NaviBeesLicenseExpireException e) {
            e.printStackTrace();
        }


        metaDataManager = AppManager.getInstance().getMetaDataManager();

        beaconManager = BeaconManager.getInstanceForApplication(context);


        try {

            UUID = metaDataManager.getApplicationConfiguration(context).getBeaconsUUID();
            rangingRegion = new Region( RegionIdentifer , Identifier.parse(UUID) , null , null);

            //TODo check monitoring license
            AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Location_Based_Notifications);

            List<MonitoredRegion> monitoredRegions = AppManager.getInstance().getMetaDataManager().getMonitoredRegions(context);
            backgroundMonitoredRegions = new ArrayList<Region>();
            foregroundMonitoredRegions = new ArrayList<Region>();
            if(monitoredRegions != null) {
                classifyMonitoredRegions(monitoredRegions);
            }

        }catch (NaviBeesLicenseNotAuthorithedException e){
            CommonUtils.monitoringNotAuthorizedToBeEnabled(context);
        }catch (NaviBeesLicenseExpireException e){
            CommonUtils.monitoringNotAuthorizedToBeEnabled(context);
        } catch (Exception e){
            e.printStackTrace();
        }

        bindBeaconManager();
    }


    public NaviBeesBeaconManager(Context context, BeaconNodeListener beaconNodeListener) {
        this.context = context.getApplicationContext();
        this.beaconNodeListener = beaconNodeListener;

        metaDataManager = AppManager.getInstance().getMetaDataManager();
        beaconManager = BeaconManager.getInstanceForApplication(this.context);


        try {

            UUID = metaDataManager.getApplicationConfiguration(context).getBeaconsUUID();
            rangingRegion = new Region( RegionIdentifer , Identifier.parse(UUID) , null , null);

            List<MonitoredRegion> monitoredRegions = AppManager.getInstance().getMetaDataManager().getMonitoredRegions(context);
            backgroundMonitoredRegions = new ArrayList<Region>();
            foregroundMonitoredRegions = new ArrayList<Region>();
            if(monitoredRegions != null) {
                classifyMonitoredRegions(monitoredRegions);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        bindBeaconManager();
    }

    private void classifyMonitoredRegions(List<MonitoredRegion> monitoredRegions) {
        //monitoringRegion1 = new Region("Region1", Identifier.parse(UUID), Identifier.fromInt(3), Identifier.fromInt(24));
        for(MonitoredRegion monitoredRegion:monitoredRegions){
            if(monitoredRegion.isValid()){
                //foreground
                if(monitoredRegion.getType().equalsIgnoreCase(MonitoredRegion.MONITORED_REGIONS_TYPE[0])){
                    Region region = new Region(monitoredRegion.getIdentifier() , Identifier.parse(monitoredRegion.getUUID()) ,Identifier.fromInt(monitoredRegion.getMajor()) , Identifier.fromInt(monitoredRegion.getMinor()));
                    this.foregroundMonitoredRegions.add(region);
                    continue;
                }

                //background
                if(monitoredRegion.getType().equalsIgnoreCase(MonitoredRegion.MONITORED_REGIONS_TYPE[1])){
                    Region region = new Region(monitoredRegion.getIdentifier() , Identifier.parse(monitoredRegion.getUUID()) ,Identifier.fromInt(monitoredRegion.getMajor()) , Identifier.fromInt(monitoredRegion.getMinor()));
                    this.backgroundMonitoredRegions.add(region);
                    continue;
                }

                //all
                if(monitoredRegion.getType().equalsIgnoreCase(MonitoredRegion.MONITORED_REGIONS_TYPE[2])){
                    Region region = new Region(monitoredRegion.getIdentifier() , Identifier.parse(monitoredRegion.getUUID()) ,Identifier.fromInt(monitoredRegion.getMajor()) , Identifier.fromInt(monitoredRegion.getMinor()));
                    this.foregroundMonitoredRegions.add(region);
                    this.backgroundMonitoredRegions.add(region);
                    continue;
                }
            }
        }

        //Save backgroundMonitoredRegions To SharedPrefernaces to be readed by Application class to be used RegionBootStrap for next time
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        editor.putString(ApplicationConstants.BACKGROUND_MONITORED_REGIONS_KEY, gson.toJson(backgroundMonitoredRegions));
        editor.commit();

    }


    public void bindBeaconManager() {
        android.util.Log.i("Service_class", "bindBeaconManager: ");
        //Disable Background Monitoring (RegionBootstrap in Application class)
        if(activity != null)
        ((NaviBeesApplication)activity.getApplication()).disableRegionBootstrap();
        android.util.Log.i("Service_class", "is any consumenr bound " + beaconManager.isAnyConsumerBound());

        if (beaconManager.isAnyConsumerBound()) {
            try {
                Field field = BeaconManager.class.getDeclaredField("consumers");
                field.setAccessible(true);
                ConcurrentMap<BeaconConsumer, Object> consumers = (ConcurrentMap<BeaconConsumer, Object>) field.get(beaconManager);
                for (BeaconConsumer consumer : consumers.keySet()) {
                    android.util.Log.i("Service_class", "unbinding an old consumer");
                    beaconManager.unbind(consumer);
                }


            } catch (Exception e) {
                android.util.Log.e("Service_class", "bindBeaconManager: ", e);
            }
        }
        beaconManager.bind(this);


    }

    public void enableBackgroundMonitoring() throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        //System.out.println("------ enableBackgroundMonitoring ------ ");

        if (beaconManager != null){

            if (beaconManager.isBound(this)) {
                beaconManager.unbind(this);
            }

            if(backgroundMonitoredRegions != null & activity!=null)
                ((NaviBeesApplication) activity.getApplication()).enableRegionBootstrap(backgroundMonitoredRegions);
            //beaconManager.setBackgroundMode(true;
        }
    }

    @Override
    public Context getApplicationContext() {
        return context.getApplicationContext();
    }
    @Override
    public boolean bindService(Intent intent, ServiceConnection connection, int mode) {
        return context.bindService(intent, connection, mode);
    }

    @Override
    public void unbindService(ServiceConnection connection) {
        context.unbindService(connection);
    }


    @Override
    public void onBeaconServiceConnect() {

        //System.out.println("------ onBeaconServiceConnect ------ ");

        //-----------------------Start of Ranging --------------------------------------------------
        beaconManager.setRangeNotifier(this);

        try {
            //System.out.println("------ onBeaconServiceConnect : startRangingBeaconsInRegion  --- UUID--- "+ UUID);
            beaconManager.startRangingBeaconsInRegion(rangingRegion);
            //---------------------------------- Start Of Foreground Monitoring -----------------------

            //ToDo Check if Monitoring exist in license or not
            AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Location_Based_Notifications);
            beaconManager.setMonitorNotifier(this);
            //Foreground
            if(foregroundMonitoredRegions != null){

                for(Region region:foregroundMonitoredRegions) {
                    beaconManager.startMonitoringBeaconsInRegion(region);
                }
            }
            //Background
            if(backgroundMonitoredRegions != null){

                for(Region region:backgroundMonitoredRegions) {
                    beaconManager.startMonitoringBeaconsInRegion(region);
                }
            }


        }catch (NaviBeesLicenseNotAuthorithedException e){
            CommonUtils.monitoringNotAuthorizedToBeEnabled(context);
            e.printStackTrace();
        }catch (NaviBeesLicenseExpireException e){
            CommonUtils.monitoringNotAuthorizedToBeEnabled(context);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private BeaconNode convert (NaviBeesBeacon ibeacon){

        //System.out.println("------ convert ------ ");

        //Log.i(TAG, "Beacon with UUID:  " + ibeacon.getId1() + " , Major:" + ibeacon.getId2() + " , Minor:" + ibeacon.getId3() + " , RSSI:" + ibeacon.getRssi() + " , TxPower:" + ibeacon.getTxPower() + ", Distance:" + ibeacon.getDistance() + " meters away.");
        //Log.i(TAG, "Distance From RSSI :  "+getDistance(ibeacon.getRssi(),ibeacon.getTxPower() ) + " meters away.");
        BeaconNode beaconNode = new BeaconNode(ibeacon.getId2().toInt() , ibeacon.getId3().toInt());
        //Add state
        // We will discard this BeaconNode if it was detected before in current batch of beacons
        // And add its state to its list
        beaconNode.getStates().add(new BeaconNodeState(ibeacon.getRssi(), ibeacon.getDistance(), this.getDistance(ibeacon.getRssi(), ibeacon.getTxPower())));

        //get Location of this beacon
        int index = metaDataManager.getTagsLocations(context).indexOf(new BeaconNodeConfigurtion(beaconNode.getMajor(), beaconNode.getMinor()));

        if (index != -1){
            BeaconNodeConfigurtion beaconNodeConfigurtion = metaDataManager.getTagsLocations(context).get(index);
            beaconNode.setLocation(new IndoorLocation(beaconNodeConfigurtion.getX(), beaconNodeConfigurtion.getY(), beaconNodeConfigurtion.getFloorIndex()));

            //Log.i(TAG, beaconNodeConfigurtion.toString());

            //
            //Set battery Status
            if(ibeacon.getBatteryStatus() != 0){//i.e CoreBlu Tag
                beaconNode.setBatteryStatus(getBatteryStatusPercentage(ibeacon.getBatteryStatus()));
            }else {
                //Any Other Beacon
                beaconNode.setBatteryStatus(-1);
            }

            //Add This beacon to beacons which will be updated (Battery Status)
            if((System.currentTimeMillis() - beaconNodeConfigurtion.getLastTimeBatteryReported()) > BATTERY_STATUS_THRESHOLD){

                ParseObject beacon = ParseObject.createWithoutData("Beacon", beaconNodeConfigurtion.getObjectId());
                boolean isBeaconExistBeforeInBeaconsToBeUpdated = false;
                //Check if we added this beacon (beaconNodeConfigurtion on Parse Beacon Table defined by ObjectID) before to  beaconsToBeUpadtedOnServer
                for(ParseObject beaconParseObject: beaconsToBeUpadtedOnServer) {
                    if (beaconParseObject.getObjectId().equals(beacon.getObjectId())) {

                        //update battery status and lastSeen
                        beaconParseObject.put("batteryStatus", beaconNode.getBatteryStatus());
                        beaconParseObject.put("lastSeenAt", new Date());

                        isBeaconExistBeforeInBeaconsToBeUpdated = true;
                        //Log.i(TAG, " This beacon.getObjectId(): " +beacon.getObjectId()+ " , added before");

                    }
                }


                if(isBeaconExistBeforeInBeaconsToBeUpdated == false) {//New i.e not exist before in beaconsToBeUpadtedOnServer
                    beacon.put("batteryStatus", beaconNode.getBatteryStatus());
                    beacon.put("lastSeenAt", new Date());
                    beacon.put("major", beaconNode.getMajor());
                    beacon.put("minor", beaconNode.getMinor());
                    beaconsToBeUpadtedOnServer.add(beacon);
                    // Log.i(TAG, " This beacon.getObjectId(): " + beacon.getObjectId() + " , is new");

                }

            }

        }else {
            //Log.i(TAG , "Beacon Detected Without Known Location (major , minor) : ( "+ ibeacon.getId2()+ " , "+ ibeacon.getId3() + " ) , we will discard it in calculation");
            beaconNode.setLocation(null);
        }

        //Log.e(TAG, "Convert:beaconNode.toString():" + beaconNode.toString());

        return beaconNode;
    }

    private int getBatteryStatusPercentage(byte batteryStatus) {
        //This method need optimization
        String batteryStatusInHex = bytesToHex( new byte[]{batteryStatus});
        double batteryStatusVoltage = (Integer.parseInt(batteryStatusInHex , 16) * 3.6) /255;
        int batteryStatusPercentage = (int)(100 * ((batteryStatusVoltage - 1.8)/(3.2 - 1.8) ));
        /*
                * ADV[2] = D1
                  ADV[2] = 209 in decimal

                  formula :
                  Voltage = (ADV[2]x 3.6) / 255
                  Voltage = (209 x 3.6) / 255  =  2.95v

                  Battery Voltage in %
                  3.2v = 100%
                  1.8v = 0%
       */

        return batteryStatusPercentage;
    }


    //RSSI to Distance
    private double getDistance(int rssi, int txPower) {
    /*
     * RSSI = TxPower - 10 * n * lg(d)
     * n = 2 (in free space)
     *
     * d = 10 ^ ((TxPower - RSSI) / (10 * n))
     */

        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    private static final char[] HEX_ARRAY = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for(int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 15];
        }

        return new String(hexChars);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        Log.i(TAG, "beacons.size() : " + beacons.size());

        List<BeaconNode> uniqueBeaconNodes = new ArrayList<BeaconNode>();
        if(metaDataManager.getTagsLocations(context) != null)
        {
            for (Beacon ibeacon : beacons) {
                //check of this ibeacon has valid reading (i.e RSSI != 0) then
                //check if this ibeacon detected before in current batch of beacons
                if (ibeacon.getRssi() != 0) {
                    //Convert Beacon to BeaconNode
                    BeaconNode beaconNode = convert((NaviBeesBeacon) ibeacon);
                    if (beaconNode.getLocation() == null) {
                        //Beacon Detected Without Known Location
                        //i.e beacon_nodes_configurations.json does not have this beacon or
                        //has it but its location (x,y) not exist
                        //so we will discard it
                        //Localizer will work on Valid (has location) Beacons only
                        continue;
                    }

                    int index = uniqueBeaconNodes.indexOf(beaconNode);
                    if (index == -1) {
                        //i.e new ibeacon
                        uniqueBeaconNodes.add(beaconNode);

                    } else {
                        //add RSSI , Accuracy and estimated Distance to its state
                        uniqueBeaconNodes.get(index).getStates().add(new BeaconNodeState(ibeacon.getRssi(), ibeacon.getDistance(), getDistance(ibeacon.getRssi(), ibeacon.getTxPower())));
                        //Log.i(TAG, "******* Beacon  (major , minor) : ( "+ ibeacon.getId2()+ " , "+ ibeacon.getId3() + " ) , has state size : "+uniqueBeaconNodes.get(index).getStates().size());

                    }
                }

            }
        }


        if(this.beaconNodeListener != null) {//i.e positioning included in license
            try {
                this.beaconNodeListener.beaconNodeCallback(uniqueBeaconNodes);
            } catch (NaviBeesLicenseNotAuthorithedException e) {
                e.printStackTrace();
            } catch (NaviBeesLicenseExpireException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void didEnterRegion(Region region) {
        //Log.i(TAG, "I just saw an beacon for the first time!");

        //createLocalNotification("Foreground-"+region.getUniqueId() , "Enter Region defined by: UUID--- " + region.getId1() + " , Major:" + region.getId2() + " , Minor:" + region.getId3());

        Intent in = new Intent(ApplicationConstants.MONITORED_REGION_ACTION);
        // Put extras into the intent as usual
        in.putExtra(ApplicationConstants.MONITORED_REGION_UNIQUE_IDENTIFIER_KEY, region.getUniqueId());
        in.putExtra(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_KEY, ApplicationConstants.MONITORED_REGION_ACTION_TYPE_ENTER_VALUE);
        // Fire the broadcast with intent packaged
        //LocalBroadcastManager.getInstance(context).sendBroadcast(in);
        // or sendBroadcast(in) for a normal broadcast;
        context.sendBroadcast(in);

    }

    @Override
    public void didExitRegion(Region region) {
        //Log.d(TAG, "Got a didExitRegion call");

        Intent in = new Intent(ApplicationConstants.MONITORED_REGION_ACTION);
        in.putExtra(ApplicationConstants.MONITORED_REGION_UNIQUE_IDENTIFIER_KEY, region.getUniqueId());
        in.putExtra(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_KEY, ApplicationConstants.MONITORED_REGION_ACTION_TYPE_EXIT_VALUE);
        context.sendBroadcast(in);

    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        //Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
    }


    /**
     * We Call updateBatteryStatusOnServer from MapActivity OnDestroy becuase we want to
     * Push Battery status and lastSeenAt columns of updated beacons (Beacon Configuration).
     * Also we edit Battery status and lastSeenAt local inside beacon_nodes_configurations file , to check next time
     * if detected beacons exceeded BATTERY_STATUS_THRESHOLD in next Condition (exist above)
     * if((System.currentTimeMillis() - beaconNodeConfigurtion.getLastTimeBatteryReported()) > BATTERY_STATUS_THRESHOLD)
     * {  // Battery Status of this beaconNodeConfigurtion need to be updated on Server }
     */

    public void updateBatteryStatusOnServer() {
        if (beaconsToBeUpadtedOnServer != null && beaconsToBeUpadtedOnServer.size() != 0) {
            ParseObject.saveAllInBackground(beaconsToBeUpadtedOnServer, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    //If there is no error , i.e update battery done
                    // we should update lastTimeBatteryReported field in BeaconNodeConfigurtion
                    // and save File as the same case in ParseServerManager
                    Log.i(TAG, "updateBatteryStatusOnServer: (e == null)" + (e == null) + " , beaconsToBeUpadtedOnServer.size():" + beaconsToBeUpadtedOnServer.size());

                    if (e == null) {

                        for (ParseObject parseObject : beaconsToBeUpadtedOnServer) {

                            int indexOfBeaconConfigurationToBeUpdated = metaDataManager.getTagsLocations(context).indexOf(new BeaconNodeConfigurtion(parseObject.getInt("major"), parseObject.getInt("minor")));
                            if (indexOfBeaconConfigurationToBeUpdated != -1) {
                                BeaconNodeConfigurtion beaconNodeConfigurtion = metaDataManager.getTagsLocations(context).get(indexOfBeaconConfigurationToBeUpdated);
                                beaconNodeConfigurtion.setBatteryStatus(parseObject.getInt("batteryStatus"));
                                beaconNodeConfigurtion.setLastTimeBatteryReported(parseObject.getDate("lastSeenAt").getTime());
                                Log.i(TAG, "updateBatteryStatusOnServer: beaconNodeConfigurtionget:ObjectId: " + beaconNodeConfigurtion.getObjectId() + " , major :" + beaconNodeConfigurtion.getMajor() + " , minor:" + beaconNodeConfigurtion.getMinor());

                            }
                        }
                        //Update beacons file
                        //Here we update the file contents only (batteryStatus , lastSeenAt on Server) for each beacon node
                        //And Keep the LastModifiedDateForBeaconsNodeConfigurations as it is in sharedPreferences to be able to get the new file
                        //on server if it will be changed there on server.
                        AppManager.getInstance().getMetaDataManager().beaconNodeConfigurtionChangeCallback(metaDataManager.getTagsLocations(context), AppManager.getInstance().getMetaDataManager().getLastModifiedDateForBeaconsNodeConfigurations().getTime());
                        // AppManager.recycle(); should be called in on Destroy
                        // of MapActivity
                    }
                    AppManager.recycle();
                }
            });
        }else{
            AppManager.recycle();
        }
    }

    public void unbind(){
        android.util.Log.i("Service_class", "unbind: ");
        if (beaconManager != null) {

            if (beaconManager.isBound(this)) {
                beaconManager.unbind(this);
            }
        }
        android.util.Log.i("Service_class", "unbind: " + beaconManager.isAnyConsumerBound());
    }

}