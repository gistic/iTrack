package com.navibees.sdk.model.metadata;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.model.metadata.json.ActivityGroup;
import com.navibees.sdk.model.metadata.json.ApplicationConfiguration;
import com.navibees.sdk.model.metadata.json.BeaconNodeConfigurtion;
import com.navibees.sdk.model.metadata.json.Facility;
import com.navibees.sdk.model.metadata.json.Floor;
import com.navibees.sdk.model.metadata.json.IndoorLocationRestrictionWrapper;
import com.navibees.sdk.model.metadata.json.MonitoredRegion;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.model.metadata.json.POICategory;
import com.navibees.sdk.model.metadata.json.Portal;
import com.navibees.sdk.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nabilnoaman on 4/14/15.
 */
class LocalMetaData {


    private Gson gson = new Gson();

    static final String TAG = "LocalMetaData";

    private String mapResourcesMetaDataPOIsCategoriesPath = null ;
    private String mapResourcesMetaDataPOIsPath    = null ;
    private String mapResourcesMetaDataTagsPath = null ;
    private String mapResourcesMetaDataFloorsPath = null ;
    private String mapResourcesMetaDataPortalsPath = null;
    private String mapResourcesMetaDataFacilitiesPath = null;
    private String mapResourcesMetaDataRestrictionsPath = null;
    private String mapResourcesMetaDataMonitoredRegionsPath = null;
    private String mapResourcesMetaDataActivityCategoriesPath = null;

    private ApplicationConfiguration applicationConfiguration;
    private List<POICategory> categories;
    private List<POI> pois;

    private List<BeaconNodeConfigurtion> locations;
    private List<Floor> floors;
    private List<Portal> portals;
    private List<Facility> facilities;
    private List<IndoorLocationRestriction> restrictions;
    private List<MonitoredRegion> monitoredRegions;
    private List<ActivityGroup> activityCategories;

    public void init(String mapResourcesMetaDataPath){
        mapResourcesMetaDataPOIsCategoriesPath = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_POIs_CATEGORIES;
        mapResourcesMetaDataPOIsPath    = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_POIs;
        mapResourcesMetaDataTagsPath = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_TAGs;
        mapResourcesMetaDataFloorsPath = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_FLOORs;
        mapResourcesMetaDataPortalsPath = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_PORTALs;
        mapResourcesMetaDataFacilitiesPath = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_FACILITIES;
        mapResourcesMetaDataRestrictionsPath = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_RESTRICTIONs;
        mapResourcesMetaDataMonitoredRegionsPath = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_MONITORED_REGIONS;
        mapResourcesMetaDataActivityCategoriesPath = mapResourcesMetaDataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_ACTVITIES;
    }

    public ApplicationConfiguration getApplicationConfiguration(Context context) {
        Log.i(TAG, " ----- getApplicationConfiguration");
        String fullPath = context.getFilesDir().getAbsolutePath()+ "/" + ApplicationConstants.MAP_RESOURCES_APP_CONFIGURATIONS ;//Internal Memory
        if(applicationConfiguration == null) {
            String appConfigurations = ReadFromfile(fullPath);
            Log.i(TAG, "applicationConfigurationJson -----  :"+ appConfigurations);
           try {
               applicationConfiguration = gson.fromJson(appConfigurations, new TypeToken<ApplicationConfiguration>() {
               }.getType());
           }catch (Exception e){
               e.printStackTrace();
           }
        }
        return applicationConfiguration;
    }

    public List<POICategory> getPOIsCategories(Context context) {
        Log.i(TAG, "-----  getPOIsCategories -----  :");
        if(categories == null) {

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata");
            }
            mapResourcesMetaDataPOIsCategoriesPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_POIs_CATEGORIES;

            String categoriesJson = ReadFromfile(mapResourcesMetaDataPOIsCategoriesPath);
            Log.i(TAG, "categoriesJson -----  :"+ categoriesJson);
            try {
                categories = gson.fromJson(categoriesJson, new TypeToken<List<POICategory>>() {
                }.getType());
            }catch (Exception e){
                e.printStackTrace();
            }
         }

        //read filter status for each category from shared pref.
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());


        if(categories != null)
        for(POICategory category:categories){
            if(category.isValid()) {
                category.setFilterEnable(sp.getBoolean(category.getName(), true));
            }else {
                categories.remove(category);
            }
        }

        return categories;
    }


    public List<POI> getPOIs(Context context) {
        Log.i(TAG, "---#########----getPOIs  ");
        if(pois == null) {

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata");
            }
            mapResourcesMetaDataPOIsPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_POIs;

            String allPOIsJson = ReadFromfile(mapResourcesMetaDataPOIsPath);
            Log.i(TAG, "allPOIsJson ---#########----  :"+ allPOIsJson);
            try {
                pois = gson.fromJson(allPOIsJson, new TypeToken<List<POI>>() {
                }.getType());
            }catch (Exception e){
                e.printStackTrace();
            }
            if(pois != null)
            for(POI poi:pois){
                if(!poi.isValid()){
                    pois.remove(poi);
                }
            }
        }
        return pois;
    }

    public List<BeaconNodeConfigurtion> getTagsLocations(Context context) {
        Log.i(TAG, "--******---  getTagsLocations ");
        if(locations == null) {

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata");
            }
            mapResourcesMetaDataTagsPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_TAGs;

            String allTagLocationsJson = ReadFromfile(mapResourcesMetaDataTagsPath);
            Log.i(TAG, "allTagLocationsJson --******---  :"+ allTagLocationsJson);
            try {
                locations = gson.fromJson(allTagLocationsJson, new TypeToken<List<BeaconNodeConfigurtion>>() {
                }.getType());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return locations;
    }


    public List<Floor> getFloors(Context context) {
        Log.i(TAG, "--******---   getFloors");
        if(floors == null) {

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata");
            }
            mapResourcesMetaDataFloorsPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_FLOORs;

            String allFloorsJson = ReadFromfile(mapResourcesMetaDataFloorsPath);
            Log.i(TAG, "allFloorsJson --******---  :"+ allFloorsJson);
            try {
                floors = gson.fromJson(allFloorsJson, new TypeToken<List<Floor>>() {
                }.getType());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return floors;

    }


    public List<Portal> getPortals(Context context) {
        Log.i(TAG, "--------getPortals --******---  ");
        if(portals == null) {

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata");
            }
            mapResourcesMetaDataPortalsPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_PORTALs;

            String allPortalsJson = ReadFromfile(mapResourcesMetaDataPortalsPath);
            Log.i(TAG, "allPortalsJson --******---  :"+ allPortalsJson);

            try {
                portals = gson.fromJson(allPortalsJson, new TypeToken<List<Portal>>() {
                }.getType());
            }catch (Exception e){
                e.printStackTrace();
            }


            if(portals != null)
            for(Portal portal:portals){
                if(!portal.isValid()){
                    portals.remove(portal);
                }
            }
        }
        return portals;

    }

    public List<Facility> getFacilities(Context context) {
        Log.i(TAG, "--------getFacilities --******---  ");
        if(facilities == null) {

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata");
            }
            mapResourcesMetaDataFacilitiesPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_FACILITIES;

            String allFacilitiesJson = ReadFromfile(mapResourcesMetaDataFacilitiesPath);
            Log.i(TAG, "allFacilitiesJson --******---  :"+ allFacilitiesJson);
            try {
                facilities = gson.fromJson(allFacilitiesJson, new TypeToken<List<Facility>>() {
                }.getType());
            }catch (Exception e){
                e.printStackTrace();
            }


            if(facilities != null)
            for(Facility facility:facilities){
                if(!facility.isValid()){
                    facilities.remove(facility);
                }
            }
        }
        return facilities;

    }

    public List<ActivityGroup> getActivityCategories(Context context){
        Log.i(TAG, "--------getActivityCategories --******---  ");
        if(activityCategories == null) {

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata");
            }
            mapResourcesMetaDataActivityCategoriesPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_ACTVITIES;

            String activitiesJSON = ReadFromfile(mapResourcesMetaDataActivityCategoriesPath);
            Log.i(TAG, "activitiesJSON --******---  :"+ activitiesJSON);
            try {
                activityCategories = gson.fromJson(activitiesJSON, new TypeToken<List<ActivityGroup>>() {
                }.getType());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return activityCategories;
    }


    public List<IndoorLocationRestriction> getRestrictions(Context context) {
        Log.i(TAG, "------ getRestrictions --******---  :");
        if(restrictions == null) {

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata");
            }
            mapResourcesMetaDataRestrictionsPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_RESTRICTIONs;

            restrictions = new ArrayList<IndoorLocationRestriction>();
            String allRestrictionsJson = ReadFromfile(mapResourcesMetaDataRestrictionsPath);

            List<IndoorLocationRestrictionWrapper> indoorLocationRestrictionWrappers = null;

            try {
                indoorLocationRestrictionWrappers = gson.fromJson(allRestrictionsJson, new TypeToken<List<IndoorLocationRestrictionWrapper>>() {
                }.getType());
            }catch (Exception e){
                e.printStackTrace();
            }

            //Convert IndoorLocationRestrictionWrapper to subclass of IndoorLocationRestriction according to type
            if(indoorLocationRestrictionWrappers != null)
            for (IndoorLocationRestrictionWrapper wrapper : indoorLocationRestrictionWrappers) {
                String type = wrapper.getType();

                IndoorLocationRestriction restriction = null;

                //point
                if (type.equalsIgnoreCase(IndoorLocationRestrictionWrapper.getRestrictionType()[0])) {

                    restriction = new PointIndoorLocationRestriction();
                    restriction.setId(wrapper.getId());
                    restriction.setFloor(wrapper.getFloor());
                    ((PointIndoorLocationRestriction) restriction).setPoint(wrapper.getPoint());
                }

                //line
                if (type.equalsIgnoreCase(IndoorLocationRestrictionWrapper.getRestrictionType()[1])) {

                    restriction = new LineIndoorLocationRestriction();
                    restriction.setId(wrapper.getId());
                    restriction.setFloor(wrapper.getFloor());
                    ((LineIndoorLocationRestriction) restriction).setStart(wrapper.getPoints()[0]);
                    ((LineIndoorLocationRestriction) restriction).setEnd(wrapper.getPoints()[1]);

                }


                //circle
                if (type.equalsIgnoreCase(IndoorLocationRestrictionWrapper.getRestrictionType()[2])) {

                    restriction = new CircleIndoorLocationRestriction();
                    restriction.setId(wrapper.getId());
                    restriction.setFloor(wrapper.getFloor());
                    ((CircleIndoorLocationRestriction) restriction).setCenter(wrapper.getCenter());
                    ((CircleIndoorLocationRestriction) restriction).setRadius(wrapper.getRadius());

                }

                //polygon
                if (type.equalsIgnoreCase(IndoorLocationRestrictionWrapper.getRestrictionType()[3])) {

                    restriction = new PolygonIndoorLocationRestriction();
                    restriction.setId(wrapper.getId());
                    restriction.setFloor(wrapper.getFloor());
                    ((PolygonIndoorLocationRestriction) restriction).setVertices(wrapper.getVertices());
                }

                restrictions.add(restriction);

            }
        }
        return restrictions;
    }

    public boolean setRestrictions(List<IndoorLocationRestriction> restrictions , String restrictionsWrapperFromServer) {
        this.restrictions = restrictions;
        return writeToFile(mapResourcesMetaDataRestrictionsPath , restrictionsWrapperFromServer);
    }

    public boolean setPortals(List<Portal> portals) {
        this.portals = portals;
        return writeToFile(mapResourcesMetaDataPortalsPath , gson.toJson(portals));
    }

    public boolean setFacilities(List<Facility> facilities) {
        this.facilities = facilities;
        return writeToFile(mapResourcesMetaDataFacilitiesPath , gson.toJson(facilities));
    }


    public boolean setFloors(List<Floor> floors) {
        this.floors = floors;
        return writeToFile(mapResourcesMetaDataFloorsPath , gson.toJson(floors));
    }

    public boolean setLocations(List<BeaconNodeConfigurtion> beaconNodeConfigurtionList) {
        this.locations = beaconNodeConfigurtionList;
        return writeToFile(mapResourcesMetaDataTagsPath , gson.toJson(beaconNodeConfigurtionList));
    }

    public boolean setPois(List<POI> pois) {
        this.pois = pois;
        return writeToFile(mapResourcesMetaDataPOIsPath , gson.toJson(pois));
    }

    public boolean setCategories(List<POICategory> categories) {
        this.categories = categories;
        return writeToFile(mapResourcesMetaDataPOIsCategoriesPath , gson.toJson(categories));
    }

    public boolean setActivityCategories(List<ActivityGroup> activityCategories){
        this.activityCategories = activityCategories;
        return writeToFile(mapResourcesMetaDataActivityCategoriesPath, gson.toJson(activityCategories));
    }

    public boolean setApplicationConfiguration(Context context , ApplicationConfiguration applicationConfiguration) {
        Log.i(TAG, "setApplicationConfiguration  ----- :");
        this.applicationConfiguration = applicationConfiguration;
        String fullPath = context.getFilesDir().getAbsolutePath()+ "/" + ApplicationConstants.MAP_RESOURCES_APP_CONFIGURATIONS ;//Internal Memory
        // Convert the object to a JSON string then save
        String appConfigurationJSON = gson.toJson(applicationConfiguration);
        Log.i(TAG, "setApplicationConfiguration  -----  appConfigurationJSON :"+ appConfigurationJSON);
        Log.i(TAG, "setApplicationConfiguration  -----  fullPath:"+ fullPath);
        return writeToFile(fullPath ,appConfigurationJSON);
    }


    public List<MonitoredRegion> getMonitoredRegions(Context context){
        Log.i(TAG, "---#########----getMonitoredRegions  ");
        if(monitoredRegions == null) {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            if(ApplicationConstants.mapResourcesMetadataPath == null) {
                //context.getFilesDir().getAbsolutePath()+ "/" +
                ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, context.getFilesDir().getAbsolutePath()+ "/" + "MapResources/Metadata/Metadata" /*"/data/data/uqu.com.navibees/files/MapResources/Metadata/Metadata"*/);
            }
            mapResourcesMetaDataMonitoredRegionsPath = ApplicationConstants.mapResourcesMetadataPath + "/" + ApplicationConstants.MAP_RESOURCES_META_DATA_MONITORED_REGIONS;

            String allMonitoredRegionsJson = ReadFromfile(mapResourcesMetaDataMonitoredRegionsPath);
            Log.i(TAG, "allMonitoredRegionsJson ---#########----  :"+ allMonitoredRegionsJson);
            this.monitoredRegions = gson.fromJson(allMonitoredRegionsJson, new TypeToken<List<MonitoredRegion>>() {
            }.getType());

            if(this.monitoredRegions != null)
            for(MonitoredRegion region:monitoredRegions){
                if(!region.isValid()){
                    this.monitoredRegions.remove(region);
                }
            }
        }
        return this.monitoredRegions;
    }

    public boolean setMonitoredRegions(List<MonitoredRegion> monitoredRegions) {
        this.monitoredRegions = monitoredRegions;
        return writeToFile(mapResourcesMetaDataMonitoredRegionsPath , gson.toJson(monitoredRegions));
    }

    public boolean isMetaDataEmpty(){
        return applicationConfiguration == null ||  floors == null;
    }

    private String ReadFromfile(String fileName) {
        Log.i(TAG, "ReadFromfile  -----  fileName :"+ fileName);

        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {

            fIn = new FileInputStream(fileName);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }

        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }

    private boolean writeToFile(String filePath , String content){
        Log.i(TAG, "writeToFile  -----  filePath :"+ filePath + " , content:"+content);

        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            out.write(content.getBytes());
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

}
