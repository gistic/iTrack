package com.navibees.sdk.model.metadata;

import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.postioning.NaviBeesMath;

/**
 * Created by nabilnoaman on 4/28/15.
 */
public class CircleIndoorLocationRestriction extends IndoorLocationRestriction {

    private IndoorLocation center;
    private double radius;


    @Override
    public IndoorLocation calculateNewCoordinates(IndoorLocation point) {
        return NaviBeesMath.closestPointOnCircle(this, point);
    }

    @Override
    public double calculateDistance(IndoorLocation point) {
        return NaviBeesMath.distanceToCircle(this, point);
    }

    @Override
    public boolean isInside(IndoorLocation point) {
        return NaviBeesMath.isInsideCircle(this, point);
    }



    public IndoorLocation getCenter() {
        return center;
    }

    public void setCenter(IndoorLocation center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }



}
