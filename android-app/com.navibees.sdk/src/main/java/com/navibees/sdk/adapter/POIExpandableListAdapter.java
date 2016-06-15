package com.navibees.sdk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.R;
import com.navibees.sdk.model.metadata.json.Facility;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.model.metadata.json.POICategory;
import com.navibees.sdk.util.CommonUtils;
import com.navibees.sdk.util.Log;
import com.navibees.sdk.util.NaviBeesAlphanumComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nabilnoaman on 4/15/15.
 */
public class POIExpandableListAdapter extends BaseExpandableListAdapter {

    private List<POICategory> originalCategories;//Original Groups

    private Map<Integer , List<Object>> originalPOIsOrFacilitiesPerCategory ;//Children may be POI or Facility contains group of POIs with the same category
    private Map<Integer , List<Object>> filteredPOIsOrFacilitiesPerCategory ;//Children after search
    private List<Facility> facilities;
    private Context context;

    final static int[] POI_CATEGORIES_MAP_ACTIVE = null;
    final static int[] POI_CATEGORIES_MAP_INACTIVE =null;

    static final String TAG = "POIExpandableListAdapter";

    private String query  = "";

    public POIExpandableListAdapter(Context context , List<POICategory> categories , NaviBeesAlphanumComparator.ComparatorType categorySortType  ,List<POI> pois , NaviBeesAlphanumComparator.ComparatorType poiSortType , List<Facility> facilities , NaviBeesAlphanumComparator.ComparatorType facilitySortType){
        this.context = context;
        this.originalCategories = categories;
        this.facilities = facilities;

        //We changed HashMap to LinkedHashMap to keep insertion order
        originalPOIsOrFacilitiesPerCategory = new LinkedHashMap<Integer, List<Object>>();
        filteredPOIsOrFacilitiesPerCategory = new LinkedHashMap<Integer, List<Object>>();

        if(categories != null) {
            Collections.sort(categories , new NaviBeesAlphanumComparator(categorySortType) );
            for (int i = 0; i < categories.size(); i++) {
                originalPOIsOrFacilitiesPerCategory.put(categories.get(i).getId(), new ArrayList<Object>());
            }
        }


        if(facilities != null) {
            Collections.sort(facilities , new NaviBeesAlphanumComparator(facilitySortType) );
            //Grouping Facilites by Category
            for (Facility facility : facilities) {
                List<Object> categoryChilds = originalPOIsOrFacilitiesPerCategory.get(facility.getCategoryId());
                if (categoryChilds != null) {
                    categoryChilds.add(facility);
                } else {
                    Log.e(TAG, "facility.getCategoryId():" + facility.getCategoryId() + " , is Not EXIST in Categories");
                }
            }
        }

        //Grouping POIs by Category
        if(pois != null) {
            Collections.sort(pois , new NaviBeesAlphanumComparator(poiSortType) );
            for (POI poi : pois) {

                List<Object> categoryChilds = originalPOIsOrFacilitiesPerCategory.get(poi.getCategoryId());
                if ( categoryChilds != null) {
                    categoryChilds.add(poi);
                } else {
                    Log.e(TAG, "poi.getCategoryId():" + poi.getCategoryId() + " , is Not EXIST in Categories");
                }
            }
        }

        filteredPOIsOrFacilitiesPerCategory.putAll(originalPOIsOrFacilitiesPerCategory);

    }

    private POICategory getCategory(Integer categoryID) {
        for(POICategory category:originalCategories){
            if(categoryID.equals(category.getId())){
                return category;
            }
        }
        return null;
    }


    @Override
    public int getGroupCount() {
        if(filteredPOIsOrFacilitiesPerCategory != null) {
            return filteredPOIsOrFacilitiesPerCategory.keySet().size();
        }
        else{
            return 0;
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(filteredPOIsOrFacilitiesPerCategory != null) {
            Integer categoryID = (Integer) filteredPOIsOrFacilitiesPerCategory.keySet().toArray()[groupPosition];
            return filteredPOIsOrFacilitiesPerCategory.get(categoryID).size();
        }
        return 0;
    }

    @Override
    public POICategory getGroup(int groupPosition) {
        Integer categoryID = (Integer) filteredPOIsOrFacilitiesPerCategory.keySet().toArray()[groupPosition];
        return getCategory(categoryID);
    }

    /**
     * Gets the data associated with the given child within the given group.
     *
     * @param groupPosition the position of the group that the child resides in
     * @param childPosition the position of the child with respect to other
     *            children in the group
     * @return the data of the child (POI or Facility)
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Integer categoryID = (Integer) filteredPOIsOrFacilitiesPerCategory.keySet().toArray()[groupPosition];
        return  filteredPOIsOrFacilitiesPerCategory.get(categoryID).get(childPosition);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {

        if(view ==null) {
            view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.com_navibees_sdk_route_to_activity_category_list_item, null);
        }


        TextView categoryName = (TextView)view.findViewById(R.id.poi_category_name);
        ImageView arrow = (ImageView) view.findViewById(R.id.arrow);

        categoryName.setText(getGroup(groupPosition).getNameWRTLang());

        //Highlight text if query
        if(!this.query.isEmpty()) {
            highlightText(categoryName, this.query);
        }

        String iconName,iconPath;

        if(isExpanded){
            arrow.setImageResource(R.drawable.com_navibees_sdk_direction_arrow_up);
            iconName = getGroup(groupPosition).getIcons().getDark();
            view.setBackgroundResource(R.drawable.com_navibees_sdk_list_category_selected);
        }else{
            arrow.setImageResource(R.drawable.com_navibees_sdk_direction_arrow_down);
            iconName = getGroup(groupPosition).getIcons().getLight();
            view.setBackgroundResource(R.drawable.com_navibees_sdk_list_category_not_selected);
        }

        //http://frescolib.org/docs/supported-uris.html
        iconPath = "file://"+ ApplicationConstants.mapResourcesImagesPath +"/" +iconName + "@2x.png" ;
        SimpleDraweeView categoryImage = (SimpleDraweeView) view.findViewById(R.id.poi_category_image);
        Uri uri = Uri.parse(iconPath);
        categoryImage.setImageURI(uri);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {

        if(view ==null) {
            view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.com_navibees_sdk_route_to_activity_poi_list_item, null);
        }

        TextView poiName = (TextView)view.findViewById(R.id.poi_name);

        //Check if Child is POI or Facility then cast it
        if(getChild(i, i1) instanceof POI) {
            poiName.setText(((POI)getChild(i, i1)).getNameWRTLang());
        }else if(getChild(i, i1) instanceof Facility) {
            poiName.setText(((Facility)getChild(i, i1)).getNameWRTLang());
        }

        //Highlight text if query
        if(!this.query.isEmpty()) {
            highlightText(poiName, this.query);
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


    public Map<Integer , List<Object>> filterData(String query){

        if(!TextUtils.isEmpty(query)){

            Map<Integer , List<Object>> newFilteredPOIsOrFacilitiesPerCategory = new LinkedHashMap<Integer, List<Object>>() ;

            for(POICategory category: originalCategories){

                //Search in this category
                //If query match this category name add this category
                //And copy originalPOIsOrFacilitiesPerCategory of this category to newFilteredPOIsOrFacilitiesPerCategory
                //If query does not match category name , LOOP on each POI and Facility in this category


                //In case POI
                //If query match poi name add this poi to newFilteredPOIsOrFacilitiesPerCategory
                //If query does not match poi name search on tags in this poi if match found add poi to newFilteredPOIsOrFacilitiesPerCategory

                //In case Facility
                //If query match facility name add facility to newFilteredPOIsOrFacilitiesPerCategory
                //If query does not match facility name , LOOP on each poi in this facility
                //for each poi
                //If query match poi name add facility to newFilteredPOIsOrFacilitiesPerCategory
                //If query does not match poi name search on tags in this poi if match found add facility to newFilteredPOIsOrFacilitiesPerCategory

                if(CommonUtils.isCategoryMatch(query , category)){
                    //Check first if category is already exist in newFilteredPOIsOrFacilitiesPerCategory
                    List<Object> categoryChilds = newFilteredPOIsOrFacilitiesPerCategory.get(category.getId());
                    if(categoryChilds == null){
                        categoryChilds = new ArrayList<Object>();
                        newFilteredPOIsOrFacilitiesPerCategory.put(category.getId(), categoryChilds);
                    }

                    categoryChilds.addAll(originalPOIsOrFacilitiesPerCategory.get(category.getId()));

                    continue;
                }

                List<Object> listOfObjects = originalPOIsOrFacilitiesPerCategory.get(category.getId());//POIs or Facility
                if(listOfObjects != null){

                    for(Object poiOrFacility: listOfObjects){
                        //In case POI
                        if(poiOrFacility instanceof POI  && (CommonUtils.isPOIMatch(query , (POI)poiOrFacility))){
                        //Search in this POI name and Tags
                            List<Object> categoryChilds = newFilteredPOIsOrFacilitiesPerCategory.get(category.getId());
                                if( categoryChilds == null){
                                    categoryChilds = new ArrayList<Object>();
                                    newFilteredPOIsOrFacilitiesPerCategory.put(category.getId(), categoryChilds);
                                }
                                categoryChilds.add(poiOrFacility);
                        }else if(poiOrFacility instanceof Facility) {
                        //In case Facility
                            List<Object> categoryChilds = newFilteredPOIsOrFacilitiesPerCategory.get(category.getId());
                                //Search in Facility Name
                                if(CommonUtils.searchInString( ((Facility) poiOrFacility).getNameWRTLang(), query )) {

                                    if (categoryChilds == null) {
                                        categoryChilds = new ArrayList<Object>();
                                    }

                                    categoryChilds.add(poiOrFacility);
                                    newFilteredPOIsOrFacilitiesPerCategory.put(category.getId(), categoryChilds);
                                    continue;
                                }else {
                                    //Search in this POIs of this facility
                                    List<POI> poisOfFacility = ((Facility) poiOrFacility).getPois();
                                    if(poisOfFacility != null) {
                                        for (POI poi : poisOfFacility) {
                                            if (CommonUtils.isPOIMatch(query, poi)) {

                                                if (categoryChilds == null) {
                                                    categoryChilds = new ArrayList<Object>();
                                                }

                                                categoryChilds.add(poiOrFacility);
                                                newFilteredPOIsOrFacilitiesPerCategory.put(category.getId(), categoryChilds);
                                                break;

                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }

            //Simulate long search Thread.sleep
            /*
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            this.query = query;
            return newFilteredPOIsOrFacilitiesPerCategory;
        }else {
            this.query = "";
            return new HashMap<Integer, List<Object>>(originalPOIsOrFacilitiesPerCategory);
        }
    }



    public void resetToOriginal(){
        this.query = "";
        filteredPOIsOrFacilitiesPerCategory.clear();
        filteredPOIsOrFacilitiesPerCategory.putAll(originalPOIsOrFacilitiesPerCategory);
        notifyDataSetChanged();
    }

    public void setNewData(Map<Integer , List<Object>> newData){
        filteredPOIsOrFacilitiesPerCategory = newData;
        notifyDataSetChanged();
    }

    public void clearFilteredData(){
        filteredPOIsOrFacilitiesPerCategory.clear();
        notifyDataSetChanged();
    }


    private void highlightText(TextView textView, String filter){
        String itemValue = textView.getText().toString();

        int startPos = -1;
        do {
            startPos = itemValue.toLowerCase().indexOf(filter.toLowerCase(), startPos+1);
        }while(startPos > 0 && itemValue.charAt(startPos-1) != ' ');
        //startPos == 0 || charAt(startPos-1) == ' '

        if (startPos != -1) // This should always be true
        {
            int endPos = startPos + filter.length();
            Spannable spannable = new SpannableString(itemValue);
            ForegroundColorSpan blueColor = new ForegroundColorSpan(Color.BLUE);
            spannable.setSpan(blueColor, startPos, endPos, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);
        }
    }
}
