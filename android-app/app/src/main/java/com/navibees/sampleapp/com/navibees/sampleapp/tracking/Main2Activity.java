package com.navibees.sampleapp.com.navibees.sampleapp.tracking;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;

import com.navibees.sampleapp.R;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.activity.MapActivity;

public class Main2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main2);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", null)
                .setTitle("Thanks")
                .setMessage("Thanks for participating in this experiment. Please leave this app installed for at least one week.")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //AppManager.getInstance().recycle();
                                Main2Activity thisActivity = Main2Activity.this;
                                Intent intent = new Intent(thisActivity, Service_class.class);
                                PendingIntent pintent = PendingIntent.getService(thisActivity, 0, intent,
                                        0);
                                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, 30000,
                                        pintent);

                                finish();
                            }
                        })
                .show();
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.recycle();
    }
}
