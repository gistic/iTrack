package com.navibees.sampleapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;


//https://github.com/codepath/android_guides/wiki/Settings-with-PreferenceFragment
public class SettingFragment extends PreferenceFragment {

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
