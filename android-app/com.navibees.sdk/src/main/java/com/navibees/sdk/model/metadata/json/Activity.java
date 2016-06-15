package com.navibees.sdk.model.metadata.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.navibees.sdk.util.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hossam on 10/12/15.
 */
public class Activity implements Parcelable{

    public int id;
    public String name;
    public String nameAr;
    public int poiId;
    public String startDate;
    public String endDate;

    public String owner;
    public String ownerAr;

    public String description;
    public String descriptionAr;

    //used internally not by parse
    public int groupId;


    public boolean isHappeningNow(){
        try {
            Date now = Calendar.getInstance().getTime();

            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date startDate = parser.parse(this.startDate);
            Date endDate = parser.parse(this.endDate);

            return now.after(startDate) && now.before(endDate);
        }catch (Exception e){

        }

        return false;
    }

    public String getNameWRTLang(){
        if(CommonUtils.isArabicLang() && nameAr != null)
            return nameAr;

        return name;
    }

    public String getDescriptionWRTLang(){
        if(CommonUtils.isArabicLang() && descriptionAr != null)
            return descriptionAr;

        return description;
    }

    public String getOwnerWRTLang(){
        if(CommonUtils.isArabicLang() && ownerAr != null)
            return ownerAr;

        return owner;
    }

    protected Activity(Parcel in) {
        id = in.readInt();
        name = in.readString();
        nameAr = in.readString();
        poiId = in.readInt();
        startDate = in.readString();
        endDate = in.readString();
        owner = in.readString();
        ownerAr = in.readString();
        description = in.readString();
        descriptionAr = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(nameAr);
        dest.writeInt(poiId);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(owner);
        dest.writeString(ownerAr);
        dest.writeString(description);
        dest.writeString(descriptionAr);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Activity> CREATOR = new Parcelable.Creator<Activity>() {
        @Override
        public Activity createFromParcel(Parcel in) {
            return new Activity(in);
        }

        @Override
        public Activity[] newArray(int size) {
            return new Activity[size];
        }
    };
}
