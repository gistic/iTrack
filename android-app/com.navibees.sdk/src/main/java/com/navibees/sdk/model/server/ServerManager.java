package com.navibees.sdk.model.server;

import android.content.Context;

import com.navibees.sdk.model.metadata.DataChangeListener;

/**
 * Created by nabilnoaman on 7/5/15.
 */
public interface ServerManager {
    public void getApplicationConfiguration(final Context context , final DataChangeListener listener);
    public void getBuildingMetaData(Context context , final DataChangeListener listener);
}
