package com.navibees.sdk.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.R;
import com.navibees.sdk.activity.ActivitiesActivity;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.json.Activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by hossam on 10/19/15.
 */
public class ActivityDescriptionFragment extends Fragment implements View.OnClickListener {

    private static final String ACTIVITY_DATA_KEY = "activity";

    public static ActivityDescriptionFragment getInstance(Activity activity){
        ActivityDescriptionFragment fragment = new ActivityDescriptionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ACTIVITY_DATA_KEY, activity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.com_navibees_sdk_activity_description_fragment, container, false);

        try {

            AppManager.getInstance().getLicenseManager().verify(getContext(), NaviBeesFeature.Temporal_Based_Event_Activities_Notification);

            Activity activity = getArguments().getParcelable(ACTIVITY_DATA_KEY);

//        setupToolbar(rootView, activity.getNameWRTLang());

            TextView titleTextView = (TextView) rootView.findViewById(R.id.com_uqu_navibees_sdk_activity_title);
            titleTextView.setText(activity.getNameWRTLang());

            TextView ownerTextView = (TextView) rootView.findViewById(R.id.com_uqu_navibees_sdk_activity_owner);
            ownerTextView.setText(activity.getOwnerWRTLang());

            TextView descTextView = (TextView) rootView.findViewById(R.id.com_uqu_navibees_sdk_activity_desc);
            descTextView.setText(activity.getDescriptionWRTLang());


            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            parser.setTimeZone(TimeZone.getTimeZone("GMT"));

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd/MM/yyyy");


            String date = "";
            try {
                Date startDate = parser.parse(activity.startDate);
                Date endDate = parser.parse(activity.endDate);

                date = formatter.format(startDate) + " - " + formatter.format(endDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            TextView dateTextView = (TextView) rootView.findViewById(R.id.com_uqu_navibees_sdk_activity_date);
            dateTextView.setText(date);


            ImageView routeToActivity = (ImageView) rootView.findViewById(R.id.com_uqu_navibees_sdk_activity_desc_routeTo_button);
            routeToActivity.setTag(activity.poiId);
            routeToActivity.setOnClickListener(this);

            hideSearchView();


        } catch (NaviBeesLicenseNotAuthorithedException e) {
            e.printStackTrace();
        } catch (NaviBeesLicenseExpireException e) {
            e.printStackTrace();
        }


        return rootView;
    }

    private void hideSearchView() {
        SearchView searchView = ((ActivitiesActivity) getActivity()).getSearchView();
        TextView actionBarTitle = ((ActivitiesActivity) getActivity()).getActionBarTitle();

        if(searchView != null) {
            searchView.setVisibility(View.GONE);
            searchView.onActionViewCollapsed();
            if (actionBarTitle != null) {
                actionBarTitle.setText(getActivity().getTitle());
                actionBarTitle.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupToolbar(View rootView, String title){
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.activitiesToolBar);
        toolbar.setTitle(title);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.com_uqu_navibees_sdk_activity_desc_routeTo_button && v.getTag() != null){
            int poiID = (int) v.getTag();
            Intent data = new Intent();
            data.putExtra(ApplicationConstants.SELECTED_ACTIVITY_POI_ID, poiID);

            getActivity().setResult(android.app.Activity.RESULT_OK, data);
            getActivity().finish();
        }
    }
}
