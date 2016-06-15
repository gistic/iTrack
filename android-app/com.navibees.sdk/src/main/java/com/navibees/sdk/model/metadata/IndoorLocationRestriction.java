package com.navibees.sdk.model.metadata;

import com.navibees.sdk.model.metadata.json.IndoorLocation;

/**
 * Created by nabilnoaman on 4/16/15.
 */
public abstract class IndoorLocationRestriction {

    private int id;
    private int floor;

    public abstract IndoorLocation calculateNewCoordinates(IndoorLocation point);
    public abstract double calculateDistance(IndoorLocation point);
    public abstract boolean isInside(IndoorLocation point);


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

}
