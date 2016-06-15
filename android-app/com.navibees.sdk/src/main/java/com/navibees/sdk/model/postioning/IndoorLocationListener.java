package com.navibees.sdk.model.postioning;

import com.navibees.sdk.model.metadata.json.IndoorLocation;

/**
 * Created by nabilnoaman on 4/16/15.
 */
public interface IndoorLocationListener {
    public void locationCallback(IndoorLocation currentLocationWithoutSmoothing ,IndoorLocation currentLocationAfterSmoothing , int numOfValidBeacons);
}
