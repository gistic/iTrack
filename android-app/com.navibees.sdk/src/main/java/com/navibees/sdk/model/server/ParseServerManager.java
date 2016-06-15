package com.navibees.sdk.model.server;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.model.metadata.CircleIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.DataChangeListener;
import com.navibees.sdk.model.metadata.IndoorLocationRestriction;
import com.navibees.sdk.model.metadata.LineIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.MetaDataManager;
import com.navibees.sdk.model.metadata.PointIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.PolygonIndoorLocationRestriction;
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
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by nabilnoaman on 4/14/15.
 */
public class ParseServerManager implements ServerManager {


    static final String TAG = "ParseServerManager";


    private Gson gson = new Gson();

    final MetaDataManager metaDataManager = AppManager.getInstance().getMetaDataManager();

    @Override
    public void getApplicationConfiguration(final Context context , final DataChangeListener listener) {
        //Get App Configuration from Parse Then Check lastModifiedDate with local one
        Log.i(TAG , "getApplicationConfiguration");

        //Login With Parse User to access protected rows
        ParseUser.logInInBackground("", "", new LogInCallback() {
            public void done(ParseUser user, ParseException parseExcep) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    Log.e(TAG, "getApplicationConfiguration:Parse User LogInCallback success");
                    try {

                        ApplicationConfiguration applicationConfiguration = metaDataManager.getApplicationConfiguration(context);
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Application");
                        query.getInBackground(applicationConfiguration.getApplicationId(), new GetCallback<ParseObject>() {
                            public void done(ParseObject object, ParseException e) {
                                if (object == null || e != null) {
                                    // something went wrong
                                    Log.d(TAG, "getApplicationConfiguration: request for Application failed.:" + e.toString());
                                } else {
                                    Log.d(TAG, "getApplicationConfiguration: The request for Application tamam.");
                                    final Date updateAtServer = object.getUpdatedAt();
                                    Date localUpdate = metaDataManager.getLastModifiedDateForAppConfiguration();
                                    Log.d(TAG, "getApplicationConfiguration: updateAtServer:"+updateAtServer);
                                    Log.d(TAG, "getApplicationConfiguration: localUpdate:"+localUpdate);

                                    if (isDateAfter(localUpdate , updateAtServer)) {
                                        //if (updateAtServer.after(localUpdate)) {
                                        Log.d(TAG, "getApplicationConfiguration: file on parse is newer.");
                                        ParseFile appConfigurations = (ParseFile) object.get("appConfigurationFile");
                                        if (appConfigurations != null) {
                                            appConfigurations.getDataInBackground(new GetDataCallback() {
                                                public void done(byte[] data, ParseException e) {
                                                    if (e == null) {
                                                        // data has the bytes for appConfigurations
                                                        String appConFigJSON = new String(data);
                                                        Log.d(TAG, "getApplicationConfiguration: appConFigJSON:" + appConFigJSON);
                                                        try {
                                                            ApplicationConfiguration newApplicationConfiguration = gson.fromJson(appConFigJSON, new TypeToken<ApplicationConfiguration>() {
                                                            }.getType());
                                                            listener.appConfigurationChangeCallback(newApplicationConfiguration, updateAtServer.getTime());
                                                        }catch (Exception ex){
                                                            ex.printStackTrace();
                                                            // something went wrong
                                                            Log.d(TAG, "getApplicationConfiguration: Parsing json failed :" + ex.toString());
                                                            listener.appConfigurationChangeCallback(null, 0);
                                                        }

                                                    } else {
                                                        // something went wrong
                                                        listener.appConfigurationChangeCallback(null, 0);
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.e(TAG, "configurations Column Name may be changed");
                                            listener.appConfigurationChangeCallback(null, 0);
                                        }
                                    } else {
                                        Log.d(TAG, "getApplicationConfiguration: file on parse is NOT new.");
                                        listener.appConfigurationChangeCallback(null, 0);
                                    }
                                }

                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    Log.e(TAG, "getApplicationConfiguration:Parse User LogInCallback Fail");

                }
            }
        });

    }

    @Override
    public void getBuildingMetaData(Context context , final DataChangeListener listener){
        Log.i(TAG , "getBuildingMetaData");

        //Note: We call metaDataManager.updateBuildingMetaDataFiles() after each json file from building table
        //downloaded & parsed succesfuly to corresponding object because each file downloaded & parsed in differrent thread in Parse

        try {

            //final MetaDataManager metaDataManager = AppManager.getInstance().getMetaDataManager();
            ApplicationConfiguration applicationConfiguration = metaDataManager.getApplicationConfiguration(context);

            ParseQuery query = ParseQuery.getQuery("Building");
            query.whereEqualTo("objectId", applicationConfiguration.getBuildingId());
            query.include("productionBeaconsConfiguration");
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (object == null || e != null) {
                        Log.d(TAG, "getBuildingMetaData: failed.:" + e.toString());
                    } else {
                        Log.d(TAG, "getBuildingMetaData: tamam.");
                        final Date updateAtServer = object.getUpdatedAt();
                        Date localUpdate = metaDataManager.getLastModifiedDateForBuildingMetaData();
                        Log.d(TAG, "getBuildingMetaData: updateAtServer:" + updateAtServer);
                        Log.d(TAG, "getBuildingMetaData: localUpdate:" + localUpdate);
                        if (isDateAfter(localUpdate, updateAtServer)) {
                            //if (updateAtServer.after(localUpdate)) {
                            Log.d(TAG, "getBuildingMetaData: Meta Data files on parse is newer.");

                            //floors
                            getFloorsFile(object, listener, updateAtServer);
                            //poisCategories
                            getPOIsCategoriesFile(object, listener, updateAtServer);
                            //pois
                            getPOIsFile(object, listener, updateAtServer);
                            //portals
                            getPortalsFile(object, listener, updateAtServer);
                            //facilities
                            getFacilitiesFile(object, listener, updateAtServer);
                            //monitoredRegions
                            getMonitoredRegionsFile(object, listener, updateAtServer);
                            //restrictions
                            getRestrictionsFile(object, listener, updateAtServer);
                            //activityGroups
                            getActivitiesFile(object, listener, updateAtServer);

                        } else {
                            Log.d(TAG, "getBuildingMetaData: file on parse is NOT new.");
                        }

                        getBeaconsFile(object, listener, updateAtServer);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getFloorsFile(ParseObject object,final DataChangeListener listener,final Date updateAtServer) {
        try {
            ParseFile floorsFile = (ParseFile) object.get("floorsFile");
            if (floorsFile != null) {
                floorsFile.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (data != null && e == null) {
                            String floorsJSON = new String(data);
                            Log.d(TAG, "getBuildingMetaData: floorsJSON:" + floorsJSON);

                            try {

                                List<Floor> floors = gson.fromJson(floorsJSON, new TypeToken<List<Floor>>() {
                                }.getType());

                                listener.floorsChangeCallback(floors, updateAtServer.getTime());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

                            }catch (Exception ex){

                                ex.printStackTrace();
                                // something went wrong
                                Log.d(TAG, "getBuildingMetaData:floorsJson parsing failed :" + ex.toString());
                                listener.floorsChangeCallback(null, 0);

                            }

                        } else {
                            // something went wrong
                            Log.d(TAG, "getBuildingMetaData: request for floors file failed.:" + e.toString());
                            listener.floorsChangeCallback(null, 0);

                        }
                    }
                });
            } else {
                Log.e(TAG, "floors Column Name may be changed");
                listener.floorsChangeCallback(null, 0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getPOIsCategoriesFile(ParseObject object,final DataChangeListener listener,final Date updateAtServer) {
        try {

            ParseFile poisCategoriesFile = (ParseFile) object.get("poisCategoriesFile");
            if (poisCategoriesFile != null) {
                poisCategoriesFile.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (data != null && e == null) {
                            String poisCategoriesJSON = new String(data);
                            Log.d(TAG, "getBuildingMetaData: poisCategoriesJSON:" + poisCategoriesJSON);

                            try {

                                List<POICategory> poisCategories = gson.fromJson(poisCategoriesJSON, new TypeToken<List<POICategory>>() {
                                }.getType());

                                listener.poiCategoriesChangeCallback(poisCategories, updateAtServer.getTime());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

                            }catch (Exception ex){
                                ex.printStackTrace();
                                // something went wrong
                                Log.d(TAG, "getBuildingMetaData:poisCategoriesJSON parsing failed :" + ex.toString());
                                //listener.poiCategoriesChangeCallback(null, 0);
                            }
                        } else {
                            // something went wrong
                            Log.d(TAG, "getBuildingMetaData: request for poisCategories file failed.:" + e.toString());
                            //listener.poiCategoriesChangeCallback(null, 0);


                        }
                    }
                });
            } else {
                Log.e(TAG, "poisCategories Column Name may be changed Or File deleted");
                listener.poiCategoriesChangeCallback(new ArrayList<POICategory>(), updateAtServer.getTime());
                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

            }
        }catch (Exception e){
            e.printStackTrace();
            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
        }
    }

    private void getPOIsFile(ParseObject object,final DataChangeListener listener,final Date updateAtServer) {
        try {

            ParseFile poisFile = (ParseFile) object.get("poisFile");
            if (poisFile != null) {
                poisFile.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (data != null && e == null) {
                            String poisJSON = new String(data);
                            Log.d(TAG, "getBuildingMetaData: poisJSON:" + poisJSON);

                            try {
                                List<POI> pois = gson.fromJson(poisJSON, new TypeToken<List<POI>>() {
                                }.getType());

                                listener.poisChangeCallback(pois, updateAtServer.getTime());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

                            }catch (Exception ex){
                                ex.printStackTrace();
                                // something went wrong
                                Log.d(TAG, "getBuildingMetaData:poisJSON parsing failed :" + ex.toString());
                                //listener.poisChangeCallback(null, 0);
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                            }

                        } else {
                            // something went wrong
                            Log.d(TAG, "getBuildingMetaData: request for pois file failed.:" + e.toString());
                            //listener.poisChangeCallback(null, 0);
                            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                        }
                    }
                });
            } else {
                Log.e(TAG, "pois Column Name may be changed Or File deleted");
                listener.poisChangeCallback(new ArrayList<POI>(), updateAtServer.getTime());
                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

            }
        }catch (Exception e){
            e.printStackTrace();
            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
        }
    }

    private void getPortalsFile(ParseObject object,final DataChangeListener listener,final Date updateAtServer) {
        try {

            ParseFile portalsFile = (ParseFile) object.get("portalsFile");
            if (portalsFile != null) {
                portalsFile.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (data != null && e == null) {
                            String portalsJSON = new String(data);
                            Log.d(TAG, "getBuildingMetaData: portalsJSON:" + portalsJSON);

                            try {

                                List<Portal> portals = gson.fromJson(portalsJSON, new TypeToken<List<Portal>>() {
                                }.getType());

                                listener.portalsChangeCallback(portals, updateAtServer.getTime());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

                            }catch (Exception ex){
                                ex.printStackTrace();
                                // something went wrong
                                Log.d(TAG, "getBuildingMetaData:portalsJSON parsing failed :" + ex.toString());
                                //listener.portalsChangeCallback(null, 0);
                            }

                        } else {
                            // something went wrong
                            Log.d(TAG, "getBuildingMetaData: request for portals file failed.:" + e.toString());
                            //listener.portalsChangeCallback(null, 0);
                        }
                    }
                });
            } else {
                Log.e(TAG, "portals Column Name may be changed Or File deleted");
                listener.portalsChangeCallback(new ArrayList<Portal>(), updateAtServer.getTime());
                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
            }
        }catch (Exception e){
            e.printStackTrace();
            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
        }
    }


    private void getFacilitiesFile(ParseObject object,final DataChangeListener listener,final Date updateAtServer) {
        try {
            ParseFile facilitiesFile = (ParseFile) object.get("facilitiesFile");
            if (facilitiesFile != null) {
                facilitiesFile.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (data != null && e == null) {
                            String facilitiesJSON = new String(data);
                            Log.d(TAG, "getBuildingMetaData: facilitiesJSON:" + facilitiesJSON);

                            try {

                                List<Facility> facilities = gson.fromJson(facilitiesJSON, new TypeToken<List<Facility>>() {
                                }.getType());

                                listener.facilitiesChangeCallback(facilities, updateAtServer.getTime());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

                            }catch (Exception ex){
                                ex.printStackTrace();
                                // something went wrong
                                Log.d(TAG, "getBuildingMetaData:facilitiesJSON parsing failed :" + ex.toString());
                                //listener.facilitiesChangeCallback(null, 0);
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                            }

                        } else {
                            // something went wrong
                            Log.d(TAG, "getBuildingMetaData: request for facilities file failed.:" + e.toString());
                            //listener.facilitiesChangeCallback(null, 0);
                            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

                        }
                    }
                });
            } else {
                Log.e(TAG, "Facilities Column Name may be changed Or File deleted");
                listener.facilitiesChangeCallback(new ArrayList<Facility>(), updateAtServer.getTime());
                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
            }
        }catch (Exception e){
            e.printStackTrace();
            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
        }
    }

    private void getMonitoredRegionsFile(ParseObject object,final DataChangeListener listener,final Date updateAtServer) {
        try {

            ParseFile monitoredRegionsFile = (ParseFile) object.get("regionsFile");
            if (monitoredRegionsFile != null) {
                monitoredRegionsFile.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (data != null && e == null) {
                            String monitoredRegionsFileJSON = new String(data);
                            Log.d(TAG, "getBuildingMetaData: monitoredRegionsFileJSON:" + monitoredRegionsFileJSON);

                            try {

                                List<MonitoredRegion> monitoredRegions = gson.fromJson(monitoredRegionsFileJSON, new TypeToken<List<MonitoredRegion>>() {
                                }.getType());

                                listener.monitoredRegionsChangeCallback(monitoredRegions, updateAtServer.getTime());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

                            }catch (Exception ex){
                                ex.printStackTrace();
                                // something went wrong
                                Log.d(TAG, "getBuildingMetaData:monitoredRegionsFileJSON parsing failed :" + ex.toString());
                                //listener.monitoredRegionsChangeCallback(null, 0);
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                            }

                        } else {
                            // something went wrong
                            Log.d(TAG, "getBuildingMetaData: request for monitoredRegions file failed.:" + e.toString());
                            //listener.monitoredRegionsChangeCallback(null, 0);
                            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                        }
                    }
                });
            } else {
                Log.e(TAG, "monitoredRegions Column Name may be changed Or File deleted");
                listener.monitoredRegionsChangeCallback(new ArrayList<MonitoredRegion>(), updateAtServer.getTime());
                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
            }

        }catch (Exception e){
            e.printStackTrace();
            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
        }
    }

    private void getRestrictionsFile(ParseObject object,final DataChangeListener listener,final Date updateAtServer) {
        try {

            ParseFile restrictionsFile = (ParseFile) object.get("restrictionsFile");
            if (restrictionsFile != null) {
                restrictionsFile.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (data != null && e == null) {
                            String restrictionsJSON = new String(data);
                            Log.d(TAG, "getBuildingMetaData: restrictionsJSON:" + restrictionsJSON);

                            try {
                                List<IndoorLocationRestriction> restrictions = new ArrayList<IndoorLocationRestriction>();
                                List<IndoorLocationRestrictionWrapper> indoorLocationRestrictionWrappers = gson.fromJson(restrictionsJSON, new TypeToken<List<IndoorLocationRestrictionWrapper>>() {
                                }.getType());

                                //Convert IndoorLocationRestrictionWrapper to subclass of IndoorLocationRestriction according to type
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


                                listener.restrictionsChangeCallback(restrictions, updateAtServer.getTime(), restrictionsJSON);
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                // something went wrong
                                Log.d(TAG, "getBuildingMetaData:restrictionsJSON parsing failed :" + ex.toString());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                            }
                        } else {
                            // something went wrong
                            Log.d(TAG, "getBuildingMetaData: request for restrictions file failed.:" + e.toString());
                            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                        }
                    }
                });

            } else {
                Log.e(TAG, "restrictions Column Name may be changed Or File deleted");
                listener.restrictionsChangeCallback(new ArrayList<IndoorLocationRestriction>(),  updateAtServer.getTime() , new String("{}"));
                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
            }
        }catch (Exception e){
            e.printStackTrace();
            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
        }
    }

    private void getBeaconsFile(ParseObject object, final DataChangeListener listener, final Date updateAtServer) {
        try {

            ParseObject parseObject = (ParseObject) object.get("productionBeaconsConfiguration");
            if(parseObject != null) {
                final Date beaconNodesConfigurationFileUpdateAtServer = parseObject.getUpdatedAt();
                Date localBeaconNodesConfigurationFileUpdate = metaDataManager.getLastModifiedDateForBeaconsNodeConfigurations();

                Log.d(TAG, "getBuildingMetaData:productionConfiguration:beaconsFile: beaconNodesConfigurationFileUpdateAtServer:" + beaconNodesConfigurationFileUpdateAtServer);
                Log.d(TAG, "getBuildingMetaData:productionConfiguration:beaconsFile: localBeaconNodesConfigurationFileUpdate:" + localBeaconNodesConfigurationFileUpdate);
                if (isDateAfter(localBeaconNodesConfigurationFileUpdate, beaconNodesConfigurationFileUpdateAtServer)) {
                    //if (beaconNodesConfigurationFileUpdateAtServer.after(localBeaconNodesConfigurationFileUpdate)) {
                    Log.d(TAG, "getBuildingMetaData:beaconNodesConfigurationFile on parse is newer.");

                    //beaconNodesConfiguration
                    ParseFile beaconNodesConfigurationFile = (ParseFile) parseObject.get("beaconsFile");
                    if (beaconNodesConfigurationFile != null) {
                        beaconNodesConfigurationFile.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (data != null && e == null) {
                                    String beaconNodesConfigurationJSON = new String(data);
                                    Log.d(TAG, "getBuildingMetaData: beaconNodesConfigurationJSON:" + beaconNodesConfigurationJSON);

                                    try {

                                        List<BeaconNodeConfigurtion> beaconNodesConfiguration = gson.fromJson(beaconNodesConfigurationJSON, new TypeToken<List<BeaconNodeConfigurtion>>() {
                                        }.getType());
                                        listener.beaconNodeConfigurtionChangeCallback(beaconNodesConfiguration, beaconNodesConfigurationFileUpdateAtServer.getTime());

                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        // something went wrong
                                        Log.d(TAG, "getBuildingMetaData:beaconNodesConfigurationJSON parsing failed :" + ex.toString());
                                        listener.beaconNodeConfigurtionChangeCallback(null, 0);
                                    }

                                } else {
                                    // something went wrong
                                    Log.d(TAG, "getBuildingMetaData: request for beaconNodesConfiguration file failed.:" + e.toString());
                                    listener.beaconNodeConfigurtionChangeCallback(null, 0);
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "productionConfiguration:beaconsFile Column Name may be changed  Or File deleted");
                        listener.beaconNodeConfigurtionChangeCallback(new ArrayList<BeaconNodeConfigurtion>(), 0);

                    }
                } else {
                    Log.d(TAG, "getBuildingMetaData:productionConfiguration:beaconsFile file on parse is NOT new.");
                    listener.beaconNodeConfigurtionChangeCallback(null, 0);
                }
            }else {
                // beacons column is not exist
                Log.e(TAG, "getBuildingMetaData:getBeaconsFile request for beaconNodesConfiguration file failed");
                listener.beaconNodeConfigurtionChangeCallback(null, 0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Get Activities file from server, this will never pass null in the listener because activities file in not essential
     * @param object
     * @param listener
     * @param updateAtServer
     */
    private void getActivitiesFile(ParseObject object, final DataChangeListener listener, final Date updateAtServer){
        try {
            ParseFile activitiesFile = object.getParseFile("activityGroupsFile");
            if(activitiesFile != null){
                activitiesFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if(bytes != null && e == null){
                            String activitiesJSON = new String(bytes);

                            Log.d(TAG, "getBuildingMetaData: activitiesJSON: " + activitiesJSON);
                            try {
                                List<ActivityGroup> activityCategories = gson.fromJson(activitiesJSON, new TypeToken<List<ActivityGroup>>() {}.getType());

                                listener.activitiesChangeCallback(activityCategories, updateAtServer.getTime());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                            }catch (Exception ex){
                                ex.printStackTrace();
                                Log.d(TAG, "getBuildingMetaData:activitiesJSON parsing failed :" + ex.toString());
                                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                            }
                        }else{
                            Log.d(TAG, "getBuildingMetaData: request for activities file failed :" + e.toString());
                            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
                        }
                    }
                });
            }else{
                Log.d(TAG, "getBuildingMetaData: activities Column Name may be changed Or File deleted");
                listener.activitiesChangeCallback(new ArrayList<ActivityGroup>(), updateAtServer.getTime());
                metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
            }
        }catch (Exception e){
            e.printStackTrace();
            metaDataManager.updateBuildingMetaDataFiles(updateAtServer.getTime());
        }
    }

    private boolean isDateAfter(Date date1 , Date date2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal2.after(cal1);
    }

}
