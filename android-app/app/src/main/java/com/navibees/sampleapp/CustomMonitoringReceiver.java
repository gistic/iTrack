package com.navibees.sampleapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.BeaconNode;
import com.navibees.sdk.model.metadata.BeaconNodeState;
import com.navibees.sdk.model.metadata.MetaDataManager;
import com.navibees.sdk.model.metadata.json.BeaconNodeConfigurtion;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.metadata.json.MonitoredRegion;
import com.navibees.sdk.model.postioning.MonitoringRegionsReceiver;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.HashSet;


public class CustomMonitoringReceiver extends MonitoringRegionsReceiver {

    static final String TAG = "MonitoringReceiver";

    private Context context;
    private String notificationTitle;
    private SampleApplicationApp app;

    public CustomMonitoringReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.app = (SampleApplicationApp) context.getApplicationContext();
        notificationTitle  = context.getString(R.string.app_name);
        super.onReceive(context,intent);
    }

    @Override
    protected void didEnterRegionForeground(MonitoredRegion region) {
        //createLocalNotification(notificationTitle , region.getMessageWRTLang()  + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds() , context);
    }
    @Override
    protected void didExitRegionForeground(MonitoredRegion region) {
        //createLocalNotification(notificationTitle,"Exit: "+ region.getMessageWRTLang(), context);
    }
    @Override
    protected void didEnterRegionBackground(MonitoredRegion region) {
        //createLocalNotification( notificationTitle , region.getMessageWRTLang()  + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds(), context);
    }
    @Override
    protected void didExitRegionBackground(MonitoredRegion region) {
        //createLocalNotification(notificationTitle , region.getMessageWRTLang() , context);
    }
    @Override
    protected void didEnterRegionAll(MonitoredRegion region) {
        //createLocalNotification(notificationTitle , region.getMessageWRTLang()  + ", time since last notification:"+ region.getTimeSinceLastFireInSeconds() , context);
        //save(region, "region_in");
        app.regions.add(region.getMajor() + "_" + region.getMinor());
        log(region, "in");
    }
    @Override
    protected void didExitRegionAll(MonitoredRegion region) {
        //createLocalNotification( notificationTitle ,"Exit: "+ region.getMessageWRTLang(), context);
        //save(region, "region_out");
        app.regions.remove(region.getMajor() + "_" + region.getMinor());
        log(region, "out");
    }
    
    private void log(MonitoredRegion region, String type){

        String str = "";
        for (String r: app.regions){
            str =str + r + ", ";
        }


        Log.i(TAG, "log: " + str);
        if ( app.textView != null)
            app.textView.setText(str);

        if (app.experimentName == null || app.experimentName.equals(""))
            return;

        ParseObject testObject = new ParseObject("Monitoring");
        if (app.location != null ){
            testObject.put("X", app.location.getX());
            testObject.put("Y", app.location.getY());
            testObject.put("floor", app.location.getFloor());
        }

        testObject.put("triggered_by_region_id", region.getMajor() + "_" + region.getMinor());
        testObject.put("trigger_type", type);
        testObject.put("timeStamp", Calendar.getInstance().getTime());
        testObject.put("installation_id", ParseInstallation.getCurrentInstallation().getInstallationId());
        testObject.put("experiment_name", app.experimentName);

        for (String r: app.regions){
            testObject.put("r_" + r, 1);
        }
        testObject.saveEventually();

    }
        

    private void save(final MonitoredRegion region, final String type){
        AppManager.getInstance().initialize(context, new AppManager.OnInitializedListener(){
            @Override
            public void onInitialized(boolean success) {
                BeaconNode beaconNode = new BeaconNode(region.getMajor() , region.getMinor());
                //get Location of this beacon
                MetaDataManager metaDataManager = AppManager.getInstance().getMetaDataManager();
                int index = metaDataManager.getTagsLocations(context).indexOf(new BeaconNodeConfigurtion(beaconNode.getMajor(), beaconNode.getMinor()));

                if (index != -1) {
                    BeaconNodeConfigurtion beaconNodeConfigurtion = metaDataManager.getTagsLocations(context).get(index);

                    try {
                        String parseAppId = "pey8pjhhotyDIusYHgSEF6SUI84ugMPVe6rqF2Ah";
                        String parseClientKey = "ZD6C8F4bhChdi0FiotmdbWjNDIvmlL5hbtVfHvsx";
                        Parse.enableLocalDatastore(context);
                        Parse.initialize(context, parseAppId, parseClientKey);
                        ParseInstallation.getCurrentInstallation().saveInBackground();
                    }catch (Exception  e){
                        Log.i(TAG, "onCreate: ",  e);
                    }

                    final ParseObject testObject = new ParseObject("Positions");
                    testObject.put("X", beaconNodeConfigurtion.getX());
                    testObject.put("Y", beaconNodeConfigurtion.getY());
                    testObject.put("floor", beaconNodeConfigurtion.getFloorIndex());
                    testObject.put("type", type);
                    testObject.put("region_id", region.getMajor() + "-" + region.getMinor());
                    testObject.put("timeStamp", Calendar.getInstance().getTime());
                    testObject.put("installation_id", ParseInstallation.getCurrentInstallation().getInstallationId());
                    testObject.saveEventually();
                    Log.i(TAG, "regionCallback: received");
                }
            }
        });
    }

    private void createLocalNotification(String title, String content, Context context){
        Log.i(TAG, "CustomMonitoringReceiver:content:" + content);

        if(content == null){
            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setTicker(content)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(Notification.PRIORITY_HIGH)//Heads-up
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.notification_small_color);//color
        }else {
            builder.setColor(0xfffeba13);// dark yellow background color similar to background of ic_launcher icon
            //builder.setSmallIcon(R.drawable.notification_small_white);//white NaviBees
            builder.setSmallIcon(R.drawable.notification_small_white_eye360);//white eye360
        }


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

    }

}
