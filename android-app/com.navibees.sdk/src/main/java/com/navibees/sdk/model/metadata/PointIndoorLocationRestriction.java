package com.navibees.sdk.model.metadata;

import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.postioning.NaviBeesMath;

/**
 * Created by nabilnoaman on 4/28/15.
 */
public class PointIndoorLocationRestriction extends IndoorLocationRestriction {

    private IndoorLocation point;

    @Override
    public IndoorLocation calculateNewCoordinates(IndoorLocation point) {
        return this.point;
    }

    @Override
    public double calculateDistance(IndoorLocation point) {
        return NaviBeesMath.eculideanDistance(this.point, point);
    }

    @Override
    public boolean isInside(IndoorLocation point) {
        return ((this.point.getX() == point.getX()) && (this.point.getY() == point.getY()));
    }


    public IndoorLocation getPoint() {
        return point;
    }

    public void setPoint(IndoorLocation point) {
        this.point = point;
    }

}
