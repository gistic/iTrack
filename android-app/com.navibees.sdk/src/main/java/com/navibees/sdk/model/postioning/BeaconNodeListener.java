package com.navibees.sdk.model.postioning;

import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.model.metadata.BeaconNode;

import java.util.List;

/**
 * Created by nabilnoaman on 4/18/15.
 */
public interface BeaconNodeListener {
    public void beaconNodeCallback(List<BeaconNode> beaconNodes) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException;
}
