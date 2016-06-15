package com.navibees.sdk.model.metadata;

import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.postioning.NaviBeesMath;

/**
 * Created by nabilnoaman on 4/28/15.
 */
public class LineIndoorLocationRestriction extends IndoorLocationRestriction{


    private IndoorLocation start;
    private IndoorLocation end;

    public LineIndoorLocationRestriction(int id , int floor , IndoorLocation start , IndoorLocation end) {
        super();
        super.setId(id);
        super.setFloor(floor);
        this.start = start;
        this.end = end;
    }

    public LineIndoorLocationRestriction(){

    }

    @Override
    public IndoorLocation calculateNewCoordinates(IndoorLocation point) {
        IndoorLocation newPoint = NaviBeesMath.closestPointOnLine(this, point);
        if (NaviBeesMath.liesOnLine(this, newPoint)){
            return newPoint;
        }
        return point;
    }

    @Override
    public double calculateDistance(IndoorLocation point) {
        return NaviBeesMath.distanceToLine(this, point);
    }

    @Override
    public boolean isInside(IndoorLocation point) {
        return NaviBeesMath.liesOnLine(this, point);
    }


    public IndoorLocation getEnd() {
        return end;
    }

    public void setEnd(IndoorLocation end) {
        this.end = end;
    }

    public IndoorLocation getStart() {
        return start;
    }

    public void setStart(IndoorLocation start) {
        this.start = start;
    }

}
