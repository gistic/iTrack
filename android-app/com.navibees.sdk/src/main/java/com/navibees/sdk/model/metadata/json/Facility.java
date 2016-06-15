package com.navibees.sdk.model.metadata.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.navibees.sdk.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nabilnoaman on 9/15/15.
 */
//http://www.parcelabler.com
public class Facility implements Parcelable{

    private Integer id;
    private Integer categoryId;
    private String name;
    private String nameAr;
    private Icon icons;
    private Boolean isShownOnMap = true;//show POIs under this facility on map if this value true
    private Boolean isExpandable = false;//Expand POIs under this facility on RouteTo Listview if this value true

    private List<POI> pois = new ArrayList<POI>();


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAr() {
        return nameAr;
    }

    public String getNameWRTLang(){
        if(CommonUtils.isArabicLang())
            return nameAr;

        return name;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public Icon getIcons() {
        return icons;
    }

    public void setIcons(Icon icons) {
        this.icons = icons;
    }

    public boolean isShownOnMap() {
        if(isShownOnMap == null){
            return true;//default value if it is not exist in json
        }else {
            return isShownOnMap;
        }
    }

    public void setIsShownOnMap(boolean isShownOnMap) {
        this.isShownOnMap = isShownOnMap;
    }

    public boolean isExpandable() {
        if(isExpandable == null){
            return false;//default value if it is not exist in json
        }else {
            return isExpandable;
        }
    }

    public void setIsExpandable(boolean isExpandable) {
        this.isExpandable = isExpandable;
    }

    public List<POI> getPois() {
        return pois;
    }

    public void setPois(List<POI> pois) {
        this.pois = pois;
    }

    protected Facility(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        categoryId = in.readByte() == 0x00 ? null : in.readInt();
        name = in.readString();
        nameAr = in.readString();
        icons = (Icon) in.readValue(Icon.class.getClassLoader());
        byte isShownOnMapVal = in.readByte();
        isShownOnMap = isShownOnMapVal == 0x02 ? null : isShownOnMapVal != 0x00;
        byte isExpandableVal = in.readByte();
        isExpandable = isExpandableVal == 0x02 ? null : isExpandableVal != 0x00;
        if (in.readByte() == 0x01) {
            pois = new ArrayList<POI>();
            in.readList(pois, POI.class.getClassLoader());
        } else {
            pois = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(id);
        }
        if (categoryId == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(categoryId);
        }
        dest.writeString(name);
        dest.writeString(nameAr);
        dest.writeValue(icons);
        if (isShownOnMap == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (isShownOnMap ? 0x01 : 0x00));
        }
        if (isExpandable == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (isExpandable ? 0x01 : 0x00));
        }
        if (pois == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(pois);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Facility> CREATOR = new Parcelable.Creator<Facility>() {
        @Override
        public Facility createFromParcel(Parcel in) {
            return new Facility(in);
        }

        @Override
        public Facility[] newArray(int size) {
            return new Facility[size];
        }
    };


    public boolean isValid() {
        //Facility is valid if it has categoryId , name.
        //nameAr may be null , so use the name as nameAr
        //icons may be null , so we will use default icon in of its category
        if( (id != null) && (name != null)  && (categoryId != null)) {
            if(nameAr == null){
                nameAr = name;
            }
            return true;
        }else {
            return false;
        }
    }


    public List<IndoorLocation> locationsAtFloor(Integer floor) {
        List<IndoorLocation> locationsAtFloor = new ArrayList<IndoorLocation>();

        if(pois != null) {
            for (POI poi : pois) {

                List<IndoorLocation> locations = poi.locationsAtFloor(floor);

                if (locations != null) {

                    for (IndoorLocation indoorLocation : locations) {
                        if (floor.equals(indoorLocation.getFloor())) {
                            locationsAtFloor.add(indoorLocation);
                        }
                    }
                }
            }


            if (locationsAtFloor.size() != 0) {
                return locationsAtFloor;
            }
        }

        return null;
    }

    public List<IndoorLocation> entryPointsAtFloor(Integer floor){
        List<IndoorLocation> entryPointsAtFloor = new ArrayList<IndoorLocation>();

        if(pois != null)
        {
            for (POI poi : pois) {
                List<IndoorLocation> entryPoints = poi.entryPointsAtFloor(floor);
                if (entryPoints != null) {

                    for (IndoorLocation point : entryPoints) {
                        if (floor.equals(point.getFloor())) {
                            entryPointsAtFloor.add(point);
                        }
                    }
                }

            }


            if (entryPointsAtFloor.size() != 0) {
                return entryPointsAtFloor;
            }
        }
        return null;
    }

}