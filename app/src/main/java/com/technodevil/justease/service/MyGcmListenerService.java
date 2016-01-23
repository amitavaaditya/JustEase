package com.technodevil.justease.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.technodevil.justease.ui.AdministratorActivity;
import com.technodevil.justease.ui.ChatActivity;
import com.technodevil.justease.util.Constants;
import com.technodevil.justease.R;
import com.technodevil.justease.ui.UserActivity;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final boolean D = true;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        if (D) Log.d(TAG, "From: " + from);
        if (D) Log.d(TAG, "Message: " + message);
        if (D) Log.d(TAG, "Status: " + data.getString(Constants.STATUS));

        Intent intent;
        assert message != null;
        switch (message) {
            case Constants.ACTION_SEND_MESSAGE:
                intent = new Intent(Constants.SEND_MESSAGE_STATUS);
                if(data.getString(Constants.STATUS).equals(Constants.NEW))
                    saveNewMessage(data);
                else {
                    if (data.getString(Constants.STATUS).equals(Constants.SUCCESS))
                        intent.putExtra(Constants.SEND_MESSAGE_STATUS, Constants.SEND_MESSAGE_SUCCESS);
                    else
                        intent.putExtra(Constants.SEND_MESSAGE_STATUS, Constants.SEND_MESSAGE_FAILURE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                break;
            case Constants.ACTION_LOGIN:
                intent = new Intent(Constants.LOGIN_STATUS);
                if(data.getString(Constants.STATUS).equals(Constants.SUCCESS)) {
                    intent.putExtra(Constants.LOGIN_STATUS, Constants.LOGIN_SUCCESS);
                    intent.putExtra(Constants.DATA, data);
                    if (D) Log.d(TAG, "Type: " + data.getString(Constants.USER_TYPE));
                }
                else
                    intent.putExtra(Constants.LOGIN_STATUS, Constants.LOGIN_FAILURE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case Constants.ACTION_REGISTER:
                intent = new Intent(Constants.REGISTER_STATUS);
                if(data.getString(Constants.STATUS).equals(Constants.SUCCESS))
                    intent.putExtra(Constants.REGISTER_STATUS,Constants.REGISTER_SUCCESS);
                else
                    intent.putExtra(Constants.REGISTER_STATUS, Constants.REGISTER_FAILURE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case Constants.ACTION_UPDATE:
                intent = new Intent(Constants.UPDATE_STATUS);
                if(data.getString(Constants.STATUS).equals(Constants.SUCCESS))
                    intent.putExtra(Constants.UPDATE_STATUS,Constants.UPDATE_SUCCESS);
                else
                    intent.putExtra(Constants.UPDATE_STATUS, Constants.UPDATE_FAILURE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case Constants.ACTION_UPDATE_PASSWORD:
                intent = new Intent(Constants.PASSWORD_CHANGE_STATUS);
                if(data.getString(Constants.STATUS).equals(Constants.SUCCESS))
                    intent.putExtra(Constants.PASSWORD_CHANGE_STATUS,Constants.PASSWORD_CHANGE_SUCCESS);
                else
                    intent.putExtra(Constants.PASSWORD_CHANGE_STATUS, Constants.PASSWORD_CHANGE_FAILURE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
            case Constants.ACTION_ENQUIRY:
                intent = new Intent(Constants.ENQUIRY_STATUS);
                if(data.getString(Constants.STATUS).equals(Constants.NEW))
                    saveNewEnquiry(data);
                else {
                    if (data.getString(Constants.STATUS).equals(Constants.SUCCESS)) {
                        intent.putExtra(Constants.ENQUIRY_STATUS, Constants.ENQUIRY_SUCCESS);
                        if (D) Log.i("enquiry_id", data.getString(Constants.ENQUIRY_ID));
                    }
                    else
                        intent.putExtra(Constants.ENQUIRY_STATUS, Constants.ENQUIRY_FAILURE);
                    intent.putExtra(Constants.ENQUIRY_ID, data.getString(Constants.ENQUIRY_ID));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                break;
            case Constants.ACTION_RESEND_ENQUIRY:
                intent = new Intent(Constants.ENQUIRY_RESEND_STATUS);
                if (data.getString(Constants.STATUS).equals(Constants.NEW)){
                    updateEnquiry(data);
                }else {
                    if (data.getString(Constants.STATUS).equals(Constants.SUCCESS)) {
                        intent.putExtra(Constants.ENQUIRY_RESEND_STATUS, Constants.ENQUIRY_RESEND_SUCCESS);
                        intent.putExtra(Constants.NEW_ENQUIRY_ID, data.getString(Constants.NEW_ENQUIRY_ID));
                    }
                    else
                        intent.putExtra(Constants.ENQUIRY_RESEND_STATUS, Constants.ENQUIRY_RESEND_FAILURE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                break;
            case Constants.ACTION_ACCEPT_ENQUIRY:
                intent = new Intent(Constants.ACCEPT_ENQUIRY_STATUS);
                if (data.getString(Constants.STATUS).equals(Constants.NEW)){
                    acceptedEnquiry(data);
                }else {
                    if (data.getString(Constants.STATUS).equals(Constants.SUCCESS))
                        intent.putExtra(Constants.ACCEPT_ENQUIRY_STATUS, Constants.ACCEPT_ENQUIRY_SUCCESS);
                    else
                        intent.putExtra(Constants.ACCEPT_ENQUIRY_STATUS, Constants.ACCEPT_ENQUIRY_FAILURE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                break;
            case Constants.ACTION_REQUEST_USER_INFO:
                intent = new Intent(Constants.REQUEST_USER_INFO_STATUS);
                if(data.getString(Constants.STATUS).equals(Constants.SUCCESS)) {
                    intent.putExtra(Constants.REQUEST_USER_INFO_STATUS, Constants.REQUEST_USER_INFO_SUCCESS);
                    intent.putExtra(Constants.DATA, data);
                }
                else
                    intent.putExtra(Constants.REQUEST_USER_INFO_STATUS, Constants.REQUEST_USER_INFO_FAILURE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                break;
        }
    }

    private void saveNewMessage(Bundle data) {
        String enquiryID = data.getString(Constants.MESSAGE_ENQUIRY_ID);
        String message = data.getString(Constants.MESSAGE);
        String messageDateTime = data.getString(Constants.MESSAGE_DATE_TIME);
        String messageDirection = Constants.INCOMING;
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.MESSAGE_ENQUIRY_ID, enquiryID);
        contentValues.put(Constants.MESSAGE, message);
        contentValues.put(Constants.MESSAGE_DATE_TIME, messageDateTime);
        contentValues.put(Constants.MESSAGE_DIRECTION, messageDirection);
        if (D) Log.d(TAG, "saveNewMessage():" + messageDirection);
        getApplicationContext().getContentResolver().insert(Constants.CONTENT_URI_MESSAGES, contentValues);

        if(!ChatActivity.running) {
            Cursor cursor = getApplicationContext().getContentResolver().query(Constants.CONTENT_URI_ENQUIRIES,
                    new String[]{Constants.ENQUIRY_ID, Constants.ENQUIRY, Constants.ENQUIRY_NEW_MESSAGE_COUNT},
                    Constants.ENQUIRY_ID + "=?", new String[]{enquiryID}, null);
            assert cursor != null;
            cursor.moveToFirst();
            String enquiry = cursor.getString(cursor.getColumnIndex(Constants.ENQUIRY));
            int newMessageCount = cursor.getInt(cursor.getColumnIndex(Constants.ENQUIRY_NEW_MESSAGE_COUNT));
            ContentValues countValue = new ContentValues();
            countValue.put(Constants.ENQUIRY_NEW_MESSAGE_COUNT, ++newMessageCount);
            getApplicationContext().getContentResolver().update(Constants.CONTENT_URI_ENQUIRIES, countValue, Constants.ENQUIRY_ID + "=?",
                    new String[]{enquiryID});
            cursor.close();

            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(Constants.MESSAGE_ENQUIRY_ID, enquiryID);
            intent.putExtra(Constants.NOTIFICATION_TITLE, enquiry);
            intent.putExtra(Constants.NOTIFICATION_BODY, message);
            sendNotification(intent);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void saveNewEnquiry(Bundle data) {
        if (D) Log.d(TAG, "new message");
        ContentValues values = new ContentValues();
        if (D) Log.i("enquiry_id:", data.getString(Constants.ENQUIRY_ID));
        values.put(Constants.ENQUIRY_ID, Integer.parseInt(data.getString(Constants.ENQUIRY_ID)));
        values.put(Constants.ENQUIRY_CHANNEL, data.getString(Constants.ENQUIRY_CHANNEL));
        values.put(Constants.ENQUIRY_TITLE, data.getString(Constants.ENQUIRY_TITLE));
        values.put(Constants.ENQUIRY, data.getString(Constants.ENQUIRY));
        values.put(Constants.ENQUIRY_DATE_TIME, data.getString(Constants.ENQUIRY_DATE_TIME));
        getApplicationContext().getContentResolver().insert(Constants.CONTENT_URI_ENQUIRIES, values);

        Intent intent = new Intent(this, AdministratorActivity.class);
        intent.putExtra(Constants.NOTIFICATION_TITLE, getString(R.string.new_enquiry));
        intent.putExtra(Constants.NOTIFICATION_BODY, data.getString(Constants.ENQUIRY_TITLE));
        sendNotification(intent);
    }

    private void updateEnquiry(Bundle data) {
        if (D) Log.d(TAG, "update enquiry");
        ContentValues values = new ContentValues();
        String enquiryID = data.getString(Constants.ENQUIRY_ID);
        String newEnquiryID = data.getString(Constants.NEW_ENQUIRY_ID);
        if (D) Log.i("enquiry_id:", newEnquiryID);
        values.put(Constants.ENQUIRY_ID, newEnquiryID);
        values.put(Constants.ENQUIRY_DATE_TIME, data.getString(Constants.ENQUIRY_DATE_TIME));
        getApplicationContext().getContentResolver().update(Uri.withAppendedPath(Constants.CONTENT_URI_ENQUIRIES, enquiryID),
                values, null, null);
        Intent intent = new Intent(this, AdministratorActivity.class);
        intent.putExtra(Constants.NOTIFICATION_TITLE, getString(R.string.enquiry_updated));
        intent.putExtra(Constants.NOTIFICATION_BODY, "");
        sendNotification(intent);
    }

    private void acceptedEnquiry(Bundle data) {
        if (D) Log.d(TAG, "accept enquiry");
        ContentValues values = new ContentValues();
        if (D) Log.i("enquiry_id:", data.getString(Constants.ENQUIRY_ID));
        String enquiryID = data.getString(Constants.ENQUIRY_ID);
        values.put(Constants.ENQUIRY_ACCEPTED, Constants.ACCEPTED);
        getApplicationContext().getContentResolver().update(Uri.withAppendedPath(Constants.CONTENT_URI_ENQUIRIES, enquiryID),
                values, null, null);
        if(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.USER_TYPE,"")
                .equals(Constants.USER)) {
            Intent intent = new Intent(this, UserActivity.class);
            intent.putExtra(Constants.NOTIFICATION_TITLE, getString(R.string.enquiry_accepted));
            intent.putExtra(Constants.NOTIFICATION_BODY, "");
            intent.putExtra(Constants.ACTION_ACCEPT_ENQUIRY, true);
            sendNotification(intent);
        }
    }

    /**
     * Create and show a notification whenever needed
     *
     * @param intent Which activity to start and data needed
     */
    private void sendNotification(Intent intent) {
        if (D) Log.d(TAG, "sendNotification()");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(intent.getStringExtra(Constants.NOTIFICATION_TITLE))
                .setContentText(intent.getStringExtra(Constants.NOTIFICATION_BODY))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int lastNotificationID = sharedPreferences.getInt(Constants.NOTIFICATION_ID, 0);
        sharedPreferences.edit().putInt(Constants.NOTIFICATION_ID, lastNotificationID + 1).apply();

        notificationManager.notify(lastNotificationID + 1 , notificationBuilder.build());
    }
}
