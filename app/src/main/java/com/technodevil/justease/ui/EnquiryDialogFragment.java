package com.technodevil.justease.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.technodevil.justease.R;
import com.technodevil.justease.service.ServerIntentService;
import com.technodevil.justease.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


/**
 *
 */
public class EnquiryDialogFragment extends DialogFragment {
    //Debugging
    public static final String TAG = "EnquiryDialogFragment";

    //List items
    String[] items ={
            "---Please select an enquiry channel---",
            "Civil",
            "Criminal"
    };

    private TextInputLayout enquiryTitleTextLayout;
    private TextInputLayout enquiryTextLayout;
    private Button submitButton;
    private ProgressBar enquiryProgressBar;
    private EnquiryTask enquiryTask;

    //Data variables
    int enquiryID;
    private String channel;
    private String title;
    private String enquiry;
    private String enquiryDateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Constants.D) Log.d(TAG,"onCreate()");
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        // Inflate the layout for this dialog
        return inflater.inflate(R.layout.dialog_new_enquiry, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog()");
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if(Constants.D) Log.d(TAG, "onViewCreated()");

        Spinner channelSpinner = (Spinner) view.findViewById(R.id.channelSpinner);
        channelSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>(Arrays.asList(items))));
        channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) channel = null;
                else channel = items[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                channel = null;
            }
        });
        enquiryTitleTextLayout = (TextInputLayout) view.findViewById(R.id.enquiryTitleTextLayout);
        enquiryTextLayout = (TextInputLayout) view.findViewById(R.id.enquiryTextLayout);
        enquiryProgressBar = (ProgressBar) view.findViewById(R.id.enquiryProgressBar);
        submitButton= (Button) view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View v) {
                enquiryTitleTextLayout.setErrorEnabled(false);
                enquiryTextLayout.setErrorEnabled(false);
                if (channel == null) {
                    Toast.makeText(getActivity(), R.string.channel_not_selected, Toast.LENGTH_SHORT).show();
                } else {
                    title = enquiryTitleTextLayout.getEditText().getText().toString();
                    if (title.equals("")) {
                        enquiryTitleTextLayout.setErrorEnabled(true);
                        enquiryTitleTextLayout.setError(getResources().getString(R.string.blank_enquiry_title));
                    } else {
                        enquiry = enquiryTextLayout.getEditText().getText().toString();
                        if (enquiry.equals("")) {
                            enquiryTextLayout.setErrorEnabled(true);
                            enquiryTextLayout.setError(getResources().getString(R.string.blank_enquiry));
                        } else {
                            enquiryTask = new EnquiryTask();
                            enquiryTask.execute();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Constants.D) Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(Constants.D) Log.d(TAG, "onPause()");
        if(enquiryTask != null) enquiryTask.cancel(false);

        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(enquiryBroadcastReceiver);
        Log.i(TAG, "Enquiry BroadcastReceiver unregistered");
    }

    @Override
    public void onDestroyView() {
        if(Constants.D) Log.d(TAG, "onDestroyView()");
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    class EnquiryTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            submitButton.setVisibility(View.GONE);
            enquiryProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.ENQUIRY_CHANNEL, channel);
            bundle.putString(Constants.ENQUIRY_TITLE, title);
            bundle.putString(Constants.ENQUIRY, enquiry);
            Calendar calendar = Calendar.getInstance();
            enquiryDateTime = calendar.get(Calendar.DAY_OF_MONTH)
                    + "-" + (1 + calendar.get(Calendar.MONTH))
                    + "-" + calendar.get(Calendar.YEAR)
                    + "   " + calendar.get(Calendar.HOUR)
                    + ":" + calendar.get(Calendar.MINUTE)
                    + " " + ((calendar.get(Calendar.AM_PM) == 0) ? "AM" : "PM");
            bundle.putString(Constants.ENQUIRY_DATE_TIME, enquiryDateTime);
            String username = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(Constants.USERNAME,"");
            bundle.putString(Constants.USERNAME,username);

            Intent intent = new Intent(getActivity(), ServerIntentService.class);
            intent.setAction(Constants.ACTION_ENQUIRY);
            intent.putExtra(Constants.DATA, bundle);
            getActivity().startService(intent);

            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(enquiryBroadcastReceiver, new IntentFilter(Constants.ENQUIRY_STATUS));
            Log.i(TAG, "Local BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                Log.e(TAG,e.getClass().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            submitButton.setVisibility(View.VISIBLE);
            enquiryProgressBar.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(getActivity())
                    .unregisterReceiver(enquiryBroadcastReceiver);
            Log.i(TAG, "Enquiry BroadcastReceiver unregistered");
            Toast.makeText(getActivity(), R.string.enquiry_failure, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    BroadcastReceiver enquiryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive()");
            if(enquiryTask != null) enquiryTask.cancel(false);
            String status = intent.getStringExtra(Constants.ENQUIRY_STATUS);
            if(status.equals(Constants.ENQUIRY_SUCCESS)) {
                //Insert into DB
                enquiryID = Integer.parseInt(intent.getStringExtra(Constants.ENQUIRY_ID));
                Log.d(TAG,"enquiryID:" + Integer.toString(enquiryID));
                Log.d(TAG, "onReceive()");
                Toast.makeText(context, R.string.enquiry_success,Toast.LENGTH_SHORT).show();
                createEnquiry();
                dismiss();
            } else {
                submitButton.setVisibility(View.VISIBLE);
                enquiryProgressBar.setVisibility(View.GONE);
                Toast.makeText(context, R.string.enquiry_failure, Toast.LENGTH_SHORT)
                        .show();
            }
            LocalBroadcastManager.getInstance(context)
                    .unregisterReceiver(enquiryBroadcastReceiver);
            Log.i(TAG, "Enquiry BroadcastReceiver unregistered");
        }
    };

    private void createEnquiry() {
        ContentValues values = new ContentValues();
        values.put(Constants.ENQUIRY_ID, enquiryID);
        values.put(Constants.ENQUIRY_CHANNEL, channel);
        values.put(Constants.ENQUIRY_TITLE, title);
        values.put(Constants.ENQUIRY, enquiry);
        values.put(Constants.ENQUIRY_DATE_TIME, enquiryDateTime);
        getActivity().getContentResolver().insert(Constants.CONTENT_URI_ENQUIRIES, values);
    }
}
