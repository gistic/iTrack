package com.navibees.sdk.model.license;

import android.content.Context;
import android.util.Base64;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.model.metadata.json.Plan;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by nabilnoaman on 1/3/16.
 */
final public class LicenseManager {

    private boolean isAccessTokenValid = false;
    private boolean isLicenseExpired = true;

    public void verify(Context context , NaviBeesFeature naviBeesFeature) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        if(context != null) {
            if(!isAccessTokenValid){
                if (isAccessTokenValid(context)) {
                    if (!isLicenseExpired(context)) {
                        if (naviBeesFeature != null) {
                            checkFeature(context, naviBeesFeature);
                        }
                    } else {
//                        throw new NaviBeesLicenseExpireException("License expired");
                    }
                } else {
   //                 throw new NaviBeesLicenseNotAuthorithedException("In Valid Access Token");
                }
            }else if(isLicenseExpired){
//                throw new NaviBeesLicenseExpireException("License expired");
            }else if (naviBeesFeature != null) {
                checkFeature(context, naviBeesFeature);
            }

        }
    }

    private void checkFeature(Context context , NaviBeesFeature naviBeesFeature) throws NaviBeesLicenseNotAuthorithedException {
            if(context == null){
    //            throw new NaviBeesLicenseNotAuthorithedException("In Valid context = null");
            }

            Plan plan = AppManager.getInstance().getMetaDataManager().getApplicationConfiguration(context).getPlan();
            if(plan != null ){
                int featureSet = plan.getFeatureStr();
                if(((featureSet >> naviBeesFeature.ordinal()) & 1 ) != 1){
   //                 throw new NaviBeesLicenseNotAuthorithedException("NaviBeesFeature:"+ naviBeesFeature +" not included in License");
                }

            }else {
  //              throw new NaviBeesLicenseNotAuthorithedException("In Valid Plan");
            }

    }

    private boolean isAccessTokenValid(Context context){
        String accessToken = AppManager.getInstance().getMetaDataManager().getApplicationConfiguration(context).getAccessToken();
        Plan plan = AppManager.getInstance().getMetaDataManager().getApplicationConfiguration(context).getPlan();
        if(accessToken == null || plan == null){
            return false;
        }


        String calculatedAccessToken = calculateAccessToken(context);
        if(calculatedAccessToken != null && accessToken.equals(calculatedAccessToken)) {
            isAccessTokenValid = true;
            return true;
        }

        isAccessTokenValid = false;
        return false;

    }

    private String calculateAccessToken(Context context) {
        Plan plan = AppManager.getInstance().getMetaDataManager().getApplicationConfiguration(context).getPlan();
        String salt = getSalt();
        if(salt == null)return null;
        StringBuffer input = new StringBuffer();
        input.append(AppManager.getInstance().getMetaDataManager().getApplicationConfiguration(context).getApplicationId());
        input.append(salt);
        input.append(plan.getFeatureStr());
        input.append(salt);
        input.append(plan.getStartDate());
        input.append(salt);
        input.append(plan.getDuration());
        return bytesToHex(getHash(input.toString(), null));
    }

    private String getSalt() {
        byte[] bytes = new byte[]{73 , 48 , 53 , 104 , 86 , 109 , 107 , 106};
        try {
            byte[] data = Base64.decode(bytes, Base64.DEFAULT);
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
           // e.printStackTrace();
        }
        return null;
    }

    private boolean isLicenseExpired(Context context){
        Plan plan = AppManager.getInstance().getMetaDataManager().getApplicationConfiguration(context).getPlan();
        Date expireDate = new Date(plan.getStartDate() + plan.getDuration() * 24L * 60L * 60L * 1000L);
        //Check if current date after expire date
        isLicenseExpired = isDateAfter(expireDate , new Date());
        return isLicenseExpired;
    }

    private byte[] getHash(String message, byte[] salt) {
        if(message != null) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.reset();
                if (salt != null) {
                    digest.update(salt);
                }
                return digest.digest(message.getBytes("UTF-8"));
            }catch (NoSuchAlgorithmException e1){
                //e1.printStackTrace();
            }catch (UnsupportedEncodingException e2){
                //e2.printStackTrace();
            }
        }

        return null;
    }


    private String bytesToHex(byte[] bytes) {
        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<bytes.length;i++) {
            String hex=Integer.toHexString(0xff & bytes[i]);
            if(hex.length()==1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private boolean isDateAfter(Date date1 , Date date2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal2.after(cal1);
    }


}
