/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.technodevil.justease.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.technodevil.justease.R;
import com.technodevil.justease.util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerIntentService extends IntentService {

    private static final String TAG = "ServerIntentService";

    public ServerIntentService() {
        super(TAG);
    }

    SharedPreferences sharedPreferences;

    /**
     * Handle the intent type
     *
     * @param intent Intent containing request type
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String action = intent.getAction();
        Bundle data = intent.getBundleExtra(Constants.DATA);
        if (Constants.D) Log.d(TAG, "onHandleIntent():" + action);
        switch (action) {
            case Constants.ACTION_LOGIN:
                login(data);
                break;
            case Constants.ACTION_FORGOT_PASSWORD:
                forgotPassword(data);
                break;
            case Constants.ACTION_REGISTER:
                register(data);
                break;
            case Constants.ACTION_UPDATE:
                update(data);
                break;
            case Constants.ACTION_TOKEN_REFRESH:
                updateToken();
                break;
            case Constants.ACTION_SEND_MESSAGE:
                sendMessage(data);
                break;
            case Constants.ACTION_UPDATE_PASSWORD:
                updatePassword(data);
                break;
            case Constants.ACTION_ENQUIRY:
                submitEnquiry(data);
                break;
            case Constants.ACTION_RESEND_ENQUIRY:
                resendEnquiry(data);
                break;
            case Constants.ACTION_ACCEPT_ENQUIRY:
                acceptEnquiry(data);
                break;
            case Constants.ACTION_REQUEST_USER_INFO:
                requestUserInfo(data);
                break;
        }
    }

    /**
     * Login with the entered details
     *
     * @param data Bundle containing data
     */
    private void login(Bundle data) {
        if (Constants.D) Log.d(TAG, "login()");
        String serverURL = Constants.SERVER_ADDRESS + "login.php";
        String token = registerGCM();
        if(token != null) {
            sharedPreferences.edit().putString(Constants.REGISTRATION_KEY, token).apply();
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Request to mail password to registered username
     *
     * @param data Bundle containing data
     */
    private void forgotPassword(Bundle data) {
        if (Constants.D) Log.d(TAG, "forgotPassword()");
        String serverURL = Constants.SERVER_ADDRESS + "forgotpassword.php";
        String token = registerGCM();
        if(token != null) {
            sharedPreferences.edit().putString(Constants.REGISTRATION_KEY, token).apply();
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Register with the entered details
     *
     * @param data Bundle containing data
     */
    private void register(Bundle data) {
        if (Constants.D) Log.d(TAG, "register():");
        String serverURL = Constants.SERVER_ADDRESS + "register.php";
        String token = registerGCM();
        if(token != null) {
            sharedPreferences.edit().putString(Constants.REGISTRATION_KEY, token).apply();
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Update with entered details
     *
     * @param data Bundle containing data
     */
    private void update(Bundle data) {
        if (Constants.D) Log.d(TAG, "update():");
        String serverURL = Constants.SERVER_ADDRESS + "update.php";
        String token = sharedPreferences.getString(Constants.REGISTRATION_KEY, null);
        if(token != null) {
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Called by MyInstanceIDListenerService to update GCM token
     */
    private void updateToken() {
        if (Constants.D) Log.d(TAG, "updateToken()");
        String token = registerGCM();
        String savedToken = sharedPreferences.getString(Constants.REGISTRATION_KEY, "");
        if(token!= null && !token.equals(savedToken)) {
            sharedPreferences.edit().putString(Constants.REGISTRATION_KEY, token).apply();
            String serverURL = Constants.SERVER_ADDRESS + "key.php";
            Bundle data = new Bundle();
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Send message with entered data
     *
     * @param data Bundle containing data
     */
    private void sendMessage(Bundle data) {
        if (Constants.D) Log.d(TAG, "sendMessage():");
        String serverURL = Constants.SERVER_ADDRESS + "sendmessage.php";
        String token = sharedPreferences.getString(Constants.REGISTRATION_KEY, null);
        if(token != null) {
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Update to new entered password
     *
     * @param data Bundle containing data
     */
    private void updatePassword(Bundle data) {
        if (Constants.D) Log.d(TAG, "updatePassword():");
        String serverURL = Constants.SERVER_ADDRESS + "updatepassword.php";
        String token = sharedPreferences.getString(Constants.REGISTRATION_KEY, null);
        if(token != null) {
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }

    }

    /**
     * Submit new enquiry with entered data
     *
     * @param data Bundle containing data
     */
    private void submitEnquiry(Bundle data) {
        if (Constants.D) Log.d(TAG, "submitEnquiry():" + data);
        String serverURL = Constants.SERVER_ADDRESS + "enquiry.php";
        String token = sharedPreferences.getString(Constants.REGISTRATION_KEY, null);
        if(token != null) {
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Resend enquiry for entered data
     *
     * @param data Bundle containing data
     */
    private void resendEnquiry(Bundle data) {
        if (Constants.D) Log.d(TAG, "resendEnquiry():" + data);
        String serverURL = Constants.SERVER_ADDRESS + "resendenquiry.php";
        String token = sharedPreferences.getString(Constants.REGISTRATION_KEY,null);
        if(token != null) {
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Accept enquiry for selected enquiryID
     *
     * @param data Bundle containing data
     */
    private void acceptEnquiry(Bundle data) {
        if (Constants.D) Log.d(TAG, "acceptEnquiry():" + data);
        String serverURL = Constants.SERVER_ADDRESS + "acceptenquiry.php";
        String token = sharedPreferences.getString(Constants.REGISTRATION_KEY,null);
        if(token != null) {
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    private void requestUserInfo(Bundle data) {
        if (Constants.D) Log.d(TAG, "requestUserInfo():" + data);
        String serverURL = Constants.SERVER_ADDRESS + "requestuserinfo.php";
        String token = sharedPreferences.getString(Constants.REGISTRATION_KEY,null);
        if(token != null) {
            data.putString(Constants.REGISTRATION_KEY, token);
            sendToServer(serverURL, data);
        }
    }

    /**
     * Register for a new GCM registration ID
     *
     * @return new registration ID, null if failed
     */
    private String registerGCM() {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            if (Constants.D) Log.i(TAG, "GCM Registration Token: " + token);
            return token;
        } catch (IOException ex) {
            if (Constants.D) Log.e(TAG, "Failed to get token");
            if (Constants.D) Log.e(TAG, ex.getClass().toString());
        }
        return null;
    }

    /**
     * Send data to server
     *
     * @param serverURL URL of the server
     * @param data Bundle containing all key-value data
     */
    private void sendToServer(String serverURL, Bundle data) {
        String postData = "";
        for(String key : data.keySet()) {
            postData += (key + "=" + data.getString(key) + '&');
        }
        postData = postData.substring(0,postData.length() - 1);
        if (Constants.D) Log.i(TAG,"sendToServer():" + postData);
        byte[] bytes = postData.getBytes();

        URL url = null;
        try {
            url = new URL(serverURL);
        } catch (MalformedURLException e) {
            if (Constants.D) Log.e(TAG,e.toString());
        }

        HttpURLConnection connection = null;
        try {
            assert url != null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setRequestMethod("POST");
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            // handle the response
            int status = connection.getResponseCode();
            if (Constants.D) Log.d(TAG, "Status:" + Integer.toString(status));
            // If response is not success
            if (status != 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String response = "";
                String line;
                while ((line=reader.readLine()) != null) {
                    response+=line;
                }
                if (Constants.D) Log.e(TAG,response);
                throw new IOException("Post failed with error code " + status);
            }
        } catch (Exception e) {
            if (Constants.D) Log.e(TAG,e.toString());
        } finally {
            if(connection != null)
                connection.disconnect();
        }
    }
}
