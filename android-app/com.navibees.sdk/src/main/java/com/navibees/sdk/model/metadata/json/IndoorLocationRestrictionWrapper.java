package com.navibees.sdk.model.metadata.json;


/**
 * Created by nabilnoaman on 4/28/15.
 */
public class IndoorLocationRestrictionWrapper {

    public final static String[] RESTRICTION_TYPE = {"point" , "line" , "circle" , "polygon"};

    private int id;
    private int floor;
    private String type;

    private IndoorLocation[] points;//has value if type is line

    private IndoorLocation[] vertices;//has value if type is polygon

    private double radius;//has value if type is circle
    private IndoorLocation center;//has value if type is circle

    private IndoorLocation point;//has value if type is point



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static String[] getRestrictionType() {
        return RESTRICTION_TYPE;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public IndoorLocation[] getPoints() {
        return points;
    }

    public void setPoints(IndoorLocation[] points) {
        this.points = points;
    }

    public IndoorLocation[] getVertices() {
        return vertices;
    }

    public void setVertices(IndoorLocation[] vertices) {
        this.vertices = vertices;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public IndoorLocation getCenter() {
        return center;
    }

    public void setCenter(IndoorLocation center) {
        this.center = center;
    }

    public IndoorLocation getPoint() {
        return point;
    }

    public void setPoint(IndoorLocation point) {
        this.point = point;
    }




}
