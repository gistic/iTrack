package com.navibees.sampleapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.navibees.sdk.activity.RouteToActivity;

public class RouteToSubActivity extends RouteToActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getActionBarLayoutBackgroundRes() {
        return R.drawable.route_to_sub_activity_action_bar_layout_bg;
    }

    protected void customiseActionBarTitle(TextView title){
        title.setTextColor(Color.BLUE);
    }

}
