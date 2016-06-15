package com.navibees.sdk.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.NaviBeesApplication;
import com.navibees.sdk.R;
import com.navibees.sdk.adapter.POIExpandableListAdapter;
import com.navibees.sdk.model.metadata.MetaDataManager;
import com.navibees.sdk.model.metadata.json.Facility;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.util.Log;
import com.navibees.sdk.util.NaviBeesAlphanumComparator;
import com.navibees.sdk.util.Toast;

import java.util.List;
import java.util.Map;


public class RouteToActivity extends AppCompatActivity implements AppManager.OnInitializedListener{


    private ExpandableListView listView;
    private POIExpandableListAdapter adapter;
    private int resourceID = R.layout.com_navibees_sdk_route_to_activity;
    private int previousExpandedGroup = -1;
    private boolean isComingFromSearch = false;

    static final String TAG = "RouteToActivity";

    private SearchView searchView = null;
    private SearchTask searchTask = null;
    private ProgressDialog progressDialog = null;

    private NaviBeesAlphanumComparator.ComparatorType categorySortType = NaviBeesAlphanumComparator.ComparatorType.BY_ID;
    private NaviBeesAlphanumComparator.ComparatorType poiSortType = NaviBeesAlphanumComparator.ComparatorType.BY_NAME ;
    private NaviBeesAlphanumComparator.ComparatorType facilitySortType = NaviBeesAlphanumComparator.ComparatorType.BY_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(getApplicationContext());
        setContentView(getContentViewRes());

        progressDialog = new ProgressDialog(this);

        setupActionBar();

        getSortType(getIntent());

        AppManager.getInstance().initialize(this, this);
    }

    private void getSortType(Intent intent) {
        if(intent != null){
            if(intent.getSerializableExtra(ApplicationConstants.ROUTE_TO_CATEGORY_SORT_TYPE_KEY) != null) {
                categorySortType = (NaviBeesAlphanumComparator.ComparatorType) intent.getSerializableExtra(ApplicationConstants.ROUTE_TO_CATEGORY_SORT_TYPE_KEY);
            }

            if(intent.getSerializableExtra(ApplicationConstants.ROUTE_TO_POI_SORT_TYPE_KEY) != null) {
                poiSortType = (NaviBeesAlphanumComparator.ComparatorType) intent.getSerializableExtra(ApplicationConstants.ROUTE_TO_POI_SORT_TYPE_KEY);
            }

            if(intent.getSerializableExtra(ApplicationConstants.ROUTE_TO_FACILITY_SORT_TYPE_KEY) != null) {
                facilitySortType = (NaviBeesAlphanumComparator.ComparatorType) intent.getSerializableExtra(ApplicationConstants.ROUTE_TO_FACILITY_SORT_TYPE_KEY);
            }
        }
    }

    @Override
    public void onInitialized(boolean success) {
        if(success){
            MetaDataManager metaDataManager = AppManager.getInstance().getMetaDataManager();

            adapter = new POIExpandableListAdapter(getApplicationContext(), metaDataManager.getPOIsCategories(this.getApplicationContext()) , categorySortType , metaDataManager.getPOIs(getApplicationContext()) , poiSortType , metaDataManager.getFacilities(getApplicationContext()) , facilitySortType);

            listView = (ExpandableListView) findViewById(R.id.list);
            listView.setAdapter(adapter);
            listView.setGroupIndicator(null);

            // Listview Group expanded listener
            //collapse previous expanded group
            listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {

                    if (!isComingFromSearch && previousExpandedGroup != -1) {
                        listView.collapseGroup(previousExpandedGroup);
                    }

                    previousExpandedGroup = groupPosition;

                }
            });

            listView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {
                    previousExpandedGroup = -1;
                }
            });


            // Listview on child click listener
            listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {

                    //Return Selected POI/Facility to Main Activity to be displayed on map to show direction to it or Nearst one if
                    // It is facility.
                    Object selectedPOIOrFacility = adapter.getChild(groupPosition, childPosition);

                    Intent returnIntent = new Intent();
                    if(selectedPOIOrFacility instanceof POI) {
                        returnIntent.putExtra(ApplicationConstants.SELECTED_POI_OR_FACILITY_KEY, (POI)selectedPOIOrFacility);
                    }else {
                        if(selectedPOIOrFacility instanceof Facility){
                            returnIntent.putExtra(ApplicationConstants.SELECTED_POI_OR_FACILITY_KEY, (Facility)selectedPOIOrFacility);
                        }
                    }
                    setResult(RESULT_OK, returnIntent);
                    finish();

                    return true;
                }
            });
        }else{
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT);
        }
    }

    private int getContentViewRes(){
        return resourceID;
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


                final TextView title = (TextView) customView.findViewById(R.id.action_bar_title);
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

                searchView = (SearchView) customView.findViewById(R.id.searchView);
                if(searchView != null){
                    //Hide title when user press search icon then SearchView expand
                    searchView.setOnSearchClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (title != null) {
                                title.setVisibility(View.GONE);
                            }
                        }});


                    searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                        @Override
                        public boolean onClose() {
                            if (title != null) {
                                title.setText(getTitle());
                                title.setVisibility(View.VISIBLE);
                            }
                            isComingFromSearch = false;
                            adapter.resetToOriginal();
                            collapseAllGroups();
                            previousExpandedGroup = -1;
                            return false;
                        }
                    });


                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            Log.i( TAG ,"onQueryTextSubmit:query"+ query);
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newQuery) {
                            Log.i( TAG ,"onQueryTextChange:newQuery"+ newQuery);

                            if(searchTask != null){
                                searchTask.cancel(true);
                                progressDialog.dismiss();
                            }

                            if (TextUtils.isEmpty(newQuery)) {
                                adapter.resetToOriginal();
                                isComingFromSearch = false;
                                collapseAllGroups();
                            } else {
                                adapter.clearFilteredData();
                                searchTask = new SearchTask();
                                searchTask.execute(newQuery);
                            }

                            return true;
                        }
                    });

                }

            }
        }catch (Resources.NotFoundException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((NaviBeesApplication) getApplication()).setAppInForeground(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((NaviBeesApplication) getApplication()).setAppInForeground(false);

    }


    protected int getActionBarLayoutRes() {
        return R.layout.com_navibees_sdk_route_to_activity_action_bar;
    }

    protected int getActionBarLayoutBackgroundRes() {
        return R.drawable.com_navibees_sdk_route_to_activity_action_bar_layout_bg;
    }

    protected void customiseActionBarTitle(TextView title) {
    }

    private void expandAllGroups(){
        int numOfGroups = adapter.getGroupCount();
        for(int i= numOfGroups-1 ; i >= 0 ; i--) {
            listView.expandGroup(i , true);
        }
    }


    private void collapseAllGroups(){
        int numOfGroups = adapter.getGroupCount();
        for(int i=0 ; i < numOfGroups ; i++) {
            listView.collapseGroup(i);
        }
    }


    private class SearchTask extends AsyncTask<String, String, Map<Integer , List<Object>>>
    {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Searching...");
            progressDialog.show();
        }

        @Override
        protected Map<Integer , List<Object>> doInBackground(String... params) {
            publishProgress(params[0]);
            return adapter.filterData(params[0]);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage("Searching for " +values[0]);
        }

        @Override
        protected void onPostExecute(Map<Integer , List<Object>> searchResult) {
            adapter.setNewData(searchResult);
            progressDialog.dismiss();
            //Expand All Groups after search
            isComingFromSearch = true;
            expandAllGroups();
        }
    }

}