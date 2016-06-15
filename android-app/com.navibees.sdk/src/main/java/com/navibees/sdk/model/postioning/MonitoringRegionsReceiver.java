package com.navibees.sdk.model.postioning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.model.metadata.json.MonitoredRegion;
import com.navibees.sdk.util.Log;

import java.util.List;

public class MonitoringRegionsReceiver extends BroadcastReceiver {

    static final String TAG = "MontoringRegionsReceiver";
    private int notificationId;
    private Context context;

    public MonitoringRegionsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        long currentTimeOfReceivingNotificationOfCurrentRegion = System.currentTimeMillis();
        Log.i(TAG, "-------------------------------------");
        Log.i(TAG , "MontoringRegionsReceiver:onReceive");
        final String regionIdentifier = intent.getStringExtra(ApplicationConstants.MONITORED_REGION_UNIQUE_IDENTIFIER_KEY);
        final String regionActionType = intent.getStringExtra(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_KEY);

        Log.i(TAG , "MontoringRegionsReceiver:regionIdentifier:"+regionIdentifier);
        Log.i(TAG , "MontoringRegionsReceiver:regionActionType:"+regionActionType);

        this.context = context;

        if (regionIdentifier != null) {
            //1. get region type (foreground/background/all)
            //2. get app state ( foreground/background)
            //1.
            List<MonitoredRegion> monitoredRegions = AppManager.getInstance().getMetaDataManager().getMonitoredRegions(context);

            if(monitoredRegions != null){

                MonitoredRegion currentMonitoredRegion = null;
                for (MonitoredRegion monitoredRegion:monitoredRegions){
                    if(monitoredRegion.getIdentifier().equalsIgnoreCase(regionIdentifier)){
                        currentMonitoredRegion = monitoredRegion ;
                        break;
                    }
                }


                //2.
                if(currentMonitoredRegion != null) {
                   Log.i(TAG, "MontoringRegionsReceiver:currentMonitoredRegion.getIdentifier():" + currentMonitoredRegion.getIdentifier());

                    //Fire Notification for currentRegion if exceed the time interval since last time fired
                    if(isFireNotification(currentMonitoredRegion ,currentTimeOfReceivingNotificationOfCurrentRegion ))
                    {
                    //all
                    if (currentMonitoredRegion.getType().equalsIgnoreCase(MonitoredRegion.MONITORED_REGIONS_TYPE[2])) {

                        if (regionActionType.equalsIgnoreCase(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_ENTER_VALUE)) {
                            didEnterRegionAll(currentMonitoredRegion);
                        } else {
                            if (regionActionType.equalsIgnoreCase(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_EXIT_VALUE)) {
                                didExitRegionAll(currentMonitoredRegion);
                            }
                        }
                    } else {

                        if (currentMonitoredRegion.getType().equalsIgnoreCase(MonitoredRegion.MONITORED_REGIONS_TYPE[0])) {
                            //foreground
                            //check app in foreground to send com_uqu_navibees_sdk_notification
                            if (isAppInForeground(context)) {

                                if (regionActionType.equalsIgnoreCase(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_ENTER_VALUE)) {
                                    didEnterRegionForeground(currentMonitoredRegion);
                                } else {
                                    if (regionActionType.equalsIgnoreCase(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_EXIT_VALUE)) {
                                        didExitRegionForeground(currentMonitoredRegion);
                                    }
                                }
                            }
                        } else {
                            if (currentMonitoredRegion.getType().equalsIgnoreCase(MonitoredRegion.MONITORED_REGIONS_TYPE[1])) {
                                //background
                                //check app in background to send com_uqu_navibees_sdk_notification
                                if (!isAppInForeground(context)) {

                                    if (regionActionType.equalsIgnoreCase(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_ENTER_VALUE)) {
                                        didEnterRegionBackground(currentMonitoredRegion);
                                    } else {
                                        if (regionActionType.equalsIgnoreCase(ApplicationConstants.MONITORED_REGION_ACTION_TYPE_EXIT_VALUE)) {
                                            didExitRegionBackground(currentMonitoredRegion);
                                        }
                                    }

                                }

                            }
                        }
                    }
                }//

                }

            }
        }

    }

    protected boolean isAppInForeground(Context context){
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean isAppInForeground = sp.getBoolean(ApplicationConstants.IS_APP_IN_FOREGROUND_KEY , false);
        Log.i(TAG , "MonitoringRegionsReceiver:isAppInForeground:"+isAppInForeground);
        return  isAppInForeground;
    }


    private boolean isFireNotification(MonitoredRegion currentMonitoredRegion , long currentTimeOfReceivingNotificationOfCurrentRegion){
        //check time interval to send notification for current region
        //to prevent duplicate notifications for the same region
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        long lastTimeFired = sp.getLong(currentMonitoredRegion.getIdentifier() , 0);
        Log.i(TAG , "MonitoringRegionsReceiver:isFireNotification:lastTimeFired:"+lastTimeFired);
        int timeSinceLastNotificationInSeconds = (int) ((currentTimeOfReceivingNotificationOfCurrentRegion - lastTimeFired)/1000);
        Log.i(TAG, "MonitoringRegionsReceiver:currentMonitoredRegion time since last fire :" +  timeSinceLastNotificationInSeconds);

        //Fire Notification for currentRegion if exceed the time interval since last time fired
        if( timeSinceLastNotificationInSeconds >= currentMonitoredRegion.getInterval()){
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(currentMonitoredRegion.getIdentifier()  ,currentTimeOfReceivingNotificationOfCurrentRegion );
            editor.commit();
            currentMonitoredRegion.setTimeSinceLastFireInSeconds(timeSinceLastNotificationInSeconds);
            return true;

        }else {
            return  false;
        }

    }


    protected void didEnterRegionForeground(MonitoredRegion region) {
        Log.i(TAG , "MontoringRegionsReceiver:didEnterRegionForeground:"+region.getIdentifier() + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds());
        //createLocalNotification("NaviBees-Foreground","Enter: "+ region.getIdentifier()  +", time since last notification:"+ region.getTimeSinceLastFireInSeconds()  , context);
    }
    protected void didExitRegionForeground(MonitoredRegion region) {
        Log.i(TAG , "MontoringRegionsReceiver:didExitRegionForeground:"+region.getIdentifier() + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds());
        //createLocalNotification("NaviBees-Foreground","Exit: "+ region.getIdentifier()  +", time since last notification:"+ region.getTimeSinceLastFireInSeconds()   , context);
    }
    protected void didEnterRegionBackground(MonitoredRegion region) {
        Log.i(TAG , "MontoringRegionsReceiver:didEnterRegionBackground:"+region.getIdentifier() + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds());
        //createLocalNotification("NaviBees-Background","Enter: "+ region.getIdentifier()  +", time since last notification:"+ region.getTimeSinceLastFireInSeconds() , context);
    }
    protected void didExitRegionBackground(MonitoredRegion region) {
        Log.i(TAG , "MontoringRegionsReceiver:didExitRegionBackground:"+region.getIdentifier() + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds());
        //createLocalNotification("NaviBees-Background","Exit: "+ region.getIdentifier()  +", time since last notification:"+ region.getTimeSinceLastFireInSeconds()   , context);
    }
    protected void didEnterRegionAll(MonitoredRegion region) {
        Log.i(TAG , "MontoringRegionsReceiver:didEnterRegionAll:"+region.getIdentifier() + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds());
        //createLocalNotification("NaviBees-All","Enter: "+ region.getIdentifier()  +", time since last notification:"+ region.getTimeSinceLastFireInSeconds()   , context);
    }
    protected void didExitRegionAll(MonitoredRegion region) {
        Log.i(TAG , "MontoringRegionsReceiver:didExitRegionAll:"+region.getIdentifier() + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds());
        //createLocalNotification("NaviBees-All","Exit: "+ region.getIdentifier() +", time since last notification:"+ region.getTimeSinceLastFireInSeconds()  , context);
    }

}
