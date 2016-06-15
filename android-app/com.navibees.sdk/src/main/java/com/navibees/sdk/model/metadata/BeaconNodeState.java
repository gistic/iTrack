package com.navibees.sdk.model.metadata;

import java.util.Date;

/**
 * Created by nabilnoaman on 4/18/15.
 */
public class BeaconNodeState {


        Date timestamp;
        int rssi;
        double accuracy;
        double estimatedDistance;

        public BeaconNodeState (int rssi , double accuracy , double estimatedDistance) {
        this.timestamp = new Date();
        this.rssi = rssi;
        this.accuracy = accuracy;
        this.estimatedDistance = estimatedDistance;
    }

      double getAccuracy() {
        return accuracy;
    }

      void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

      Date getTimestamp() {
        return timestamp;
    }

      void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

      int getRssi() {
        return rssi;
    }

      void setRssi(int rssi) {
        this.rssi = rssi;
    }

      double getEstimatedDistance() {
        return estimatedDistance;
    }

      void setEstimatedDistance(double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    
   
    
}
