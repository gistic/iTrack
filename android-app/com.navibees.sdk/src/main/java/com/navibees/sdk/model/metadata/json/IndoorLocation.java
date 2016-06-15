package com.navibees.sdk.model.metadata.json;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nabilnoaman on 4/15/15.
 */
//http://www.parcelabler.com
public class IndoorLocation implements Parcelable {

    private Double x;
    private Double y;
    private Integer floor;
    private IndoorLocationConfidence confidence;


    public IndoorLocation(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public IndoorLocation(Double x, Double y , int floor) {
        this.x = x;
        this.y = y;
        this.floor = floor;
    }

    /**
     *
     * @return
     * The x
     */
    public Double getX() {
        return x;
    }

    /**
     *
     * @param x
     * The x
     */
    public void setX(Double x) {
        this.x = x;
    }

    /**
     *
     * @return
     * The y
     */
    public Double getY() {
        return y;
    }

    /**
     *
     * @param y
     * The y
     */
    public void setY(Double y) {
        this.y = y;
    }

    /**
     *
     * @return
     * The floor
     */
    public Integer getFloor() {
        return floor;
    }

    /**
     *
     * @param floor
     * The floor
     */
    public void setFloor(Integer floor) {
        this.floor = floor;
    }



    public IndoorLocationConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(IndoorLocationConfidence confidence) {
        this.confidence = confidence;
    }



    protected IndoorLocation(Parcel in) {
        x = in.readByte() == 0x00 ? null : in.readDouble();
        y = in.readByte() == 0x00 ? null : in.readDouble();
        floor = in.readByte() == 0x00 ? null : in.readInt();
        confidence = (IndoorLocationConfidence) in.readValue(IndoorLocationConfidence.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (x == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(x);
        }
        if (y == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(y);
        }
        if (floor == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(floor);
        }
        dest.writeValue(confidence);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IndoorLocation> CREATOR = new Parcelable.Creator<IndoorLocation>() {
        @Override
        public IndoorLocation createFromParcel(Parcel in) {
            return new IndoorLocation(in);
        }

        @Override
        public IndoorLocation[] newArray(int size) {
            return new IndoorLocation[size];
        }
    };
}