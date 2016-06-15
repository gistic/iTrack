package com.navibees.sdk.model.metadata.json;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.navibees.sdk.util.CommonUtils;

/**
 * Created by nabilnoaman on 4/13/15.
 */

public class POICategory implements Parcelable {

    private Integer id;
    private String name;
    private String nameAr;
    @SerializedName("icons")
    private Icon icons;
    private boolean filterEnable;//show POI on map if this value true

    public boolean isFilterEnable() {
        return filterEnable;
    }

    public void setFilterEnable(boolean filterEnable) {
        this.filterEnable = filterEnable;
    }

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


    @Override
    public String toString() {
        return "id:"+id+" , name:"+name +", nameAr:"+nameAr;
    }

    protected POICategory(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        name = in.readString();
        nameAr = in.readString();
        icons = (Icon) in.readValue(Icon.class.getClassLoader());
        filterEnable = in.readByte() != 0x00;
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
        dest.writeString(name);
        dest.writeString(nameAr);
        dest.writeValue(icons);
        dest.writeByte((byte) (filterEnable ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<POICategory> CREATOR = new Parcelable.Creator<POICategory>() {
        @Override
        public POICategory createFromParcel(Parcel in) {
            return new POICategory(in);
        }

        @Override
        public POICategory[] newArray(int size) {
            return new POICategory[size];
        }
    };



    public boolean isValid() {

        //Category is valid if it has id & name
        //nameAr may be null , so use the name as nameAr
        //icons may be null , so we will use default icon in drawable

        if( (id != null) && (name != null)) {
            if(nameAr == null){
                nameAr = name;
            }
            return true;
        }else {
            return false;
        }
    }

}