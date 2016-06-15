package com.navibees.sampleapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.widget.Toast;

import com.navibees.sampleapp.com.navibees.sampleapp.tracking.Service_class;
import com.navibees.sdk.activity.MapActivity;


public class MapActivitySubClass extends MapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MapActivitySubClass.this, Service_class.class);
        PendingIntent pintent = PendingIntent.getService(MapActivitySubClass.this, 0, intent,
                0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 30000,
                 pintent);

        //super.getMyLocationImageView().setImageResource(R.drawable.com_uqu_navibees_sdk_wirless_stars);
        //super.getRouteToImageView().setBackgroundResource(R.drawable.rounded_corners_background);
    }


    /*
    @Override
    protected Class<RouteToSubActivity> getRouteToActivity() {
        return RouteToSubActivity.class;
    }
    */

    @Override
    protected Class<AgendaSubActivity> getActivitiesActivity() {
        return AgendaSubActivity.class;
    }


    //-------------------Current Location On/Off Resources ----------------
    /*
    @Override
    protected int getCurrentLocationOnDrawableRes() {
        return R.mipmap.ic_launcher;
    }
    @Override
    protected int getCurrentLocationOffDrawableRes() {
        return R.drawable.com_uqu_navibees_sdk_gistic;
    }
    */
    //---------------------------------------------------------------------


    //------------------- Listeners ---------------------------------------
/*
    @Override
    protected void myLocationOnClickListener() {
        super.myLocationOnClickListener();
        Toast.makeText(getApplicationContext() , "myLocationOnClickListener" , Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void directionOnClickListener() {
        super.directionOnClickListener();

        Toast.makeText(getApplicationContext() , "directionOnClickListener" , Toast.LENGTH_SHORT).show();
    }

*/
/*
    //-------------- Menu Under Floors Action -----------------
    @Override
    protected void floorsActionItemOnClickListener() {
        super.floorsActionItemOnClickListener();
        Toast.makeText(getApplicationContext() , "floorsActionItemOnClickListener" , Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void floorsOnMenuItemClick(MenuItem item) {
        super.floorsOnMenuItemClick(item);
        Toast.makeText(getApplicationContext() , "floorsOnMenuItemClick" , Toast.LENGTH_SHORT).show();
    }
    //------------------------------------------------------
*/

    //-------------- Menu Under More Action -----------------
  /*
    @Override
    protected void moreActionItemOnClickListener() {
        super.moreActionItemOnClickListener();
        Toast.makeText(getApplicationContext() , "moreActionItemOnClickListener" , Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void mapFilterMenuItemOnClickListener() {
        super.mapFilterMenuItemOnClickListener();
        Toast.makeText(getApplicationContext() , "mapFilterMenuItemOnClickListener" , Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void activitiesMenuItemOnClickListener(){
        super.activitiesMenuItemOnClickListener();
        Toast.makeText(getApplicationContext() , "activitiesMenuItemOnClickListener" , Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void aboutMenuItemOnClickListener() {
        super.aboutMenuItemOnClickListener();
        Toast.makeText(getApplicationContext() , "aboutMenuItemOnClickListener" , Toast.LENGTH_SHORT).show();
    }

*/
    @Override
    protected void moreActionExtraMenuItemOnClickListener(MenuItem item) {
        Toast.makeText(getApplicationContext(), "moreActionExtraMenuItemOnClickListener:title:" + item.getTitle(), Toast.LENGTH_SHORT).show();

        if (item.getItemId() == R.id.third_party_extra1_settings) {
            Intent i = new Intent(MapActivitySubClass.this, SettingActivity.class);
            startActivity(i);
        }
    }

    //====================================== ActionBar =================================


    /*
    protected int getActionBarLayoutRes() {
        return -1;
    }
*/



    @Override
    protected int getMoreActionsMenuRes() {
        return R.menu.custom_map_activity_menu;
    }


    /*
    @Override
    protected void customizeActionBarTitle(TextView title) {

        title.setTextColor(Color.RED);
        title.setTextSize(10);

    }
   */

    /*
    @Override
    protected int getActionBarLayoutBackgroundRes() {
        return R.drawable.action_bar_layout_bg;
    }
    */



    /*
    @Override
    protected void customizeActionBarMoreActionsView(View moreActions) {

        moreActions.setBackgroundResource(R.drawable.action_bar_more_actions_bg);
        ((ImageView)moreActions).setImageResource(R.mipmap.ic_launcher);

    }
    */

    /*
    @Override
    protected void customizeActionBarFloorsSelectionIcon(ImageView floorsSelectionIcon) {
        floorsSelectionIcon.setBackgroundColor(Color.LTGRAY);
        floorsSelectionIcon.setImageResource(R.drawable.com_uqu_navibees_sdk_floor_btn);
    }
    */

    /*
    @Override
    protected void customizeActionBarFloorName(TextView floorNameTV) {
        floorNameTV.setBackgroundColor(Color.BLUE);
        floorNameTV.setTextColor(Color.RED);
        floorNameTV.setTextSize(20);
       }
       */


    //Callout
 /*   @Override
    protected int getCalloutLayoutBackgroundRes() {
        return com.uqu.navibees.sdk.R.drawable.com_uqu_navibees_sdk_map_activity_callout_layout_bg;
    }
*/

    /*
    @Override
    protected void customizeCalloutPOIName(TextView poiName) {
        //poiName.setTextColor(Color.YELLOW);
    }
*/
    /*
    @Override
    protected void customizeCalloutLeftIcon(ImageView leftIcon) {
    }
    */



/*
    @Override
    protected void customizeCalloutRightIcon(ImageView rightIcon) {
    }
*/

    /*
    @Override
    protected void calloutOnClickListener() {
        //Toast.makeText(getApplicationContext() , "calloutOnClickListener" , Toast.LENGTH_SHORT).show();
    }
*/
    //-------------------------
    //CurrentLocation Pin & Confidence hover

    /*
    protected int getCurrentLocationPinRes() {
        //return com.uqu.navibees.sdk.R.drawable.com_uqu_navibees_sdk_current_location_pin;
        return -1;
    }


    protected int getLowConfidenceRes() {
        //return com.uqu.navibees.sdk.R.drawable.com_uqu_navibees_sdk_current_location_low_confidence;
        return -1;
    }

    protected int getMediumConfidenceRes() {
        //return com.uqu.navibees.sdk.R.drawable.com_uqu_navibees_sdk_current_location_medium_confidence;
        return -1;
    }
*/

}
