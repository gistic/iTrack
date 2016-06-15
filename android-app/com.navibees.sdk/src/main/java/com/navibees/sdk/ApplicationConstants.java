package com.navibees.sdk;

/**
 * Created by nabilnoaman on 4/15/15.
 */
public class ApplicationConstants {

    final public static int REQUEST_CODE_DIRECTIONS = 0;
    final public static int REQUEST_CODE_ACTIVITY = 2;
    final public static int REQUEST_CODE_PERMISSION_COARSE_LOCATION = 3;
    final public static int REQUEST_CODE_ENABLE_LOCATION = 4;
    final public static int ACTIVITIES_FRAGMENT_ALL_INDEX = 0;
    final public static int ACTIVITIES_FRAGMENT_CURRENT_INDEX = 1;


    final public static String SELECTED_POI_OR_FACILITY_KEY = "selected_poi_or_facility_to_go";
    final public static String SELECTED_ACTIVITY_POI_ID = "selected_activity_poi_id";


     //Map Resources
    final public static String MAP_RESOURCES_FOLDER = "MapResources";
    final public static String MAP_RESOURCES_APP_CONFIGURATIONS = "MapResources/appConfiguration.json";
    final public static String MAP_RESOURCES_FOLDER_IMAGE = "MapResources/Images";
    final public static String MAP_RESOURCES_FOLDER_META_DATA = "MapResources/Metadata";
    final public static String MAP_RESOURCES_FOLDER_NETWORK_DATASETS = "MapResources/NetworkDatasets";
    final public static String MAP_RESOURCES_FOLDER_TILED_LAYERS = "MapResources/TiledMaps";


    public static String mapResourcesImagesPath,mapResourcesMetadataPath , mapResourcesNetworkDatasetsPath , mapResourcesTiledLayerPath;

    final public static String MAP_RESOURCES_META_DATA_POIs_CATEGORIES = "poisCategories.json";
    final public static String MAP_RESOURCES_META_DATA_POIs = "pois.json";
    final public static String MAP_RESOURCES_META_DATA_TAGs = "beaconsConfiguration.json";
    final public static String MAP_RESOURCES_META_DATA_FLOORs = "floors.json";
    final public static String MAP_RESOURCES_META_DATA_PORTALs = "portals.json";
    final public static String MAP_RESOURCES_META_DATA_RESTRICTIONs = "restrictions.json";
    final public static String MAP_RESOURCES_META_DATA_MONITORED_REGIONS = "monitoredRegions.json";
    final public static String MAP_RESOURCES_META_DATA_FACILITIES = "facilities.json";
    final public static String MAP_RESOURCES_META_DATA_ACTVITIES = "activityGroups.json";



    final public static String MAP_RESOURCES_IMAGES_PATH_KEY = "mapResourcesImagesPath";
    final public static String MAP_RESOURCES_META_DATA_PATH_KEY = "mapResourcesMetadataPath";
    final public static String MAP_RESOURCES_NETWORK_DATASETS_PATH_KEY = "mapResourcesNetworkDatasetsPath";
    final public static String MAP_RESOURCES_TILED_LAYER_PATH_KEY = "mapResourcesTiledLayerPath";


    final public static String APP_CONFIGURATIONS_LAST_MODIFICATION_DATE_KEY = "com.navibees.appConfigurations.modificationDate";
    final public static String BUILDING_META_DATA_LAST_MODIFICATION_DATE_KEY = "com.navibees.buildingMetaData.modificationDate";
    final public static String BEACONS_CONFIGURATIONS_LAST_MODIFICATION_DATE_KEY = "com.navibees.beaconNodesConfigurations.modificationDate";

    final public static String BACKGROUND_MONITORED_REGIONS_KEY = "com.navibees.background.monitored.regions";
    final public static String MONITORED_REGION_ACTION = "com.navibees.monitored.region.action";
    final public static String MONITORED_REGION_ACTION_TYPE_KEY = "com.navibees.monitored.region.action.type";
    final public static String MONITORED_REGION_ACTION_TYPE_ENTER_VALUE = "ENTER";
    final public static String MONITORED_REGION_ACTION_TYPE_EXIT_VALUE = "EXIT";
    final public static String MONITORED_REGION_UNIQUE_IDENTIFIER_KEY = "com.navibees.monitored.region.uniqueIdentifier";
    final public static String IS_APP_IN_FOREGROUND_KEY = "com.navibees.is.app.in.foreground";


    final public static String MAP_RESOURCES_APP_VERSION_KEY = "com.navibees.mapResources.app.version";

    final public static String ROUTE_TO_POI_SORT_TYPE_KEY = "com.navibees.routeTo.poiSortType";
    final public static String ROUTE_TO_CATEGORY_SORT_TYPE_KEY = "com.navibees.routeTo.categorySortType";
    final public static String ROUTE_TO_FACILITY_SORT_TYPE_KEY = "com.navibees.routeTo.facilitySortType";



}
