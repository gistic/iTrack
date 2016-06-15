package com.navibees.sdk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.R;
import com.navibees.sdk.activity.ActivitiesActivity;
import com.navibees.sdk.adapter.ActivitiesPagerAdapter;

/**
 * Created by hossam on 10/19/15.
 */
public class ActivitiesPagerFragment extends Fragment implements ViewPager.OnPageChangeListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.com_navibees_sdk_activities_pager_fragment, container, false);

//        setupToolBar(rootView);

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.activitiesViewPager);
        viewPager.setAdapter(new ActivitiesPagerAdapter(getActivity(), getChildFragmentManager()));

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.activitiesTabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(this);
        return rootView;

    }


    private void setupToolBar(View rootView){
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.activitiesToolBar);
        toolbar.setTitle(getActivity().getTitle());
    }

    //Show SearchView in All hide in Current Fragment
    @Override
    public void onPageSelected(int position) {
        SearchView searchView = ((ActivitiesActivity) getActivity()).getSearchView();
        TextView actionBarTitle = ((ActivitiesActivity) getActivity()).getActionBarTitle();
        if(searchView != null && (actionBarTitle != null)) {
            if(position == ApplicationConstants.ACTIVITIES_FRAGMENT_ALL_INDEX) {
                searchView.setVisibility(View.VISIBLE);
                if(!searchView.isIconified()){
                    actionBarTitle.setVisibility(View.GONE);
                }
            }
            else if (position == ApplicationConstants.ACTIVITIES_FRAGMENT_CURRENT_INDEX) {
                searchView.setVisibility(View.GONE);
                actionBarTitle.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
