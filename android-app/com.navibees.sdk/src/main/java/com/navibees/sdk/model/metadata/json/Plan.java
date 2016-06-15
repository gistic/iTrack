package com.navibees.sdk.model.metadata.json;

/**
 * Created by nabilnoaman on 1/3/16.
 */
public class Plan {

    private int featureStr;
    private long startDate;
    private long duration;

    public int getFeatureStr() {
        return featureStr;
    }

    public void setFeatureStr(int featureStr) {
        this.featureStr = featureStr;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}
