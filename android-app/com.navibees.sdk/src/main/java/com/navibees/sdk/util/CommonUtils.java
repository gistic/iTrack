package com.navibees.sdk.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.R;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.model.metadata.json.POICategory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Created by nabilnoaman on 10/5/15.
 */
public class CommonUtils {

    static final String TAG = "CommonUtils";

    /**
     * Checking for all possible internet providers
     * **/
    public static boolean isThereAreInternetConnection(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network[] networks = connectivityManager.getAllNetworks();
                NetworkInfo networkInfo;
                for (Network mNetwork : networks) {
                    networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                    Log.i(TAG, " Network Name : " + networkInfo.getTypeName());

                    if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {

                        Log.i(TAG, "Connected Network Name: " + networkInfo.getTypeName());
                        return true;
                    }
                }
            } else {
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        Log.i(TAG, "Network Name: " + anInfo.getTypeName());
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            Log.i(TAG, "Connected Network Name: " + anInfo.getTypeName());
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    public static boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    public static boolean isLocationServicesEnabled(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null){
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    ||
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }

    public static boolean isArabicLang(){
        return Locale.getDefault().getLanguage().equals("ar");
    }

    public static void showNeededPermissionsToReadBeacons(Context context){
        if(!isBluetoothEnabled()) {
            showSettingsAlertForBluetooth(context);
        }else {
            showPermissionsForMarshmallow(context);
        }
    }

    private static void showPermissionsForMarshmallow(Context context){
        if(isMarshmallowOrAbove() && !isLocationServicesGranted(context)){
            requestLocationPermission((Activity) context);
        }else if(isMarshmallowOrAbove() && isLocationServicesGranted(context) && !isLocationServicesEnabled(context)){
            showSettingsAlertForLocation(context);
        }
    }

    @TargetApi(23)
    private static void requestLocationPermission(Activity activity){
        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ApplicationConstants.REQUEST_CODE_PERMISSION_COARSE_LOCATION);
    }

    public static void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults, Context context) {
        if(requestCode == ApplicationConstants.REQUEST_CODE_PERMISSION_COARSE_LOCATION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("AppManager", "location access granted");
                showPermissionsForMarshmallow(context);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.request_location_permission_title);
                builder.setMessage(R.string.location_permission_denied_message);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }

        }
    }

    public static boolean isMarshmallowOrAbove(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @TargetApi(23)
    public static boolean isLocationServicesGranted(Context context){
        return context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private static void showSettingsAlertForBluetooth(final Context context){
        //AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
        //getApplicationContext() fire exception"Unable to add window - token null is not for an application"
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.enable_bluetooth_dialog_title);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.enable_bluetooth_dialog_message);

        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.enable();

                showPermissionsForMarshmallow(context);
            }
        });

        alertDialog.setNegativeButton(android.R.string.no, null);

        // Showing Alert Message
        alertDialog.show();
    }

    private static void showSettingsAlertForLocation(final Context context){
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        LocationRequest lowRequest = LocationRequest.create();
        lowRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(lowRequest);
        builder.setNeedBle(true);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult((Activity) context, ApplicationConstants.REQUEST_CODE_ENABLE_LOCATION);
                        } catch (Exception e) {
                        }
                        break;
                }
            }
        });


    }

    public static boolean isFirstTimeInCurrentVersion(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int storedBuildVersion = sp.getInt(ApplicationConstants.MAP_RESOURCES_APP_VERSION_KEY, -1);
        try {
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            if( versionCode > storedBuildVersion){
                return true;
            }else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return true;
        }
    }

    /***
     * Check if poi name or its tags match all parts in query in any order
     * @param query search query , can contain multi part separated with spaces
     * @param poi
     * @return
     */
    public static boolean isPOIMatch(String query , POI poi){

        if(query != null && poi != null){

            //Search POIName
            boolean matchPOIName = searchInString(poi.getNameWRTLang() , query);
            if(matchPOIName){
                return true;
            }


            //Search Tags
            if((poi.getTags() != null) && (poi.getTags().size() > 0 )){
                for(String tag:poi.getTags()){
                    boolean matchPOITag = searchInString(tag , query);
                    if(matchPOITag){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /***
     *  Check if poiCategory name match all parts in query in any order
     * @param query search query , can contain multi part separated with spaces
     * @param poiCategory
     * @return
     */
    public static boolean isCategoryMatch(String query , POICategory poiCategory) {

        if(query != null && poiCategory != null) {
            return searchInString(poiCategory.getNameWRTLang() , query);
        }

        return false;
    }

    public static boolean searchInString(String originalString, String query) {
        //Similar to Search in Contacts App.
        // Break out all of the parts(tokens) of the query
        // by splitting on white space
        //Search in starting of all parts in String
        String[] queryParts          = query.toUpperCase().split(" ");
        String[] stringParts = originalString.toUpperCase().split(" ");

        boolean result = false;
        for (int queryIndex = 0 ; queryIndex < queryParts.length ; queryIndex++) {

            boolean partMatch = false;

            for(String sPart : stringParts) {
                if (sPart.startsWith(queryParts[queryIndex])) {
                    partMatch = true;
                    break;
                }
            }

            if(queryIndex == 0){
                result = partMatch;
            }else {
                result = result && partMatch;
            }
        }

        return result;
    }



    public static void dumpIntent(Intent i){
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.i(TAG, "Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.i(TAG,"[" + key + "=" + bundle.get(key)+"]");
            }
            Log.i(TAG, "Dumping Intent end");
        }
    }


    public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     *
     * px = dp * (dpi/160)
     * Note: 1dp == 1 pixel in 160 dpi which is mdpi
     *
     * Small Icon : it's 24dp size with a 2dp padding , means 22dp content
     * Large Icon : it's 64dp size
     * assuming we will use
     *
     * Small Icon = 24dp == 24 px on mdpi
     * ldpi     --> 18*18
     * mdpi     --> 24*24
     * hdpi     --> 36*36
     * xhdpi    --> 48*48
     * xxhdpi   --> 72x72
     * xxxhdpi  --> 96x96
     *
     * Large Icon = 64dp == 64 px on mdpi
     * ldpi     --> 48*48
     * mdpi     --> 64*64
     * hdpi     --> 96*96
     * xhdpi    --> 128*128
     * xxhdpi   --> 192x192
     * xxxhdpi  --> 256x256
     *
     *
     * dpi:
     * ldpi     --> 120
     * mdpi     --> 160
     * hdpi     --> 240
     * xhdpi    --> 320
     * xxhdpi   --> 480
     * xxxhdpi  --> 640
     *
     * Assume we will use one image for large icon on server which is xxhdpi (192x192 pixel) i.e factor = 480/160 = 3
     * referanceDensityOfOriginalImage = 3
     */
    public static float getImageFactor(Context context , float referanceDensityOfOriginalImage){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float multiplier=metrics.density/referanceDensityOfOriginalImage;
        return multiplier;
    }

    public static int[] getNotificationLargeIconSize(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenDPI = metrics.densityDpi;

        if (screenDPI >= DisplayMetrics.DENSITY_XXXHIGH) {
            return new int[]{256 , 256};
        } else if (screenDPI >= DisplayMetrics.DENSITY_XXHIGH) {
            return new int[]{192 , 192};
        } else if (screenDPI >= DisplayMetrics.DENSITY_XHIGH) {
            return new int[]{128 , 128};
        } else if (screenDPI >= DisplayMetrics.DENSITY_HIGH) {
            return new int[]{96 , 96};
        }else if (screenDPI >= DisplayMetrics.DENSITY_MEDIUM) {
            return new int[]{64 , 64};
        } else {
            return new int[]{48 , 48};
        }

    }



    public static void monitoringNotAuthorizedToBeEnabled(Context context){
        if(context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(ApplicationConstants.BACKGROUND_MONITORED_REGIONS_KEY, "-");
            editor.commit();
        }
    }
}
