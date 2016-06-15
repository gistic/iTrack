package com.navibees.sdk.model.postioning;

import android.content.Context;

import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.BeaconNode;
import com.navibees.sdk.model.metadata.json.IndoorLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nabilnoaman on 4/27/15.
 */
final public class WeightedCentroidPositionLocator extends PositionLocator {

    static final String TAG = "WeightedCentroidPositionLocator";


    // select K nearest tags
    final static int  K = 4;

    public WeightedCentroidPositionLocator(Context context) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException{
        super(context);
    }

    @Override
    public IndoorLocation calculateLocation(List<BeaconNode> beaconNodes) {
        int currentFloor = getCurrentFloor(beaconNodes);

        // filter floor outliers
        List<BeaconNode> detectedTagsFromCurrentFloor = filterTagsByFloor(beaconNodes, currentFloor);

        beaconNodes = null;


        int limit = detectedTagsFromCurrentFloor.size();
        if(limit > 0)
        {
          List<Double> newRssi = new ArrayList<Double>();
          double sumRssi = 0.0;
          // select K nearest
          Collections.sort(detectedTagsFromCurrentFloor, new Comparator<BeaconNode>() {

            public int compare(BeaconNode tag1, BeaconNode tag2) {
                int result = 0;

                if (tag1.meanRSSI() == tag2.meanRSSI()) {
                    result = 0;
                } else {
                    if (tag1.meanRSSI() < tag2.meanRSSI()) {
                        result = 1;

                    } else {
                        result = -1;
                    }
                }

                return result;
            }
        });

        //Log.e(TAG , "---START---");
        for(int i = 0 ; i< detectedTagsFromCurrentFloor.size() ; i++)  {
            BeaconNode beaconNode = detectedTagsFromCurrentFloor.get(i);
            //Log.d(TAG , "i : "+ i + "[major , minor]:[ "+ beaconNode.getMajor() + " , "+beaconNode.getMinor()+" ] , getMeanRSSI: "+ beaconNode.getMeanRSSI()+ " , meanRSSI(): "+beaconNode.meanRSSI());
        }
        //Log.e(TAG , "---END---");

        if (K < limit) {
            limit = K;
        }


        for (int i = 0; i < limit; i++) {
            double nrssi = calculateWeight1(detectedTagsFromCurrentFloor.get(i).meanRSSI());
            newRssi.add(i, nrssi);
            sumRssi = sumRssi + nrssi;
        }

        double locationX = 0.0;
        double locationY = 0.0;
        for (int i = 0; i < limit; i++) {
            double weight = newRssi.get(i) / sumRssi; // normalize weight
            locationX += (weight * detectedTagsFromCurrentFloor.get(i).getLocation().getX());
            locationY += (weight * detectedTagsFromCurrentFloor.get(i).getLocation().getY());
        }

        IndoorLocation currentLocation = new IndoorLocation(locationX, locationY , currentFloor);
        currentLocation.setConfidence(getConfidence(detectedTagsFromCurrentFloor));

        return currentLocation;
    }else {
            return null;
        }
    }


    private double calculateWeight1(double meanRssi){
        double a = meanRssi / 20.0;
        double b = Math.pow(10.0, a);
        double weight = Math.pow(b, 1.3); // g = 1.3
        return weight;
    }

    private double calculateWeight2(double meanRssi) {
        double a = meanRssi / 10.0 ;
        double b  = Math.pow(10.0, a);
        double weight  = Math.sqrt(Math.pow(b, 1.0)); // g = 1.0
        return weight;
    }


}