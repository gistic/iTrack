package com.navibees.sdk.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.NaviBeesApplication;
import com.navibees.sdk.R;
import com.navibees.sdk.model.metadata.MetaDataManager;
import com.navibees.sdk.model.metadata.json.POICategory;

import java.util.List;


public class POIsCategoriesFilterActivity extends AppCompatActivity implements AppManager.OnInitializedListener {

    private ListView listView;
    private POICategoryFilterListAdapter adapter;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private int resourceID = R.layout.com_navibees_sdk_pois_category_filter_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(getApplicationContext());
        setContentView(getContentViewRes());

        setupActionBar();

        AppManager.getInstance().initialize(this, this);

    }

    @Override
    public void onInitialized(boolean success) {
        if(success) {
            MetaDataManager metaDataManager = AppManager.getInstance().getMetaDataManager();
            List<POICategory> categoryList = metaDataManager.getPOIsCategories(this.getApplicationContext());

            adapter = new POICategoryFilterListAdapter(categoryList);

            listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(adapter);
        }
    }


    private int getContentViewRes(){
        return resourceID;
    }

    private void handleCategory(POICategory category) {

        //Update status of POICategory in SharedPreferances
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sp.edit();

        editor.putBoolean(category.getName(), category.isFilterEnable());
        editor.commit();

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


    protected int getActionBarLayoutRes() {
        return R.layout.com_navibees_sdk_pois_category_filter_activity_action_bar;
    }

    protected int getActionBarLayoutBackgroundRes() {
        return R.drawable.com_navibees_sdk_pois_category_filter_activity_action_bar_layout_bg;
    }

    protected void customiseActionBarTitle(TextView title) {
    }

    private class POICategoryFilterListAdapter extends BaseAdapter implements View.OnClickListener {


        private List<POICategory> categoriesList;

        public POICategoryFilterListAdapter(List<POICategory> categoriesList) {
            this.categoriesList = categoriesList;
        }

        @Override
        public int getCount() {
            if(categoriesList != null) {
                return categoriesList.size();
            }else {
                return 0;
            }
        }

        @Override
        public Object getItem(int i) {
            return categoriesList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        /**
         * Overrides the BaseAdapter getView function to be able to customize list view
         * item row as needed and add the required action listeners
         *
         * @param position Check BaseAdapter class documentation
         * @param view     Check BaseAdapter class documentation
         * @param parent   Check BaseAdapter class documentation
         * @return The view of the item row in the list view
         */
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            if (view == null) {
                view = ((LayoutInflater) (POIsCategoriesFilterActivity.this.getApplicationContext()).getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.com_navibees_sdk_pois_category_filter_activity_list_item, null);
            }

            //view.setId(R.id.list_item);

            TextView categoryName = (TextView) view.findViewById(R.id.poi_category_name);

            categoryName.setText(categoriesList.get(position).getNameWRTLang());

            // Setting the click action listener for toggle button
            Switch toggleButton = (Switch) view.findViewById(R.id.toggleButton);
            toggleButton.setTag(position);
            toggleButton.setOnClickListener(this);
            toggleButton.setChecked(categoriesList.get(position).isFilterEnable());

            String iconName, iconPath;

            if (categoriesList.get(position).isFilterEnable()) {
                iconName = categoriesList.get(position).getIcons().getDark();
                //view.setBackgroundResource(Color.argb(0xff , 0xee , 0xee , 0xee));
                categoryName.setTextColor(Color.BLACK);
            } else {
                iconName = categoriesList.get(position).getIcons().getLight();
                //view.setBackgroundResource(Color.argb(0xff , 0xee , 0xee , 0xee));
                categoryName.setTextColor(Color.LTGRAY);
            }

            //http://frescolib.org/docs/supported-uris.html
            iconPath = "file://" + ApplicationConstants.mapResourcesImagesPath + "/" + iconName + "@2x.png";
            SimpleDraweeView categoryImage = (SimpleDraweeView) view.findViewById(R.id.poi_category_image);
            Uri uri = Uri.parse(iconPath);
            categoryImage.setImageURI(uri);


            return view;
        }

        /**
         * This function responsible for handling clicks on toggle button
         *
         * @param view The toggle button that was clicked in the list view
         */
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.toggleButton) {
                POICategory category = categoriesList.get(Integer.parseInt(view.getTag().toString()));

                boolean on = ((Switch) view).isChecked();

                categoriesList.get(Integer.parseInt(view.getTag().toString())).setFilterEnable(on);

                this.notifyDataSetChanged();


                handleCategory(category);


            }
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

}
