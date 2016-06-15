package com.navibees.sdk.model.postioning;

import com.navibees.sdk.model.metadata.CircleIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.LineIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.PolygonIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.json.IndoorLocation;

/**
 * Created by nabilnoaman on 4/28/15.
 */
public class NaviBeesMath {

    static class IndoorLocationDelta{
         double indoorDeltaX;
         double indoorDeltaY;

        public IndoorLocationDelta(double indoorDeltaX, double indoorDeltaY){
            this.indoorDeltaX = indoorDeltaX;
            this.indoorDeltaY = indoorDeltaY;
        }
    }

    // ===========================

    // calculate a vector between 2 points
    private static IndoorLocationDelta calculateVector(IndoorLocation p1,IndoorLocation p2){
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        return new IndoorLocationDelta(dx, dy);
    }

// ===========================

    // calculate the dot product between 2 vectors
    private static double dotProduct(double[] v1 , double[] v2) {
        double dp = 0.0;
        if (v1.length != v2.length){
            return dp;
        }
        for (int i=0; i<v1.length; i++){
            dp += v1[i] * v2[i];
        }
        return dp;
    }

    // calculate the dot product between 2 vectors
// represented as IndoorLocations
    private static double dotProduct(IndoorLocation v1 , IndoorLocation v2) {
        double[] vec1 =  new double[]{v1.getX(), v1.getY()};
        double[] vec2 =  new double[]{v2.getX(), v2.getY()};
        return dotProduct(vec1, vec2);
    }

// ===========================

    // calculate the eculidean distance between 2 points
    private static double eculideanDistance(double[] p1, double[] p2) {
        double distance2 = 0.0;
        int count  = Math.max(p1.length, p2.length);
        for (int i=0; i<count; i++){
            double e1 = 0.0;
            double e2 = 0.0;
            if (i >= 0 && i < p1.length){
                e1 = p1[i];
            }
            if (i >= 0 && i < p2.length){
                e2 = p2[i];
            }
            distance2 += Math.pow(e2 - e1, 2.0);
        }
        return Math.sqrt(distance2);
    }

    // calculate the eculidean distance between 2 points
// represented as IndoorLocations
    public static double eculideanDistance(IndoorLocation p1, IndoorLocation p2){
        double[] point1 = new double[]{p1.getX(), p1.getY()};
        double[] point2 = new double[]{p2.getX(), p2.getY()};
        return eculideanDistance(point1, point2);
    }

// ===========================

    // calculate the length power 2 of a vector
    private static double length2(double[] v) {
        double len2 = 0.0;
        for (double coordinate : v) {
            len2 += Math.pow(coordinate, 2.0);
        }
        return len2;
    }

    // calculate the length power 2 of a vector
// represented as IndoorLocation
    private static double length2(IndoorLocation v) {
        double[] vec = new double[]{v.getX(), v.getY()};
        return length2(vec);
    }

    // calculate the length of a vector
    private static double length(double[] v)  {
        return Math.sqrt(length2(v));
    }

    // calculate the length of a vector
// represented as IndoorLocation
    private static double length(IndoorLocation v){
        return length2(v);
    }

// ===========================
static class Slope{

    private double degree;
    private boolean infinity;

    Slope(double degree, boolean infinity) {
        this.degree = degree;
        this.infinity = infinity;
    }


}

    // calculate slope of a line given its 2 endpoints
    private static Slope calculateSlope(IndoorLocation p1 ,IndoorLocation p2) {
        if (p1.getX() == p2.getX()) {
            // infinite slope
            return new Slope((double)Integer.MAX_VALUE , true);
        }
        double slope = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
        return new Slope(slope, false);
    }

    static class Intersection{

        private IndoorLocation intersectionPoint;
        private boolean parallel;

        Intersection(IndoorLocation intersectionPoint, boolean parallel) {
            this.intersectionPoint = intersectionPoint;
            this.parallel = parallel;
        }


    }

    public static double calculateAngleBetweenTwoLines(LineIndoorLocationRestriction line1, LineIndoorLocationRestriction line2){
        double ab = eculideanDistance(line1.getStart(), line1.getEnd());
        double bc = eculideanDistance(line2.getStart(), line2.getEnd());
        double ac = eculideanDistance(line1.getStart(), line2.getEnd());

        double angle = Math.acos( ( (ab * ab) + (bc * bc) - (ac * ac) ) / (2 * ab * bc) );
        angle = Math.toDegrees(angle);
        return angle;
    }

    // calculate intersection point between 2 lines
    private static Intersection intersectionBetweenTwoLines(LineIndoorLocationRestriction line1 , LineIndoorLocationRestriction line2) {
        double x1 = line1.getStart().getX();
        double y1 = line1.getStart().getY();
        double x2 = line1.getEnd().getX();
        double y2 = line1.getEnd().getY();
        double x3 = line2.getStart().getX();
        double y3 = line2.getStart().getY();
        double x4 = line2.getEnd().getX();
        double y4 = line2.getEnd().getY();
        double d= (x1-x2)*(y3-y4)-(y1-y2)*(x3-x4);
        if (d == 0)
        {
            // the 2 lines are parallel or coincident
            return new Intersection(new IndoorLocation(0.0,0.0) ,true);
        }
        double n1 = (x1*y2-y1*x2)*(x3-x4);
        n1 -= (x1-x2)*(x3*y4-y3*x4);
        double n2 = (x1*y2-y1*x2)*(y3-y4);
        n2 -= (y1-y2)*(x3*y4-y3*x4);
        return new Intersection(new IndoorLocation(n1/d , n2/d) ,false);
    }

// ===========================

    // calculate perpendicular distance between point p
// and a line that's enpoints are p1 and p2
    private static double distanceToLine(IndoorLocation p1 , IndoorLocation p2 , IndoorLocation p ) {
        double twiceArea = 0.0;
        twiceArea += (p2.getY() - p1.getY()) * p.getX();
        twiceArea -= (p2.getX() - p1.getX()) * p.getY();
        twiceArea += p2.getX() * p1.getY();
        twiceArea -= p2.getY() * p1.getX();
        return (Math.abs(twiceArea) / eculideanDistance(p1, p2));
    }

    // calculate perpendicular distance between
// point p and a line
    public static double distanceToLine(LineIndoorLocationRestriction line , IndoorLocation p) {
        return distanceToLine(line.getStart(), line.getEnd(), p);
    }

    // calculate projected point p' of p on a line
// that's enpoints are p1 and p2
    private static IndoorLocation closestPointOnLine(IndoorLocation p1 ,IndoorLocation p2 , IndoorLocation p) {
        IndoorLocationDelta e1 = calculateVector(p1, p2);
        IndoorLocationDelta e2 = calculateVector(p1, p);
        double dp = dotProduct(new double[]{e1.indoorDeltaX, e1.indoorDeltaY}, new double[]{e2.indoorDeltaX, e2.indoorDeltaY});
        double len2 = length2(new double[]{e1.indoorDeltaX, e1.indoorDeltaY});
        IndoorLocation projectedPoint = new IndoorLocation( 0.0, 0.0);
        projectedPoint.setX(p1.getX() + (dp * e1.indoorDeltaX) / len2);
        projectedPoint.setY(p1.getY() + (dp * e1.indoorDeltaY) / len2);
        return projectedPoint;
    }

    // calculate projected point p' of p on a line
    public static IndoorLocation closestPointOnLine(LineIndoorLocationRestriction line , IndoorLocation p)  {
        return closestPointOnLine(line.getStart(), line.getEnd(), p);
    }

    public static double isPointInsideLine(LineIndoorLocationRestriction line , IndoorLocation p){
        int LINE_HALF_THICKNESS = 3;
        IndoorLocation start = line.getStart();
        IndoorLocation end = line.getEnd();

        IndoorLocation pDash = closestPointOnLine(line, p);

        double distance = eculideanDistance(p, pDash);
        if(distance > LINE_HALF_THICKNESS) return Integer.MAX_VALUE;

        double crossproduct = (pDash.getY() - start.getY()) * (end.getX() - start.getX()) - (pDash.getX() - start.getX()) * (end.getY() - start.getY());
        if (Math.abs(crossproduct) > 0.01) return Integer.MAX_VALUE;

        double dotproduct = (pDash.getX() - start.getX()) * (end.getX() - start.getX()) + (pDash.getY() - start.getY()) * (end.getY() - start.getY());
        if (dotproduct < 0)  return Integer.MAX_VALUE;

        double squaredLengthBA = (end.getX() - start.getX()) * (end.getX() - start.getX()) + (end.getY() - start.getY()) * (end.getY() - start.getY());
        if (dotproduct > squaredLengthBA) return Integer.MAX_VALUE;

        return distance;
    }

    // determine if a point lies on a line
    public static boolean liesOnLine(LineIndoorLocationRestriction line , IndoorLocation p)
    {
        // return (distanceToLine(line, p) == 0)
        double slope = 0.0;
        double intercept = 0.0;
        double x1 = line.getStart().getX();
        double y1 = line.getStart().getY();
        double x2 = line.getEnd().getX();
        double y2 = line.getEnd().getY();
        double px = p.getX();
        double py = p.getY();
        double left = 0.0;
        double top = 0.0;
        double right = 0.0;
        double bottom = 0.0;
        double dx = 0.0;
        double dy = 0.0;

        dx = x2 - x1;
        dy = y2 - y1;

        slope = dy / dx;
        intercept = y1 - slope * x1;

        if(x1 < x2){
            left = x1;
            right = x2;
        }
        else{
            left = x2;
            right = x1;
        }
        if(y1 < y2){
            top = y2;
            bottom = y1;
        }
        else{
            top = y1;
            bottom = y2;
        }

        if( slope * px + intercept > (py - 0.01) &&
                slope * px + intercept < (py + 0.01))
        {
            if( px >= left && px <= right &&
                    py >= bottom && py <= top )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        return false;
    }

// ===========================

    // calculate the distance between a point and circle
    public static double distanceToCircle(CircleIndoorLocationRestriction circle , IndoorLocation p){
        double distanceToCenter = eculideanDistance(circle.getCenter(), p);
        return (distanceToCenter - circle.getRadius());
    }

    public static boolean isInsideCircle(CircleIndoorLocationRestriction circle ,IndoorLocation p)  {
        return (distanceToCircle(circle, p) <= 0);
    }

    // calculate the point that lies on the circle and has
// closest distance to the given point
    public static IndoorLocation closestPointOnCircle(CircleIndoorLocationRestriction circle, IndoorLocation p) {
        double x1 = 0.0;
        double y1 = 0.0;
        double x2 = 0.0;
        double y2 = 0.0;
        double xp = 0.0;
        double yp = 0.0;

        Slope slope = calculateSlope(p, circle.getCenter());

        double m = slope.degree;
        boolean infinity = slope.infinity;
        if (infinity) {
            x1 = p.getX();
            x2 = p.getX();
            double a = 1.0;
            double b = -2.0 * circle.getCenter().getY();
            double c = Math.pow(circle.getCenter().getX(), 2.0);
            c += Math.pow(circle.getCenter().getY(), 2.0);
            c -= Math.pow(circle.getRadius(), 2.0);
            c += Math.pow(x1, 2.0) - (2.0 * p.getX() * circle.getCenter().getX());
            double disc = Math.pow(b, 2.0) - (4.0 * a * c);
            y1 = ((-1.0 * b) + Math.sqrt(disc)) / (2.0 * a);
            y2 = ((-1.0 * b) - Math.sqrt(disc)) / (2.0 * a);
        }
        else {
            double bl = p.getY() - (m * p.getX());
            double a  = Math.pow(m, 2.0) + 1.0;
            double b  = (-2.0 * circle.getCenter().getX());
            b += (2.0 * m * bl);
            b += (-2.0 * m * circle.getCenter().getY());
            double c = Math.pow(circle.getCenter().getX(), 2.0);
            c += Math.pow(circle.getCenter().getY(), 2.0);
            c -= Math.pow(circle.getRadius(), 2.0);
            c += Math.pow(bl, 2.0);
            c += (-2.0 * bl * circle.getCenter().getY());
            double disc = Math.pow(b, 2.0) - (4 * a * c);
            x1 = ((-1.0 * b) + Math.sqrt(disc)) / (2.0 * a);
            x2 = ((-1.0 * b) - Math.sqrt(disc)) / (2.0 * a);
            y1 = (m * x1) + bl;
            y2 = (m * x2) + bl;
        }
        double dist1 = eculideanDistance(new double[]{p.getX(), p.getY()}, new double[]{x1, y1});
        double dist2 = eculideanDistance(new double[]{p.getX(), p.getY()} , new double[]{x2, y2});
        if (dist1 < dist2) {
            xp = x1;
            yp = y1;
        }
        else {
            xp = x2;
            yp = y2;
        }
        return new IndoorLocation(xp, yp);
    }

// ===========================

    // calculate the area of a polygon given its vertices
    private double calculateAreaOfPolygon(IndoorLocation[] vertices) {
        double area = 0.0;
        for (int i=0; i<vertices.length; i+=1) {
            double Xi  = vertices[i].getX();
            double Xi1 = vertices[(i+1)%(vertices.length)].getX();
            double Yi  = vertices[i].getY();
            double Yi1 = vertices[(i+1)%(vertices.length)].getY();
            area += (Xi*Yi1) - (Xi1*Yi);
        }
        return (Math.abs(area) / 2.0);
    }

    // calculate the area of a polygon given
// as PolygonIndoorLocationRestriction
    private double calculateAreaOfPolygon(PolygonIndoorLocationRestriction polygon) {
        return calculateAreaOfPolygon(polygon.getVertices());
    }

    // calculate the centroid of a polygon given its vertices
    private static IndoorLocation calculateCentroidOfPolygon(IndoorLocation[] vertices)  {
        double Cx  = 0.0;
        double Cy  = 0.0;
        double A   = 0.0;
        for (int i=0; i<vertices.length; i+=1) {
            double Xi  = vertices[i].getX();
            double Xi1 = vertices[(i+1)%(vertices.length)].getX();
            double Yi  = vertices[i].getY();
            double Yi1 = vertices[(i+1)%(vertices.length)].getY();
            double t = (Xi*Yi1 - Xi1*Yi);
            Cx += ((Xi + Xi1) * t);
            Cy += ((Yi + Yi1) * t);
            A  += t;
        }
        return new IndoorLocation( Cx / (3.0 * A),  Cy / (3.0 * A));
    }

    // calculate the centroid of a polygon given
// as PolygonIndoorLocationRestriction
    private static IndoorLocation calculateCentroidOfPolygon(PolygonIndoorLocationRestriction polygon)  {
        return calculateCentroidOfPolygon(polygon.getVertices());
    }

    // check if a point is inside a polygon
    public static boolean isInsidePolygon(PolygonIndoorLocationRestriction polygon ,IndoorLocation p)
    {
        boolean c = false;
        int j = polygon.getVertices().length-1;
        for (int i = 0; i < polygon.getVertices().length; j = i++) {
            boolean check1 = ((polygon.getVertices()[i].getY() > p.getY()) != ( polygon.getVertices()[j].getY() > p.getY()));
            boolean check2 = (p.getX() < (polygon.getVertices()[j].getX()-polygon.getVertices()[i].getX()) * (p.getY()-polygon.getVertices()[i].getY()) / (polygon.getVertices()[j].getY()-polygon.getVertices()[i].getY()) + polygon.getVertices()[i].getX());
            if ( check1 && check2) {
                c = !c;
            }
        }
        return c;
    }

    // calculate the point that lies on the polygon and has
// closest distance to the given point
    public static IndoorLocation closestPointOnPolygon(PolygonIndoorLocationRestriction polygon , IndoorLocation p) {
        IndoorLocation centroid = calculateCentroidOfPolygon(polygon);
        LineIndoorLocationRestriction mainLine  = new LineIndoorLocationRestriction( -1,  -1, p, centroid);
        IndoorLocation closestPoint = centroid;
        double minDistance  = eculideanDistance(centroid, p);
        for (int i =0; i<polygon.getVertices().length; i+=1) {
            IndoorLocation vertex1 = polygon.getVertices()[i];
            IndoorLocation vertex2 = polygon.getVertices()[(i+1)%(polygon.getVertices().length)];
            LineIndoorLocationRestriction polygonSide = new LineIndoorLocationRestriction(-1,  -1, vertex1,  vertex2);
            Intersection intersection = intersectionBetweenTwoLines(mainLine, polygonSide);
            IndoorLocation point = intersection.intersectionPoint;
            boolean parallel = intersection.parallel;

            if (!parallel) {
                double distance = eculideanDistance(p, point);
                if (distance < minDistance && liesOnLine(polygonSide, point)) {
                    minDistance = distance;
                    closestPoint = point;
                }
            }
        }
        return (closestPoint);
    }

    // calculate the point that lies on the polygon and has
// closest distance to the given point
    public static double distanceToPolygon(PolygonIndoorLocationRestriction polygon , IndoorLocation p)  {
        IndoorLocation centroid = calculateCentroidOfPolygon(polygon);
        LineIndoorLocationRestriction mainLine = new LineIndoorLocationRestriction(-1, -1, p, centroid);
        IndoorLocation closestPoint = centroid;
        double minDistance  = eculideanDistance(centroid, p);
        for (int i =0; i<polygon.getVertices().length; i+=1) {
            IndoorLocation vertex1 = polygon.getVertices()[i];
            IndoorLocation vertex2 = polygon.getVertices()[(i+1)%(polygon.getVertices().length)];
            LineIndoorLocationRestriction polygonSide  =  new LineIndoorLocationRestriction( -1, -1, vertex1, vertex2);
            Intersection intersection = intersectionBetweenTwoLines(mainLine, polygonSide);
            IndoorLocation point = intersection.intersectionPoint;
            boolean parallel = intersection.parallel;
            if (!parallel) {
                double distance = eculideanDistance(p, point);
                if (distance < minDistance && liesOnLine(polygonSide, point)) {
                    minDistance = distance;
                }
            }
        }
        return minDistance;
    }


}
