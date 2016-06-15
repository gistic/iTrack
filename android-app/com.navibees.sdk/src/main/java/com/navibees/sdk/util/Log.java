package com.navibees.sdk.util;

import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.BuildConfig;

/**
 * Created by nabilnoaman on 10/20/15.
 */
public class Log {


    public static void i(String tag, String string) {
        if (BuildConfig.SHOW_LOGS) android.util.Log.i(tag, string);
    }
    public static void e(String tag, String string) {
        if (BuildConfig.SHOW_LOGS) android.util.Log.e(tag, string);
    }
    public static void d(String tag, String string) {
        if (BuildConfig.SHOW_LOGS) android.util.Log.d(tag, string);
    }
    public static void v(String tag, String string) {
        if (BuildConfig.SHOW_LOGS) android.util.Log.v(tag, string);
    }
    public static void w(String tag, String string) {
        if (BuildConfig.SHOW_LOGS) android.util.Log.w(tag, string);
    }
}