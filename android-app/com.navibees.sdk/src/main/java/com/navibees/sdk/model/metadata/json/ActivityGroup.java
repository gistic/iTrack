package com.navibees.sdk.model.metadata.json;

import com.navibees.sdk.util.CommonUtils;

import java.util.List;

/**
 * Created by hossam on 10/12/15.
 */
public class ActivityGroup {

    public int id;
    public String name;
    public String nameAr;
    public List<Activity> activities;

    public String getNameWRTLang(){
        if(CommonUtils.isArabicLang() && nameAr != null)
            return nameAr;

        return name;
    }
}
