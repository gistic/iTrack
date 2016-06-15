package com.navibees.sdk.model.metadata;

import android.os.Parcel;

import org.altbeacon.beacon.Beacon;

/**
 * Created by nabilnoaman on 9/8/15.
 */
public class NaviBeesBeacon extends Beacon {

    private static final String TAG = "NaviBeesBeacon";

    private byte batteryStatus;

    public static final Creator<NaviBeesBeacon> CREATOR = new Creator() {
        public NaviBeesBeacon createFromParcel(Parcel in) {
            return new NaviBeesBeacon(in);
        }

        public NaviBeesBeacon[] newArray(int size) {
            return new NaviBeesBeacon[size];
        }
    };

    protected NaviBeesBeacon(Beacon beacon) {
        this.mBluetoothAddress = beacon.getBluetoothAddress();
        this.mIdentifiers = beacon.getIdentifiers();
        this.mBeaconTypeCode = beacon.getBeaconTypeCode();
        this.mDataFields = beacon.getDataFields();
        this.mDistance = beacon.getDistance();
        this.mRssi = beacon.getRssi();
        this.mTxPower = beacon.getTxPower();
    }

    public NaviBeesBeacon() {
    }

    protected NaviBeesBeacon(Parcel in) {
        super(in);
        this.batteryStatus = in.readByte();
    }

    public byte getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(byte batteryStatus) {
        this.batteryStatus = batteryStatus;
    }



    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeByte(this.batteryStatus);
    }

}
