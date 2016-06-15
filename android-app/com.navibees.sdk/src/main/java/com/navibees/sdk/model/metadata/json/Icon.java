package com.navibees.sdk.model.metadata.json;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nabilnoaman on 4/14/15.
 */
public class Icon implements Parcelable {

    @Expose
    private String dark;
    @Expose
    private String light;
    @SerializedName("map_active")
    @Expose
    private String mapActive;
    @SerializedName("map_inactive")
    @Expose
    private String mapInactive;

    /**
     *
     * @return
     * The dark
     */
    public String getDark() {
        return dark;
    }

    /**
     *
     * @param dark
     * The dark
     */
    public void setDark(String dark) {
        this.dark = dark;
    }

    /**
     *
     * @return
     * The light
     */
    public String getLight() {
        return light;
    }

    /**
     *
     * @param light
     * The light
     */
    public void setLight(String light) {
        this.light = light;
    }

    /**
     *
     * @return
     * The mapActive
     */
    public String getMapActive() {
        return mapActive;
    }

    /**
     *
     * @param mapActive
     * The map_active
     */
    public void setMapActive(String mapActive) {
        this.mapActive = mapActive;
    }

    /**
     *
     * @return
     * The mapInactive
     */
    public String getMapInactive() {
        return mapInactive;
    }

    /**
     *
     * @param mapInactive
     * The map_inactive
     */
    public void setMapInactive(String mapInactive) {
        this.mapInactive = mapInactive;
    }


    protected Icon(Parcel in) {
        dark = in.readString();
        light = in.readString();
        mapActive = in.readString();
        mapInactive = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dark);
        dest.writeString(light);
        dest.writeString(mapActive);
        dest.writeString(mapInactive);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Icon> CREATOR = new Parcelable.Creator<Icon>() {
        @Override
        public Icon createFromParcel(Parcel in) {
            return new Icon(in);
        }

        @Override
        public Icon[] newArray(int size) {
            return new Icon[size];
        }
    };
}