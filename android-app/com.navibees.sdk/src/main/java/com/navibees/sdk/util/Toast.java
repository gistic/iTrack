package com.navibees.sdk.util;

import android.content.Context;

import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.BuildConfig;

/**
 * Created by hossam on 10/27/15.
 */
public class Toast {

    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;

    public static void makeText(Context context, String text, int length){
        if(BuildConfig.SHOW_TOASTS){
            android.widget.Toast.makeText(context, text, length).show();
        }
    }
}
