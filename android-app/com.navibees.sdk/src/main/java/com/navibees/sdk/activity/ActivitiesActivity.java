package com.navibees.sdk.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.NaviBeesApplication;
import com.navibees.sdk.R;
import com.navibees.sdk.fragment.ActivitiesPagerFragment;
import com.navibees.sdk.fragment.ActivityDescriptionFragment;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.json.Activity;

public class ActivitiesActivity extends AppCompatActivity implements AppManager.OnInitializedListener {

    private SearchView searchView = null;
    private TextView actionBarTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_navibees_sdk_activities_activity);

        setupActionBar();

//        getSupportActionBar().hide();
//        setupToolBar();

        AppManager.getInstance().initialize(this, this);
    }

    private void setupToolBar(){
        getSupportActionBar().hide();
        Toolbar toolbar = (Toolbar) findViewById(R.id.activitiesToolBar);
        toolbar.setTitle(getTitle());
    }


    protected int getActionBarLayoutRes(){
        return R.layout.com_navibees_sdk_activities_activity_action_bar;
    }

    protected int getActionBarLayoutBackgroundRes(){
        return R.drawable.com_navibees_sdk_pois_category_filter_activity_action_bar_layout_bg;
    }

    protected void customiseActionBarTitle(TextView title) {
    }

    @Override
    final public void onInitialized(boolean success) {
        if(success) {
//            ViewPager viewPager = (ViewPager) findViewById(R.id.activitiesViewPager);
//            viewPager.setAdapter(new ActivitiesPagerAdapter(this, getSupportFragmentManager()));
//
//            TabLayout tabLayout = (TabLayout) findViewById(R.id.activitiesTabs);
//            tabLayout.setupWithViewPager(viewPager);

            try {
                AppManager.getInstance().getLicenseManager().verify(ActivitiesActivity.this , NaviBeesFeature.Temporal_Based_Event_Activities_Notification);
                openActivitiesPagerFragment();
            } catch (NaviBeesLicenseNotAuthorithedException e) {
                e.printStackTrace();
            } catch (NaviBeesLicenseExpireException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupActionBar() {

        //User may return invalid res id in getActionBarLayoutRes
        try {
            if(getActionBarLayoutRes() != -1)// incase -1 i.e there is no custom action bar
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


                actionBarTitle = (TextView) customView.findViewById(R.id.action_bar_title);
                if (actionBarTitle != null) {
                    actionBarTitle.setText(getTitle());
                    customiseActionBarTitle(actionBarTitle);
                }
                customView.findViewById(R.id.up_navigation).setBackgroundResource(getActionBarLayoutBackgroundRes());

                //Handle Up Navigation
                customView.findViewById(R.id.up_navigation).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

                searchView = (SearchView) customView.findViewById(R.id.searchView);

            }
        }catch (Resources.NotFoundException e){
            e.printStackTrace();
        }
    }

    public void openActivityDescriptionFragment(Activity activity){
        ActivityDescriptionFragment fragment = ActivityDescriptionFragment.getInstance(activity);
        openFragment(fragment, true);
    }

    public void openActivitiesPagerFragment(){
        ActivitiesPagerFragment fragment = new ActivitiesPagerFragment();
        openFragment(fragment, false);
    }

    public void openFragment(Fragment fragment, boolean addToBackStack){
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.com_uqu_navibees_activities_activity_fragment_container, fragment);
        if(addToBackStack)
            trans.addToBackStack(null);
        trans.commitAllowingStateLoss();
    }

    //Default visibility of info icon is GONE , if you want to show it
    //You have to override this methods (example AgendaSubActivity in App module)
    public void customiseActivityInfoIcon(ImageView activityInfoImageView) {
    }

    public SearchView getSearchView(){
        return searchView;
    }

    public TextView getActionBarTitle() {
        return actionBarTitle;
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
}
