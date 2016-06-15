package com.navibees.sdk.model.postioning;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;

import com.navibees.sdk.model.metadata.NaviBeesBeacon;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;

/**
 * Created by nabilnoaman on 9/8/15.
 */
public class NaviBeesBeaconParser extends BeaconParser {

    public static final String TAG = "NaviBeesBeaconParser";

    /**
     * Constructs an NaviBeesBeacon Parser and sets its layout
     */
    public NaviBeesBeaconParser() {
        super();
        //iBeacon layout
        //http://stackoverflow.com/questions/25027983/is-this-the-correct-layout-to-detect-ibeacons-with-altbeacons-android-beacon-li?rq=1
        /*
        4C00 02 15 585CDE931B0142CC9A1325009BEDC65E 0000 0000 C5

        <company identifier (2 bytes)> <type (1 byte)> <data length (1 byte)> <uuid (16 bytes)> <major (2 bytes)> <minor (2 bytes)> <RSSI @ 1m>

        Apple Company Identifier (Little Endian), 0x004c
        data type, 0x02 => iBeacon
        data length, 0x15 = 21
        uuid: 585CDE931B0142CC9A1325009BEDC65E
        major: 0000
        minor: 0000
        measured power at 1 meter: 0xc5 = -59
        */
        this.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
    }
    /**
     * Construct an NaviBeesBeacon from a Bluetooth LE packet collected by Android's Bluetooth APIs,
     * including the raw Bluetooth device info
     *
     * @param scanData The actual packet bytes
     * @param rssi The measured signal strength of the packet
     * @param device The Bluetooth device that was detected
     * @return An instance of an <code>Beacon</code>
     */
    @TargetApi(5)
    @Override
    public Beacon fromScanData(byte[] scanData, int rssi, BluetoothDevice device) {
        Beacon naviBeesBeacon = fromScanData(scanData, rssi, device, new NaviBeesBeacon());

        //     BluetoothAdvertisementPayload =  manufacturer advertisements + Apple specific iBeacon information
        String bluetoothAdvertisementPayload = bytesToHex(scanData);
        //Log.d(TAG ,"bluetoothAdvertisementPayload:"+bluetoothAdvertisementPayload );
        byte batteryStatus  = 0;
        if(bluetoothAdvertisementPayload.indexOf("ff4c0002") != -1 ){
            //this packet is iBeacon

            String manufacturerAdvertisements = bluetoothAdvertisementPayload.substring(0 ,bluetoothAdvertisementPayload.indexOf("ff4c0002"));
            //Log.d(TAG ,"manufacturerAdvertisements:"+manufacturerAdvertisements );
            //for CoreBlu for example will be : 03 08 D1(Battery Status) 42 1A
            if(manufacturerAdvertisements.length() == 10){
                batteryStatus = scanData[2];
                /*
                * ADV[2] = D1
                  ADV[2] = 209 in decimal

                  formula :
                  Voltage = (ADV[2]x 3.6) / 255
                  Voltage = (209 x 3.6) / 255  =  2.95v

                  Battery Voltage in %
                  3.2v = 100%
                  1.8v = 0%
                  */
            }

        }

        if(naviBeesBeacon != null) {
            ((NaviBeesBeacon) naviBeesBeacon).setBatteryStatus(batteryStatus);
        }

        return naviBeesBeacon;
    }




}
