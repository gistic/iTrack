package com.navibees.sdk.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.navibees.sdk.NaviBeesApplication;
import com.navibees.sdk.R;


public class AboutActivity extends AppCompatActivity {

    private int resourceID = R.layout.com_navibees_sdk_about_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewRes());
        setupActionBar();
    }

    protected int getContentViewRes(){
        return resourceID;
    }

    private void setupActionBar() {

        //User may return invalid res id in getActionBarLayoutRes
        try {
            if(getActionBarLayoutRes() != -1)// in case -1 i.e there is no custom action bar
            {
                final View customView = LayoutInflater.from(this).inflate( getActionBarLayoutRes() , null);


                final ActionBar actionBar = getSupportActionBar();
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
                ActionBar.LayoutParams params = new
                        ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);

                actionBar.setCustomView(customView, params);
                //Remove the thin strip on the left which appear in Android L
                Toolbar parent = (Toolbar) customView.getParent();
                parent.setContentInsetsAbsolute(0, 0);

                customView.setBackgroundResource(getActionBarLayoutBackgroundRes());


                TextView title = (TextView) customView.findViewById(R.id.action_bar_title);
                if (title != null) {
                    title.setText(getTitle());
                    customiseActionBarTitle(title);
                }



                customView.findViewById(R.id.up_navigation).setBackgroundResource(getActionBarLayoutBackgroundRes());

                //Handle Up Navigation
                customView.findViewById(R.id.up_navigation).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });



            }
        }catch (Resources.NotFoundException e){
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        ((NaviBeesApplication)getApplication()).setAppInForeground(true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        ((NaviBeesApplication) getApplication()).setAppInForeground(false);

    }



    protected int getActionBarLayoutRes() {
        return R.layout.com_navibees_sdk_about_activity_action_bar;
    }

    protected int getActionBarLayoutBackgroundRes() {
        return R.drawable.com_navibees_sdk_about_activity_action_bar_layout_bg;
    }

    protected void customiseActionBarTitle(TextView title) {
    }


}
