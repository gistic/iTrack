package com.navibees.sdk.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.R;
import com.navibees.sdk.fragment.ActivitiesListFragment;

/**
 * Created by hossam on 10/17/15.
 */
public class ActivitiesPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    public ActivitiesPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case ApplicationConstants.ACTIVITIES_FRAGMENT_ALL_INDEX:
                return ActivitiesListFragment.getInstance(false);
            case ApplicationConstants.ACTIVITIES_FRAGMENT_CURRENT_INDEX:
                return ActivitiesListFragment.getInstance(true);
            default:
                return ActivitiesListFragment.getInstance(false);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case ApplicationConstants.ACTIVITIES_FRAGMENT_ALL_INDEX:
                return mContext.getString(R.string.all_tab_title);
            case ApplicationConstants.ACTIVITIES_FRAGMENT_CURRENT_INDEX:
                return mContext.getString(R.string.current_tab_title);
        }
        return "";
    }
}
