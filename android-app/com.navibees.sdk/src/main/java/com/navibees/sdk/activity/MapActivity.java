package com.navibees.sdk.activity;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnPanListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.BuildConfig;
import com.navibees.sdk.NaviBeesApplication;
import com.navibees.sdk.R;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.CircleIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.IndoorLocationRestriction;
import com.navibees.sdk.model.metadata.LineIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.MetaDataManager;
import com.navibees.sdk.model.metadata.PointIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.PolygonIndoorLocationRestriction;
import com.navibees.sdk.model.metadata.json.ApplicationConfiguration;
import com.navibees.sdk.model.metadata.json.Facility;
import com.navibees.sdk.model.metadata.json.Floor;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.metadata.json.IndoorLocationConfidence;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.model.metadata.json.POICategory;
import com.navibees.sdk.model.metadata.json.Portal;
import com.navibees.sdk.model.postioning.IndoorLocationListener;
import com.navibees.sdk.model.postioning.NaviBeesBeaconManager;
import com.navibees.sdk.model.postioning.PositionManager;
import com.navibees.sdk.model.postioning.SensorFusionManager;
import com.navibees.sdk.model.routing.EsriRouting;
import com.navibees.sdk.model.routing.TurnByTurnNavigation;
import com.navibees.sdk.util.CommonUtils;
import com.navibees.sdk.util.Log;
import com.navibees.sdk.util.NaviBeesAlphanumComparator;
import com.navibees.sdk.util.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MapActivity extends AppCompatActivity implements IndoorLocationListener, AppManager.OnInitializedListener, EsriRouting.OnRoutingFinishedListener, OnStatusChangedListener, OnPanListener, OnZoomListener, OnSingleTapListener {


    static final String TAG = "MapActivity";

    private boolean isTrackerOn = false;//we will start it automatically after loading meta data

    private ImageView myLocation;
    private ImageView direction;

    private MapView mapView;

    private GraphicsLayer currentLocationGraphicsLayer;
    private GraphicsLayer routesGraphicsLayer;
    private GraphicsLayer restrictionsGraphicsLayer;
    private GraphicsLayer poisGraphicsLayer;

    private FrameLayout mapOverlay;

    private  ArrayList<String> floorsName;

    private Graphic currentLocationGraphic;
    private Graphic currentLocationWithoutSmoothingGraphic;
    private Graphic currentLocationHoverLowConfidenceGraphic;
    private Graphic currentLocationHoveMediumConfidenceGraphic;

    private PictureMarkerSymbol currentLocationSymbol;
    private SimpleMarkerSymbol currentLocationWithoutSmoothingSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
    private PictureMarkerSymbol currentLocationHoverLowConfidenceSymbol;
    private PictureMarkerSymbol currentLocationHoverMediumConfidenceSymbol;

    private IndoorLocation defaultLocation ;
    private IndoorLocation mCurrentLocation;

    private MetaDataManager metaDataManager = null ;
    private Floor floor;
    private List<POICategory> categories;
    private List<POI> allPOIs;
    private Map<Integer , List<POI>> currentFloorPOIsPerCategory;
    private int[][] currentFloorPOIsPerCategoryIDs;

    private View mCallout;
    private View mMultiFloorCallout;
    private static final int HIT_TOLERANCE = 20;//In pixels, should be adjusted according to zoom level

    private TextView floorNameTV;
    private List<Floor> floorList;

    //Multi Floor Routing
    private int endPointGraphicIDSourceFloor;

    private int startPointGraphicIDTargetFloor;

    private int mMultiRouteSourceFloorIndex = -1;
    private int mMultiRouteTargetPOIFloorIndex = -1;
    private Point mSourceCalloutLocation;
    private Point mTargetCalloutLocation;

    private SparseArray<Graphic[]> routeGraphicPerFloor = null; //startPointGraphic, routeGraphic, endPointGraphic

    //Restrictions (for debugging only)
    private int[] currentFloorRestrictionsGraphicsIDs = null;

    public static int screenDPI;

    private ApplicationConfiguration applicationConfiguration ;

    private TextView numberOfBeacons;

    private boolean isAutomaticModeEnabled = true;//

    private static final int CHANGE_FLOOR_THRESHOLD = 2;
    private int previousFloorIndex = -1;
    private int changeFloorCounter = 0;

    private final String CURRENT_LOCATION_KEY = "com.navibees.MapActivity.currentLocation";
    private final String SOURCE_FLOOR_KEY = "com.navibees.MapActivity.mMultiRouteSourceFloorIndex";
    private final String TARGET_FLOOR_KEY = "com.navibees.MapActivity.mMultiRouteTargetPOIFloorIndex";
    private final String SOURCE_CALLOUT_LOCATION_KEY = "com.navibees.MapActivity.mSourceCalloutLocation";
    private final String TARGET_CALLOUT_LOCATION_KEY = "com.navibees.MapActivity.mTargetCalloutLocation";
    private final String ROUTE_GRAPHIC_PER_FLOOR_KEY = "com.navibees.MapActivity.routeGraphicPerFloor";

    private TextView mNavigationTextView;
    private int segmentGraphicId;

    private PositionManager positionManager;
    private  NaviBeesBeaconManager naviBeesBeaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_navibees_sdk_map_activity);

        setupActionBar();
        //http://stackoverflow.com/questions/3166501/getting-the-screen-density-programmatically-in-android
        //http://developer.android.com/guide/practices/screens_support.html
        //xxhdpi (extra-extra-high) ~480dpi
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenDPI = metrics.densityDpi;

        mapOverlay = (FrameLayout) findViewById(R.id.com_uqu_navibees_sdk_map_activity_mapOverlay);

        myLocation = (ImageView) findViewById(R.id.com_uqu_navibees_sdk_map_activity_myLocation);
        myLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                myLocationOnClickListener();

            }
        });
        myLocation.setEnabled(false);

        direction = (ImageView) findViewById(R.id.com_uqu_navibees_sdk_map_activity_direction);
        direction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                directionOnClickListener();
            }
        });

        numberOfBeacons = (TextView) findViewById(R.id.com_uqu_navibees_sdk_map_activity_numberOfBeacons);
        if (BuildConfig.SHOW_BEACONS) {
            numberOfBeacons.setVisibility(View.VISIBLE);
        }

        mNavigationTextView = (TextView) findViewById(R.id.com_navibees_sdk_navigation_text_view);

        if (savedInstanceState != null) {
            //Restore current location from savedInstaceState to be used in get route. This to handle activity destroyed case.
            mCurrentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION_KEY);

            mMultiRouteSourceFloorIndex = savedInstanceState.getInt(SOURCE_FLOOR_KEY);
            mMultiRouteTargetPOIFloorIndex = savedInstanceState.getInt(TARGET_FLOOR_KEY);
            mSourceCalloutLocation = (Point) savedInstanceState.getSerializable(SOURCE_CALLOUT_LOCATION_KEY);
            mTargetCalloutLocation = (Point) savedInstanceState.getSerializable(TARGET_CALLOUT_LOCATION_KEY);

            routeGraphicPerFloor = getRouteGraphicPerFloor(savedInstanceState);

            initiateTurnByTurnNavigation(routeGraphicPerFloor , mMultiRouteTargetPOIFloorIndex , null);
        }

        try {
            AppManager.getInstance().initialize(this, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addAllLayers(){
        currentLocationGraphicsLayer = new GraphicsLayer();
        currentLocationGraphicsLayer = new GraphicsLayer();
        poisGraphicsLayer = new GraphicsLayer();
        routesGraphicsLayer = new GraphicsLayer();

        if(BuildConfig.SHOW_RESTRICTIONS) {
            restrictionsGraphicsLayer = new GraphicsLayer();
            mapView.addLayer(restrictionsGraphicsLayer);
        }

        mapView.addLayer(poisGraphicsLayer);
        mapView.addLayer(routesGraphicsLayer);
        mapView.addLayer(currentLocationGraphicsLayer);
    }

    private void initializeCurrentLocationSymbols(){
        try {
            currentLocationSymbol = new PictureMarkerSymbol(this.getApplicationContext(), ContextCompat.getDrawable(this, getCurrentLocationPinRes()));
        }catch (Resources.NotFoundException e){
            e.printStackTrace();
            currentLocationSymbol = new PictureMarkerSymbol(this.getApplicationContext(), ContextCompat.getDrawable(this, R.drawable.com_navibees_sdk_current_location_pin));
        }

        try{
            currentLocationHoverLowConfidenceSymbol = new PictureMarkerSymbol(this.getApplicationContext() , ContextCompat.getDrawable(this, getLowConfidenceRes()) );
        }catch (Resources.NotFoundException e) {
            e.printStackTrace();
            currentLocationHoverLowConfidenceSymbol = new PictureMarkerSymbol(this.getApplicationContext() , ContextCompat.getDrawable(this, R.drawable.com_navibees_sdk_current_location_low_confidence) );
        }

        try{
            currentLocationHoverMediumConfidenceSymbol = new PictureMarkerSymbol(this.getApplicationContext() , ContextCompat.getDrawable(this, getMediumConfidenceRes()) );
        }catch (Resources.NotFoundException e){
            e.printStackTrace();
            currentLocationHoverMediumConfidenceSymbol = new PictureMarkerSymbol(this.getApplicationContext() , ContextCompat.getDrawable(this, R.drawable.com_navibees_sdk_current_location_medium_confidence) );
        }
    }

    @Override
    final public void onInitialized(boolean success) {
        Log.i(TAG, "on Initialized: " + success);
        try {

            if (success) {
                metaDataManager = AppManager.getInstance().getMetaDataManager();
                applicationConfiguration = metaDataManager.getApplicationConfiguration(getApplicationContext());
                setupActionBar();

                defaultLocation = new IndoorLocation(applicationConfiguration.getInitialLocation().getX(), applicationConfiguration.getInitialLocation().getY(), applicationConfiguration.getInitialLocation().getFloor());
                defaultLocation.setConfidence(IndoorLocationConfidence.Low);
                mCurrentLocation = mCurrentLocation == null ? defaultLocation : mCurrentLocation;


                routeGraphicPerFloor = routeGraphicPerFloor == null? new SparseArray<Graphic[]>() : routeGraphicPerFloor;

                floorList = metaDataManager.getFloors(getApplicationContext());

                //prepareFloorsName to be used in FloorsPopup Menu
                floorsName = new ArrayList<String>();
                if(floorList != null) {
                    for (Floor floor : floorList) {
                        floorsName.add(getString(R.string.floor_label_floors_horizontal_listView) + " " + floor.getNameWRTLang());
                    }

                    mapView = (MapView) findViewById(R.id.com_uqu_navibees_sdk_map_activity_mapView);

                    initializeCurrentLocationSymbols();

                    floor = floorList.get(mCurrentLocation.getFloor().intValue());
                    drawCurrentFloorMap();
                    isTrackerOn = applicationConfiguration.isTrackingEnabledByDefault();
                    AppManager.getInstance().getTTSManager(this);

                }else {
                    naviBeesBeaconManager = AppManager.getInstance().getNaviBeesBeaconManager(this);
                }
            }else {
                Toast.makeText(getApplicationContext(), "Can't Load MetaData Files", Toast.LENGTH_SHORT);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private boolean clickOnEntryPointsOfPortals(float x, float y){
        Point mapPoint = mapView.toMapPoint(x, y);
        int[] hitGraphicsID = routesGraphicsLayer.getGraphicIDs(x, y, HIT_TOLERANCE);
        if(hitGraphicsID != null && hitGraphicsID.length > 0){
            for(int touchGraphicID : hitGraphicsID){
                if(touchGraphicID == endPointGraphicIDSourceFloor){
                    String floorName = floorList.get(mMultiRouteTargetPOIFloorIndex).getNameWRTLang();
                    showMultiFloorRouteCallout(getString(R.string.routing_to_floor) + " (" + floorName + ")", mapPoint, true);
                    return true;
                }else if(touchGraphicID == startPointGraphicIDTargetFloor){
                    String fromFloorName = floorList.get(mMultiRouteSourceFloorIndex).getNameWRTLang();
                    showMultiFloorRouteCallout(getString(R.string.routing_from_floor) + " (" + fromFloorName + ")", mapPoint, false);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Save current location to be used in get route. This to handle activity destroyed case.
        outState.putParcelable(CURRENT_LOCATION_KEY, mCurrentLocation);

        outState.putInt(SOURCE_FLOOR_KEY, mMultiRouteSourceFloorIndex);
        outState.putInt(TARGET_FLOOR_KEY, mMultiRouteTargetPOIFloorIndex);
        outState.putSerializable(SOURCE_CALLOUT_LOCATION_KEY, mSourceCalloutLocation);
        outState.putSerializable(TARGET_CALLOUT_LOCATION_KEY, mTargetCalloutLocation);

        saveRouteGraphicsPerFloor(outState);

        super.onSaveInstanceState(outState);
    }

    //To Avoid ClassCastException bug in Android < 5 , if we saved Graphics[] for each floor as Serializable in Bundle
    //We will save routeGraphicsPerFloor as bundle of bundles
    //Where outer bundle holds all routesGraphics for all floors , keys is floor index & its size = routeGraphicPerFloor.size() ,
    // inner bundle holds route Graphics fer floor
    //Referance to similar problem : https://code.google.com/p/android/issues/detail?id=3847
    private void saveRouteGraphicsPerFloor(Bundle outState){
        Bundle allRouteGraphicsMap = new Bundle();
        if(routeGraphicPerFloor != null) {
            for (int i = 0; i < routeGraphicPerFloor.size(); i++) {
                int floorKey = routeGraphicPerFloor.keyAt(i);
                Graphic[] graphicsPerFloor = routeGraphicPerFloor.get(floorKey);
                if(graphicsPerFloor != null) {
                    Bundle floorRouteGraphicsMap = new Bundle();
                    for (int j = 0; j < graphicsPerFloor.length; j++) {
                        floorRouteGraphicsMap.putSerializable( j + "", graphicsPerFloor[j]);
                    }
                    allRouteGraphicsMap.putBundle(floorKey + "", floorRouteGraphicsMap);
                }
            }
        }
        outState.putBundle(ROUTE_GRAPHIC_PER_FLOOR_KEY, allRouteGraphicsMap);
    }

    private SparseArray<Graphic[]> getRouteGraphicPerFloor(Bundle inState){
        SparseArray<Graphic[]> result = new SparseArray<Graphic[]>();
        Bundle allRouteGraphicsMap  = inState.getBundle(ROUTE_GRAPHIC_PER_FLOOR_KEY);
        if(allRouteGraphicsMap != null) {
            Set<String> mapKeySet = allRouteGraphicsMap.keySet();
            for (String floorKey : mapKeySet) {

                int floorIndex = Integer.parseInt(floorKey);
                Bundle floorRouteGraphicsMap = allRouteGraphicsMap.getBundle(floorKey);
                Graphic[] floorRouteGraphics = new Graphic[floorRouteGraphicsMap.size()];

                for(int j = 0 ; j < floorRouteGraphicsMap.size() ; j++){
                    floorRouteGraphics[j] = (Graphic)floorRouteGraphicsMap.getSerializable(j+"");
                }

                result.put(floorIndex, floorRouteGraphics);
            }
        }

        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == ApplicationConstants.REQUEST_CODE_DIRECTIONS && resultCode == RESULT_OK) {
                Object selectedPOIOrFacility = data.getParcelableExtra(ApplicationConstants.SELECTED_POI_OR_FACILITY_KEY);

                if(AppManager.getInstance().getTTSManager(this) != null) {
                    AppManager.getInstance().getTTSManager(this).resetTTS();
                }

                EsriRouting routing = AppManager.getInstance().getEsriRouting(this , this);
                if(routing != null) {
                    routing.startRouting(mCurrentLocation, selectedPOIOrFacility, -1, -1, true);
                }

            }else if(requestCode == ApplicationConstants.REQUEST_CODE_ACTIVITY && resultCode == RESULT_OK){
                int poiID = data.getIntExtra(ApplicationConstants.SELECTED_ACTIVITY_POI_ID, -1);

                if(AppManager.getInstance().getTTSManager(this) != null) {
                    AppManager.getInstance().getTTSManager(this).resetTTS();
                }

                EsriRouting routing = AppManager.getInstance().getEsriRouting(this , this);
                if(routing != null) {
                    routing.startRouting(mCurrentLocation, null, poiID, -1, true);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ((NaviBeesApplication) getApplication()).setAppInForeground(true);

        if(mapView != null){
            mapView.unpause();

            if(categories != null && currentFloorPOIsPerCategoryIDs != null ) {
                //Show/Hide Graphic according to filter of category
                for (int i = 0; i < categories.size(); i++) {

                    if (categories.get(i).isFilterEnable()) {

                        for (int j = 0; j < currentFloorPOIsPerCategoryIDs[i].length; j++) {
                            poisGraphicsLayer.setGraphicVisible(currentFloorPOIsPerCategoryIDs[i][j], true);
                        }

                    } else {

                        for (int j = 0; j < currentFloorPOIsPerCategoryIDs[i].length; j++) {
                            poisGraphicsLayer.setGraphicVisible(currentFloorPOIsPerCategoryIDs[i][j], false);
                        }
                    }

                }
            }
        }

        if(isTrackerOn)
            SensorFusionManager.getInstance().registerSensor(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mapView != null) {

            mapView.pause();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        ((NaviBeesApplication) getApplication()).setAppInForeground(false);

        if(mapView != null) {
            mapView.pause();
        }

        SensorFusionManager.getInstance().unregisterSensor();

    }

    @Override
    protected void onDestroy() {

        // Don't forget to shutdown!
        if (AppManager.getInstance().getTTSManager(this) != null) {
            AppManager.getInstance().getTTSManager(this).shutDown();
        }

        super.onDestroy();

        //Stop Reporting location
        if(positionManager != null) {
            positionManager.disableReportingLocation();
        }

        if(AppManager.getInstance().getTurnByTurnNavigation(this.getApplicationContext()) != null){
            AppManager.getInstance().getTurnByTurnNavigation(this.getApplicationContext()).reset();
        }


        //Enable Background Montoring & Update Battery Status
        if(naviBeesBeaconManager != null)
        {
            try {
                //May be Montoring not included in licennse so we should catch NaviBeesLicenseNotAuthorithedException and NaviBeesLicenseExpireException
                naviBeesBeaconManager.enableBackgroundMonitoring();
            } catch (NaviBeesLicenseNotAuthorithedException e) {
                CommonUtils.monitoringNotAuthorizedToBeEnabled(this.getApplicationContext());
                Log.e(TAG, e.toString());

            } catch (NaviBeesLicenseExpireException e) {

                CommonUtils.monitoringNotAuthorizedToBeEnabled(this.getApplicationContext());
                Log.e(TAG, e.toString());
            }

            AppManager.recycle();
        }else {
            AppManager.recycle();
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    TextView title = (TextView) customView.findViewById(R.id.action_bar_title);
                    if (title != null) {
                        if (metaDataManager != null) {
                            Log.i(TAG, "setupActionBar try to get appName from applicationConfiguration");
                            String appName = metaDataManager.getApplicationConfiguration(getApplicationContext()).getNameWRTLang();
                            if(appName == null) {
                                appName = getTitle().toString();
                            }
                            title.setText(appName);
                        } else {
                            title.setText(getTitle().toString());
                        }

                        customizeActionBarTitle(title);
                    }
                }
            });


            //MoreActions
                View moreActions = customView.findViewById(R.id.moreActions);
                if (moreActions != null) {
                    customizeActionBarMoreActionsView(moreActions);
                    moreActions.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(metaDataManager != null) {
                                moreActionItemOnClickListener();
                                viewMoreActionsPopupMenu(v);
                            }
                        }
                    });
                }


            //Floors Selections
                View floorsSelectionContainer = customView.findViewById(R.id.floorsSelectionContainer);
                if (floorsSelectionContainer != null) {

                    ImageView floorsSelectionIcon = (ImageView) customView.findViewById(R.id.floorsSelectionIcon);
                    if(floorsSelectionIcon != null) {
                        customizeActionBarFloorsSelectionIcon(floorsSelectionIcon);
                    }

                    floorNameTV = (TextView) customView.findViewById(R.id.floorName);

                    if(floorNameTV != null) {
                        customizeActionBarFloorName(floorNameTV);
                    }

                    floorsSelectionContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(metaDataManager != null)
                            {
                                floorsActionItemOnClickListener();
                                viewFloorsPopupMenu(v);
                            }
                        }
                    });

                }

        }
        }catch (Resources.NotFoundException e){
            e.printStackTrace();
            //throw new Resources.NotFoundException(e.toString());//Here we should use our own Exception
        }
    }

    @Override
    public void locationCallback(IndoorLocation currentLocationWithoutSmoothing ,IndoorLocation currentLocationAfterSmoothing  ,final int numOfValidBeacons){
        IndoorLocation currentLocation = currentLocationAfterSmoothing;

        if(BuildConfig.SHOW_BEACONS){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numberOfBeacons.setText("" + numOfValidBeacons);
                }
            });
        }

        if(!isTrackerOn)
            return;

        if(currentLocation != null){

            if(previousFloorIndex == -1){
                mCurrentLocation = currentLocation;
                previousFloorIndex = currentLocation.getFloor().intValue();
                changeFloorCounter = 0;
                changeToFloor(previousFloorIndex, false);

            }else if(currentLocation.getFloor().intValue() != mCurrentLocation.getFloor().intValue()){

                if(currentLocation.getFloor().intValue() == previousFloorIndex && floorList.get(currentLocation.getFloor().intValue()) != null){
                    changeFloorCounter = Math.min(++changeFloorCounter, CHANGE_FLOOR_THRESHOLD);
                    if( (changeFloorCounter >= CHANGE_FLOOR_THRESHOLD)){
                        mCurrentLocation = currentLocation;
                        changeFloorCounter = 0;
                    }
                }else{
                    changeFloorCounter = 0;
                }

                previousFloorIndex = currentLocation.getFloor().intValue();
            }else{
                changeFloorCounter = 0;
                mCurrentLocation = currentLocation;
                previousFloorIndex = mCurrentLocation.getFloor().intValue();
            }
        }else{
            mCurrentLocation.setConfidence(IndoorLocationConfidence.Low);
        }

        if(isAutomaticModeEnabled && mCurrentLocation.getFloor().intValue() != floor.getIndex() ){
            changeToFloor(mCurrentLocation.getFloor().intValue(), false);
        }else {
            drawCurrentLocationGraphics();
        }

        boolean isMapRotated = drawTurnByTurnGraphics();
        if(!isMapRotated && isAutomaticModeEnabled){
            double angle = SensorFusionManager.getInstance().getRotationAngleForMap(mCurrentLocation);
            if(angle != -1) mapView.setRotationAngle(angle);
        }

    }

    private boolean drawTurnByTurnGraphics(){
        boolean mapRotated = false;

        if(AppManager.getInstance().getTurnByTurnNavigation(this.getApplicationContext()) != null){
            final TurnByTurnNavigation.NavigationResult result = AppManager.getInstance().getTurnByTurnNavigation(this.getApplicationContext()).getCurrentResult(this.getApplicationContext(), mCurrentLocation);

            if(result != null){
                if(result.mSegment != null) {
                    if(isAutomaticModeEnabled) {
                        mapView.setRotationAngle(result.mRotationAngle);
                        mapRotated = true;
                    }

                    if(mCurrentLocation.getFloor().intValue() == floor.getIndex()) {
                        SimpleLineSymbol sym = new SimpleLineSymbol(Color.RED, 5, SimpleLineSymbol.STYLE.SOLID);
                        Polyline line = new Polyline();
                        LineIndoorLocationRestriction segment = result.mSegment;
                        line.startPath(segment.getStart().getX(), segment.getStart().getY());
                        line.lineTo(segment.getEnd().getX(), segment.getEnd().getY());

                        Graphic segmentGraphic = new Graphic(line, sym);
                        if (segmentGraphicId == -1)
                            segmentGraphicId = routesGraphicsLayer.addGraphic(segmentGraphic);
                        else
                            routesGraphicsLayer.updateGraphic(segmentGraphicId, segmentGraphic);
                    }else{
                        removeSegmentGraphic();
                    }
                }else{
                    removeSegmentGraphic();
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mNavigationTextView.getVisibility() == View.INVISIBLE) {
                            int height = mNavigationTextView.getHeight();
                            ObjectAnimator animator = ObjectAnimator.ofFloat(mNavigationTextView, "y", -height, 0);
                            animator.setDuration(600);
                            animator.setInterpolator(new LinearInterpolator());
                            animator.addListener(showListener);
                            animator.start();
                        }

                        mNavigationTextView.setText(result.mMessage);
                        if(AppManager.getInstance().getTTSManager(MapActivity.this) != null){
                            AppManager.getInstance().getTTSManager(MapActivity.this).speak(result.mMessage);
                        }
                    }
                });

            }else{
                removeSegmentGraphic();

                if(mNavigationTextView.getVisibility() == View.VISIBLE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int height = mNavigationTextView.getHeight();
                            ObjectAnimator animator = ObjectAnimator.ofFloat(mNavigationTextView, "y", 0, -height);
                            animator.setDuration(600);
                            animator.setInterpolator(new LinearInterpolator());
                            animator.addListener(hideListener);
                            animator.start();
                        }
                    });
                }
            }

        }

        return mapRotated;
    }

    private void removeSegmentGraphic(){
        if(segmentGraphicId != -1)
            routesGraphicsLayer.removeGraphic(segmentGraphicId);

        segmentGraphicId = -1;
    }

    private Animator.AnimatorListener showListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            mNavigationTextView.setVisibility(View.VISIBLE);
        }
        @Override
        public void onAnimationEnd(Animator animation) {}
        @Override
        public void onAnimationCancel(Animator animation) {}
        @Override
        public void onAnimationRepeat(Animator animation) {}
    };

    private Animator.AnimatorListener hideListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {}
        @Override
        public void onAnimationEnd(Animator animation) {
            mNavigationTextView.setVisibility(View.INVISIBLE);
        }
        @Override
        public void onAnimationCancel(Animator animation) {}
        @Override
        public void onAnimationRepeat(Animator animation) {}
    };

    private void startTracking(){

        isAutomaticModeEnabled = true;
        isTrackerOn = true;

        if(mapView.getCallout() != null) {
            mapView.getCallout().hide();
        }


        CommonUtils.showNeededPermissionsToReadBeacons(this);

        if(positionManager != null) {
            positionManager.startTracking();
        }

        myLocation.setImageResource(getCurrentLocationOnDrawableRes());

        if(mCurrentLocation.getFloor().intValue() == floor.getIndex()) {
            centerMapOnCurrentLocation();
        }

        SensorFusionManager.getInstance().registerSensor(this);
    }

    private void centerMapOnCurrentLocation(){
        Point center = new Point(mCurrentLocation.getX(), mCurrentLocation.getY());
        if (mapView.getScale() > applicationConfiguration.getDefaultZoomLevel()) {
            mapView.zoomin(true);
            mapView.zoomToScale(center, applicationConfiguration.getDefaultZoomLevel());
        } else {
            mapView.centerAt(center, true);
        }
    }

    private void stopTracking(){
        if(positionManager != null) {
            positionManager.disableReportingLocation();
        }
        isTrackerOn = false;
        myLocation.setImageResource(getCurrentLocationOffDrawableRes());

        SensorFusionManager.getInstance().unregisterSensor();
    }

    private void resetAllLayers(){
        currentLocationGraphicsLayer.removeAll();
        segmentGraphicId = -1;
        routesGraphicsLayer.removeAll();
        if(restrictionsGraphicsLayer != null)
            restrictionsGraphicsLayer.removeAll();
        poisGraphicsLayer.removeAll();
    }

    private void changeToFloor(final int floorIndex, boolean centerMapOnRoute){

        if (mapView.getCallout() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapView.getCallout().hide();
                }
            });
        }

        resetAllLayers();

        floor = floorList.get(floorIndex);

        drawCurrentFloorMap();

        if(BuildConfig.SHOW_RESTRICTIONS)
            drawRestrictionsOfCurrentFloor();

        drawPOIsOfCurrentFloor();

        drawCurrentLocationGraphics();

        drawRouteGraphicsOfCurrentFloor(centerMapOnRoute);

    }

    private void drawRestrictionsOfCurrentFloor(){
        if(positionManager != null) {
            List<IndoorLocationRestriction> currentFloorRestrictions = positionManager.filterRestrictionsByFloor(metaDataManager.getRestrictions(getApplicationContext()), floor.getIndex());
            if (currentFloorRestrictions != null) {
                restrictionsGraphicsLayer.removeGraphics(currentFloorRestrictionsGraphicsIDs);

                List<Graphic> currentFloorRestrictionsGraphics = new ArrayList<Graphic>();

                for (IndoorLocationRestriction restriction : currentFloorRestrictions) {

                    if (restriction instanceof PointIndoorLocationRestriction) {

                        // create a point marker symbol (red, size 10, of type circle)
                        SimpleMarkerSymbol simpleMarker = new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE);
                        Point pointGeometry = new Point(((PointIndoorLocationRestriction) restriction).getPoint().getX(), ((PointIndoorLocationRestriction) restriction).getPoint().getY());
                        // create a graphic with the geometry and marker symbol
                        Graphic pointGraphic = new Graphic(pointGeometry, simpleMarker);

                        currentFloorRestrictionsGraphics.add(pointGraphic);

                        continue;
                    }

                    if (restriction instanceof LineIndoorLocationRestriction) {

                        // create a line symbol (green, 3 thick and a dash style)
                        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.GREEN, 3, SimpleLineSymbol.STYLE.SOLID);

                        // create the line geometry
                        Polyline lineGeometry = new Polyline();
                        lineGeometry.startPath(((LineIndoorLocationRestriction) restriction).getStart().getX(), ((LineIndoorLocationRestriction) restriction).getStart().getY());
                        lineGeometry.lineTo(((LineIndoorLocationRestriction) restriction).getEnd().getX(), ((LineIndoorLocationRestriction) restriction).getEnd().getY());

                        // create the graphic using the geometry and the symbol
                        Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
                        currentFloorRestrictionsGraphics.add(lineGraphic);


                        continue;
                    }

                    if (restriction instanceof CircleIndoorLocationRestriction) {

                        // create a point marker symbol (red, size 10, of type circle)
                        SimpleMarkerSymbol centerSimpleMarker = new SimpleMarkerSymbol(Color.YELLOW, 10, SimpleMarkerSymbol.STYLE.CIRCLE);
                        Point centerPointGeometry = new Point(((CircleIndoorLocationRestriction) restriction).getCenter().getX(), ((CircleIndoorLocationRestriction) restriction).getCenter().getY());
                        // create a graphic with the geometry and marker symbol
                        Graphic centerGraphic = new Graphic(centerPointGeometry, centerSimpleMarker);

                        SimpleMarkerSymbol radiusSimpleMarker = new SimpleMarkerSymbol(Color.CYAN, (int) ((CircleIndoorLocationRestriction) restriction).getRadius(), SimpleMarkerSymbol.STYLE.CIRCLE);
                        // create a graphic with the geometry and marker symbol
                        Graphic radiusGraphic = new Graphic(centerPointGeometry, radiusSimpleMarker);

                        currentFloorRestrictionsGraphics.add(radiusGraphic);
                        currentFloorRestrictionsGraphics.add(centerGraphic);

                        continue;
                    }

                    if (restriction instanceof PolygonIndoorLocationRestriction) {
                /*
                // create a line symbol (green, 3 thick and a dash style)
                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.GREEN, 3, SimpleLineSymbol.STYLE.DASH);

                // create the line geometry
                Polyline lineGeometry = new Polyline();
                lineGeometry.startPath(-302557, 7570663);
                lineGeometry.lineTo(-302959, 7570868);
                lineGeometry.lineTo(-303042, 7571220);
                lineGeometry.lineTo(-302700, 7571803);
                lineGeometry.lineTo(-304043, 7576654);
                lineGeometry.lineTo(-300544, 7585289);
                lineGeometry.lineTo(-294365, 7592435);
                lineGeometry.lineTo(-290122, 7594445);
                lineGeometry.lineTo(-285283, 7595488);

                // create the graphic using the geometry and the symbol
                Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);

                */
                        continue;
                    }

                }


                Graphic[] currentFloorRestrictionsGraphicArray = new Graphic[currentFloorRestrictionsGraphics.size()];
                currentFloorRestrictionsGraphics.toArray(currentFloorRestrictionsGraphicArray);
                currentFloorRestrictionsGraphicsIDs = restrictionsGraphicsLayer.addGraphics(currentFloorRestrictionsGraphicArray);
            }
        }
    }

//    private void drawSegmentsOfRoute(Graphic route){
//        List<List<IndoorLocation>> segments = TurnByTurnNavigation.getSegments(route.getGeometry());
//        for(int i=0; i<segments.size(); i++){
//            List<IndoorLocation> segm = segments.get(i);
//            SimpleLineSymbol sym = null;
//            if(i%3 == 0)
//                sym = new SimpleLineSymbol(Color.YELLOW, 3, SimpleLineSymbol.STYLE.DASH);
//            else if(i%3 == 1)
//                sym = new SimpleLineSymbol(Color.GREEN, 3, SimpleLineSymbol.STYLE.DASH);
//            else if(i%3 == 2)
//                sym = new SimpleLineSymbol(Color.MAGENTA, 3, SimpleLineSymbol.STYLE.DASH);
//
//            Polyline line = new Polyline();
//            IndoorLocation point = segm.get(0);
//            line.startPath( new Point( point.getX(), point.getY() ) );
//            point = segm.get(segm.size()-1);
//            line.lineTo( new Point( point.getX(), point.getY() ) ) ;
//
//            Graphic lineGraphic = new Graphic(line , sym);
//            routesGraphicsLayer.addGraphic(lineGraphic);
//        }
//    }

//    private void animateDirections(final Graphic route){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<List<IndoorLocation>> segments = TurnByTurnNavigation.getSegments(route.getGeometry());
//                List<String> directions = TurnByTurnNavigation.getDirections(segments);
//                int lineGraphicId = -1;
//                SimpleLineSymbol sym = new SimpleLineSymbol(Color.RED, 5, SimpleLineSymbol.STYLE.SOLID);
//                isAutomaticModeEnabled = false;
//                for(int i=0; i<segments.size(); i++){
//                    List<IndoorLocation> segm = segments.get(i);
//                    Polyline line = new Polyline();
//                    IndoorLocation point = segm.get(0);
//                    line.startPath( new Point( point.getX(), point.getY() ) );
//                    point = segm.get(segm.size()-1);
//                    line.lineTo(new Point(point.getX(), point.getY())) ;
//
//                    if(lineGraphicId != -1)
//                        routesGraphicsLayer.removeGraphic(lineGraphicId);
//
//                    lineGraphicId = routesGraphicsLayer.addGraphic(new Graphic(line, sym));
//
//                    mapView.setExtent(line, 20, true);
//
//                    String message = "";
//                    if(i<directions.size())
//                        message = "Move straight then " + directions.get(i);
//                    else
//                        message = "Move straight and be happy, you have arrived!";
//
//                    final String finalMessage = message;
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), finalMessage, Toast.LENGTH_SHORT );
//                        }
//                    });
//
//                    try {
//                        Thread.sleep(3000);
//                    }catch(Exception e){
//
//                    }
//
//                }
//                routesGraphicsLayer.removeGraphic(lineGraphicId);
//                isAutomaticModeEnabled = true;
//            }
//        }).start();
//
//    }

    private void drawStopPointsOfRoute(Graphic route){
        SimpleMarkerSymbol sym = new SimpleMarkerSymbol(Color.GREEN, 10, SimpleMarkerSymbol.STYLE.CIRCLE);
        SimpleMarkerSymbol sym2 = new SimpleMarkerSymbol(Color.MAGENTA, 10, SimpleMarkerSymbol.STYLE.CIRCLE);
        Polyline polyline = (Polyline) route.getGeometry();
        for(int i=0; i<polyline.getPointCount(); i++){
            Graphic point = null;
            if(i%2 == 0)
                point = new Graphic(polyline.getPoint(i), sym);
            else
                point = new Graphic(polyline.getPoint(i), sym2);

            routesGraphicsLayer.addGraphic(point);

        }
    }

    private void drawRouteGraphicsOfCurrentFloor(boolean centerMapOnRoute){
        segmentGraphicId = -1;
        routesGraphicsLayer.removeAll();
        //Drawing of routing with its start and end points
        if(routeGraphicPerFloor.get(floor.getIndex()) != null){
            Graphic[] graphics = routeGraphicPerFloor.get(floor.getIndex());

            //Always add route graphic
            routesGraphicsLayer.addGraphic(graphics[1]);

//            drawStopPointsOfRoute(graphics[1]);

            //Condition for start point graphic
            if(mMultiRouteTargetPOIFloorIndex == floor.getIndex()) {
                startPointGraphicIDTargetFloor = routesGraphicsLayer.addGraphic(graphics[0]);
                String fromFloorName = floorList.get(mMultiRouteSourceFloorIndex).getNameWRTLang();
                showMultiFloorRouteCallout(getString(R.string.routing_from_floor) + " (" + fromFloorName + ")", mTargetCalloutLocation, false);

                if(centerMapOnRoute) {
                    mapView.setExtent(graphics[1].getGeometry(), 40, true);
                }

            }else{
                routesGraphicsLayer.addGraphic(graphics[0]);
            }

            //condition for end point graphic
            if(mMultiRouteSourceFloorIndex == floor.getIndex()){
                endPointGraphicIDSourceFloor = routesGraphicsLayer.addGraphic(graphics[2]);
                String toFloorName = floorList.get(mMultiRouteTargetPOIFloorIndex).getNameWRTLang();
                showMultiFloorRouteCallout(getString(R.string.routing_to_floor) + " (" + toFloorName + ")", mSourceCalloutLocation, true);

                if(centerMapOnRoute) {
                    mapView.setExtent(graphics[1].getGeometry(), 40, true);
                }

            }else{
                routesGraphicsLayer.addGraphic(graphics[2]);
            }

        }
    }

    private void drawPOIsOfCurrentFloor() {

        poisGraphicsLayer.removeAll();

        categories = metaDataManager.getPOIsCategories(this.getApplicationContext());
        allPOIs = metaDataManager.getPOIs(getApplicationContext());
        List<Facility> facilities = metaDataManager.getFacilities(getApplicationContext());

        List<POI> currentFloorPOIs = new ArrayList<POI>();
        List<Graphic> currentFloorPOIsGraphics = new ArrayList<Graphic>();

        currentFloorPOIsPerCategory = new HashMap<Integer, List<POI>>();
        Map<Integer, List<Graphic>> currentFloorPOIsGraphicsPerCategory = new HashMap<Integer, List<Graphic>>();

        if (categories != null) {

            for (int i = 0; i < categories.size(); i++) {
                currentFloorPOIsPerCategory.put(categories.get(i).getId(), new ArrayList<POI>());
                currentFloorPOIsGraphicsPerCategory.put(categories.get(i).getId(), new ArrayList<Graphic>());
            }


            if(allPOIs != null) {
                //Grouping POIs by Category
                for (POI poi : allPOIs) {
                    if (poi.locationsAtFloor(floor.getIndex()) != null) {
                        currentFloorPOIs.add(poi);
                        currentFloorPOIsPerCategory.get(poi.getCategoryId()).add(poi);
                    }
                }
            }

            //Add POIs inside each facility to POIs
            if (facilities != null) {
                for (Facility facility : facilities) {
                    //1.Check If Facility categoryID belongTo existing Category
                    //2.Check If facility has pois
                    //3.Check If facility should be shown on map.
                    if ( facility.getPois() != null && facility.isShownOnMap() ) {
                        List<POI> poiList = facility.getPois();
                        for (POI poi : poiList) {
                            if (poi.locationsAtFloor(floor.getIndex()) != null) {
                                currentFloorPOIs.add(poi);
                                currentFloorPOIsPerCategory.get(poi.getCategoryId()).add(poi);
                                //If there is no icon for POI , put icon of facility
                                if(poi.getIcons() == null){
                                    poi.setIcons(facility.getIcons());
                                }
                            }
                        }

                    } else {
                        Log.e(TAG, "Facility with name : " + facility.getName() + " , doesn't have pois or isShownOnMap = false");
                    }
                }
            }


            for (POI poi : currentFloorPOIs) {
                Log.i(TAG, "---poi.getName()--- :" + poi.getName());

                PictureMarkerSymbol poiSymbol = null;
                String poiIconPath = null;

                String poiActiveIconName = null;
                if (poi.getIcons() != null) {
                    poiActiveIconName = poi.getIcons().getMapActive();
                }else{
                    POICategory poiCategory = getCategory(poi);//;categories.get(poi.getCategoryId());/
                    if (poiCategory.getIcons() != null) {
                        poiActiveIconName = poiCategory.getIcons().getMapActive();
                    }
                }
                if(poiActiveIconName != null){

                    if (screenDPI >= DisplayMetrics.DENSITY_XXXHIGH) {
                        poiIconPath = ApplicationConstants.mapResourcesImagesPath + "/" + poiActiveIconName + "@3x.png";
                    } else if (screenDPI >= DisplayMetrics.DENSITY_XXHIGH) {
                        poiIconPath = ApplicationConstants.mapResourcesImagesPath + "/" + poiActiveIconName + "@2x.png";
                    } else {
                        poiIconPath = ApplicationConstants.mapResourcesImagesPath + "/" + poiActiveIconName + "@1x.png";
                    }

                    if(new File(poiIconPath).exists()) {
                        poiSymbol = new PictureMarkerSymbol(Drawable.createFromPath(poiIconPath));
                    }else {
                        poiSymbol = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(this, R.drawable.com_navibees_sdk_default_poi_active));
                    }
                }else{
                    poiSymbol = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(this, R.drawable.com_navibees_sdk_default_poi_active));
                }

                //Save attributes with each poi to help us in search for touched poi
                //Same POI exists more than one (more than one Point) in same floor
                //& More Than one entry point
                //We will consider new location is new duplicate POI
                List<IndoorLocation> locations = poi.locationsAtFloor(floor.getIndex());
                if(locations != null){
                    IndoorLocation indoorLocation = locations.get(0);
                    Point poiPointGeometry = new Point(indoorLocation.getX(), indoorLocation.getY());
                    // Attribute
                    // Map<String , POI> attribute = new HashMap<String , POI>();
                    Map<String, Object> attribute = new HashMap<String, Object>();
                    attribute.put("categoryID", poi.getCategoryId());

                    Graphic poiGraphic = new Graphic(poiPointGeometry, poiSymbol, attribute);

                    currentFloorPOIsGraphics.add(poiGraphic);
                    currentFloorPOIsGraphicsPerCategory.get(poi.getCategoryId()).add(poiGraphic);
                    currentFloorPOIsPerCategory.get(poi.getCategoryId()).add(poi);//duplicate POI to help us in routing
                }
            }

            currentFloorPOIsPerCategoryIDs = new int[categories.size()][];

            for (int i = 0; i < categories.size(); i++) {
                currentFloorPOIsPerCategoryIDs[i] = new int[currentFloorPOIsPerCategory.get(categories.get(i).getId()).size()];
                Graphic[] currentFloorPOIsGraphicsPerCategoryArray = new Graphic[currentFloorPOIsGraphicsPerCategory.get(categories.get(i).getId()).size()];
                currentFloorPOIsGraphicsPerCategory.get(categories.get(i).getId()).toArray(currentFloorPOIsGraphicsPerCategoryArray);

                currentFloorPOIsPerCategoryIDs[i] = poisGraphicsLayer.addGraphics(currentFloorPOIsGraphicsPerCategoryArray);

                //Hide Graphic for POI with Category disabled filter
                if (!categories.get(i).isFilterEnable()) {
                    for (int j = 0; j < currentFloorPOIsPerCategoryIDs[i].length; j++) {
                        poisGraphicsLayer.setGraphicVisible(currentFloorPOIsPerCategoryIDs[i][j], false);
                    }
                }
            }
        }

        //for showing or hiding
        showOrHidePoisWrtZoomLevel();

    }

    private void drawCurrentLocationGraphics(){
        currentLocationGraphicsLayer.removeAll();

        if(mCurrentLocation.getFloor().intValue() == floor.getIndex()){
            Point pointGeometry = new Point(mCurrentLocation.getX(), mCurrentLocation.getY());
            currentLocationGraphic = new Graphic(pointGeometry, currentLocationSymbol);
            currentLocationGraphicsLayer.addGraphic(currentLocationGraphic);


            if(mCurrentLocation.getConfidence().equals(IndoorLocationConfidence.Average)){
                currentLocationHoveMediumConfidenceGraphic = new Graphic(pointGeometry , currentLocationHoverMediumConfidenceSymbol);
                currentLocationGraphicsLayer.addGraphic(currentLocationHoveMediumConfidenceGraphic);
            }else if(mCurrentLocation.getConfidence().equals(IndoorLocationConfidence.Low)){
                currentLocationHoverLowConfidenceGraphic = new Graphic(pointGeometry , currentLocationHoverLowConfidenceSymbol);
                currentLocationGraphicsLayer.addGraphic(currentLocationHoverLowConfidenceGraphic);
            }

            if(isAutomaticModeEnabled){
                centerMapOnCurrentLocation();
            }


        }
    }

    private void drawCurrentFloorMap(){


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (floorNameTV != null) {
                    floorNameTV.setText("" + floor.getNameWRTLang());
                }

            }
        });

        mapView.setAllowRotationByPinch(true);
        Log.d(TAG, "###### Floor index: " + floor.getIndex() + " , Floor basemap: " + floor.getBasemap());

        if( (mapView.getLayers() != null ) && (mapView.getLayers().length > 0 ) && mapView.getLayer(0) instanceof  ArcGISLocalTiledLayer)
            mapView.removeLayer(0);

        //ArcGISTiledMapServiceLayer tiledMapServiceLayer = new ArcGISTiledMapServiceLayer(floor.getBasemap());
        //https://developers.arcgis.com/android/api-reference/reference/com/esri/android/map/ags/ArcGISLocalTiledLayer.html
        ArcGISLocalTiledLayer localTiledLayer = new ArcGISLocalTiledLayer(ApplicationConstants.mapResourcesTiledLayerPath+"/"+ floor.getTilePackage()+ ".tpk");

        mapView.addLayer(localTiledLayer, 0);
        mapView.setMapBackground(Color.WHITE, Color.BLACK, 0, 0);

        mapView.setOnStatusChangedListener(this);
        mapView.setOnPanListener(this);
        mapView.setOnZoomListener(this);
        mapView.setOnSingleTapListener(this);

    }

    @Override
    public void onStatusChanged(Object o, STATUS status) {
        Log.e(TAG, "Source :" + o + " , status:" + status);
        if( status.equals(STATUS.INITIALIZED) ) {

            addAllLayers();
            changeToFloor(mCurrentLocation.getFloor().intValue() , false);

            if(mCurrentLocation.getFloor().intValue() == floor.getIndex()){
                Point pointGeometry = new Point(mCurrentLocation.getX(), mCurrentLocation.getY());
                mapView.zoomToScale(pointGeometry, applicationConfiguration.getDefaultZoomLevel());
            }

            positionManager = AppManager.getInstance().getPositionManager(MapActivity.this, MapActivity.this);
            naviBeesBeaconManager = AppManager.getInstance().getNaviBeesBeaconManager(this);

            if (isTrackerOn) {
                startTracking();
            }

            myLocation.setEnabled(true);

        }

        if(status.equals(STATUS.LAYER_LOADED)){
            mapOverlay.setVisibility(View.GONE);
            showOrHidePoisWrtZoomLevel();
        }

        if(status.equals(STATUS.LAYER_LOADING_FAILED) ){
            mapOverlay.setVisibility(View.VISIBLE);
            poisGraphicsLayer.setVisible(false);
        }
    }

    @Override
    public void onSingleTap(float x, float y){
        mapView.getCallout().hide();

        if (clickOnEntryPointsOfPortals(x, y))
            return;

        int[] hitGraphicsID = poisGraphicsLayer.getGraphicIDs(x, y, HIT_TOLERANCE);
        if (hitGraphicsID != null && hitGraphicsID.length > 0) {

            for (int touchedGrahicID : hitGraphicsID) {
                Integer nearstPOICategoryID = (Integer) poisGraphicsLayer.getGraphic(touchedGrahicID).getAttributes().get("categoryID");

                if(nearstPOICategoryID == null)
                    continue;

                int nearstPOICategoryIndex = -1;
                for (int i = 0; i < categories.size(); i++) {
                    if (nearstPOICategoryID.intValue() == categories.get(i).getId()) {
                        nearstPOICategoryIndex = i;
                        break;
                    }
                }

                if (nearstPOICategoryIndex != -1 && categories.get(nearstPOICategoryIndex).isFilterEnable()) {

                    for (int j = 0; j < currentFloorPOIsPerCategoryIDs[nearstPOICategoryIndex].length; j++) {

                        if (touchedGrahicID == currentFloorPOIsPerCategoryIDs[nearstPOICategoryIndex][j]) {

                            POI poi = currentFloorPOIsPerCategory.get(nearstPOICategoryID).get(j);

                            showCalloutForPOI(poi);

                            return;
                        }
                    }
                }
            }
        }
    }


    @Override
    public void prePointerMove(float v, float v1, float v2, float v3) {
        isAutomaticModeEnabled = false;
    }

    @Override
    public void postPointerMove(float v, float v1, float v2, float v3) {
        isAutomaticModeEnabled = false;
    }

    @Override
    public void prePointerUp(float v, float v1, float v2, float v3) {
        isAutomaticModeEnabled = false;
    }

    @Override
    public void postPointerUp(float v, float v1, float v2, float v3) {
        isAutomaticModeEnabled = false;
    }

    @Override
    public void preAction(float v, float v1, double v2) {}

    @Override
    public void postAction(float v, float v1, double v2) {
        showOrHidePoisWrtZoomLevel();
    }

    private void showOrHidePoisWrtZoomLevel(){
        if ((int) mapView.getScale() <= applicationConfiguration.getMinimumPOIVisiableZoomLevel()) {
            poisGraphicsLayer.setVisible(true);
        } else {
            poisGraphicsLayer.setVisible(false);
        }
    }

    private POICategory getCategory(POI poi) {
        if(categories != null) {
            for (POICategory category : categories) {
                if (poi.getCategoryId().intValue() == category.getId().intValue()) {
                    return category;
                }
            }
        }
        return null;
    }

    private void showMultiFloorRouteCallout(String text, Point calloutLocation , final boolean isSourceFloor) {

        try {

            // If the com_uqu_navibees_sdk_map_activity_callout has never been created, inflate it
            if (mMultiFloorCallout == null) {
                LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mMultiFloorCallout = inflater.inflate( getCalloutLayoutRes(), null);
                mMultiFloorCallout.setBackgroundResource(getCalloutLayoutBackgroundRes());
            }


            ImageView leftIcon = (ImageView) mMultiFloorCallout.findViewById(R.id.callout_left_icon);
            if(leftIcon != null){
                customizeCalloutLeftIcon(leftIcon);
                leftIcon.setVisibility(View.GONE);

            }

            ImageView rightIcon = (ImageView) mMultiFloorCallout.findViewById(R.id.callout_right_icon);
            if(rightIcon != null){
                customizeCalloutRightIcon(rightIcon);
            }

            // Show the com_uqu_navibees_sdk_map_activity_callout with the given text at the given location
            TextView poiName = ((TextView) mMultiFloorCallout.findViewById(R.id.poi_name));
            if(poiName != null) {
                poiName.setText(text);
                customizeCalloutPOIName(poiName);
            }

            mapView.getCallout().show(calloutLocation, mMultiFloorCallout);

            mMultiFloorCallout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapView.getCallout().hide();
                    isAutomaticModeEnabled = false;
                    calloutOnClickListener();
                    if (isSourceFloor) {
                        changeToFloor(mMultiRouteTargetPOIFloorIndex, true);
                    } else {
                        changeToFloor(mMultiRouteSourceFloorIndex, true);
                    }
                }
            });

        }catch (Resources.NotFoundException e){
            e.printStackTrace();
            //throw new Resources.NotFoundException(e.toString());//Here we should use our own Exception
        }
    }

    private void showCalloutForPOI(final POI selectedPOI){

        if(mCallout == null){
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mCallout = inflater.inflate(getCalloutLayoutRes(), null , false);
            mCallout.setBackgroundResource(getCalloutLayoutBackgroundRes());
        }

        ImageView leftIcon = (ImageView) mCallout.findViewById(R.id.callout_left_icon);
        if(leftIcon != null){
            customizeCalloutLeftIcon(leftIcon);
        }

        ImageView rightIcon = (ImageView) mCallout.findViewById(R.id.callout_right_icon);
        if(rightIcon != null){
            customizeCalloutRightIcon(rightIcon);
        }

        TextView poiName = (TextView) mCallout.findViewById(R.id.poi_name);
        if(poiName != null){
            poiName.setText(selectedPOI.getNameWRTLang());
            customizeCalloutPOIName(poiName);
        }

        IndoorLocation poiLocation = selectedPOI.locationsAtFloor(floor.getIndex()).get(0);
        Point calloutLocation = new Point(poiLocation.getX(), poiLocation.getY());

        mapView.getCallout().show(calloutLocation, mCallout);

        mCallout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calloutOnClickListener();

                if(AppManager.getInstance().getTTSManager(MapActivity.this) != null) {
                    AppManager.getInstance().getTTSManager(MapActivity.this).resetTTS();
                }
                EsriRouting routing = AppManager.getInstance().getEsriRouting(MapActivity.this , MapActivity.this);
                if(routing != null) {
                    routing.startRouting(mCurrentLocation, selectedPOI, selectedPOI.getId(), floor.getIndex(), false);
                }
            }
        });

    }

    @Override
    public void onRoutingFinished(SparseArray<Graphic[]> result, int multiRouteSourceFloorIndex, int multiRouteTargetPOIFloorIndex, Point sourceCalloutLocaiton, Point targetCalloutLocation , Portal potalUsed) {
        resetRouteGraphicsVariables();
        mMultiRouteTargetPOIFloorIndex = multiRouteTargetPOIFloorIndex;

        if(result != null) {
            routeGraphicPerFloor = result;
            mMultiRouteSourceFloorIndex = multiRouteSourceFloorIndex;
            mSourceCalloutLocation = sourceCalloutLocaiton;
            mTargetCalloutLocation = targetCalloutLocation;

            initiateTurnByTurnNavigation(result , multiRouteTargetPOIFloorIndex , potalUsed);
        }

        if(mapView != null && routesGraphicsLayer != null) {

            if(mapView.getCallout() != null)
                mapView.getCallout().hide();

            drawRouteGraphicsOfCurrentFloor(false);

            if (!isTrackerOn || !isAutomaticModeEnabled) {
                startTracking();
            }
        }

    }

    private void initiateTurnByTurnNavigation(SparseArray<Graphic[]>  routesGraphics , int targetFloor , Portal potalUsed) {
        SparseArray<Geometry> routes = new SparseArray<Geometry>();
        for(int i=0; i<routesGraphics.size(); i++){
            int floorKey = routesGraphics.keyAt(i);
            Graphic[] graphics =  routesGraphics.get(floorKey);
            routes.put(floorKey, graphics[1].getGeometry());
        }

        if(AppManager.getInstance().getTurnByTurnNavigation(this.getApplicationContext()) != null){
            AppManager.getInstance().getTurnByTurnNavigation(this.getApplicationContext()).updateEsriRoute(routes, targetFloor , potalUsed);
        }

    }

    private void resetRouteGraphicsVariables(){
        mMultiRouteSourceFloorIndex = -1;
        mMultiRouteTargetPOIFloorIndex = -1;

        segmentGraphicId = -1;

        if(AppManager.getInstance().getTurnByTurnNavigation(this.getApplicationContext()) != null){
            AppManager.getInstance().getTurnByTurnNavigation(this.getApplicationContext()).reset();
        }
        
        if(routesGraphicsLayer != null) {
            routesGraphicsLayer.removeAll();
        }

        if(routeGraphicPerFloor != null) {
            routeGraphicPerFloor.clear();
        }else {
            routeGraphicPerFloor = new SparseArray<Graphic[]>();//recreate it if it was destroyed when switching to Any other activity
        }
    }

    private void viewMoreActionsPopupMenu(final View anchor) {
        //View vItem = findViewById(R.id.action_floor_selection);
        final PopupMenu popMenu = new PopupMenu( this , anchor);
        popMenu.getMenuInflater().inflate(getMoreActionsMenuRes(), popMenu.getMenu());


        popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                popMenu.dismiss();
                int i = item.getItemId();

                //Add flexibility if developer who will extend MapActivity class to remove any entry (map filter , about) from
                //com_uqu_navibees_sdk_map_activity_menu_action_map_filter.xml
                try{
                    if (i == R.id.com_navibees_sdk_map_activity_menu_action_map_filter) {
                       mapFilterMenuItemOnClickListener();
                       openMapFilterActivity();
                       return true;
                     }
                }catch (NoSuchFieldError e){
                    e.printStackTrace();
                }

                try {
                    if (i == R.id.com_navibees_sdk_map_activity_menu_action_about) {
                        aboutMenuItemOnClickListener();
                        openAboutActivity();
                        return true;
                    }
                }catch (NoSuchFieldError e){
                    e.printStackTrace();
                }

                try {
                    if (i == R.id.com_navibees_sdk_map_activity_menu_action_activities) {
                        activitiesMenuItemOnClickListener();
                        openActivitiesActivity();
                        return true;
                    }
                }catch (NoSuchFieldError e){
                    e.printStackTrace();
                }

                //Third Party Items
                moreActionExtraMenuItemOnClickListener(item);
                return true;
            }
        });

        popMenu.show();
    }

    private void viewFloorsPopupMenu(final View anchor) {
        //View vItem = findViewById(R.id.action_floor_selection);
        final PopupMenu popMenu = new PopupMenu( this , anchor);
        for (int i = 0; i < floorsName.size(); i++)
        {
            popMenu.getMenu().add(0, i, i, floorsName.get(i));
        }

        popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                popMenu.dismiss();
                floorsOnMenuItemClick(item);
                return true;
            }
        });

        popMenu.show();
    }



    private void openAboutActivity() {
        Intent i = new Intent(MapActivity.this.getApplicationContext(), getAboutActivity());
        startActivity(i);
    }

    private void openMapFilterActivity() {
        Intent i = new Intent(MapActivity.this.getApplicationContext(), getPOIsCategoriesFilterActivity());
        startActivity(i);
    }


    private void openActivitiesActivity(){
        Intent i = new Intent(this, getActivitiesActivity());
        startActivityForResult(i, ApplicationConstants.REQUEST_CODE_ACTIVITY);
    }


    // set of listeners to be used by sub-class of MapActivity in Third Party apps

    //-------------- myLocation ImageView -----------------
    protected void myLocationOnClickListener() {
        Log.i(TAG, "myLocationOnClickListener");
        if(isTrackerOn){
            stopTracking();
        }else {

            if(AppManager.getInstance().getTTSManager(this) != null) {
                AppManager.getInstance().getTTSManager(this).resetTTS();
            }
            
            resetRouteGraphicsVariables();
            startTracking();
        }

    }
    //------------------------------------------------------

    //-------------- direction (routeTo) ImageView -----------------
    protected void directionOnClickListener() {
        Log.i(TAG, "directionOnClickListener");
        Intent i = new Intent(MapActivity.this.getApplicationContext(), getRouteToActivity());
        i.putExtra(ApplicationConstants.ROUTE_TO_CATEGORY_SORT_TYPE_KEY , getRouteToCategorySortType());
        i.putExtra(ApplicationConstants.ROUTE_TO_POI_SORT_TYPE_KEY , getRouteToPOISortType());
        i.putExtra(ApplicationConstants.ROUTE_TO_FACILITY_SORT_TYPE_KEY , getRouteToFacilitySortType());

        startActivityForResult(i, ApplicationConstants.REQUEST_CODE_DIRECTIONS);
    }

    //------------------------------------------------------

    //-------------- Menu Under Floors Action -----------------
    protected void floorsActionItemOnClickListener() {
        Log.i(TAG, "floorsActionItemOnClickListener");
    }

    protected void floorsOnMenuItemClick(MenuItem item) {
        Log.i(TAG , "floorsOnMenuItemClick");
        //using item.getItemId() is equivalent to floorIndex in floors.json
        int floorIndex = item.getItemId();
        if(floor.getIndex() != floorIndex) {
            isAutomaticModeEnabled = false;
            //Change Floor
            changeToFloor(floorIndex, false);
        }

    }

    //------------------------------------------------------


    //-------------- Menu Under More Action -----------------
    protected void moreActionItemOnClickListener() {
        Log.i(TAG , "moreActionItemOnClickListener");
    }
    protected void mapFilterMenuItemOnClickListener() {
        Log.i(TAG , "mapFilterMenuItemOnClickListener");
    }
    protected void aboutMenuItemOnClickListener() {
        Log.i(TAG, "aboutMenuItemOnClickListener");
    }

    protected void activitiesMenuItemOnClickListener(){
        Log.i(TAG , "activitiesMenuItemOnClickListener");
    }

    protected void moreActionExtraMenuItemOnClickListener(MenuItem item) {
        Log.i(TAG , "moreActionExtraMenuItemOnClickListener");
    }
    //------------------------------------------------------


    //get UI component can be used by sub class of Map Activity to customize look & feel

    //----------------ActionBar----------------------
    protected int getActionBarLayoutRes() {
        return R.layout.com_navibees_sdk_map_activity_action_bar;
    }

    protected int getActionBarLayoutBackgroundRes() {
        return R.drawable.com_navibees_sdk_map_activity_action_bar_layout_bg;
    }

    protected void customizeActionBarTitle(TextView title) {
    }

    protected int getMoreActionsMenuRes() {
        return R.menu.com_navibees_sdk_map_activity_menu_more_actions;
    }

    protected void customizeActionBarMoreActionsView(View moreActions) {
    }

    protected void customizeActionBarFloorsSelectionIcon(ImageView floorsSelectionIcon) {
    }

    protected void customizeActionBarFloorName(TextView floorNameTV) {
    }

    //----------------MyLoaction and RouteTo
    protected ImageView getMyLocationImageView() {
        return myLocation;
    }

    protected int getCurrentLocationOnDrawableRes() {
        return R.drawable.com_navibees_sdk_current_location_on;
    }

    protected int getCurrentLocationOffDrawableRes() {
        return R.drawable.com_navibees_sdk_current_location_off;
    }


    protected ImageView getRouteToImageView() {
        return direction;
    }



    //--------------- Callout
    protected int getCalloutLayoutRes() {
        return R.layout.com_navibees_sdk_map_activity_callout;
    }

    protected int getCalloutLayoutBackgroundRes() {
        return R.drawable.com_navibees_sdk_map_activity_callout_layout_bg;
    }

    protected void customizeCalloutPOIName(TextView poiName) {
    }

    protected void customizeCalloutLeftIcon(ImageView leftIcon) {
    }

    protected void customizeCalloutRightIcon(ImageView rightIcon) {
    }
    protected void calloutOnClickListener() {
        Log.i(TAG , "calloutOnClickListener");
    }

    //-----------------------------

    //CurrentLocation Pin & Confidence Hover

    protected int getCurrentLocationPinRes() {
        return R.drawable.com_navibees_sdk_current_location_pin;
    }


    protected int getLowConfidenceRes() {
        return R.drawable.com_navibees_sdk_current_location_low_confidence;
    }

    protected int getMediumConfidenceRes() {
        return R.drawable.com_navibees_sdk_current_location_medium_confidence;
    }

    //----------------------------------------


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CommonUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    protected Class<? extends  AboutActivity> getAboutActivity() {
        return AboutActivity.class;
    }


    protected Class<? extends  RouteToActivity> getRouteToActivity() {
        return RouteToActivity.class;
    }


    protected Class<? extends  ActivitiesActivity> getActivitiesActivity() {
        return ActivitiesActivity.class;
    }

    protected Class<? extends POIsCategoriesFilterActivity> getPOIsCategoriesFilterActivity() {
        return POIsCategoriesFilterActivity.class;
    }

    //--------- Route To Sort By

    private NaviBeesAlphanumComparator.ComparatorType getRouteToCategorySortType() {
        if(isRouteToCategorySortedByName()) {
            return NaviBeesAlphanumComparator.ComparatorType.BY_NAME;
        }
        return NaviBeesAlphanumComparator.ComparatorType.BY_ID;
    }

    protected boolean isRouteToCategorySortedByName(){
        return false;
    }


    private NaviBeesAlphanumComparator.ComparatorType getRouteToPOISortType() {
        if(isRouteToPOISortedByName()) {
            return NaviBeesAlphanumComparator.ComparatorType.BY_NAME;
        }
        return NaviBeesAlphanumComparator.ComparatorType.BY_ID;
    }

    protected boolean isRouteToPOISortedByName(){
        return true;
    }


    private NaviBeesAlphanumComparator.ComparatorType getRouteToFacilitySortType() {
        if(isRouteToFacilitySortedByName()) {
            return NaviBeesAlphanumComparator.ComparatorType.BY_NAME;
        }else return NaviBeesAlphanumComparator.ComparatorType.BY_ID;
    }


    protected boolean isRouteToFacilitySortedByName(){
        return true;
    }

    /*
    protected Class<? extends SettingActivity> getSettingsActivity() {
        return SettingActivity.class;
    }
    */


}