package com.navibees.sdk.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.R;
import com.navibees.sdk.activity.ActivitiesActivity;
import com.navibees.sdk.model.metadata.json.Activity;
import com.navibees.sdk.model.metadata.json.ActivityGroup;
import com.navibees.sdk.util.CommonUtils;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by hossam on 10/18/15.
 */
public class ActivitiesRecyclerAdapter extends RecyclerView.Adapter<ActivitiesRecyclerAdapter.ActivityViewHolder> implements StickyRecyclerHeadersAdapter<ActivitiesRecyclerAdapter.ActivityHeaderViewHolder>{

    private List<Activity> originalActivities;
    private SparseArray<String> originalActivityGroupName;

    private List<ActivityGroup> originalActivityGroups;
    private List<Activity> searchResultActivities;

    private int mGroupType;

    public static final int GROUP_BY_CATEGORY = 1;
    public static final int GROUP_BY_START_DATE = 2;
    public static final int GROUP_BY_POI = 3;

    private Context mParentActivity;
    private String query = "";

    public ActivitiesRecyclerAdapter(List<ActivityGroup> activityGroups, boolean currentFilter, Context parentActivity){
        originalActivityGroups = activityGroups;
        mParentActivity = parentActivity;
        originalActivities = new ArrayList<Activity>();
        searchResultActivities = new ArrayList<Activity>();
        originalActivityGroupName = new SparseArray<String>();
        for(ActivityGroup group : activityGroups){

            for(Activity activity : group.activities) {
                if(!currentFilter || activity.isHappeningNow() ) {
                    originalActivities.add(activity);
                }
                activity.groupId = group.id;
            }

            originalActivityGroupName.append(group.id, group.getNameWRTLang());
        }
        mGroupType = GROUP_BY_CATEGORY;


        searchResultActivities.addAll(originalActivities);
    }

    public ActivitiesRecyclerAdapter(List<ActivityGroup> activityGroups, boolean currentFilter, Context parentActivity, int groupBy){
        this(activityGroups, currentFilter, parentActivity);
        mGroupType = groupBy;
    }
    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cellView = inflater.inflate(R.layout.com_navibees_sdk_activity_item, parent, false);
        return new ActivityViewHolder(cellView);
    }

    @Override
    public void onBindViewHolder(ActivityViewHolder holder, int position) {
        Activity activity = searchResultActivities.get(position);

        holder.mActivityNameTextView.setText(activity.getNameWRTLang());
        highlightText(holder.mActivityNameTextView , query);

        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        parser.setTimeZone(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        String startDateString = "--";
        String endDateString = "--";
        try {
            Date startDate = parser.parse(activity.startDate);
            startDateString = formatter.format(startDate);

            Date endDate = parser.parse(activity.endDate);
            endDateString = formatter.format(endDate);
        }catch (Exception e){
            e.printStackTrace();
        }
        holder.mActivityStartEndDateTextView.setText(startDateString + " - " + endDateString);

        holder.mActivityInfoImageView.setTag(activity);

        holder.itemView.setTag(activity.poiId);

    }

    @Override
    public long getHeaderId(int position) {
        switch (mGroupType){
            case GROUP_BY_CATEGORY:
                return searchResultActivities.get(position).groupId;
            case GROUP_BY_START_DATE:
                return searchResultActivities.get(position).startDate.hashCode();
            case GROUP_BY_POI:
                return searchResultActivities.get(position).poiId;
            default:
                return searchResultActivities.get(position).groupId;
        }
    }

    @Override
    public ActivityHeaderViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View headerView = inflater.inflate(R.layout.com_navibees_sdk_activity_header_item, viewGroup, false);
        return new ActivityHeaderViewHolder(headerView);
    }

    @Override
    public void onBindHeaderViewHolder(ActivityHeaderViewHolder activityHeaderViewHolder, int position) {
        Activity activity = searchResultActivities.get(position);

        String name = originalActivityGroupName.get(activity.groupId, "");

        activityHeaderViewHolder.mHeaderNameTextView.setText(name);
    }

    @Override
    public int getItemCount() {
        return searchResultActivities.size();
    }

    public class ActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mActivityNameTextView;
        public TextView mActivityStartEndDateTextView;
        public ImageView mActivityInfoImageView;

        public ActivityViewHolder(View itemView) {
            super(itemView);
            mActivityNameTextView = (TextView) itemView.findViewById(R.id.activityNameTextView);
            mActivityStartEndDateTextView = (TextView) itemView.findViewById(R.id.activityStartEndDateTextView);
            mActivityInfoImageView = (ImageView) itemView.findViewById(R.id.activityInfoImageView);

            if(mParentActivity instanceof ActivitiesActivity){
                ((ActivitiesActivity) mParentActivity).customiseActivityInfoIcon(mActivityInfoImageView);
                mActivityInfoImageView.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.activityInfoImageView){
                if(v.getTag() != null && v.getTag() instanceof  Activity) {
                    Activity activity = (Activity) v.getTag();

                    if(mParentActivity instanceof ActivitiesActivity){
                        ((ActivitiesActivity) mParentActivity).openActivityDescriptionFragment(activity);
                    }
                }
            }else{
                if(v.getTag() != null){
                    int poiID = (int) v.getTag();

                    if(mParentActivity instanceof  ActivitiesActivity){
                        ActivitiesActivity activity = (ActivitiesActivity) mParentActivity;

                        Intent data = new Intent();
                        data.putExtra(ApplicationConstants.SELECTED_ACTIVITY_POI_ID, poiID);
                        activity.setResult(android.app.Activity.RESULT_OK, data);
                        activity.finish();
                    }
                }
            }

        }
    }

    public class ActivityHeaderViewHolder extends RecyclerView.ViewHolder{
        public TextView mHeaderNameTextView;

        public ActivityHeaderViewHolder(View itemView) {
            super(itemView);
            mHeaderNameTextView = (TextView) itemView.findViewById(R.id.activityHeaderTextView);
        }
    }

    public List<Activity> searchData(String query) {
        if(!TextUtils.isEmpty(query)){

            List<Activity> resultActivities = new ArrayList<Activity>() ;

            for(ActivityGroup activityGroupName: originalActivityGroups){

                //Search in this Group
                //If query match this Group name copy all activities under this group to resultActivities
                //If query does not match Group name , LOOP on each Activity and check its name


                List<Activity> listOfActivities = activityGroupName.activities;

                if(CommonUtils.searchInString(activityGroupName.getNameWRTLang() , query)){

                    if(listOfActivities != null) {
                        resultActivities.addAll(listOfActivities);
                    }

                    continue;
                }

                if(listOfActivities != null){

                    for(Activity activity: listOfActivities){
                            //Search in this POI name and Tags
                            if(CommonUtils.searchInString(activity.getNameWRTLang() , query)){
                            resultActivities.add(activity);
                            }
                    }
                }
            }

            this.query = query;
            return resultActivities;
        }else {
            this.query = "";
            return new ArrayList<Activity>(originalActivities);
        }
    }



    public void resetToOriginal(){
        this.query = "";
        searchResultActivities.clear();
        searchResultActivities.addAll(originalActivities);
        notifyDataSetChanged();
    }

    public void setNewData(List<Activity> newData){
        searchResultActivities = newData;
        notifyDataSetChanged();
    }

    public void clearFilteredData(){
        searchResultActivities.clear();
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
