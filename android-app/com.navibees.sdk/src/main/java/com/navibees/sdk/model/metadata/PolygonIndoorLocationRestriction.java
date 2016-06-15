package com.navibees.sdk.model.metadata;

import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.postioning.NaviBeesMath;

/**
 * Created by nabilnoaman on 4/28/15.
 */
public class PolygonIndoorLocationRestriction extends IndoorLocationRestriction {

    private IndoorLocation[] vertices;

    @Override
    public IndoorLocation calculateNewCoordinates(IndoorLocation point) {
        return NaviBeesMath.closestPointOnPolygon(this, point);
    }

    @Override
    public double calculateDistance(IndoorLocation point) {
       return NaviBeesMath.distanceToPolygon(this, point);
    }

    @Override
    public boolean isInside(IndoorLocation point) {
        return NaviBeesMath.isInsidePolygon(this, point);
    }


    public IndoorLocation[] getVertices() {
        return vertices;
    }

    public void setVertices(IndoorLocation[] vertices) {
        this.vertices = vertices;
    }


}
