package com.navibees.sdk.model.metadata;

import com.navibees.sdk.model.metadata.json.ActivityGroup;
import com.navibees.sdk.model.metadata.json.ApplicationConfiguration;
import com.navibees.sdk.model.metadata.json.BeaconNodeConfigurtion;
import com.navibees.sdk.model.metadata.json.Facility;
import com.navibees.sdk.model.metadata.json.Floor;
import com.navibees.sdk.model.metadata.json.MonitoredRegion;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.model.metadata.json.POICategory;
import com.navibees.sdk.model.metadata.json.Portal;

import java.util.List;

/**
 * Created by nabilnoaman on 5/9/15.
 */
public interface DataChangeListener {

    public void appConfigurationChangeCallback(ApplicationConfiguration applicationConfiguration , long lastModifiedDate);
    public void poiCategoriesChangeCallback(List<POICategory> poiCategoryList , long lastModifiedDate);
    public void poisChangeCallback(List<POI> poiList , long lastModifiedDate);
    public void beaconNodeConfigurtionChangeCallback(List<BeaconNodeConfigurtion> beaconNodeConfigurtionList , long lastModifiedDate);
    public void floorsChangeCallback(List<Floor> floorList , long lastModifiedDate);
    public void portalsChangeCallback(List<Portal> portals , long lastModifiedDate);
    public void facilitiesChangeCallback(List<Facility> facilities , long lastModifiedDate);
    public void restrictionsChangeCallback(List<IndoorLocationRestriction> restrictionList , long lastModifiedDate , String restrictionsWrapperFromServer );
    public void monitoredRegionsChangeCallback(List<MonitoredRegion> regions , long lastModifiedDate);
    public void activitiesChangeCallback(List<ActivityGroup> activityCategories, long lastModifiedDate);
}
