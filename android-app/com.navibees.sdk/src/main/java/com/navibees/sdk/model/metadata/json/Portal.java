package com.navibees.sdk.model.metadata.json;

import android.os.Parcel;

import com.navibees.sdk.util.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nabilnoaman on 4/16/15.
 */
public class Portal extends POI {

    public final static String[] PORTAL_TYPE = {"elevator" , "stair" , "escalator"};
    public final static String[] PORTAL_TYPE_ARABIC = {"المصعد", "السلم", "السلم المتحرك"};
    public final static Map<String , Integer> COST = new HashMap<String , Integer>();

    static
    {
        COST.put(PORTAL_TYPE[0] , 0);
        COST.put(PORTAL_TYPE[1] , 5);
        COST.put(PORTAL_TYPE[2] , 1);
    };


    private String type;
    public int indexInList;//not exist in json , used to speed up routing

    protected Portal(Parcel in) {
        super(in);
        type = in.readString();
        indexInList = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(type);
        dest.writeInt(indexInList);
    }



    public String getType() {
        return type;
    }

    public String getTypeWRTLang() {

        if(CommonUtils.isArabicLang() && type != null){
            for(int i=0; i<PORTAL_TYPE.length; i++){
                if(PORTAL_TYPE[i].equalsIgnoreCase(type))//ignore capital letter in index 0 E (Elevator , Escalator),S (Stair)
                    return PORTAL_TYPE_ARABIC[i];
            }
        }else if(!type.startsWith("e")) {//convert first char to be capital in English
            type = "E".concat(type.substring(1));
        }else if(!type.startsWith("s")){
            type = "S".concat(type.substring(1));
        }

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
