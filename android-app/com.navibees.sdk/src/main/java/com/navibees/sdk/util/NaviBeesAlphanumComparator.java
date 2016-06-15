package com.navibees.sdk.util;


import com.navibees.sdk.model.metadata.json.Facility;
import com.navibees.sdk.model.metadata.json.POI;
import com.navibees.sdk.model.metadata.json.POICategory;

public class NaviBeesAlphanumComparator extends AlphanumComparator
{
    public enum ComparatorType {
        BY_NAME, BY_ID
    }

    private ComparatorType comparatorType = ComparatorType.BY_NAME;
    public NaviBeesAlphanumComparator(ComparatorType comparatorType){
        super();
        if(comparatorType != null)
        this.comparatorType = comparatorType;
    }

    @Override
    public int compare(Object o1, Object o2) {
        if((o1 instanceof POI) && (o2 instanceof POI)){
            return compare((POI) o1, (POI) o2);
        }else if((o1 instanceof POICategory) && (o2 instanceof POICategory)){
            return compare((POICategory) o1, (POICategory) o2);
        }else if((o1 instanceof Facility) && (o2 instanceof Facility)){
            return compare((Facility) o1, (Facility) o2);
        }else {
            return 0;
        }
    }

        private int compare(POI poi1, POI poi2) {
        if(comparatorType.equals(ComparatorType.BY_NAME)){
            return super.compare(poi1.getNameWRTLang() , poi2.getNameWRTLang());
        }else if(comparatorType.equals(ComparatorType.BY_ID)){
            return poi1.getId().compareTo(poi2.getId());
        }

        return 0;
    }


    private int compare(POICategory poiCategory1, POICategory poiCategory2) {
        if(comparatorType.equals(ComparatorType.BY_NAME)){
            return super.compare(poiCategory1.getNameWRTLang() , poiCategory2.getNameWRTLang());
        }else if(comparatorType.equals(ComparatorType.BY_ID)){
            return poiCategory1.getId().compareTo(poiCategory2.getId());
        }

        return 0;
    }


    private int compare(Facility facility1, Facility facility2) {
        if(comparatorType.equals(ComparatorType.BY_NAME)){
            return super.compare(facility1.getNameWRTLang() , facility2.getNameWRTLang());
        }else if(comparatorType.equals(ComparatorType.BY_ID)){
            return facility1.getId().compareTo(facility2.getId());
        }

        return 0;
    }
}