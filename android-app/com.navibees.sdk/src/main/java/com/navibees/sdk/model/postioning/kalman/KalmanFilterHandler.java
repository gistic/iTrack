package com.navibees.sdk.model.postioning.kalman;

    /*
(The MIT License.)

Copyright (c) 2009, Kevin Lacker.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/*
 * This is a Java port of original C code by Kevin Lacker.
 * https://github.com/lacker/ikalman
 *
 * Ported by Andrey Novikov, 2013
 */


/* To use these functions:

 1. Start with a KalmanFilter created by alloc_filter_velocity2d.
 2. At fixed intervals, call update_velocity2d with the lat/long.
 3. At any time, to get an estimate for the current position,
 bearing, or speed, use the functions:
 get_lat_long
 get_bearing
 get_mph
 */


import android.content.Context;

import com.navibees.sdk.AppManager;
import com.navibees.sdk.model.license.NaviBeesFeature;
import com.navibees.sdk.model.license.NaviBeesLicenseExpireException;
import com.navibees.sdk.model.license.NaviBeesLicenseNotAuthorithedException;
import com.navibees.sdk.util.Log;

//https://github.com/andreynovikov/GeoTrackFilter
final public class KalmanFilterHandler {

    private KalmanFilter kalmanFilter;
    private double secondsPerTimestep = 0.1;
    private double noise = 1.0;

    private Long previousTime = null;


	/*
	 * Create a GPS filter that only tracks two dimensions of position and
	 * velocity. The inherent assumption is that changes in velocity are
	 * randomly distributed around 0. Noise is a parameter you can use to alter
	 * the expected noise. 1.0 is the original, and the higher it is, the more a
	 * path will be "smoothed". Free with free_filter after using.
	 */

    public KalmanFilterHandler(Context context) throws NaviBeesLicenseNotAuthorithedException, NaviBeesLicenseExpireException {
        AppManager.getInstance().getLicenseManager().verify(context , NaviBeesFeature.Positioning);

        /*
		 * The state model has four dimensions: x, y, x', y' Each time step we
		 * can only observe position, not velocity, so the observation vector
		 * has only two dimensions.
		 */

        kalmanFilter = new KalmanFilter(4, 2 , context);
        init();
    }

    private void init(){
        kalmanFilter.state_transition.set_identity_matrix();
        set_seconds_per_timestep(secondsPerTimestep);

		/* We observe (x, y) in each time step */
        kalmanFilter.observation_model.set_matrix(1.0, 0.0, 0.0, 0.0,
                0.0, 1.0, 0.0, 0.0);

        /* The start position is totally unknown, so give a high variance */
        kalmanFilter.state_estimate.set_matrix(0.0, 0.0, 0.0, 0.0);
        kalmanFilter.estimate_covariance.set_identity_matrix();
        double trillion = 1000.0 * 1000.0 * 1000.0 * 1000.0;
        kalmanFilter.estimate_covariance.scale_matrix(trillion);

		/* Noise in our observation */
        kalmanFilter.observation_noise_covariance.set_matrix(0.3 , 0.0, 0.0, 0.3);

		/* Noise in the world. */
        double pos = 0.001;
        kalmanFilter.process_noise_covariance.set_matrix(
                pos, 0.0, 0.0, 0.0,
                0.0, pos, 0.0, 0.0,
                0.0, 0.0, pos, 0.0,
                0.0, 0.0, 0.0, pos);

    }


    /* Set the seconds per timestep in the velocity2d model. */
	/*
	 * The position units are in thousandths of latitude and longitude. The
	 * velocity units are in thousandths of position units per second.
	 *
	 * So if there is one second per timestep, a velocity of 1 will change the
	 * lat or long by 1 after a million timesteps.
	 *
	 * Thus a typical position is hundreds of thousands of units. A typical
	 * velocity is maybe ten.
	 */
    void set_seconds_per_timestep(double seconds_per_timestep) {
        kalmanFilter.state_transition.data[0][2] = seconds_per_timestep;
        kalmanFilter.state_transition.data[1][3] = seconds_per_timestep;
    }

    /* Update the velocity2d model with new gps data. */
    public void update_velocity2d(double lat, double lon) {

        long currentTime = System.currentTimeMillis();

        if(this.previousTime == null) {
            Log.i("PositionManager", "Current updateVelocity2d 1");
            set_seconds_per_timestep(0.0);
        }
        else {
            Log.i("PositionManager", "Current updateVelocity2d 2 , (currentTime - this.previousTime)/1000 = "+ (currentTime - this.previousTime)/1000);
            set_seconds_per_timestep((currentTime - this.previousTime)/1000);
        }


        this.previousTime = currentTime;

        kalmanFilter.observation.set_matrix(lat , lon);
        kalmanFilter.update();
    }

    /* Extract a lat long from a velocity2d Kalman filter. */
    public double[] get_lat_long() {
        double[] latlon = new double[2];
        latlon[0] = kalmanFilter.state_estimate.data[0][0];
        latlon[1] = kalmanFilter.state_estimate.data[1][0];
        return latlon;
    }

    public void resetKalmanFilter() {
        this.init();
    }


}
