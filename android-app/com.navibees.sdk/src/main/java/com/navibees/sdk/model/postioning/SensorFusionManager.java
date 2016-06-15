package com.navibees.sdk.model.postioning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.metadata.json.IndoorLocationConfidence;

import java.util.Arrays;

/**
 * Created by hossam on 10/5/15.
 */
public class SensorFusionManager implements SensorEventListener{

    private static SensorFusionManager instance;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagnetSensor;
    private Sensor mGyroSensor;

    private boolean mAccelerometerSet = false;
    private boolean mMagnetometerSet = false;

    //variables relevant to gyroscope
    private float[] mLastAccelerometer;
    private float[] mLastMagnetometer;
    private float[] mInitialRotationMatrix;
    private float[] deltaRotationVector;
    private float[] mCurrentRotationMatrix;
    private long timestamp = 0;
    private final float NS2S = 1.0f / 1000000000.0f;
    private final float OMEGA_EPSILON = 0.01f;

    //variables relevant to rotate map based on movement
    private final int NUM_POINTS = 5;
    private final int MIN_POINTS = 3;
    private final int MIN_DISTANCE = 3; //In meters
    private final double SLOPE_DIFFERENCE_EPSILON = 0.05;
    private IndoorLocation[] lastPoints = new IndoorLocation[NUM_POINTS];
    private enum Direction {N, S, E, W, NE, NW, SE, SW, C};

    private Direction gyroscopeDirection;

    private static final String TAG = "SensorFusionManager";

    private SensorFusionManager(){

    }

    public static SensorFusionManager getInstance(){
        if(instance == null)
            instance = new SensorFusionManager();

        return instance;
    }

    public void registerSensor(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mLastAccelerometer = new float[3];
        mLastMagnetometer = new float[3];
        mInitialRotationMatrix = new float[9];
        deltaRotationVector = new float[4];
        mCurrentRotationMatrix = new float[9];
        timestamp = 0;

        gyroscopeDirection = Direction.C;

        mAccelerometerSet = false;
        mMagnetometerSet = false;

        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void unregisterSensor(){
        if(mSensorManager != null)
            mSensorManager.unregisterListener(this);

        mLastAccelerometer = null;
        mLastMagnetometer = null;
        mInitialRotationMatrix = null;
        deltaRotationVector = null;
        mCurrentRotationMatrix = null;
        timestamp = 0;

        gyroscopeDirection = Direction.C;

        mAccelerometerSet = false;
        mMagnetometerSet = false;

        mSensorManager = null;
        mAccelerometerSensor = null;
        mMagnetSensor = null;
        mGyroSensor = null;

    }


    public double getRotationAngleForMap(IndoorLocation newLocation){
        if(newLocation == null || newLocation.getConfidence() != IndoorLocationConfidence.High)
            return -1;

        //Initialize all points to the first point.
        if(lastPoints[0] == null){
            for(int i=0; i<NUM_POINTS; i++){
                lastPoints[i] = newLocation;
            }
            return -1;
        }

        //Move points to the tail, and make new point on head
        for(int i=1; i<NUM_POINTS; i++){
            lastPoints[i-1] = lastPoints[i];
        }
        lastPoints[NUM_POINTS-1] = newLocation;

        //Check first between last two points and verify their direction with direction from gyroscope
        double dx = lastPoints[NUM_POINTS-1].getX() - lastPoints[NUM_POINTS-2].getX();
        double dy = lastPoints[NUM_POINTS-1].getY() - lastPoints[NUM_POINTS-2].getY();

        double movementTheta = -1;
        if(dx != 0) {

            movementTheta = Math.atan(dy / dx);
            movementTheta = Math.toDegrees(movementTheta);
            movementTheta = Math.round(movementTheta);

            if (dx > 0 && dy > 0) {
                movementTheta = 90 - movementTheta;
            } else if (dx < 0 && dy > 0) {
                movementTheta = 270 + (-movementTheta);
            } else if (dx > 0 && dy < 0) {
                movementTheta = 90 + (-movementTheta);
            } else if (dx < 0 && dy < 0) {
                movementTheta = 270 - movementTheta;
            } else if (dx > 0 && dy == 0) {
                movementTheta = 90;
            } else if (dx < 0 && dy == 0) {
                movementTheta = 270;
            }
        }else {
            if(dx == 0 && dy == 0) {
                return -1;
            }else if(dx == 0 && dy > 0) {
                movementTheta = 0;
            }else if(dx == 0 && dy < 0) {
                movementTheta = 180;
            }
        }

//        Log.d(TAG, "Movement Angle = " + movementTheta);
//        Log.d(TAG, "Movement Direction = " + getDirection(movementTheta));

        if(gyroscopeDirection == getDirection(movementTheta))
            return movementTheta;

        //Compare all slopes between consecutive points to check if they all have the same slope.
        //If not return
        double oldSlope = getSlope(lastPoints[NUM_POINTS-1], lastPoints[NUM_POINTS-2]);
        double newSlope = 0;
        double difference = 0;
        for(int i=NUM_POINTS-2; i > 0; i--){
            newSlope = getSlope(lastPoints[i], lastPoints[i-1]);
            difference = Math.abs(newSlope - oldSlope);
            if(difference < SLOPE_DIFFERENCE_EPSILON){
                oldSlope = newSlope;
                if(NUM_POINTS - (i-1) >= MIN_POINTS && NaviBeesMath.eculideanDistance(lastPoints[NUM_POINTS-1], lastPoints[i-1]) > MIN_DISTANCE)
                    return movementTheta;
            }else{
                return -1;
            }
        }

        return -1;
    }

    private Direction getDirection(double degrees){
        Direction result = Direction.C;
        if(degrees > 0 && degrees < 90)
            result = Direction.NE;
        else if(degrees > 90 && degrees < 180)
            result = Direction.SE;
        else if(degrees > 180 && degrees < 270)
            result = Direction.SW;
        else if(degrees > 270 && degrees < 360)
            result = Direction.NW;
        else if(degrees == 0 || degrees == 360)
            result = Direction.N;
        else if(degrees == 90)
            result = Direction.E;
        else if(degrees == 180)
            result = Direction.S;
        else if(degrees == 270)
            result = Direction.W;
        return result;
    }

    private double getSlope(IndoorLocation p2, IndoorLocation p1){
        if(p1.getX() == p2.getX())
            return Integer.MAX_VALUE;

        return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == mAccelerometerSensor || event.sensor == mMagnetSensor){
            if(event.sensor == mAccelerometerSensor){
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mAccelerometerSet = true;
            }else if(event.sensor == mMagnetSensor){
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mMagnetometerSet = true;
            }

            if(mAccelerometerSet && mMagnetometerSet){
                SensorManager.getRotationMatrix(mInitialRotationMatrix, null, mLastAccelerometer, mLastMagnetometer);

                Arrays.fill(mCurrentRotationMatrix, 0);
                mCurrentRotationMatrix[0] = 1;
                mCurrentRotationMatrix[4] = 1;
                mCurrentRotationMatrix[8] = 1;

                mCurrentRotationMatrix = matrixMultiplication(mCurrentRotationMatrix, mInitialRotationMatrix);

                mSensorManager.unregisterListener(this, mMagnetSensor);
                mSensorManager.unregisterListener(this, mAccelerometerSensor);
            }
        }else if(event.sensor == mGyroSensor) {
            if (timestamp != 0 && mAccelerometerSet && mMagnetometerSet) {

                float dT = (event.timestamp - timestamp) * NS2S;
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                if (omegaMagnitude > OMEGA_EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;

                float[] deltaRotationMatrix = new float[9];
                SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

                mCurrentRotationMatrix = matrixMultiplication(mCurrentRotationMatrix, deltaRotationMatrix);

                float[] gyroscopeOrientation = new float[3];
                SensorManager.getOrientation(mCurrentRotationMatrix, gyroscopeOrientation);

                float azimuthAngleRad = gyroscopeOrientation[0];
                double azimuthAngleDegrees = (Math.toDegrees(azimuthAngleRad) + 360) % 360;
                azimuthAngleDegrees = Math.round(azimuthAngleDegrees);

//                Log.d(TAG, "Gyro Angle = " + azimuthAngleDegrees);

                gyroscopeDirection = getDirection(azimuthAngleDegrees);
//                Log.d(TAG, "Gyro Direction = " + gyroscopeDirection);

            }
            timestamp = event.timestamp;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private float[] matrixMultiplication(float[] a, float[] b){
        float[] result = new float[9];

        result[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
        result[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
        result[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];

        result[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
        result[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
        result[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];

        result[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
        result[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
        result[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];

        return result;
    }
}
