package com.navibees.sdk;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.navibees.sdk.activity.NotificationHandlerActivity;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.json.NaviBeesNotification;
import com.navibees.sdk.util.CommonUtils;
import com.navibees.sdk.util.Log;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import java.util.Random;

/**
 * Created by nabilnoaman on 11/16/15.
 */
public class CustomPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    private static final String TAG = "CustomPushBroadcastReceiver";

    private Gson gson = new Gson();

   /*
   {
	  "title": "Notification Title",
	  "message": "Notification Body",
	  "icon": "http://www.metta.org.uk/travel/images/wasp.jpg",
	  "photo": "http://www.metta.org.uk/travel/images/wasp.jpg",
	  "video": "https://www.youtube.com/watch?v=K5KAc5CoCuk"
	}
*/



    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Broadcast_Notifications);
            super.onReceive(context, intent);
        } catch (NaviBeesLicenseNotAuthorithedException e) {
            Log.e(TAG, e.toString());
        } catch (NaviBeesLicenseExpireException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onPushOpen(Context context, Intent intent) {
        Log.i(TAG, "onPushOpen");
        try {
            AppManager.getInstance().getLicenseManager().verify(context, NaviBeesFeature.Broadcast_Notifications);

            // Send a Parse Analytics "push opened" event
            ParseAnalytics.trackAppOpenedInBackground(intent);

            String uriString = null;
            String pushData = intent.getStringExtra(super.KEY_PUSH_DATA);
            NaviBeesNotification naviBeesNotification = gson.fromJson(pushData, new TypeToken<NaviBeesNotification>() {
            }.getType());

            if (!TextUtils.isEmpty(naviBeesNotification.getPhoto())) {
                uriString = naviBeesNotification.getPhoto();
            } else if (!TextUtils.isEmpty(naviBeesNotification.getVideo())) {
                uriString = naviBeesNotification.getVideo();
            }

            Intent activityIntent = null;
            if (!TextUtils.isEmpty(uriString)) {

                activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(activityIntent);

                /*
                //Incase we want to handle photo/video by our self
                activityIntent = new Intent(context, getNotificationHanlderActivity());
                activityIntent.putExtras(intent.getExtras());
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(activityIntent);
                */
            }


        } catch (NaviBeesLicenseNotAuthorithedException e) {
            Log.e(TAG, e.toString());
        } catch (NaviBeesLicenseExpireException e) {
            Log.e(TAG, e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Can't convert pushData string to naviBeesNotification");
            e.printStackTrace();
        }
    }


    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        // do nothing
    }

    @Override
    public void onPushReceive(Context context, Intent intent) {
        Log.i(TAG, "onPushReceive");

//        CommonUtils.dumpIntent(intent);
        try {
            AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Broadcast_Notifications);
            createNotification(intent ,context);
        } catch (NaviBeesLicenseNotAuthorithedException e) {
            Log.e(TAG, e.toString());
        } catch (NaviBeesLicenseExpireException e) {
            Log.e(TAG, e.toString());
        }

    }

    private void createNotification(final Intent intent , final Context context) {
        NaviBeesNotification naviBeesNotification;
        String message = "";
        String title = "";
        String largeIcon = null;
        try {
            String pushData = intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA);
            naviBeesNotification = gson.fromJson(pushData, new TypeToken<NaviBeesNotification>() {
            }.getType());
            largeIcon = naviBeesNotification.getIcon();
            title = naviBeesNotification.getTitle();
            message = naviBeesNotification.getMessage();
        } catch (Exception e) {
            Log.e(TAG, "Can't convert pushData string to naviBeesNotification");
            e.printStackTrace();
        }


        if (TextUtils.isEmpty(message)) {
            message = getDefaultMessage(context);
        }

        if (TextUtils.isEmpty(title)) {
            title = getDefaultTitle(context);
        }

        Log.i(TAG, "message is " + message);
        Log.i(TAG, "title is " + title);
        Log.i(TAG, "icon is " + largeIcon);


        Bundle extras = intent.getExtras();

        Random random = new Random();
        int contentIntentRequestCode = random.nextInt(Integer.MAX_VALUE);
        int deleteIntentRequestCode = random.nextInt(Integer.MAX_VALUE);

        // Security consideration: To protect the app from tampering, we require that intent filters
        // not be exported. To protect the app from information leaks, we restrict the packages which
        // may intercept the push intents.
        String packageName = context.getPackageName();

        Intent contentIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_OPEN);
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);

        Intent deleteIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);

        final PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        final Bitmap defaultLargeIcon = getDefaultLargerIcon(context);

        if (!TextUtils.isEmpty(largeIcon)) {
            //float multiplier = CommonUtils.getImageFactor(context , ((float)DisplayMetrics.DENSITY_XXHIGH/DisplayMetrics.DENSITY_MEDIUM));

            //ImageRequest imageRequest = ImageRequest.fromUri(largeIcon);

            int[] largeIconSize = CommonUtils.getNotificationLargeIconSize(context);
            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(largeIcon))
                    .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                    //.setDownsampleEnabled(true)
                    .setResizeOptions(new ResizeOptions(largeIconSize[0], largeIconSize[1]))
                    .build();

            final String finalTitle = title;
            final String finalMessage = message;


            //https://github.com/facebook/fresco/issues/202
            Fresco.initialize(context);
            DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(imageRequest, context);

            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable final Bitmap bitmap) {
                    // You can use the bitmap in only limited ways
                    // No need to do any cleanup.
                    NotificationManager myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if(bitmap != null) {
                        myNotificationManager.notify((int) System.currentTimeMillis(), buildNotification(context, finalTitle, finalMessage, bitmap, pContentIntent, pDeleteIntent));
                    }else {
                        myNotificationManager.notify((int) System.currentTimeMillis(), buildNotification(context, finalTitle, finalMessage, defaultLargeIcon, pContentIntent, pDeleteIntent));
                    }
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here.
                    NotificationManager myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    myNotificationManager.notify((int) System.currentTimeMillis(), buildNotification(context , finalTitle ,finalMessage , defaultLargeIcon , pContentIntent , pDeleteIntent ));

                }
            }, CallerThreadExecutor.getInstance());
        }else {

            NotificationManager myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            myNotificationManager.notify((int) System.currentTimeMillis(), buildNotification(context , title ,message, defaultLargeIcon , pContentIntent , pDeleteIntent ));

        }


    }

    private Notification buildNotification(Context context , String title, String message, Bitmap largeIcon , PendingIntent contentIntent ,PendingIntent deleteIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(message)
                .setColor(getSmallIconBackgroundColor(context))
                .setPriority(Notification.PRIORITY_HIGH)//Heads-up
                .setSmallIcon(getSmallIconId(context))
                .setLargeIcon(largeIcon)
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_ALL);

        return builder.build();
    }

    protected int getSmallIconBackgroundColor(Context context) {
        return context.getResources().getColor(android.R.color.holo_orange_light);
    }


    //return app label
    protected String getDefaultTitle(Context context) {
        Log.i(TAG, "getDefaultTitle");
        ApplicationInfo appInfo = context.getApplicationInfo();
        return context.getPackageManager().getApplicationLabel(appInfo).toString();
    }

    protected String getDefaultMessage(Context context) {
        Log.i(TAG, "getDefaultMessage");
        return "";
    }

    protected Bitmap getDefaultLargerIcon(Context context){
        Log.i(TAG, "getDefaultLargerIcon");
       return BitmapFactory.decodeResource(context.getResources(), R.drawable.com_navibees_sdk_notification_large_ic);
    }

    protected int getSmallIconId(Context context) {
        Log.i(TAG, "getSmallIconId");
        return R.drawable.com_navibees_sdk_notification_small;
       // return R.drawable.notification;
    }

    private Bitmap getLargeIcon(Context context, String largeIconURL) {
        Log.i(TAG, "getLargeIcon : " + largeIconURL);

        try {
            if (!TextUtils.isEmpty(largeIconURL)) {
                //Disable StrictMode then renable it to avoid NetworkOnMainThreadException then we will
                //download the image in another service
                //http://code.tutsplus.com/tutorials/android-best-practices-strictmode--mobile-7581
                StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(oldPolicy)
                        .permitNetwork()
                        .build());

                Bitmap bmURL = CommonUtils.getBitmapFromURL(largeIconURL);

                //enable StrictMode again
                StrictMode.setThreadPolicy(oldPolicy);

                float multiplier = CommonUtils.getImageFactor(context , ((float)DisplayMetrics.DENSITY_XXHIGH/DisplayMetrics.DENSITY_MEDIUM));
                bmURL = Bitmap.createScaledBitmap(bmURL, (int) (bmURL.getWidth() * multiplier), (int) (bmURL.getHeight() * multiplier), false);
                if (bmURL != null) {
                    return bmURL;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.com_navibees_sdk_notification_large_ic);
    }


    private Class<? extends Activity> getNotificationHanlderActivity(){
        return NotificationHandlerActivity.class;
    }

}
