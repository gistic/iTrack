package com.navibees.sdk.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.R;
import com.navibees.sdk.activity.ActivitiesActivity;
import com.navibees.sdk.adapter.ActivitiesRecyclerAdapter;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.json.Activity;
import com.navibees.sdk.model.metadata.json.ActivityGroup;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hossam on 10/17/15.
 */
public class ActivitiesListFragment extends Fragment implements  SearchView.OnQueryTextListener {

    private SearchTask searchTask = null;
    private SearchView searchView = null;
    private TextView actionBarTitle = null;

    private ActivitiesRecyclerAdapter adapter = null;

    public static ActivitiesListFragment getInstance(boolean currentFilter){
        ActivitiesListFragment fragment = new ActivitiesListFragment();
        Bundle args = new Bundle();
        args.putBoolean("currentFilter", currentFilter);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.com_navibees_sdk_activities_list_fragment, container, false);
        try {
            AppManager.getInstance().getLicenseManager().verify(getContext(), NaviBeesFeature.Temporal_Based_Event_Activities_Notification);

            boolean currentFilter = getArguments().getBoolean("currentFilter");

            List<ActivityGroup> groups = AppManager.getInstance().getMetaDataManager().getActivityCategories(getActivity());
            if (groups == null)
                groups = new ArrayList<ActivityGroup>();

            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.activitiesRecyclerView);
            adapter = new ActivitiesRecyclerAdapter(groups, currentFilter, getActivity());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(adapter));

            if (!currentFilter) {
                handleSearchView();
            }

        } catch (NaviBeesLicenseNotAuthorithedException e) {
            e.printStackTrace();
        } catch (NaviBeesLicenseExpireException e) {
            e.printStackTrace();
        }


        return rootView;
    }

    private void handleSearchView() {
        searchView = ((ActivitiesActivity) getActivity()).getSearchView();
        actionBarTitle = ((ActivitiesActivity) getActivity()).getActionBarTitle();

        if (searchView != null) {

            searchView.setVisibility(View.VISIBLE);

            //Hide title when user press search icon then SearchView expand
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionBarTitle != null) {
                        actionBarTitle.setVisibility(View.GONE);
                    }
                }
            });

            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    if (actionBarTitle != null) {
                        actionBarTitle.setText((getActivity()).getTitle());
                        actionBarTitle.setVisibility(View.VISIBLE);
                    }

                    adapter.resetToOriginal();

                    return false;
                }
            });

            searchView.setOnQueryTextListener(this);
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {

        if(searchTask != null){
            searchTask.cancel(true);
        }

        if (TextUtils.isEmpty(newQuery)) {
            adapter.resetToOriginal();
        } else {
            adapter.clearFilteredData();
            searchTask = new SearchTask();
            searchTask.execute(newQuery);
        }
        return true;
    }


    private class SearchTask extends AsyncTask<String, String, List<Activity>>
    {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<Activity> doInBackground(String... params) {
            publishProgress(params[0]);
            return adapter.searchData(params[0]);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(List<Activity> searchResult) {
            adapter.setNewData(searchResult);
        }
    }

}
