package com.navibees.sdk.model.postioning;

import android.app.Activity;
import android.content.Context;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.BeaconNode;
import com.navibees.sdk.model.metadata.IndoorLocationRestriction;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.postioning.kalman.KalmanFilterHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by nabilnoaman on 4/16/15.
 */
final public class PositionManager implements BeaconNodeListener {

    private Context context;

    private IndoorLocationListener indoorLocationListener;

    private NaviBeesBeaconManager naviBeesBeaconManager;
    private KalmanFilterHandler kalmanFilter;

    static final String TAG = "PositionManager";

    List<IndoorLocationRestriction> allRestrictions = new ArrayList<IndoorLocationRestriction>();
    private boolean reportLocationEnabled = false;

    public PositionManager(/*Context context */Activity activity , IndoorLocationListener indoorLocationListener) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        AppManager.getInstance().getLicenseManager().verify(activity.getApplicationContext() , NaviBeesFeature.Positioning);

        this.context = activity.getApplicationContext();
        this.indoorLocationListener = indoorLocationListener;
        this.kalmanFilter = new KalmanFilterHandler(this.context);
        naviBeesBeaconManager = new NaviBeesBeaconManager(activity, this);

        allRestrictions = AppManager.getInstance().getMetaDataManager().getRestrictions(context);

    }

    public PositionManager(Context context, IndoorLocationListener indoorLocationListener) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        this.context = context.getApplicationContext();
        this.indoorLocationListener = indoorLocationListener;
        this.kalmanFilter = new KalmanFilterHandler(this.context);
        naviBeesBeaconManager = new NaviBeesBeaconManager(this.context, this);
        allRestrictions = AppManager.getInstance().getMetaDataManager().getRestrictions(context);

    }

    public void startTracking() {
        reportLocationEnabled = true;
        this.kalmanFilter.resetKalmanFilter();
        //naviBeesBeaconManager.startRanging();
    }

    public void disableReportingLocation() {
        reportLocationEnabled = false;
    }


    public void enableReportingLocation() {
        reportLocationEnabled = true;
    }



    @Override
    public void beaconNodeCallback(List<BeaconNode> beaconNodes) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {

        //get indoor Location from localizer then apply restrictions
        IndoorLocation currentLocationWithoutSmoothing = calculateCurrentLocation(PositionLocator.LOCALIZER_ALGORTHIM.WEIGHTED_CENTROID , beaconNodes);

        IndoorLocation currentLocationAfterSmoothing = null;

        if(currentLocationWithoutSmoothing != null) {

            //Apply Kalman Filter
            this.kalmanFilter.update_velocity2d( currentLocationWithoutSmoothing.getX() , currentLocationWithoutSmoothing.getY());
            double[] estimatedLocation = kalmanFilter.get_lat_long();
            //Log.e("KalmanFilter" , "currentLocationWithoutSmoothing ("+currentLocationWithoutSmoothing.getX() +" , "+ currentLocationWithoutSmoothing.getY() + " ) ");
            currentLocationAfterSmoothing = new IndoorLocation(estimatedLocation[0] , estimatedLocation[1]);
            currentLocationAfterSmoothing.setFloor(currentLocationWithoutSmoothing.getFloor());
            currentLocationAfterSmoothing.setConfidence(currentLocationWithoutSmoothing.getConfidence());
            //Log.e("KalmanFilter" , "currentLocationAfterSmoothing ("+currentLocationAfterSmoothing.getX() +" , "+ currentLocationAfterSmoothing.getY() + " ) ");

            //Apply Restriction If exists for current floor
            List<IndoorLocationRestriction> currentFloorRestrictions = filterRestrictionsByFloor(allRestrictions , currentLocationWithoutSmoothing.getFloor());

            if (currentFloorRestrictions != null ) {
                //alignLocation(currentLocationWithoutSmoothing, currentFloorRestrictions);
                //alignLocation(currentLocationAfterSmoothing, currentFloorRestrictions);
            }

        }

        if(reportLocationEnabled) {
            indoorLocationListener.locationCallback(currentLocationWithoutSmoothing, currentLocationAfterSmoothing, beaconNodes.size());
        }
    }


    // calculate the location of the device based on the given readings
    private IndoorLocation calculateCurrentLocation(PositionLocator.LOCALIZER_ALGORTHIM locatorType, List<BeaconNode> tags) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        if(tags != null && tags.size() > 0) {
            PositionLocator locator;
            switch (locatorType) {
                case WEIGHTED_CENTROID:
                    locator = new WeightedCentroidPositionLocator(context);
                    break;
                case TRILLATERATION:
                    locator = null;
                    break;
                default:
                    locator = new WeightedCentroidPositionLocator(context);
            }

            return locator.calculateLocation(tags);
        }else {
            return null;
        }
    }


    // align the pre calculated position taking into account the given restrictions
    private void alignLocation(IndoorLocation location ,  List<IndoorLocationRestriction> restrictions){
        double newCoordinateX = location.getX();
        double newCoordinateY = location.getY();
        double minDistance = (double)Integer.MAX_VALUE;
        int minIndex = -1;

        for (int i =0; i<restrictions.size(); i+=1)
        {
            double distance = restrictions.get(i).calculateDistance(location);

            if (distance < minDistance)
            {
                IndoorLocation tmpPoint = restrictions.get(i).calculateNewCoordinates(location);
                if (tmpPoint.getX() != location.getX() || tmpPoint.getY() != location.getY()){
                    minDistance = distance;
                    minIndex = i;
                    newCoordinateX = tmpPoint.getX();
                    newCoordinateY = tmpPoint.getY();
                }
            }
        }

        location.setX(newCoordinateX);
        location.setY(newCoordinateY);
    }

    public List<IndoorLocationRestriction> filterRestrictionsByFloor(List<IndoorLocationRestriction> allRestrictions , int floor)  {
        List<IndoorLocationRestriction>  floorRestrictions = new ArrayList<IndoorLocationRestriction>();
        for(IndoorLocationRestriction restriction: allRestrictions) {
            if (restriction.getFloor() == floor) {
                floorRestrictions.add(restriction);
            }
        }
        return floorRestrictions;
    }

    public NaviBeesBeaconManager getNaviBeesBeaconManager(){
        return naviBeesBeaconManager;
    }

}

