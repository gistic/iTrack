package com.navibees.sampleapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.navibees.sdk.activity.ActivitiesActivity;

public class AgendaSubActivity extends ActivitiesActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void customiseActivityInfoIcon(ImageView activityInfoImageView) {
        activityInfoImageView.setVisibility(View.VISIBLE);
    }
}
