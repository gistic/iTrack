package com.navibees.sdk.model.metadata.json;


import com.navibees.sdk.util.CommonUtils;

/**
 * Created by nabilnoaman on 4/16/15.
 */
public class Floor {

    private int index;
    private String name;
    private String nameAr;
    private String basemap;
    private String geodatabase;
    private String networkDataset;
    private String tilePackage;

    public Floor(String name ,String basemap){
        this.name = name ;
        this.basemap = basemap;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public String getNameWRTLang(){
        if(CommonUtils.isArabicLang())
            return nameAr;

        return name;

    }

    public String getBasemap() {
        return basemap;
    }

    public void setBasemap(String basemap) {
        this.basemap = basemap;
    }

    public String getGeodatabase() {
        return geodatabase;
    }

    public void setGeodatabase(String geodatabase) {
        this.geodatabase = geodatabase;
    }

    public String getNetworkDataset() {
        return networkDataset;
    }

    public void setNetworkDataset(String networkDataset) {
        this.networkDataset = networkDataset;
    }


    public String getTilePackage() {
        return tilePackage;
    }

    public void setTilePackage(String tilePackage) {
        this.tilePackage = tilePackage;
    }


}