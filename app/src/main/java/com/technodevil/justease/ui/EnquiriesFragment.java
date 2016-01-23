package com.technodevil.justease.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.technodevil.justease.R;
import com.technodevil.justease.database.MyLoaderCallBacks;
import com.technodevil.justease.service.ServerIntentService;
import com.technodevil.justease.util.Constants;
import com.technodevil.justease.util.OnFragmentChangedListener;

import java.util.Calendar;

/**
 * Fragment for Enquiries
 */
public class EnquiriesFragment extends ListFragment {
    //Debugging
    private static final String TAG = "EnquiriesFragment";
    private static final boolean D = true;

    private String userType;
    public static EnquiryCursorAdapter enquiryCursorAdapter;

    Cursor clickedPosition;
    int clickedEnquiryID;
    int newEnquiryID;

    private TextView nameView;
    private TextView mobileNoView;
    private TextView emailIDView;
    private LinearLayout userInfoLayout;
    private Button button;
    private Button requestUserInfoButton;
    ProgressBar progressBar;

    String enquiryDateTime;

    EnquiryInfoDialog enquiryInfoDialog;
    ResendTask resendTask;
    AcceptEnquiryTask acceptEnquiryTask;
    RequestUserInfoTask requestUserInfoTask;

    OnFragmentChangedListener onFragmentChangedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (D) Log.d(TAG, "onAttach()");
        try {
            onFragmentChangedListener = (OnFragmentChangedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnFragmentChangedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.d(TAG, "onCreate()");
        setRetainInstance(true);
        userType = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Constants.USER_TYPE,"");
        if (userType.equals(Constants.USER))
            onFragmentChangedListener.onFragmentChanged(getResources().getString(R.string.my_enquiries));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onViewCreated()");
        // Adapter to display menu
        enquiryCursorAdapter = new EnquiryCursorAdapter(getActivity(),null);
        setListAdapter(enquiryCursorAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getListView().setBackgroundColor(getResources().getColor(R.color.background,getActivity().getTheme()));
        } else {
            //noinspection deprecation
            getListView().setBackgroundColor(getResources().getColor(R.color.background));
        }
        getListView().setDivider(null);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (D) Log.d(TAG, "onResume()");
        MyLoaderCallBacks myLoaderCallBacks = new MyLoaderCallBacks(getActivity());
        getActivity().getSupportLoaderManager().initLoader(1, null, myLoaderCallBacks);
    }

    @Override
    public void onPause(){
        super.onResume();
        if (D) Log.d(TAG, "onPause()");
    }

    private static class ViewHolder {
        TextView enquiryTitleView;
        TextView enquiryDateTimeView;
        TextView enquiryStatusView;
    }

    public class EnquiryCursorAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public EnquiryCursorAdapter(Context getActivity, Cursor c) {
            super(getActivity, c, 0);
            this.mInflater = (LayoutInflater)getActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override public int getCount() {
            return getCursor() == null ? 0 : super.getCount();
        }

        @Override
        public View newView(Context getActivity, Cursor cursor, ViewGroup parent) {
            View itemLayout = mInflater.inflate(R.layout.main_list_item, parent, false);
            ViewHolder holder = new ViewHolder();
            itemLayout.setTag(holder);
            holder.enquiryTitleView = (TextView) itemLayout.findViewById(R.id.enquiryTitleView);
            holder.enquiryDateTimeView = (TextView) itemLayout.findViewById(R.id.enquiryDateTimeView);
            holder.enquiryStatusView = (TextView) itemLayout.findViewById(R.id.enquiryStatusView);
            return itemLayout;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bindView(View view, Context getActivity, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.enquiryTitleView.setText(cursor.getString(cursor.getColumnIndex(Constants.ENQUIRY_TITLE)));
            if(userType.equals(Constants.USER) &&
                cursor.getString(cursor.getColumnIndex(Constants.ENQUIRY_ACCEPTED)).equals(Constants.ACCEPTED)) {
                    holder.enquiryStatusView.setText(cursor.getInt(cursor.getColumnIndex(Constants.ENQUIRY_NEW_MESSAGE_COUNT))
                            + " new messages");

            } else {
                holder.enquiryStatusView.setText(cursor.getString(cursor.getColumnIndex(Constants.ENQUIRY_ACCEPTED)));
            }
            holder.enquiryDateTimeView.setText(cursor.getString(cursor.getColumnIndex(Constants.ENQUIRY_DATE_TIME)));
            if (D) Log.i(TAG,"adding:" + cursor.getInt(cursor.getColumnIndex(Constants.ENQUIRY_ID)));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (D) Log.d(TAG, "onListItemClick():" + position);
        clickedPosition = getActivity().getContentResolver().query(Constants.CONTENT_URI_ENQUIRIES,
                new String[]{Constants.ENQUIRY_ID, Constants.ENQUIRY_CHANNEL, Constants.ENQUIRY_TITLE,
                        Constants.ENQUIRY, Constants.ENQUIRY_ACCEPTED},
                null, null, Constants.ENQUIRY_ID + " desc");
        assert clickedPosition != null;
        for(int i = 0; i <= position; i++)
            clickedPosition.moveToNext();
        clickedEnquiryID = clickedPosition.getInt(clickedPosition.getColumnIndex(Constants.ENQUIRY_ID));
        if (D) Log.d(TAG, "clickedEnquiryID:" + clickedEnquiryID);

        if(userType.equals(Constants.USER) &&
            clickedPosition.getString(clickedPosition.getColumnIndex(Constants.ENQUIRY_ACCEPTED)).equals(Constants.ACCEPTED)) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(Constants.MESSAGE_ENQUIRY_ID, Integer.toString(clickedEnquiryID));
                startActivity(intent);

        } else {
            enquiryInfoDialog = new EnquiryInfoDialog(getActivity());
            enquiryInfoDialog.createDialog();
            enquiryInfoDialog.show();
        }
    }

    class EnquiryInfoDialog extends Dialog {
        public EnquiryInfoDialog(Context getActivity) {
            super(getActivity);
        }
        public void createDialog() {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_enquiry_info);

            //UI elements
            TextView enquiryChannelView = (TextView) findViewById(R.id.channelView);
            TextView enquiryTitleView = (TextView) findViewById(R.id.titleView);
            TextView enquiryView = (TextView) findViewById(R.id.enquiryView);
            progressBar = (ProgressBar)findViewById(R.id.progressBar);
            enquiryChannelView.setText(clickedPosition.getString(clickedPosition.getColumnIndex(Constants.ENQUIRY_CHANNEL)));
            enquiryTitleView.setText(clickedPosition.getString(clickedPosition.getColumnIndex(Constants.ENQUIRY_TITLE)));
            enquiryView.setText(clickedPosition.getString(clickedPosition.getColumnIndex(Constants.ENQUIRY)));
            String accepted = clickedPosition.getString(clickedPosition.getColumnIndex(Constants.ENQUIRY_ACCEPTED));

            button = (Button) findViewById(R.id.button1);

            if (userType.equals(Constants.USER)) {
                //Only possible if enquiry is pending
                button.setText(R.string.resend_enquiry);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resendTask = new ResendTask();
                        resendTask.execute();
                    }
                });
            } else {
                if (!accepted.equals(Constants.PENDING)) {
                    if(accepted.equals(Constants.MY_CLIENT)) {
                        if (D) Log.i(TAG, "here");
                        button.setTextColor(Color.BLACK);
                        button.setText(R.string.my_client);
                        button.setEnabled(false);
                        Button openChatButton = (Button) findViewById(R.id.button2);
                        openChatButton.setVisibility(View.VISIBLE);
                        openChatButton.setText(R.string.open_chat);
                        openChatButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (D) Log.i(TAG, "onClick():");
                                dismiss();
                                clickedPosition.close();
                                //open chat
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra(Constants.MESSAGE_ENQUIRY_ID, Integer.toString(clickedEnquiryID));
                                startActivity(intent);
                            }
                        });
                    } else {
                        button.setTextColor(Color.BLACK);
                        button.setText(R.string.not_my_client);
                        button.setEnabled(false);
                    }
                } else if(accepted.equals(Constants.PENDING)) {
                    nameView = (TextView)findViewById(R.id.nameView);
                    mobileNoView = (TextView)findViewById(R.id.mobileNoView);
                    emailIDView = (TextView)findViewById(R.id.emailIDView);
                    userInfoLayout = (LinearLayout) findViewById(R.id.userInfoLayout);
                    button.setText(R.string.accept_chat);
                    requestUserInfoButton = (Button) findViewById(R.id.button2);
                    requestUserInfoButton.setText(R.string.request_user_info);
                    requestUserInfoButton.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            acceptEnquiryTask = new AcceptEnquiryTask();
                            acceptEnquiryTask.execute();
                        }
                    });
                    requestUserInfoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestUserInfoTask = new RequestUserInfoTask();
                            requestUserInfoTask.execute();
                        }
                    });
                }
            }
        }
    }

    class ResendTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            button.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ENQUIRY_ID, Integer.toString(clickedEnquiryID));
            Calendar calendar = Calendar.getInstance();
            enquiryDateTime = String.format("%d-%d-%d%3s%02d:%02d%s%s",
                    calendar.get(Calendar.DAY_OF_MONTH),
                    (1 + calendar.get(Calendar.MONTH)),
                    calendar.get(Calendar.YEAR),
                    " ",
                    calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE),
                    " ",
                    (calendar.get(Calendar.AM_PM) == 0) ? "AM" : "PM");
            bundle.putString(Constants.ENQUIRY_DATE_TIME, enquiryDateTime);

            Intent intent = new Intent(getActivity(), ServerIntentService.class);
            intent.setAction(Constants.ACTION_RESEND_ENQUIRY);
            intent.putExtra(Constants.DATA, bundle);
            getActivity().startService(intent);

            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(resendEnquiryBroadcastReceiver, new IntentFilter(Constants.ENQUIRY_RESEND_STATUS));
            if (D) Log.i(TAG, "Resend Enquiry BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                if (D) Log.e(TAG,e.getClass().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            button.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(getActivity())
                    .unregisterReceiver(resendEnquiryBroadcastReceiver);
            if (D) Log.i(TAG, "Resend Enquiry BroadcastReceiver unregistered");
            Toast.makeText(getActivity(), R.string.enquiry_resend_failure, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    class AcceptEnquiryTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            button.setVisibility(View.GONE);
            requestUserInfoButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ENQUIRY_ID, Integer.toString(clickedEnquiryID));
            String username = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Constants.USERNAME,"");
            bundle.putString(Constants.USERNAME, username);

            Intent intent = new Intent(getActivity(), ServerIntentService.class);
            intent.setAction(Constants.ACTION_ACCEPT_ENQUIRY);
            intent.putExtra(Constants.DATA, bundle);
            getActivity().startService(intent);

            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(acceptEnquiryBroadcastReceiver, new IntentFilter(Constants.ACCEPT_ENQUIRY_STATUS));
            if (D) Log.i(TAG, "Accept Enquiry BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                if (D) Log.e(TAG,e.getClass().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            button.setVisibility(View.VISIBLE);
            requestUserInfoButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(getActivity())
                    .unregisterReceiver(acceptEnquiryBroadcastReceiver);
            if (D) Log.i(TAG, "Accept Enquiry BroadcastReceiver unregistered");
            Toast.makeText(getActivity(), R.string.accept_enquiry_failure, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    class RequestUserInfoTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            button.setVisibility(View.GONE);
            requestUserInfoButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ENQUIRY_ID, Integer.toString(clickedEnquiryID));

            Intent intent = new Intent(getActivity(), ServerIntentService.class);
            intent.setAction(Constants.ACTION_REQUEST_USER_INFO);
            intent.putExtra(Constants.DATA, bundle);
            getActivity().startService(intent);

            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(requestUserInfoBroadcastReceiver, new IntentFilter(Constants.REQUEST_USER_INFO_STATUS));
            if (D) Log.i(TAG, "Request User Info BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                if (D) Log.e(TAG,e.getClass().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            button.setVisibility(View.VISIBLE);
            requestUserInfoButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(getActivity())
                    .unregisterReceiver(requestUserInfoBroadcastReceiver);
            if (D) Log.i(TAG, "Request User Info BroadcastReceiver unregistered");
            Toast.makeText(getActivity(), R.string.enquiry_resend_failure, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    BroadcastReceiver resendEnquiryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context getActivity, Intent intent) {
            if (D) Log.d(TAG, "onReceive()");
            if(resendTask != null) resendTask.cancel(false);
            String status = intent.getStringExtra(Constants.ENQUIRY_RESEND_STATUS);
            if(status.equals(Constants.ENQUIRY_RESEND_SUCCESS)) {
                //Insert into DB
                newEnquiryID = Integer.parseInt(intent.getStringExtra(Constants.NEW_ENQUIRY_ID));
                        if (D) Log.d(TAG, "enquiryID:" + Integer.toString(newEnquiryID));
                Toast.makeText(getActivity, R.string.enquiry_resend_success,Toast.LENGTH_SHORT).show();
                enquiryInfoDialog.dismiss();
                updateEnquiry();
            } else {
                button.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity, R.string.enquiry_failure, Toast.LENGTH_SHORT)
                        .show();
            }
            LocalBroadcastManager.getInstance(getActivity)
                    .unregisterReceiver(resendEnquiryBroadcastReceiver);
            if (D) Log.i(TAG, "Resend Enquiry BroadcastReceiver unregistered");
        }
    };

    private void updateEnquiry() {
        ContentValues values = new ContentValues();
        values.put(Constants.ENQUIRY_ID, newEnquiryID);
        values.put(Constants.ENQUIRY_DATE_TIME, enquiryDateTime);
        getActivity().getContentResolver().update(Uri.withAppendedPath(Constants.CONTENT_URI_ENQUIRIES, Integer.toString(clickedEnquiryID)),
                values,
                null,
                null);
    }

    BroadcastReceiver acceptEnquiryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context getActivity, Intent intent) {
            if (D) Log.d(TAG, "onReceive()");
            if(acceptEnquiryTask != null) acceptEnquiryTask.cancel(false);
            String status = intent.getStringExtra(Constants.ACCEPT_ENQUIRY_STATUS);
            if(status.equals(Constants.ACCEPT_ENQUIRY_SUCCESS)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.ENQUIRY_ACCEPTED, Constants.MY_CLIENT);
                getActivity.getContentResolver().update(Uri.withAppendedPath(Constants.CONTENT_URI_ENQUIRIES,
                        Integer.toString(clickedEnquiryID)), contentValues, null, null);
                Toast.makeText(getActivity, R.string.accept_enquiry_success, Toast.LENGTH_SHORT).show();
                enquiryInfoDialog.dismiss();
                createChat();
            } else {
                button.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity, R.string.accept_enquiry_failure, Toast.LENGTH_SHORT)
                        .show();
            }
            LocalBroadcastManager.getInstance(getActivity)
                    .unregisterReceiver(acceptEnquiryBroadcastReceiver);
            if (D) Log.i(TAG, "Accept Enquiry BroadcastReceiver unregistered");
        }
    };

    private void createChat() {

    }

    BroadcastReceiver requestUserInfoBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context getActivity, Intent intent) {
            if (D) Log.d(TAG, "onReceive()");
            if(requestUserInfoTask != null) requestUserInfoTask.cancel(false);
            String status = intent.getStringExtra(Constants.REQUEST_USER_INFO_STATUS);
            if(status.equals(Constants.REQUEST_USER_INFO_SUCCESS)) {
                Bundle data = intent.getBundleExtra(Constants.DATA);
                String name = data.getString(Constants.FIRST_NAME) + " " + data.getString(Constants.LAST_NAME);
                String mobileNo = data.getString(Constants.MOBILE_NO);
                String emailID = data.getString(Constants.EMAIL_ID);
                userInfoLayout.setVisibility(View.VISIBLE);
                nameView.setText(name);
                mobileNoView.setText(mobileNo);
                emailIDView.setText(emailID);
                button.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                button.setVisibility(View.VISIBLE);
                requestUserInfoButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity, R.string.accept_enquiry_failure, Toast.LENGTH_SHORT)
                        .show();
            }
            LocalBroadcastManager.getInstance(getActivity)
                    .unregisterReceiver(requestUserInfoBroadcastReceiver);
            if (D) Log.i(TAG, "Request User Info BroadcastReceiver unregistered");
        }
    };
}
