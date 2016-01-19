package com.technodevil.justease;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


/**
 *
 */
public class ChannelsFragment extends ListFragment {
    //Debugging
    private static final String TAG = "ChannelsFragment";
    private static final boolean D = true;

    //List items
    String[] items ={
            "Civil",
            "Criminal"
    };

    //UI elements
    private Dialog dialog;
    private Button submitButton;
    private ProgressBar enquiryProgressBar;
    private EnquiryTask enquiryTask;

    //Data variables
    int enquiryID;
    private String channel;
    private String title;
    private String enquiry;
    private String enquiryDateTime;

    private ViewPager viewPager;

    public static ChannelsFragment newInstance(ViewPager viewPager) {
        ChannelsFragment channelsFragment = new ChannelsFragment();
        channelsFragment.viewPager = viewPager;
        return channelsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.d(TAG,"onCreate()");
        //Enable options menu
        //setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(D) Log.d(TAG, "onViewCreated()");
        // Adapter to display menu
        ChannelsAdapter adapter = new ChannelsAdapter(getActivity(),R.layout.channels_list_item, items);
        setListAdapter(adapter);
        getListView().setDivider(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getListView().setBackgroundColor(getResources().getColor(R.color.background,getActivity().getTheme()));
        } else {
            //noinspection deprecation
            getListView().setBackgroundColor(getResources().getColor(R.color.background));
        }
        getListView().setDivider(null);
    }

    private static class ChannelsHolder {
        ImageView icon;
        TextView listItem;
    }

    private class ChannelsAdapter extends ArrayAdapter<String> {
        private final int layoutResourceID;
        private final String[] channels;

        public ChannelsAdapter(Context context, int layoutResourceID, String[] channels) {
            super(context, layoutResourceID, channels);
            this.layoutResourceID = layoutResourceID;
            this.channels = channels;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ChannelsHolder holder;
            if (view == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                view = inflater.inflate(layoutResourceID, parent, false);
                holder = new ChannelsHolder();
                holder.icon = (ImageView)view.findViewById(R.id.icon);
                holder.listItem = (TextView)view.findViewById(R.id.listItem);
                view.setTag(holder);
            } else {
                holder = (ChannelsHolder) view.getTag();
            }
            String channel = channels[position];
            holder.listItem.setText(channel);
            holder.icon.setImageResource(R.drawable.ball);
            return view;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(D) Log.d(TAG, "onPause()");
        if(enquiryTask != null) enquiryTask.cancel(false);

        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(enquiryBroadcastReceiver);
        Log.i(TAG, "Enquiry BroadcastReceiver unregistered");
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.chat_enquiry);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        TextView channelView = (TextView) dialog.findViewById(R.id.channelView);
        channelView.setText(items[position]);
        enquiryProgressBar = (ProgressBar)dialog.findViewById(R.id.enquiryProgressBar);
        submitButton = (Button)dialog.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                channel = items[position];
                title = ((EditText) dialog.findViewById(R.id.enquiryTitleEditText))
                        .getText().toString();
                enquiry = ((EditText) dialog.findViewById(R.id.enquiryEditText))
                        .getText().toString();
                enquiryTask = new EnquiryTask();
                enquiryTask.execute();
            }
        });
        dialog.show();
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
                dialog.dismiss();
                createEnquiry();
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
        viewPager.setCurrentItem(Constants.USER_FRAGMENT_ENQUIRIES);
    }
}
