package com.navibees.sdk.model.metadata.json;

import com.navibees.sdk.util.CommonUtils;

/**
 * Created by nabilnoaman on 5/7/15.
 */
public class ApplicationConfiguration {

    private String projectId;
    private String venueId;
    private String buildingId;
    private String applicationId;
    private int defaultZoomLevel;
    private int minimumPOIVisiableZoomLevel;
    private IndoorLocation initialLocation;
    private boolean trackingEnabledByDefault;
    private String beaconsUUID;
    private String name;
    private String nameAr;
    private String accessToken;
    private Plan plan;


    /**
     *
     * @return
     * The projectId
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     *
     * @param projectId
     * The projectId
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     *
     * @return
     * The venueId
     */
    public String getVenueId() {
        return venueId;
    }

    /**
     *
     * @param venueId
     * The venueId
     */
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    /**
     *
     * @return
     * The buildingId
     */
    public String getBuildingId() {
        return buildingId;
    }

    /**
     *
     * @param buildingId
     * The buildingId
     */
    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    /**
     *
     * @return
     * The applicationId
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     *
     * @param applicationId
     * The applicationId
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     *
     * @return
     * The defaultZoomLevel
     */
    public int getDefaultZoomLevel() {
        return defaultZoomLevel;
    }

    /**
     *
     * @param defaultZoomLevel
     * The defaultZoomLevel
     */
    public void setDefaultZoomLevel(int defaultZoomLevel) {
        this.defaultZoomLevel = defaultZoomLevel;
    }

    /**
     *
     * @return
     * The initialLocation
     */
    public IndoorLocation getInitialLocation() {
        return initialLocation;
    }

    /**
     *
     * @param initialLocation
     * The initialLocation
     */
    public void setInitialLocation(IndoorLocation initialLocation) {
        this.initialLocation = initialLocation;
    }

    /**
     *
     * @return
     * The trackingEnabledByDefault
     */
    public boolean isTrackingEnabledByDefault() {
        return trackingEnabledByDefault;
    }

    /**
     *
     * @param trackingEnabledByDefault
     * The trackingEnabledByDefault
     */
    public void setTrackingEnabledByDefault(boolean trackingEnabledByDefault) {
        this.trackingEnabledByDefault = trackingEnabledByDefault;
    }




    public String getBeaconsUUID() {
        return beaconsUUID;
    }

    public void setBeaconsUUID(String beaconsUUID) {
        this.beaconsUUID = beaconsUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAr() {
        return nameAr;
    }

    public String getNameWRTLang(){
        if(CommonUtils.isArabicLang())
            return nameAr;

        return name;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public int getMinimumPOIVisiableZoomLevel() {
        return minimumPOIVisiableZoomLevel;
    }

    public void setMinimumPOIVisiableZoomLevel(int minimumPOIVisiableZoomLevel) {
        this.minimumPOIVisiableZoomLevel = minimumPOIVisiableZoomLevel;
    }



    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
