package com.navibees.sdk.model.metadata.json;

import java.util.Date;

/**
 * Created by nabilnoaman on 4/16/15.
 */


public class BeaconNodeConfigurtion {

    private String objectId;
    private int major;
    private int minor;
    private double x;
    private double y;
    private int floorIndex;
    private int power;
    private int rate;
    private String modelId;


    private int batteryStatus;
    private long lastTimeBatteryReported;



    public BeaconNodeConfigurtion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }


    /**
     * @return The major
     */
    public int getMajor() {
        return major;
    }

    /**
     * @param major The major
     */
    public void setMajor(int major) {
        this.major = major;
    }

    /**
     * @return The minor
     */
    public int getMinor() {
        return minor;
    }

    /**
     * @param minor The minor
     */
    public void setMinor(int minor) {
        this.minor = minor;
    }

    /**
     * @return The x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x The x
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return The y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y The y
     */
    public void setY(double y) {
        this.y = y;
    }


    public int getFloorIndex() {
        return floorIndex;
    }

    public void setFloorIndex(int floorIndex) {
        this.floorIndex = floorIndex;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public long getLastTimeBatteryReported() {
        return lastTimeBatteryReported;
    }

    public void setLastTimeBatteryReported(long lastTimeBatteryReported) {
        this.lastTimeBatteryReported = lastTimeBatteryReported;
    }


    //We consider two beacons are equal if they have the same major & minor value OR both beacons have objectID
    //we use this in convert() in NaviBeaconManager to get Location of beacon given major/minor values
    //int index = tagsLocations.indexOf(new BeaconNodeConfigurtion(beaconNode.getMajor() , beaconNode.getMinor()));
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BeaconNodeConfigurtion) == false) {
            return false;
        }
        BeaconNodeConfigurtion rhs = ((BeaconNodeConfigurtion) other);
        if ((rhs.getMajor() == this.getMajor() && rhs.getMinor() == this.getMinor()))
            return true;

        return false;
    }

    @Override
    public String toString() {
        return "[major , minor]:[ "+major+" , "+minor+" ] ,*, (location.X , location.Y) : ( "+x + " , "+y+" )"+
                "[betteryStatus]:[ "+batteryStatus + "] (objectId): ( "+objectId+" ) ,*, (lastTimeBatteryReported):("+new Date(lastTimeBatteryReported)+")";
    }

}