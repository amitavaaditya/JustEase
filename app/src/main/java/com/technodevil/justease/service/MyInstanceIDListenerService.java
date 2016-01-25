package com.technodevil.justease.service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.technodevil.justease.util.Constants;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        if (Constants.D) Log.i(TAG,"onTokenRefresh()");
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, ServerIntentService.class);
        intent.setAction(Constants.ACTION_TOKEN_REFRESH);
        startService(intent);
    }
}
