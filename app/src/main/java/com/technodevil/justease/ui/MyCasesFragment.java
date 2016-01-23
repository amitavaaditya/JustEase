package com.technodevil.justease.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.technodevil.justease.R;
import com.technodevil.justease.database.MyLoaderCallBacks;
import com.technodevil.justease.util.Constants;
import com.technodevil.justease.util.OnFragmentChangedListener;

/**
 *
 */
public class MyCasesFragment extends ListFragment {
    //Debugging
    private static final String TAG = "MyCasesFragment";
    private static final boolean D = true;

    public static MyCasesCursorAdapter myCasesCursorAdapter;

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
        onFragmentChangedListener.onFragmentChanged(getResources().getString(R.string.my_cases));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onViewCreated()");
        // Adapter to display menu
        myCasesCursorAdapter = new MyCasesCursorAdapter(getActivity(),null);
        setListAdapter(myCasesCursorAdapter);
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
        getActivity().getSupportLoaderManager().initLoader(0, null, myLoaderCallBacks);
    }

    @Override
    public void onPause(){
        super.onResume();
        if (D) Log.d(TAG, "onPause()");
    }

    private static class ViewHolder {
        TextView enquiryTitleView;
        TextView enquiryDateTimeView;
        TextView enquiryNewMessageCountView;
    }

    public class MyCasesCursorAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public MyCasesCursorAdapter(Context getActivity, Cursor c) {
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
            holder.enquiryNewMessageCountView = (TextView) itemLayout.findViewById(R.id.enquiryStatusView);
            return itemLayout;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bindView(View view, Context getActivity, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.enquiryTitleView.setText(cursor.getString(cursor.getColumnIndex(Constants.ENQUIRY_TITLE)));
            int newMessages = cursor.getInt(cursor.getColumnIndex(Constants.ENQUIRY_NEW_MESSAGE_COUNT));
            holder.enquiryNewMessageCountView.setText(newMessages + " new messages");
            holder.enquiryDateTimeView.setText(cursor.getString(cursor.getColumnIndex(Constants.ENQUIRY_DATE_TIME)));
            if (D) Log.i(TAG,"adding:" + cursor.getInt(cursor.getColumnIndex(Constants.ENQUIRY_ID)));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (D) Log.d(TAG, "onListItemClick():" + position);
        Cursor clickedPosition = getActivity().getContentResolver().query(Constants.CONTENT_URI_ENQUIRIES,
                new String[]{Constants.ENQUIRY_ID, Constants.ENQUIRY_ACCEPTED},
                Constants.ENQUIRY_ACCEPTED + "=?",
                new String[]{Constants.MY_CLIENT},
                Constants.ENQUIRY_ID + " desc");
        assert clickedPosition != null;
        for(int i = 0; i <= position; i++)
            clickedPosition.moveToNext();
        String clickedEnquiryID = Integer.toString(clickedPosition
                .getInt(clickedPosition.getColumnIndex(Constants.ENQUIRY_ID)));
        if (D) Log.d(TAG, "clickedEnquiryID:" + clickedEnquiryID);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.MESSAGE_ENQUIRY_ID, clickedEnquiryID);
        startActivity(intent);
        clickedPosition.close();
    }

}
