package com.bme.solon;

import android.os.AsyncTask;

/**
 * {@link com.bme.solon.SplashActivity} should cancel this {@link AsyncTask} when the final dialog is dismissed.
 */
public class GetPermissionsAsync extends AsyncTask<Void, Void, Void> {

    /**
     * Continuously loop until cancelled.
     */
    @Override
    protected Void doInBackground(Void... v) {
        while (!isCancelled()) {}
        return null;
    }


}
