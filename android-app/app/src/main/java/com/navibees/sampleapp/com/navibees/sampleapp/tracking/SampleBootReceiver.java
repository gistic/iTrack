package com.navibees.sampleapp.com.navibees.sampleapp.tracking;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted. This receiver is set to be disabled (android:enabled="false") in the
 * application's manifest file. When the user sets the alarm, the receiver is enabled.
 * When the user cancels the alarm, the receiver is disabled, so that rebooting the
 * device will not trigger this receiver.
 */
// BEGIN_INCLUDE(autostart)
public class SampleBootReceiver extends BroadcastReceiver {
//    SampleAlarmReceiver alarm = new SampleAlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            Log.i("Service_class", "Boot received");
        {

            Intent alarmIntent = new Intent(context, Service_class.class);
            PendingIntent pintent = PendingIntent.getService(context, 0, alarmIntent,
                    0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10000, 30000,
                    pintent);
        }
    }
}
//END_INCLUDE(autostart)
