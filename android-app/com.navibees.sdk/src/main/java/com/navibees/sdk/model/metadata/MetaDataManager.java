package com.navibees.sdk.model.metadata;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.json.ActivityGroup;
import com.navibees.sdk.model.metadata.json.ApplicationConfiguration;
import com.navibees.sdk.model.metadata.json.BeaconNodeConfigurtion;
import com.navibees.sdk.model.metadata.json.Facility;
import com.navibees.sdk.model.metadata.json.Floor;
import com.navibees.sdk.model.metadata.json.MonitoredRegion;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.model.metadata.json.POICategory;
import com.navibees.sdk.model.metadata.json.Portal;
import com.navibees.sdk.model.server.ServerManager;
import com.navibees.sdk.util.CommonUtils;
import com.navibees.sdk.util.Log;
import com.navibees.sdk.util.Toast;

import java.util.Date;
import java.util.List;

/**
 * Created by nabilnoaman on 5/7/15.
 */
final public class MetaDataManager implements DataChangeListener {

    private LocalMetaData localMetaData;
    private ServerManager serverManager;

    private Context context;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    static final String TAG = "MetaDataManager";

    private List<Floor> floors = null;
    private List<POICategory> poisCategories = null;
    private List<POI> pois = null;
    private List<Portal> portals = null;
    private List<Facility> facilities = null;
    private List<IndoorLocationRestriction> restrictions = null;
    private String restrictionWrappersAsJSON = null;
    private LocalMetaDataListener localMetaDataListener;

    private List<BeaconNodeConfigurtion> beaconNodeConfigurtionList;
    private long beaconsFileLastModifiedDate;

    private List<MonitoredRegion> monitoredRegions = null;

    private List<ActivityGroup> activityCategories = null;


    public MetaDataManager() {
        localMetaData = new LocalMetaData();
    }

    public void setLocalMetaDataListener(LocalMetaDataListener localMetaDataListener){
        this.localMetaDataListener = localMetaDataListener;
    }

    public void synchDataWithServer(Context context){

        Log.i(TAG , "synchDataWithServer");
        this.context = context;

        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();

        serverManager = AppManager.getInstance().getServerManager();

        try {
            serverManager.getApplicationConfiguration(context, this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public ApplicationConfiguration getApplicationConfiguration(Context context) {
        return localMetaData.getApplicationConfiguration(context);
    }

    public List<POICategory> getPOIsCategories(Context context) {
        return localMetaData.getPOIsCategories(context);
    }


    public List<POI> getPOIs(Context context) {
        return localMetaData.getPOIs(context);
    }


    public List<BeaconNodeConfigurtion> getTagsLocations(Context context) {
        return localMetaData.getTagsLocations(context);
    }


    public List<Floor> getFloors(Context context) {
        try {
            AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature._2D_Maps);
            return localMetaData.getFloors(context);
        } catch (NaviBeesLicenseNotAuthorithedException e) {
            e.printStackTrace();
        } catch (NaviBeesLicenseExpireException e) {
            e.printStackTrace();
        }

        return null;
    }


    public List<Portal> getPortals(Context context) {
        return localMetaData.getPortals(context);
    }

    public List<Facility> getFacilities(Context context) {
        return localMetaData.getFacilities(context);
    }


    public List<MonitoredRegion> getMonitoredRegions(Context context) {
        //ToDo Check Monitoring exist in License or not
        try {
            AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Location_Based_Notifications);
            return localMetaData.getMonitoredRegions(context);
        } catch (NaviBeesLicenseNotAuthorithedException e) {
            CommonUtils.monitoringNotAuthorizedToBeEnabled(context);
            e.printStackTrace();
        } catch (NaviBeesLicenseExpireException e) {
            CommonUtils.monitoringNotAuthorizedToBeEnabled(context);
            e.printStackTrace();
        }

        return null;
    }



    public List<IndoorLocationRestriction> getRestrictions(Context context) {
        return localMetaData.getRestrictions(context);
    }

    public List<ActivityGroup> getActivityCategories(Context context){
        return localMetaData.getActivityCategories(context);
    }

    @Override
    public void appConfigurationChangeCallback(ApplicationConfiguration applicationConfiguration , long lastModifiedDate) {

        Log.i(TAG , "appConfigurationChangeCallback:lastModifiedDate: "+lastModifiedDate);

        if(applicationConfiguration != null) {

            localMetaData.setApplicationConfiguration(context, applicationConfiguration);

            setLastModifiedDateForAppConfiguration(lastModifiedDate);

            Toast.makeText(context, "ApplicationConfiguration updated", Toast.LENGTH_SHORT);
        }else {
            Log.i(TAG , "appConfigurationChangeCallback:applicationConfiguration:"+applicationConfiguration);
        }

        serverManager.getBuildingMetaData(context , this);
    }

    @Override
    public void poiCategoriesChangeCallback(List<POICategory> poiCategoryList  , long lastModifiedDate) {
        Log.i(TAG, "poiCategoriesChangeCallback");

        if(poiCategoryList != null) {
            this.poisCategories = poiCategoryList;
        }
    }

    @Override
    public void poisChangeCallback(List<POI> poiList  , long lastModifiedDate) {
        Log.i(TAG, "poisChangeCallback");

        if(poiList != null) {
            this.pois = poiList;
        }
    }

    @Override
    public void floorsChangeCallback(List<Floor> floorList  , long lastModifiedDate) {
        Log.i(TAG , "floorsChangeCallback");
        if(floorList != null) {
            this.floors = floorList;
        }
    }

    @Override
    public void portalsChangeCallback(List<Portal> portals  , long lastModifiedDate) {
        Log.i(TAG , "portalsChangeCallback");

        if(portals != null) {
            this.portals = portals;
        }
    }

    @Override
    public void facilitiesChangeCallback(List<Facility> facilities  , long lastModifiedDate) {
        Log.i(TAG , "facilitiesChangeCallback");

        if(facilities != null) {
            this.facilities = facilities;
        }
    }

    @Override
    public void restrictionsChangeCallback(List<IndoorLocationRestriction> restrictionList  , long lastModifiedDate , String restrictionsWrapperFromServer) {
        Log.i(TAG , "restrictionsChangeCallback");

        if(restrictionList != null && restrictionsWrapperFromServer != null) {
            this.restrictions = restrictionList;
            this.restrictionWrappersAsJSON = restrictionsWrapperFromServer;
        }
    }

    @Override
    public void monitoredRegionsChangeCallback(List<MonitoredRegion> monitoredRegions, long lastModifiedDate) {
        Log.i(TAG , "monitoredRegionsChangeCallback");

        if(monitoredRegions != null) {
            this.monitoredRegions = monitoredRegions;
        }
    }

    @Override
    public void activitiesChangeCallback(List<ActivityGroup> activityCategories, long lastModifiedDate) {
        Log.i(TAG, "activitesChangeCallBack");

        if(activityCategories != null){
            this.activityCategories = activityCategories;
        }
    }


    @Override
    public void beaconNodeConfigurtionChangeCallback(List<BeaconNodeConfigurtion> beaconNodeConfigurtionList  , long beaconsFileLastModifiedDate) {
        Log.i(TAG , "beaconNodeConfigurtionChangeCallback:lastModifiedDate: "+beaconsFileLastModifiedDate);

        if(beaconNodeConfigurtionList != null) {
            this.beaconNodeConfigurtionList = beaconNodeConfigurtionList;
            this.beaconsFileLastModifiedDate = beaconsFileLastModifiedDate;
            new SaveBeaconsFileTask().execute();

        }
    }




    public Date getLastModifiedDateForAppConfiguration(){
        //If there is no modification date exist return Date(0) i.e 1970
        if(sp != null)
            return new Date(sp.getLong(ApplicationConstants.APP_CONFIGURATIONS_LAST_MODIFICATION_DATE_KEY , 0));
        else
            return new Date(0);
    }


    public Date getLastModifiedDateForBuildingMetaData(){
        //If there is no modification date exist return Date(0) i.e 1970
        if(sp != null)
            return new Date(sp.getLong(ApplicationConstants.BUILDING_META_DATA_LAST_MODIFICATION_DATE_KEY , 0));
        else
            return new Date(0);
    }



    public Date getLastModifiedDateForBeaconsNodeConfigurations(){
        //If there is no modification date exist return Date(0) i.e 1970
        if(sp != null)
            return new Date(sp.getLong(ApplicationConstants.BEACONS_CONFIGURATIONS_LAST_MODIFICATION_DATE_KEY , 0));
        else
            return new Date(0);
    }

    public void setLastModifiedDateForBeaconsNodeConfigurations(long lastModifiedDateForBeaconsNodeConfigurations){
        editor.putLong(ApplicationConstants.BEACONS_CONFIGURATIONS_LAST_MODIFICATION_DATE_KEY , lastModifiedDateForBeaconsNodeConfigurations);
        editor.commit();
    }

    public void setLastModifiedDateForAppConfiguration(long lastModifiedDateForAppConfiguration){
        editor.putLong(ApplicationConstants.APP_CONFIGURATIONS_LAST_MODIFICATION_DATE_KEY , lastModifiedDateForAppConfiguration);
        editor.commit();
    }

    public synchronized void updateBuildingMetaDataFiles(long lastModifiedDateForBuildingMetaData){

        Log.i(TAG , "updateBuildingMetaDataFiles:lastModifiedDateForBuildingMetaData: "+lastModifiedDateForBuildingMetaData);

        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles: (poisCategories != null):"+(poisCategories != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles:  (pois != null) :"+ (pois != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles: (floors != null) :"+ (floors != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles: (portals != null) :"+(portals != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles: (facilities != null) :"+(facilities != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles: (restrictions != null) :"+ (restrictions != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles: (monitoredRegions != null) :"+ (monitoredRegions != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles: (restrictionWrappersAsJSON != null) :"+ (restrictionWrappersAsJSON != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: -----------------------------");
        Log.i(TAG , "updateBuildingMetaDataFiles: (activityCategories != null) :"+ (activityCategories != null));
        Log.i(TAG , "updateBuildingMetaDataFiles: *****************************");

        //All files are optionals except Floors file , so if local files == null
        boolean isAllFilesUpadted = (floors != null);

        if(getPOIsCategories(context) != null){
            isAllFilesUpadted &= (poisCategories != null);
        }

        if(getPOIs(context) != null){
            isAllFilesUpadted &= (pois != null);
        }

        if(getPortals(context) != null){
            isAllFilesUpadted &= (portals != null);
        }

        if(getFacilities(context) != null){
            isAllFilesUpadted &= (facilities != null);
        }

        if(getMonitoredRegions(context) != null){
            isAllFilesUpadted &= (monitoredRegions != null);
        }

        if(getRestrictions(context) != null){
            isAllFilesUpadted &= (restrictions != null) && (restrictionWrappersAsJSON != null);
        }

        if(getActivityCategories(context) != null){
            isAllFilesUpadted &= (activityCategories != null);
        }

        if(isAllFilesUpadted){
            new SaveBuildingMetaDataJSONFilesTask().execute(lastModifiedDateForBuildingMetaData);

        }else {

             Toast.makeText(context, "Can't update building metadata" ,Toast.LENGTH_SHORT);
        }

    }

    public void loadAllMetaDataJSONFilesInBackground(Context context){
        this.context = context;
        new LoadAllMetaDataJSONFilesTask().execute();
    }

    public boolean isMetaDataEmpty(){
        return localMetaData.isMetaDataEmpty();
    }

    public void initLocalMetaData(String mapResourcesMetaDataPath){
        localMetaData.init(mapResourcesMetaDataPath);
    }
    private class LoadAllMetaDataJSONFilesTask extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            //optionalFiles
            localMetaData.getActivityCategories(context);
            localMetaData.getPOIsCategories(context);
            localMetaData.getPOIs(context);
            localMetaData.getTagsLocations(context);
            localMetaData.getPortals(context);
            localMetaData.getFacilities(context);
            localMetaData.getRestrictions(context);

            return (localMetaData.getFloors(context)!= null) && (localMetaData.getApplicationConfiguration(context) != null);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            localMetaDataListener.allMetaDataFilesAreReady(success);
        }
    }


    public interface LocalMetaDataListener{
        public void allMetaDataFilesAreReady(Boolean success);
    }


    private class SaveBuildingMetaDataJSONFilesTask extends AsyncTask<Long, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Long... arg)
        {
            Log.i(TAG , "SaveAllMetaDataJSONFilesTask:doInBackground: "+arg[0]);

            try {

                boolean success =
                        localMetaData.setCategories(poisCategories) &&
                                localMetaData.setPois(pois) &&
                                localMetaData.setFloors(floors) &&
                                localMetaData.setPortals(portals) &&
                                localMetaData.setFacilities(facilities) &&
                                localMetaData.setMonitoredRegions(monitoredRegions) &&
                                localMetaData.setRestrictions(restrictions , restrictionWrappersAsJSON) &&
                                localMetaData.setActivityCategories(activityCategories);

                if(success) {
                    editor.putLong(ApplicationConstants.BUILDING_META_DATA_LAST_MODIFICATION_DATE_KEY, /*lastModifiedDateForBuildingMetaData*/ arg[0]);
                    success &= editor.commit();
                }

                return success;

            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Log.i(TAG , "SaveAllMetaDataJSONFilesTask:onPostExecute:update: "+success);

            Toast.makeText(context, "Building Metadata updated:"+success, Toast.LENGTH_SHORT);
        }
    }



    private class SaveBeaconsFileTask extends AsyncTask<Void , Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Void... arg)
        {
            Log.i(TAG , "SaveBeaconsFileTask:doInBackground");

            try {

                localMetaData.setLocations(beaconNodeConfigurtionList);
                setLastModifiedDateForBeaconsNodeConfigurations(beaconsFileLastModifiedDate);

                return true;

            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Log.i(TAG , "SaveBeaconsFileTask:onPostExecute:update: "+success);

            Toast.makeText(context, "BeaconsFile updated:"+success, Toast.LENGTH_SHORT);
        }
    }

}