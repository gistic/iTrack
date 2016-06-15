package com.navibees.sdk.model.metadata;

import com.navibees.sdk.model.metadata.json.IndoorLocation;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by nabilnoaman on 4/16/15.
 * This class represent Beacon Readings
 * This is equivalent to BeaconNodeConfigurtion plus states (RSSI) readings
 */
public class BeaconNode {

    private int major;
    private int minor;
    private IndoorLocation location;
    private int txPower;
    private List<BeaconNodeState> states;
    private double meanRSSI;
    private double meanAccuracy;
    private double meanEstimatedDistance;

    private int batteryStatus;

    public BeaconNode(int major, int minor) {
        this.major = major;
        this.minor = minor;
        this.states = new ArrayList<BeaconNodeState>();
    }


    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public IndoorLocation getLocation() {
        return location;
    }

    public void setLocation(IndoorLocation location) {
        this.location = location;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }


    public List<BeaconNodeState> getStates() {
        return states;
    }

    public void setStates(List<BeaconNodeState> states) {
        this.states = states;
    }

    public double getMeanRSSI() {
        return meanRSSI;
    }

    public void setMeanRSSI(double meanRSSI) {
        this.meanRSSI = meanRSSI;
    }

    public double getMeanAccuracy() {
        return meanAccuracy;
    }

    public void setMeanAccuracy(double meanAccuracy) {
        this.meanAccuracy = meanAccuracy;
    }

    public double getMeanEstimatedDistance() {
        return meanEstimatedDistance;
    }

    public void setMeanEstimatedDistance(double meanEstimatedDistance) {
        this.meanEstimatedDistance = meanEstimatedDistance;
    }

    @Override
    public String toString() {
        String x = location != null ? ""+location.getX() : "null";
        String y = location != null ? ""+location.getY() : "null";
        return "[major , minor]:[ "+major+" , "+minor+" ] ,*, (location.X , location.Y) : ( "+ x + " , "+ y +" )"+
                "[betteryStatus]:[ "+batteryStatus + "] (meanRSSI): ( "+meanRSSI+" ) ,*,"+" ( meanAccuracy ) : ("+meanAccuracy+" ) " + " ,*, " + " ( txPower @ 1m ) : ( "+txPower  +" )";
    }


    // calculate the mean of the RSSI readings
    public double meanRSSI(){
       if (this.states.size() == 0) {
            this.meanRSSI = Double.NEGATIVE_INFINITY; // no readings , -INF
            return this.meanRSSI;
       }
        // simple averaging
        int sum = 0;
        for(BeaconNodeState state:states){
            sum += state.rssi;
        }
        this.meanRSSI = (double)sum / states.size();
        return this.meanRSSI ;
    }

    // calculate the mean of the Accuracy readings
    public double meanAccuracy(){
        if (this.states.size() == 0) {
            this.meanAccuracy = Double.NEGATIVE_INFINITY; // no readings , -INF
            return this.meanAccuracy;
        }
        // simple averaging
        int sum = 0;
        for(BeaconNodeState state:states){
            sum += state.accuracy;
        }
        this.meanAccuracy = (double)sum / states.size();
        return this.meanAccuracy ;
    }

    // calculate the mean of the EstimatedDistance readings
    public double meanEstimatedDistance(){
        if (this.states.size() == 0) {
            this.meanEstimatedDistance = Double.NEGATIVE_INFINITY; // no readings , -INF
            return this.meanEstimatedDistance;
        }
        // simple averaging
        int sum = 0;
        for(BeaconNodeState state:states){
            sum += state.estimatedDistance;
        }
        this.meanEstimatedDistance = (double)sum / states.size();
        return this.meanEstimatedDistance ;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.batteryStatus = batteryStatus;
    }


}