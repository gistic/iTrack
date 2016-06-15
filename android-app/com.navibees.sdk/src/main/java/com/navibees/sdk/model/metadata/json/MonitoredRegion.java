package com.navibees.sdk.model.metadata.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.navibees.sdk.util.CommonUtils;

/**
 * Created by nabilnoaman on 7/6/15.
 */
public class MonitoredRegion implements Parcelable {

    /*
    {
        "identifier": "com.navibees.monitoring.regionName",
            "type": "foreground" / "background" / "all",
            "message": "Region # 1",
            "messageAr": "منطقة رقم ١",
            "UUID": "D3ACCFE2-E95D-433C-BCF4-643BECC5D217",
            "major": 3,
            "minor": 20,
            "interval": 1
    }

    */

    private String identifier;
    private String type;
    private String message;
    private String messageAr;
    private String UUID;
    private int major;
    private int minor;
    private int interval;
    //This field will not be in json file but we will use it to handle this NAV-185
    private int timeSinceLastFireInSeconds;


    public final static String[] MONITORED_REGIONS_TYPE = {"foreground" , "background" , "all"};

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getMessageAr() {
        return messageAr;
    }

    public void setMessageAr(String messageAr) {
        this.messageAr = messageAr;
    }


    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }


    public int getTimeSinceLastFireInSeconds() {
        return timeSinceLastFireInSeconds;
    }

    public void setTimeSinceLastFireInSeconds(int timeSinceLastFireInSeconds) {
        this.timeSinceLastFireInSeconds = timeSinceLastFireInSeconds;
    }

    protected MonitoredRegion(Parcel in) {
        identifier = in.readString();
        type = in.readString();
        message = in.readString();
        messageAr = in.readString();
        UUID = in.readString();
        major = in.readInt();
        minor = in.readInt();
        interval = in.readInt();
        timeSinceLastFireInSeconds = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(type);
        dest.writeString(message);
        dest.writeString(messageAr);
        dest.writeString(UUID);
        dest.writeInt(major);
        dest.writeInt(minor);
        dest.writeInt(interval);
        dest.writeInt(timeSinceLastFireInSeconds);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MonitoredRegion> CREATOR = new Parcelable.Creator<MonitoredRegion>() {
        @Override
        public MonitoredRegion createFromParcel(Parcel in) {
            return new MonitoredRegion(in);
        }

        @Override
        public MonitoredRegion[] newArray(int size) {
            return new MonitoredRegion[size];
        }
    };

    public boolean isValid() {

        //MonitoredRegion is valid if it has valid identifier & valid UUID
        if( (identifier != null) && (UUID != null)) {
            if( (identifier.length() > 0) && (UUID.length() == 36)){
                return true;
            }

        }

        return false;
    }

    public String getMessageWRTLang(){
        if(CommonUtils.isArabicLang())
            return messageAr;

        return message;
    }


}