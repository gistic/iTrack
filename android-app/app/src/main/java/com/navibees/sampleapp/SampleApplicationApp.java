package com.navibees.sampleapp;

import android.util.Log;
import android.widget.TextView;

import com.navibees.sdk.NaviBeesApplication;
import com.crashlytics.android.Crashlytics;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.parse.Parse;
import com.parse.ParseInstallation;

import java.util.ArrayList;
import java.util.HashSet;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nabilnoaman on 11/9/15.
 */
public class SampleApplicationApp extends NaviBeesApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        String parseAppId = "pey8pjhhotyDIusYHgSEF6SUI84ugMPVe6rqF2Ah";
        String parseClientKey = "ZD6C8F4bhChdi0FiotmdbWjNDIvmlL5hbtVfHvsx";
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, parseAppId, parseClientKey);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }


    @Override
    protected String getClientID(){
        return "9l3EM98oLnraKNLc";
    }

    @Override
    protected String getLicenseCode() {
        return "runtimestandard,101,rud420639096,03-feb-2016,A3C63PJS3HL7HBRSX019"  ;
    }

    HashSet<String> regions = new HashSet<String>();
    TextView textView;
    IndoorLocation location;
    String experimentName = null;

}
