package com.navibees.sdk.model.metadata.json;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.navibees.sdk.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nabilnoaman on 4/14/15.
 */
//http://www.parcelabler.com
public class POI implements Parcelable {


    private Integer id;
    private Integer categoryId;
    private String name;
    private String nameAr;
    private Double minimumVisibleScale;
    @SerializedName("locations")
    private List<IndoorLocation> locations = new ArrayList<IndoorLocation>();
    @SerializedName("entryPoints")
    private List<IndoorLocation> entryPoints = new ArrayList<IndoorLocation>();
    @SerializedName("icons")
    private Icon icons;
    private List<String> tags = new ArrayList<String>();

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The categoryId
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     *
     * @param categoryId
     * The categoryId
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
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



    /**
     *
     * @return
     * The minimumVisibleScale
     */
    public Double getMinimumVisibleScale() {
        return minimumVisibleScale;
    }

    /**
     *
     * @param minimumVisibleScale
     * The minimumVisibleScale
     */
    public void setMinimumVisibleScale(Double minimumVisibleScale) {
        this.minimumVisibleScale = minimumVisibleScale;
    }

    /**
     *
     * @return
     * The locations
     */
    public List<IndoorLocation> getLocations() {
        return locations;
    }

    /**
     *
     * @param locations
     * The locations
     */
    public void setLocations(List<IndoorLocation> locations) {
        this.locations = locations;
    }

    /**
     *
     * @return
     * The entryPoints
     */
    public List<IndoorLocation> getEntryPoints() {
        return entryPoints;
    }

    /**
     *
     * @param entryPoints
     * The entryPoints
     */
    public void setEntryPoints(List<IndoorLocation> entryPoints) {
        this.entryPoints = entryPoints;
    }

    /**
     *
     * @return
     * The icons
     */
    public Icon getIcons() {
        return icons;
    }

    /**
     *
     * @param icons
     * The icons
     */
    public void setIcons(Icon icons) {
        this.icons = icons;
    }


    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    protected POI(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        categoryId = in.readByte() == 0x00 ? null : in.readInt();
        name = in.readString();
        nameAr = in.readString();
        minimumVisibleScale = in.readByte() == 0x00 ? null : in.readDouble();
        if (in.readByte() == 0x01) {
            locations = new ArrayList<IndoorLocation>();
            in.readList(locations, IndoorLocation.class.getClassLoader());
        } else {
            locations = null;
        }
        if (in.readByte() == 0x01) {
            entryPoints = new ArrayList<IndoorLocation>();
            in.readList(entryPoints, IndoorLocation.class.getClassLoader());
        } else {
            entryPoints = null;
        }
        icons = (Icon) in.readValue(Icon.class.getClassLoader());
        if (in.readByte() == 0x01) {
            tags = new ArrayList<String>();
            in.readList(tags, String.class.getClassLoader());
        } else {
            tags = null;
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
        if (minimumVisibleScale == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(minimumVisibleScale);
        }
        if (locations == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(locations);
        }
        if (entryPoints == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(entryPoints);
        }
        dest.writeValue(icons);
        if (tags == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(tags);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<POI> CREATOR = new Parcelable.Creator<POI>() {
        @Override
        public POI createFromParcel(Parcel in) {
            return new POI(in);
        }

        @Override
        public POI[] newArray(int size) {
            return new POI[size];
        }
    };


    public List<IndoorLocation> locationsAtFloor(Integer floor) {
        List<IndoorLocation> locationsAtFloor = new ArrayList<IndoorLocation>();
        if(locations != null)
        {
            for (IndoorLocation indoorLocation : locations) {
                if (floor.equals(indoorLocation.getFloor())) {
                    locationsAtFloor.add(indoorLocation);
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
        if(entryPoints != null) {
            for (IndoorLocation point : entryPoints) {
                if (floor.equals(point.getFloor())) {
                    entryPointsAtFloor.add(point);
                }
            }

            if (entryPointsAtFloor.size() != 0) {
                return entryPointsAtFloor;
            }

        }
        return null;
    }


    public boolean isValid() {

        //POI is valid if it has categoryId , name , locations & entryPoints
        //nameAr may be null , so use the name as nameAr
        //icons may be null , so we will use default icon of its category
        if( (id != null) && (name != null) && (locations != null) && (entryPoints != null) ) {
            if(nameAr == null){
                nameAr = name;
            }
            return true;
        }else {
            return false;
        }
    }

}