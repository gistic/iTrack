package com.navibees.sdk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.license.LicenseManager;
import com.navibees.sdk.model.metadata.MetaDataManager;
import com.navibees.sdk.model.postioning.IndoorLocationListener;
import com.navibees.sdk.model.postioning.NaviBeesBeaconManager;
import com.navibees.sdk.model.postioning.PositionManager;
import com.navibees.sdk.model.routing.EsriRouting;
import com.navibees.sdk.model.routing.TurnByTurnNavigation;
import com.navibees.sdk.model.server.ParseServerManager;
import com.navibees.sdk.model.server.ServerManager;
import com.navibees.sdk.model.tts.TTSManager;
import com.navibees.sdk.util.AssetsManager;
import com.navibees.sdk.util.CommonUtils;

/**
 * Created by nabilnoaman on 4/15/15.
 */

final public class AppManager implements AssetsManager.AssetManagerListener, MetaDataManager.LocalMetaDataListener{

    private static AppManager ourInstance;

    private static MetaDataManager metaDataManager;
    private static PositionManager positionManager;
    private static ServerManager serverManager;
    private static AssetsManager assetsManager;
    private static NaviBeesBeaconManager naviBeesBeaconManager;
    private static LicenseManager licenseManager;
    private static TTSManager ttsManager = null;
    private static TurnByTurnNavigation turnByTurnNavigation;



    private OnInitializedListener mInitializationListener;
    private Context mContext;
    private ProgressDialog mProgressBar;
    private boolean isInitializing = false;


    public static AppManager getInstance() {
        if(ourInstance == null){
            ourInstance = new AppManager();
        }
        return ourInstance;
    }

    private AppManager() {
    }

    public MetaDataManager getMetaDataManager(){
        if(metaDataManager == null){
            metaDataManager = new MetaDataManager();
        }
        return metaDataManager;
    }

    public PositionManager getPositionManager(Activity activity , IndoorLocationListener indoorLocationListener){
        if(positionManager == null) {
            try {
                positionManager = new PositionManager(activity, indoorLocationListener);
            } catch (NaviBeesLicenseNotAuthorithedException e) {
                e.printStackTrace();
            } catch (NaviBeesLicenseExpireException e) {
                e.printStackTrace();
            }
        }

        return positionManager;
    }

    public PositionManager getPositionManager(Context context, IndoorLocationListener indoorLocationListener){
        if(positionManager == null) {
            try {
                positionManager = new PositionManager(context, indoorLocationListener);
            } catch (NaviBeesLicenseNotAuthorithedException e) {
                e.printStackTrace();
            } catch (NaviBeesLicenseExpireException e) {
                e.printStackTrace();
            }
        }

        return positionManager;
    }


    public NaviBeesBeaconManager getNaviBeesBeaconManager(Activity activity){
        if(naviBeesBeaconManager == null){
            if(positionManager != null){
                naviBeesBeaconManager = positionManager.getNaviBeesBeaconManager();
            }else {
                naviBeesBeaconManager = new NaviBeesBeaconManager(activity, null);
            }
        }
        return naviBeesBeaconManager;
    }

    public NaviBeesBeaconManager getNaviBeesBeaconManager(Context context){
        if(naviBeesBeaconManager == null){
            if(positionManager != null){
                naviBeesBeaconManager = positionManager.getNaviBeesBeaconManager();
            }else {
                naviBeesBeaconManager = new NaviBeesBeaconManager(context, null);
            }
        }
        return naviBeesBeaconManager;
    }

    public ServerManager getServerManager(){
        if(serverManager == null) {
            //In case we build our own server side instead Parse we should change returned ServerManager Implementation
            serverManager = new ParseServerManager();
        }

        return serverManager;
    }

    public AssetsManager getAssetsManager(Context context , AssetsManager.AssetManagerListener assetManagerListener){
        if(assetsManager == null) {
            assetsManager = new AssetsManager(context.getApplicationContext(), assetManagerListener);
        }

        return assetsManager;
    }

    public TTSManager getTTSManager(Context context){
//        if(ttsManager == null){
//            try {
//                ttsManager = new TTSManager(context);
//            } catch (NaviBeesLicenseNotAuthorithedException e) {
//                e.printStackTrace();
//            } catch (NaviBeesLicenseExpireException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        if(ttsManager != null) {
//            //check if user enabled this feature or no
//            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//            boolean isTTSEnabledByUser = sp.getBoolean(context.getString(R.string.com_navibees_sdk_preference_user_enabled_tts_key), false);
//            if (!isTTSEnabledByUser) {
//                return null;
//            }
//        }

        return ttsManager;
    }


    public EsriRouting getEsriRouting(Activity activity, EsriRouting.OnRoutingFinishedListener listener){
        try {
            return new EsriRouting(activity, listener);
        } catch (NaviBeesLicenseNotAuthorithedException e) {
            e.printStackTrace();
        } catch (NaviBeesLicenseExpireException e) {
            e.printStackTrace();
        }

        return null;
    }

    public TurnByTurnNavigation getTurnByTurnNavigation(Context context){
        if(turnByTurnNavigation == null){
            try {
                turnByTurnNavigation = TurnByTurnNavigation.getInstance(context);
            } catch (NaviBeesLicenseNotAuthorithedException e) {
                e.printStackTrace();
            } catch (NaviBeesLicenseExpireException e) {
                e.printStackTrace();
            }
        }
        return turnByTurnNavigation;
    }


    public LicenseManager getLicenseManager(){
        if(licenseManager == null){
            licenseManager = new LicenseManager();
        }

        return licenseManager;
    }

    public static void recycle(){
        metaDataManager = null;
        positionManager  = null;
        serverManager  = null;
        assetsManager = null;
        naviBeesBeaconManager = null;
        licenseManager = null;
        ttsManager = null;
        turnByTurnNavigation = null;
        ourInstance = null;
    }

    public void initialize(Context context, OnInitializedListener initializationListener){
        mContext = context;
        mInitializationListener = initializationListener;

        continueInitialize();

    }

    private void continueInitialize(){
        if(isAppConstantsEmpty() || getMetaDataManager().isMetaDataEmpty()) {
            if(mProgressBar != null && mProgressBar.isShowing()){
                mProgressBar.dismiss();
                mProgressBar = null;
            }
            mProgressBar = new ProgressDialog(mContext);
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressBar.setMessage(mContext.getString(R.string.progress_bar_init_message));
            mProgressBar.setCancelable(false);
            mProgressBar.setCanceledOnTouchOutside(false);
//            mProgressBar.show();

            if(!isInitializing) {
                isInitializing = true;
                getAssetsManager(mContext, this).copyAssetsFolderInBackground();
            }
        } else {
            mInitializationListener.onInitialized(checkLicense());
        }
    }

    @Override
    public void assetsCopyCallback(Boolean success) {
        //Toast.makeText(mContext, "Assets Copied : " + success.booleanValue(), Toast.LENGTH_SHORT).show();

        getMetaDataManager().initLocalMetaData(ApplicationConstants.mapResourcesMetadataPath);
        getMetaDataManager().setLocalMetaDataListener(this);
        getMetaDataManager().loadAllMetaDataJSONFilesInBackground(mContext);
    }


    @Override
    public void allMetaDataFilesAreReady(Boolean success) {
        if(mProgressBar != null && mProgressBar.isShowing()){
            mProgressBar.dismiss();
            mProgressBar = null;
        }
        isInitializing = false;
        success &= checkLicense();
        mInitializationListener.onInitialized(success);
        //If there is internet we will Sync With Server
        /*
        if(CommonUtils.isThereAreInternetConnection(mContext)) {
            getMetaDataManager().synchDataWithServer(mContext);
        }
        */
    }

    public interface OnInitializedListener{
        void onInitialized(boolean success);
    }


    private boolean isAppConstantsEmpty(){
        return TextUtils.isEmpty(ApplicationConstants.mapResourcesImagesPath)
                || TextUtils.isEmpty(ApplicationConstants.mapResourcesMetadataPath)
                || TextUtils.isEmpty(ApplicationConstants.mapResourcesNetworkDatasetsPath)
                || TextUtils.isEmpty(ApplicationConstants.mapResourcesTiledLayerPath);
    }

    private boolean checkLicense() {
        //Check License if Valid & Not Expired pass true to mInitializationListener else pass false
        try {
            getLicenseManager().verify(mContext , null);
            return true;
        }catch (NaviBeesLicenseNotAuthorithedException e1){
            e1.printStackTrace();
        }
        catch (NaviBeesLicenseExpireException e2){
            e2.printStackTrace();
        }
        return false;
    }
}