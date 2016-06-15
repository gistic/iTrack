package com.navibees.sdk.model.routing;

import android.content.Context;
import android.util.SparseArray;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.R;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.LineIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.metadata.json.Portal;
import com.navibees.sdk.model.postioning.NaviBeesMath;
import com.navibees.sdk.model.postioning.kalman.Matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hossam on 11/16/15.
 */
final public class TurnByTurnNavigation {

    private static final double THETA_EPSILON = 5;//in degrees
    private static final double DEF_EPSILON = 0.5;//in meters
    private static final String TAG = "TurnByTurnNavigation";
    private static final double DETECT_TURN_THRESHOLD = 5;//in meters
    private static final double DISTANCE_FROM_FINAL_THRESHOLD = 5;

    private static TurnByTurnNavigation mSingleton;

    private SparseArray<List<LineIndoorLocationRestriction>> mSegmentsPerFloor;

    private int mTargetFloor = -1;
    private Portal mPortal;

    private final String[] mActions = {
            "Go straight",
            "Turn gentle right",
            "Turn gentle left",
            "Turn right",
            "Turn left",
            "Turn sharp right",
            "Turn sharp left",
            "Make right U-turn",
            "Make left U-turn",
            "Start",
            "You have arrived",
            "Go up to floor %L",
            "Go down to floor %L",
    };

    public static TurnByTurnNavigation getInstance(Context context) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {

        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature._2D_Maps);

        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Positioning);

        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Multi_Floor_Navigation);

        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.TurnByTurn_Navigation);

        if(mSingleton == null)
            mSingleton = new TurnByTurnNavigation();

        return mSingleton;
    }

    public Portal getCurrentUsedPortal(){
        return mPortal;
    }

    public void updateEsriRoute(SparseArray<Geometry> routes, int targetFloor, Portal portal){

        SparseArray<List<LineIndoorLocationRestriction>> temp = new SparseArray<List<LineIndoorLocationRestriction>>();
        for(int i=0; i<routes.size(); i++) {
            int floorKey = routes.keyAt(i);
            Polyline routePolyLine = (Polyline) routes.get(floorKey);
            IndoorLocation[] points = new IndoorLocation[routePolyLine.getPointCount()];
            for (int j = 0; j < points.length; j++) {
                Point point = routePolyLine.getPoint(j);
                points[j] = new IndoorLocation(point.getX(), point.getY());
            }

            temp.put(floorKey, getSegments(points));
        }

        mTargetFloor = targetFloor;
        if(routes.size() == 1){
            mTargetFloor = routes.keyAt(0);
        }
        mSegmentsPerFloor = temp;

        mPortal = portal;

    }

    public NavigationResult getCurrentResult(Context context , IndoorLocation location){
        NavigationResult result = null;
        if(mSegmentsPerFloor != null){
            List<LineIndoorLocationRestriction> segments = mSegmentsPerFloor.get(location.getFloor().intValue());
            if(segments != null) {
                double bestSegmentScore = Integer.MAX_VALUE;
                for (int i = 0; i < segments.size(); i++) {
                    LineIndoorLocationRestriction segment = segments.get(i);
                    double segmentScore = NaviBeesMath.isPointInsideLine(segment, location);
                    if (segmentScore < bestSegmentScore) {
                        result = new NavigationResult();
                        result.mSegment = segment;
                        result.mRotationAngle = getRotationAngle(segment.getStart(), segment.getEnd());
                        LineIndoorLocationRestriction next = i + 1 < segments.size() ? segments.get(i + 1) : null;
                        result.mMessage = getNavigationMessage(context , segment, next, location, segments.get(segments.size() - 1).getEnd());
                        bestSegmentScore = segmentScore;
                    }
                }
                if(result == null) {
                    IndoorLocation finalLocation = segments.get(segments.size() - 1).getEnd();
                    double distanceFromFinalLocation = NaviBeesMath.eculideanDistance(location, finalLocation);
                    if (distanceFromFinalLocation <= DISTANCE_FROM_FINAL_THRESHOLD) {
                        result = new NavigationResult();
                        int distance = (int) Math.round(distanceFromFinalLocation);
                        result.mMessage = getNearbyDestinationMessage(context , location, distance);
                    }
                }
            }
        }
        return result;
    }

    public class NavigationResult{
        public LineIndoorLocationRestriction mSegment;
        public String mMessage;
        public double mRotationAngle;
    }

    public void reset(){
        mSegmentsPerFloor = null;
        mTargetFloor = -1;
        mPortal = null;
    }

    private List<LineIndoorLocationRestriction> getSegments(IndoorLocation[] points){
        List< LineIndoorLocationRestriction > segments = new ArrayList<LineIndoorLocationRestriction>();
        List<IndoorLocation> segment = new ArrayList<IndoorLocation>();
        segment.add(points[0]);
        double lastTheta = 0, newTheta, defX, defY;

        boolean firstTime = true;

        for(int i=1; i<points.length; i++){
            IndoorLocation lastPoint = segment.get(segment.size()-1);
            defX = points[i].getX() - lastPoint.getX();
            if(defX == 0) defX = 0.0001;
            defY = points[i].getY() - lastPoint.getY();

            newTheta = Math.atan(defY / defX);
            newTheta = Math.toDegrees(newTheta);

            if(NaviBeesMath.eculideanDistance(points[i] , lastPoint) > DEF_EPSILON ) {

                if (!firstTime && Math.abs(newTheta - lastTheta) > THETA_EPSILON) {
                    if(segment.size() > 1) {
                        IndoorLocation start = segment.get(0);
                        IndoorLocation end = segment.get(segment.size()-1);
                        LineIndoorLocationRestriction line = new LineIndoorLocationRestriction(0, 0, start, end);
                        segments.add(line);
                    }

                    segment.clear();
                    segment.add(lastPoint);
                    lastTheta = newTheta;
                }

                if(firstTime){
                    firstTime = false;
                    lastTheta = newTheta;
                }
                segment.add(points[i]);
            }
        }

        if(segment.size() > 1){
            IndoorLocation start = segment.get(0);
            IndoorLocation end = segment.get(segment.size()-1);
            LineIndoorLocationRestriction line = new LineIndoorLocationRestriction(0, 0, start, end);
            segments.add(line);
        }

        return segments;
    }


    private String getNearbyDestinationMessage( Context context , IndoorLocation location, int distance){
        String message = "";
        if(location.getFloor().intValue() == mTargetFloor){
            if(distance <= 2){
                message = getArriveMessage(context);
            }else{
                String format = getDistanceToDestinationMessage(context);
                message = String.format(format , distance);
            }
        }else{

            String floorName =  AppManager.getInstance().getMetaDataManager().getFloors(context).get(mTargetFloor).getNameWRTLang();

            if(distance <= 2){
                message = getGotoFloorMessage(context) + " " + floorName;
            }else{
                String format = getDistanceToPortalMessage(context);
                message = String.format(format, mPortal.getTypeWRTLang() , distance , floorName);
            }
        }
        return message;
    }


    private String getNavigationMessage(Context context , LineIndoorLocationRestriction current, LineIndoorLocationRestriction next, IndoorLocation location, IndoorLocation finalLocation){
        String message = "Unknown!";

        double distanceFromFinalLocation = NaviBeesMath.eculideanDistance(location, finalLocation);
        if( distanceFromFinalLocation <= DISTANCE_FROM_FINAL_THRESHOLD){
            int distance = (int) Math.round(distanceFromFinalLocation);
            message = getNearbyDestinationMessage(context , location, distance);
        }else {
            double distanceFromSegmentEnd = NaviBeesMath.eculideanDistance(location, current.getEnd());
            int distance = (int) Math.round(distanceFromSegmentEnd);

            if(next != null && distanceFromSegmentEnd <= DETECT_TURN_THRESHOLD){
                double angle = getRotationAngle(current.getStart(), current.getEnd());
                angle = Math.toRadians(angle);

                IndoorLocation pivotPoint = current.getStart();

                Matrix translation = new Matrix(3, 3);
                translation.set_matrix(1, 0, -pivotPoint.getX(),
                        0, 1, -pivotPoint.getY(),
                        0, 0, 1);

                Matrix rotation = new Matrix(3, 3);
                rotation.set_matrix(Math.cos(angle), -Math.sin(angle), 0,
                        Math.sin(angle), Math.cos(angle), 0,
                        0, 0, 1);

                Matrix invTranslation = new Matrix(3, 3);
                invTranslation.set_matrix(1, 0, pivotPoint.getX(),
                        0, 1, pivotPoint.getY(),
                        0, 0, 1);


                Matrix tempResult = new Matrix(3, 3);
                Matrix.multiply_matrix(invTranslation, rotation, tempResult);
                Matrix transformation = new Matrix(3, 3);
                Matrix.multiply_matrix(tempResult, translation, transformation);

                IndoorLocation firstPoint = next.getStart();
                IndoorLocation secondPoint = next.getEnd();

                Matrix newPoint = new Matrix(3, 1);
                Matrix oldPoint = new Matrix(3, 1);
                oldPoint.set_matrix(firstPoint.getX(), firstPoint.getY(), 1);
                Matrix.multiply_matrix(transformation, oldPoint, newPoint);

                firstPoint = new IndoorLocation(newPoint.data[0][0], newPoint.data[1][0]);

                oldPoint.set_matrix(secondPoint.getX(), secondPoint.getY(), 1);
                Matrix.multiply_matrix(transformation, oldPoint, newPoint);

                secondPoint = new IndoorLocation(newPoint.data[0][0], newPoint.data[1][0]);

                double angleBetweenTwoLines = NaviBeesMath.calculateAngleBetweenTwoLines(current, next);
                String describeTurn = getStringRepresentAngle(context , angleBetweenTwoLines);

                String turn = getTurnLeftMessage(context);
                if (secondPoint.getX() > firstPoint.getX())
                    turn = getTurnRightMessage(context);

                String format = getCompleteTurnMessage(context);
                message = String.format(format, describeTurn, turn, distance);

            }else{
                message = getGoStraightMessage(context);
            }

        }
        return message;
    }

    private String getGoStraightMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_go_straight);
    }

    private String getCompleteTurnMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_compltete_turn);
    }

    private String getArriveMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_arrive);
    }

    private String getTurnLeftMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_turn_left);
    }


    private String getTurnRightMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_turn_right);
    }

    private String getDistanceToDestinationMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_distance_to_destination);
    }

    private String getGotoFloorMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_go_to_floor);
    }

    private String getDistanceToPortalMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_distance_to_portal);
    }


    private String getSharpMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_sharp);
    }

    private String getGentleMessage(Context context) {
        return context.getResources().getString(R.string.turn_by_turn_message_gentle);
    }


    private String getStringRepresentAngle(Context context , double angle){
        int intAngle = (int) Math.round(angle);
        String representation = "";

        int HALF_ANGLE = 90;

        if(intAngle > HALF_ANGLE + 20)
            representation = getGentleMessage(context);
        else if(intAngle < HALF_ANGLE - 20)
            representation = getSharpMessage(context);

        return representation;
    }


    private List<String> getDirections(List<List<IndoorLocation>> segments){
        List<String> directions = new ArrayList<String>();

        for(int i=1; i<segments.size(); i++){
            List<IndoorLocation> firstSegment = segments.get(i-1);
            List<IndoorLocation> secondSegment = segments.get(i);

            double angle = getRotationAngle(firstSegment.get(0), firstSegment.get(firstSegment.size()-1));
            angle = Math.toRadians(angle);

            IndoorLocation pivotPoint = firstSegment.get(0);

            Matrix translation = new Matrix(3, 3);
            translation.set_matrix(1, 0, -pivotPoint.getX(),
                    0, 1, -pivotPoint.getY(),
                    0, 0, 1);

            Matrix rotation = new Matrix(3, 3);
            rotation.set_matrix(    Math.cos(angle),    -Math.sin(angle),   0,
                    Math.sin(angle),    Math.cos(angle),    0,
                    0,                  0,                  1);

            Matrix invTranslation = new Matrix(3, 3);
            invTranslation.set_matrix(1, 0, pivotPoint.getX(),
                    0, 1, pivotPoint.getY(),
                    0, 0, 1);


            Matrix tempResult = new Matrix(3, 3);
            Matrix.multiply_matrix(invTranslation, rotation, tempResult);
            Matrix transformation = new Matrix(3, 3);
            Matrix.multiply_matrix(tempResult, translation, transformation);


            IndoorLocation firstPoint = secondSegment.get(0);
            IndoorLocation secondPoint = secondSegment.get(secondSegment.size() - 1);

            Matrix newPoint = new Matrix(3, 1);
            Matrix oldPoint = new Matrix(3, 1);
            oldPoint.set_matrix(firstPoint.getX(), firstPoint.getY(), 1);
            Matrix.multiply_matrix(transformation, oldPoint, newPoint);

            firstPoint = new IndoorLocation(newPoint.data[0][0], newPoint.data[1][0]);

            oldPoint.set_matrix(secondPoint.getX(), secondPoint.getY(), 1);
            Matrix.multiply_matrix(transformation, oldPoint, newPoint);

            secondPoint = new IndoorLocation(newPoint.data[0][0], newPoint.data[1][0]);

            if(secondPoint.getX() > firstPoint.getX())
                directions.add("Turn To Right");
            else
                directions.add("Turn To Left");

        }

        return directions;
    }

    private double getRotationAngle(IndoorLocation p1, IndoorLocation p2){
        double dx = p2.getX() - p1.getX();
        if(dx == 0) dx = 0.0001;
        double dy = p2.getY() - p1.getY();

        double angle = Math.atan(dy/dx);
        angle = Math.toDegrees(angle);

        if (dx > 0 && dy > 0) {
            angle = 90 - angle;
        } else if (dx < 0 && dy > 0) {
            angle = 270 + (-angle);
        } else if (dx > 0 && dy < 0) {
            angle = 90 + (-angle);
        } else if (dx < 0 && dy < 0) {
            angle = 270 - angle;
        } else if (dx > 0 && dy == 0) {
            angle = 90;
        } else if (dx < 0 && dy == 0) {
            angle = 270;
        }
        return angle;
    }

}
