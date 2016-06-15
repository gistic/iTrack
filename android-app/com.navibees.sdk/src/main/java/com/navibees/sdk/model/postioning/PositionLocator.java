package com.navibees.sdk.model.postioning;

import android.content.Context;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.BeaconNode;
import com.navibees.sdk.model.metadata.json.IndoorLocationConfidence;
import com.navibees.sdk.model.metadata.json.IndoorLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nabilnoaman on 4/27/15.
 * all localization algorithms/techniques should extent this abstract class
 */
public abstract class PositionLocator {

    public enum LOCALIZER_ALGORTHIM  {WEIGHTED_CENTROID, TRILLATERATION}

    public PositionLocator(Context context) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Positioning);
    }

    public abstract IndoorLocation calculateLocation (List<BeaconNode> beaconNodes);

    // get the floor of the tag
    // as a function of major and/or minor
    protected int getTagFloor(BeaconNode tag){
        /*
        int floor = 0;
        floor = tag.getMajor()/ 10;
        return floor;
        */
        return tag.getLocation().getFloor().intValue();
    }

    // get the floor of the user given all readings
    protected int getCurrentFloor(List<BeaconNode> tags){
        Map<Integer/*floor*/ , Integer/*num of tags of this floor*/> numOfTagsPerFloor = new HashMap<Integer,Integer>();

        int floorWithMaxTags = -1;
        int maxTagsPerFloor = 0;

        for(BeaconNode tag:tags){
            int floor = getTagFloor(tag);
            if(numOfTagsPerFloor.containsKey(floor)){
                numOfTagsPerFloor.put(floor ,  ( numOfTagsPerFloor.get(floor).intValue()+ 1));
            }else {
                numOfTagsPerFloor.put(floor , 1);
            }

            //update maxTagsPerFloor floorWithMaxTags
            if(numOfTagsPerFloor.get(floor) > maxTagsPerFloor){
                maxTagsPerFloor = numOfTagsPerFloor.get(floor);
                floorWithMaxTags = floor;
            }
        }

        return floorWithMaxTags;
    }

    // filter tags by floor
    protected List<BeaconNode> filterTagsByFloor(List<BeaconNode> allDetectedTags , int floor){
        List<BeaconNode> tagsAtThisFloor = new ArrayList<BeaconNode>();
        for(BeaconNode tag:allDetectedTags){
            if(floor == getTagFloor(tag)){
                tagsAtThisFloor.add(tag);
            }
        }
        return tagsAtThisFloor;
    }


    // detect confidence according to number of beacons in selected floor not all detected beacons
    protected IndoorLocationConfidence getConfidence(List<BeaconNode> tags) {
        IndoorLocationConfidence confidence = IndoorLocationConfidence.Low;
        switch (tags.size()) {
            case 0: confidence = IndoorLocationConfidence.Low;
                break;
            case 1:
            case 2: confidence = IndoorLocationConfidence.Average;
                break;
            default: confidence = IndoorLocationConfidence.High;
        }


        return confidence;
    }

}


