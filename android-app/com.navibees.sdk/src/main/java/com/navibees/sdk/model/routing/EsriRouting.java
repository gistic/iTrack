package com.navibees.sdk.model.routing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.SparseArray;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.na.CostAttribute;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.NetworkDescription;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.R;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.json.Facility;
import com.navibees.sdk.model.metadata.json.Floor;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.model.metadata.json.Portal;
import com.navibees.sdk.util.Log;
import com.navibees.sdk.util.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hossam on 11/8/15.
 */
final public class EsriRouting {


    private static final String TAG = "EsriRouting";

    private Activity mActivity;
    private OnRoutingFinishedListener mListener;
    private List<Floor> floorList;
    private List<Portal> portalList;

    private IndoorLocation mCurrentLocation;
    private Object mTarget;
    private int mTargetId;
    private int mTargetFloor;
    private Point mCurrentLocationPoint;

    private SimpleMarkerSymbol startPointSourceSym;
    private SimpleMarkerSymbol endPointSourceSym;

    private SimpleMarkerSymbol startPointTargetSym;
    private SimpleMarkerSymbol endPointTargetSym;

    private SimpleMarkerSymbol startPointSym;
    private SimpleMarkerSymbol endPointSym;

    private final int SQUARE_SIZE = 15;
    private final int CIRCLE_SIZE = 10;
    private final int OUTLINE_SIZE = 2;

    private int multiRouteSourceFloorIndex;
    private int multiRouteTargetPOIFloorIndex;
    private Point mSourceCalloutLocation;
    private Point mTargetCalloutLocation;

    private String mErrorMessage;

    private Map<String,Boolean> portalsStatusForUser ;

    private Portal usedPortal;

    public interface OnRoutingFinishedListener{
        public void onRoutingFinished(SparseArray<Graphic[]> result, int multiRouteSourceFloorIndex, int multiRouteTargetPOIFloorIndex, Point sourceCalloutLocaiton, Point targetCalloutLocation , Portal potalUsed);
    }

    public EsriRouting(Activity activity, OnRoutingFinishedListener listener) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        AppManager.getInstance().getLicenseManager().verify(activity.getApplicationContext() , NaviBeesFeature._2D_Maps);
        AppManager.getInstance().getLicenseManager().verify(activity.getApplicationContext() , NaviBeesFeature.Multi_Floor_Navigation);

        mActivity = activity;
        mListener = listener;

        initializeRoutingSymbols();
        getPortalTypeStatusForUser(activity);
    }

    private void getPortalTypeStatusForUser(Context context) {
        //Get Prefreed portals for user from sharedpreferrences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        portalsStatusForUser = new HashMap<String , Boolean>();

        //Elevator
        Boolean portalElevatorType = sp.getBoolean(context.getString(R.string.com_navibees_sdk_preference_portal_elevator_key), false);
        //Stair
        Boolean portalStairType = sp.getBoolean(context.getString(R.string.com_navibees_sdk_preference_portal_stair_key), false);
        //Escalator
        Boolean portalEscalatorType = sp.getBoolean(context.getString(R.string.com_navibees_sdk_preference_portal_escalator_key), false);

        portalsStatusForUser.put(Portal.PORTAL_TYPE[0] , portalElevatorType);
        portalsStatusForUser.put(Portal.PORTAL_TYPE[1] , portalStairType);
        portalsStatusForUser.put(Portal.PORTAL_TYPE[2], portalEscalatorType);

    }

    private void initializeRoutingSymbols(){
        startPointSym = new SimpleMarkerSymbol(Color.BLUE, CIRCLE_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
        startPointSym.setOutline(new SimpleLineSymbol(Color.BLACK, OUTLINE_SIZE, SimpleLineSymbol.STYLE.SOLID));

        endPointSym = new SimpleMarkerSymbol(Color.RED, CIRCLE_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
        endPointSym.setOutline(new SimpleLineSymbol(Color.BLACK, OUTLINE_SIZE, SimpleLineSymbol.STYLE.SOLID));

        startPointSourceSym = new SimpleMarkerSymbol(Color.BLUE, CIRCLE_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
        startPointSourceSym.setOutline(new SimpleLineSymbol(Color.BLACK, OUTLINE_SIZE, SimpleLineSymbol.STYLE.SOLID));

        endPointSourceSym = new SimpleMarkerSymbol(Color.RED, SQUARE_SIZE, SimpleMarkerSymbol.STYLE.SQUARE);
        endPointSourceSym.setOutline(new SimpleLineSymbol(Color.BLACK, OUTLINE_SIZE, SimpleLineSymbol.STYLE.SOLID));

        startPointTargetSym = new SimpleMarkerSymbol(Color.RED, SQUARE_SIZE, SimpleMarkerSymbol.STYLE.SQUARE);
        startPointTargetSym.setOutline(new SimpleLineSymbol(Color.BLACK, OUTLINE_SIZE, SimpleLineSymbol.STYLE.SOLID));

        endPointTargetSym = new SimpleMarkerSymbol(Color.RED, CIRCLE_SIZE, SimpleMarkerSymbol.STYLE.CIRCLE);
        endPointTargetSym.setOutline(new SimpleLineSymbol(Color.BLACK, OUTLINE_SIZE, SimpleLineSymbol.STYLE.SOLID));
    }

    public void startRouting(IndoorLocation myLocation, Object target, int targetId, int targetFloor, boolean generalPOI){
        mCurrentLocation = myLocation;
        mCurrentLocationPoint = new Point(myLocation.getX(), myLocation.getY());

        mTarget = target;
        mTargetId = targetId;
        mTargetFloor = targetFloor;

        multiRouteSourceFloorIndex = -1;
        multiRouteTargetPOIFloorIndex = -1;
        mTargetCalloutLocation = null;
        mSourceCalloutLocation = null;

        mErrorMessage = "";

        new EsriRoutingTask().execute(generalPOI);
    }


    private class EsriRoutingTask extends AsyncTask<Boolean, Void, SparseArray<Graphic[]>>{

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(mActivity);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage(mActivity.getString(R.string.routing_progress_dialog_message));
            dialog.show();
        }

        @Override
        protected SparseArray<Graphic[]> doInBackground(Boolean... params) {

            floorList = AppManager.getInstance().getMetaDataManager().getFloors(mActivity);
            portalList = AppManager.getInstance().getMetaDataManager().getPortals(mActivity);

            if(mTarget == null)
                mTarget = getPOIByID(mTargetId);


            boolean generalPOI = params[0];
            if(!generalPOI){

                if(mTargetFloor == mCurrentLocation.getFloor().intValue()){
                    Point entryPoint = getEntryPointForShortestPath(mCurrentLocation, mTarget);
                    return routeToEntryPointOfTargetPOIInCurrentFloor(mCurrentLocationPoint, entryPoint);
                }else{
                    Floor sourceFloor = floorList.get(mCurrentLocation.getFloor());
                    Floor targetFloor = floorList.get(mTargetFloor);
                    List<Portal> portals = getPortalsBetweenTwoFloors(sourceFloor, targetFloor);
                    return routeMeToPOIInAnotherFloor(portals, mTarget, targetFloor);
                }

            }else{
                return routeToGeneralPOIOrFacility(mTarget);
            }
        }

        @Override
        protected void onPostExecute(SparseArray<Graphic[]> result) {
            dialog.dismiss();
            dialog = null;
            mListener.onRoutingFinished(result, multiRouteSourceFloorIndex, multiRouteTargetPOIFloorIndex, mSourceCalloutLocation, mTargetCalloutLocation , usedPortal);
            if(!TextUtils.isEmpty(mErrorMessage)){
                Toast.makeText(mActivity, mErrorMessage, Toast.LENGTH_SHORT);
            }
        }
    }

    private SparseArray<Graphic[]> routeToGeneralPOIOrFacility(Object selectedPOIOrFacility){

        //check if selected POI/(POI under selected Facility exists in My CurrentLocation Floor or NO
        List<IndoorLocation> locationsOfSelectedPOIInCurrentFloor = getLocationsAtFloor(selectedPOIOrFacility, mCurrentLocation.getFloor().intValue());

        if(locationsOfSelectedPOIInCurrentFloor != null)
        {
            //Selected POI in my currentLocation Floor
            Point shortestEntryPointOfSelectedPOI = getEntryPointForShortestPath(mCurrentLocation, selectedPOIOrFacility);
            if (shortestEntryPointOfSelectedPOI != null) {
                return routeToEntryPointOfTargetPOIInCurrentFloor(mCurrentLocationPoint, shortestEntryPointOfSelectedPOI);
            } else {
                mErrorMessage = mActivity.getString(R.string.poi_no_entry_point);
            }
        }else if (isPortalExistAtFloor(mCurrentLocation.getFloor().intValue())) {

            List<Floor> floorsOfSelectedPOI = getFloorsOfPOI(selectedPOIOrFacility);
            if(floorsOfSelectedPOI != null) {
                Set<Floor> connectedFloors = getConnectedFloors(floorList.get(mCurrentLocation.getFloor()), floorsOfSelectedPOI);
                if(connectedFloors != null){
                    for (Floor floorWithCommonPortal : connectedFloors) {
                        List<Portal> allPortalsBetweenTwoFloors = getPortalsBetweenTwoFloors(floorList.get(mCurrentLocation.getFloor()), floorWithCommonPortal);
                        SparseArray<Graphic[]> result = routeMeToPOIInAnotherFloor(allPortalsBetweenTwoFloors, selectedPOIOrFacility, floorWithCommonPortal);

                        if (result != null) return result;

                    }
                }else {
                    mErrorMessage = mActivity.getString(R.string.poi_not_in_my_current_location_floor_no_portal_connecting_between);
                }


            }else {
                mErrorMessage = mActivity.getString(R.string.poi_no_floor);
            }
        }else {
            mErrorMessage = mActivity.getString(R.string.poi_not_in_my_current_loaction_floor_no_portal_in_current_floor);
        }

        return null;
    }

    private List<IndoorLocation> getLocationsAtFloor(Object poiOrFacility, int floor){
        if(poiOrFacility instanceof POI){
            return ((POI) poiOrFacility).locationsAtFloor(floor);
        }else if(poiOrFacility instanceof Facility){
            return ((Facility) poiOrFacility).locationsAtFloor(floor);
        }else{
            return null;
        }
    }

    private List<IndoorLocation> getEntryPointsForObject(Object obj, int floorIndex){
        if(obj instanceof  POI){
            return ((POI) obj).entryPointsAtFloor(floorIndex);
        }else if(obj instanceof  Facility){
            return ((Facility) obj).entryPointsAtFloor(floorIndex);
        }
        return null;
    }

    private POI getPOIByID(int poiID){
        POI result = null;
        List<POI> allPOIs = AppManager.getInstance().getMetaDataManager().getPOIs(mActivity);
        for (int i = 0; allPOIs != null && i < allPOIs.size(); i++) {
            POI poi = allPOIs.get(i);

            if (poi.getId().intValue() == poiID) {
                result = poi;
                break;
            }
        }
        return result;
    }

    private List<Floor> getFloorsOfPOI(Object poiOrFacility) {
        List<Floor> floorsOfPOI = new ArrayList<Floor>();
        for(Floor floor:floorList){
            if(getLocationsAtFloor(poiOrFacility, floor.getIndex()) != null){
                floorsOfPOI.add(floor);
            }
        }

        if(floorsOfPOI.size() != 0){
            return floorsOfPOI;
        }

        return null;
    }

    private boolean isPortalExistAtFloor(int floorIndex) {
        if(portalList != null) {
            for (Portal portal : portalList) {
                if (portal.locationsAtFloor(floorIndex) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<Floor> getConnectedFloors(Floor floorX, List<Floor> floors) {
        HashSet<Floor> result = new HashSet<Floor>();

        if(floors.contains(floorX)){
            result.add(floorX);
        }

        for(Floor floor : floors){
            if(!floor.equals(floorX)){
                List<Portal> portalsInFloor = getPortalsAtFloor(floor);
                if(portalsInFloor != null)
                {
                    for (Portal portal : portalsInFloor) {
                        if (portal.entryPointsAtFloor(floorX.getIndex()) != null) {
                            result.add(floor);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    private List<Portal> getPortalsBetweenTwoFloors(Floor floor1, Floor floor2) {
        List<Portal> result = new ArrayList<Portal>();
        if(portalList != null) {
            for (Portal portal : portalList) {
                if (portal.entryPointsAtFloor(floor1.getIndex()) != null && portal.entryPointsAtFloor(floor2.getIndex()) != null) {
                    result.add(portal);
                }
            }
        }

        if(result.size() > 0)
            return result;
        else
            return null;
    }

    private List<Portal> getPortalsAtFloor(Floor floorX) {
        List<Portal> portalsInFloorX = new ArrayList<Portal>();
        for (Portal portal:portalList){
            if(portal.entryPointsAtFloor(floorX.getIndex()) != null){
                portalsInFloorX.add(portal);
            }
        }
        if (portalsInFloorX.size() > 0)
            return portalsInFloorX;
        else
            return null;
    }

    private Double getRouteDistance(Point startPoint , Point endPoint , Floor floor){
        try {
            Route route = getRoute(startPoint , endPoint , floor);
            double distance = route.getTotalKilometers() * 1000.0;
            Log.i(TAG, "getRouteDistance meter :" + distance);
            return distance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Route getRoute(Point startPoint , Point endPoint , Floor floor){
        //https://developers.arcgis.com/android/guide/find-a-route.htm
        //https://developers.arcgis.com/android/guide/display-driving-directions.htm
        try {

            RouteTask routeTask = RouteTask.createLocalRouteTask(ApplicationConstants.mapResourcesNetworkDatasetsPath + "/" + floor.getGeodatabase() + ".geodatabase", floor.getNetworkDataset());
            // Create a parameters object and retrieve the network description
            RouteParameters routeParams = routeTask.retrieveDefaultRouteTaskParameters();
            NetworkDescription description = routeTask.getNetworkDescription();
            List<CostAttribute> costAttributes = description.getCostAttributes();
            //Log.i(TAG, "costAttributes.size() :"+costAttributes.size());

            // Assign the first cost attribute as the impedance
            if (costAttributes.size() == 2) {
                routeParams.setImpedanceAttributeName(costAttributes.get(1).getName());
            }

            // create routing features class
            NAFeaturesAsFeature naFeatures = new NAFeaturesAsFeature();
            // Create the stop points from point geometry
            StopGraphic startPnt = new StopGraphic(startPoint);
            StopGraphic endPnt = new StopGraphic(endPoint);
            // set features on routing feature class
            naFeatures.setFeatures(new Graphic[] {startPnt, endPnt});
            // set stops on routing feature class
            routeParams.setStops(naFeatures);
            routeParams.setReturnStops(true);

            RouteResult results = routeTask.solve(routeParams);
            Route route = results.getRoutes().get(0);
            double distance = route.getTotalKilometers() * 1000.0;
            Log.i(TAG, "getRoute meter :"+distance + " , in floor:"+floor.getIndex());

            Geometry startPointRoute = results.getStops().getGraphics()[0].getGeometry();
            Geometry endPointRoute = results.getStops().getGraphics()[1].getGeometry();

            startPointRoute.copyTo(startPoint);
            endPointRoute.copyTo(endPoint);

            return route;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Point getEntryPointForShortestPath(IndoorLocation startLocation, Object targetPOIOrFacility) {
        Point nearestPoint = null;
        Point startPoint = new Point(startLocation.getX() , startLocation.getY());
        Double minDistance = Double.MAX_VALUE;
        List<IndoorLocation> targetPOIEntryPointsAtFloorX = getEntryPointsForObject(targetPOIOrFacility, startLocation.getFloor().intValue());

        if(targetPOIEntryPointsAtFloorX != null) {
            //Convert IndoorLocation to Point & Calculate the distance & compare
            for (IndoorLocation indoorLocation : targetPOIEntryPointsAtFloorX) {
                Point endPoint = new Point(indoorLocation.getX(), indoorLocation.getY());
                Double distance = getRouteDistance(startPoint, endPoint, floorList.get(startLocation.getFloor()));
                if (distance != null) {
                    if (distance.doubleValue() < minDistance.doubleValue()) {
                        minDistance = distance;
                        nearestPoint = endPoint;
                    }

                }

            }
        }

        //If no entry points, set nearest point to location of poi or facility
        if(nearestPoint == null){
            List<IndoorLocation> locations = getLocationsAtFloor(targetPOIOrFacility, startLocation.getFloor().intValue());
            if(locations != null) {
                nearestPoint = new Point(locations.get(0).getX(), locations.get(0).getY());
            }
        }

        return nearestPoint;
    }

    @Deprecated
    private Portal getNearestPortal(IndoorLocation location , List<Portal> portals) {
        Portal result = null;
        Double minDistance = Double.MAX_VALUE;
        Point startPoint = new Point(location.getX() , location.getY());
        for(int i=0; i<portals.size(); i++){
            Portal portal = portals.get(i);
            Log.i(TAG, "Portal:" + portal.getName());
            Point nearestEntryPointForPortal = getEntryPointForShortestPath(location , portal);
            Double distance = getRouteDistance(startPoint , nearestEntryPointForPortal , floorList.get(location.getFloor()));
            if(distance != null){
                if(distance.doubleValue() < minDistance.doubleValue()){
                    minDistance = distance;
                    result = portal;
                    result.indexInList = i;
                }

            }
        }

        return result;
    }


    private Portal getNearestPortal(IndoorLocation location , List<Portal> portals , int floorDiff) {
        Portal result = null;
        Double minDistance = Double.MAX_VALUE;//Double.MAX_VALUE;
        // if user chooses to avoid portal type , we continue using it
        // So if all other types of portals (preferred by user) can't build successful route & non-preferred
        // can build successful route we select the nearest one from it.
        Point startPoint = new Point(location.getX() , location.getY());
        for(int i=0; i<portals.size(); i++){
            Portal portal = portals.get(i);
            Log.i(TAG, "Portal:" + portal.getName());

            Point nearestEntryPointForPortal = getEntryPointForShortestPath(location , portal);
            Double distance = getRouteDistance(startPoint , nearestEntryPointForPortal , floorList.get(location.getFloor()));
            if(distance != null){
                //Add Portal Cost to distance
                if((portal.getType() != null) && (Portal.COST.get(portal.getType()) != null)){

                    distance = distance + Portal.COST.get(portal.getType()) * floorDiff;

                    //Check if Portal type is not preferred by user
                    if(portalsStatusForUser.get(portal.getType())){
                        distance = (Integer.MIN_VALUE) + distance.doubleValue();
                    }
                }

                if(distance.doubleValue() < minDistance.doubleValue()){
                    minDistance = distance;
                    result = portal;
                    result.indexInList = i;
                }
            }
        }

        return result;
    }

    private SparseArray<Graphic[]> routeToEntryPointOfTargetPOIInCurrentFloor(Point startPoint , Point endPoint){
        try {

            SparseArray<Graphic[]> routeGraphicPerFloor = new SparseArray<Graphic[]>();

            Graphic[] graphics = new Graphic[3];
            graphics[1] = getRouteGraphic(startPoint , endPoint , floorList.get(mCurrentLocation.getFloor()));
            graphics[0] = new Graphic(startPoint, startPointSym);
            graphics[2] = new Graphic(endPoint, endPointSym);

            routeGraphicPerFloor.put(mCurrentLocation.getFloor().intValue(), graphics);

            return routeGraphicPerFloor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private SparseArray<Graphic[]> routeToEntryPointOfTargetPOIInAnotherFloor(Point currentLocationPoint, Point portalEntryPointInCurrentFloor, Point portalEntryPointInSelectedPOIFloor, Point selectedPOIEntryPoint, int selectedPOIFloorIndex) {
        Graphic[] sourceFloorGraphics = new Graphic[3];
        sourceFloorGraphics[1] = getRouteGraphic(currentLocationPoint, portalEntryPointInCurrentFloor , floorList.get(mCurrentLocation.getFloor()));
        sourceFloorGraphics[0] = new Graphic(currentLocationPoint, startPointSourceSym);
        sourceFloorGraphics[2] = new Graphic(portalEntryPointInCurrentFloor, endPointSourceSym);

        Graphic[] targetFloorGraphics = new Graphic[3];
        targetFloorGraphics[1] = getRouteGraphic(portalEntryPointInSelectedPOIFloor, selectedPOIEntryPoint , floorList.get(selectedPOIFloorIndex));
        targetFloorGraphics[0] = new Graphic(portalEntryPointInSelectedPOIFloor, startPointTargetSym);
        targetFloorGraphics[2] = new Graphic(selectedPOIEntryPoint, endPointTargetSym);

        if(sourceFloorGraphics[1] != null && targetFloorGraphics[1] != null){
            SparseArray<Graphic[]> routeGraphicPerFloor = new SparseArray<Graphic[]>();

            multiRouteSourceFloorIndex = mCurrentLocation.getFloor().intValue();//current Floor
            multiRouteTargetPOIFloorIndex = selectedPOIFloorIndex;
            mSourceCalloutLocation = portalEntryPointInCurrentFloor;
            mTargetCalloutLocation = portalEntryPointInSelectedPOIFloor;

            routeGraphicPerFloor.put(multiRouteSourceFloorIndex, sourceFloorGraphics);
            routeGraphicPerFloor.put(multiRouteTargetPOIFloorIndex, targetFloorGraphics);

            return routeGraphicPerFloor;

        }

        return null;

    }

    private SparseArray<Graphic[]> routeMeToPOIInAnotherFloor(List<Portal> allPortalsBetweenTwoFloors, Object selectedPOIFromAnotherFloor, Floor selectedPOIFloor) {
        if (allPortalsBetweenTwoFloors == null) {
            mErrorMessage = mActivity.getString(R.string.poi_not_in_my_current_location_floor_no_portal);
            return null;
        }else {
            List<Portal> copyOfAllPortalsBetweenTwoFloors = new ArrayList<Portal>(allPortalsBetweenTwoFloors);

            //Portal bestPortalToUse = getNearestPortal(mCurrentLocation, copyOfAllPortalsBetweenTwoFloors);
            Portal bestPortalToUse = getNearestPortal(mCurrentLocation, copyOfAllPortalsBetweenTwoFloors , Math.abs(selectedPOIFloor.getIndex() - mCurrentLocation.getFloor().intValue()));

            while(bestPortalToUse != null){

                boolean portalHasEntriesAtTwoFloors = bestPortalToUse.entryPointsAtFloor(mCurrentLocation.getFloor()) != null
                        && bestPortalToUse.entryPointsAtFloor(selectedPOIFloor.getIndex()) != null;

                if(portalHasEntriesAtTwoFloors){
                    Point portalPointInMyFloor = getEntryPointForShortestPath(mCurrentLocation, bestPortalToUse);

                    Point[] entries = getShortestPathBetweenPortalAndPOI(bestPortalToUse, selectedPOIFromAnotherFloor, selectedPOIFloor);
                    if(entries[0] != null && entries[1] != null) {
                        usedPortal = bestPortalToUse;
                        return routeToEntryPointOfTargetPOIInAnotherFloor(mCurrentLocationPoint, portalPointInMyFloor, entries[0], entries[1], selectedPOIFloor.getIndex());
                        //Switch map to current location floor , to start routing from there
//                        if (!isShownFloorIsMyCurrentLocationFloor) {
//                            changeToFloor(mCurrentLocation.getFloor(), false);
//                        }
//
//
//
//                        if (mCurrentLocation.getFloor().intValue() == floor.getIndex()) {
//                            centerMapOnCurrentLocation();
//                        }
//
//
//                        if (!isTrackerOn || !isAutomaticModeEnabled) {
//                            startTracking(false);
//                        }

                    }
                }

                copyOfAllPortalsBetweenTwoFloors.remove(bestPortalToUse.indexInList);
                bestPortalToUse = getNearestPortal(mCurrentLocation, copyOfAllPortalsBetweenTwoFloors, Math.abs(selectedPOIFloor.getIndex() - mCurrentLocation.getFloor().intValue()));
            }

            return null;
        }
    }

    /**
     * This method will return shortest path between entry points of a portal and POI in the selected floor
     * @param portal Portal to use
     * @param targetPOIOrFacility Target to reach
     * @param floor Floor at which portal and target POI exist
     * @return Two points that represent the shortest path between entry point of a portal and POI in the selected floor.
     *         First point will be entry point of portal, Second point will be entry point of POI
     */
    private Point[] getShortestPathBetweenPortalAndPOI(Object portal, Object targetPOIOrFacility, Floor floor){
        Point[] result = new Point[2];
        Point startPoint = null, endPoint = null;

        double minDistance = Double.MAX_VALUE;

        List<IndoorLocation> portalEntryPointsAtFloorX = getEntryPointsForObject(portal, floor.getIndex());
        List<IndoorLocation> targetEntryPointsAtFloorX = getEntryPointsForObject(targetPOIOrFacility, floor.getIndex());

        if(portalEntryPointsAtFloorX != null && targetEntryPointsAtFloorX != null){
            for(IndoorLocation portalEntry : portalEntryPointsAtFloorX){
                startPoint = new Point(portalEntry.getX(), portalEntry.getY());

                for(IndoorLocation targetEntry : targetEntryPointsAtFloorX){
                    endPoint = new Point(targetEntry.getX(), targetEntry.getY());

                    Double distance = getRouteDistance(startPoint, endPoint  , floor);
                    if(distance != null) {
                        if (distance.doubleValue() < minDistance) {
                            minDistance = distance.doubleValue();
                            result[0] = startPoint;
                            result[1] = endPoint;
                        }
                    }

                }
            }
        }
        return result;
    }

    private Graphic getRouteGraphic(Point startPoint , Point endPoint , Floor floor){
        //https://developers.arcgis.com/android/guide/find-a-route.htm
        //https://developers.arcgis.com/android/guide/display-driving-directions.htm
        try {
            Route route = getRoute(startPoint, endPoint, floor);

            // Access the whole route geometry and add it as a graphic
            Geometry routeGeom = route.getRouteGraphic().getGeometry();
            double distance = route.getTotalKilometers() * 1000.0;
            Map<String , Object> attribute = new HashMap<String , Object>();
            attribute.put("distance", distance);
            Graphic routeGraphic = new Graphic(routeGeom, new SimpleLineSymbol(Color.BLUE,3) , attribute);

            return routeGraphic;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
