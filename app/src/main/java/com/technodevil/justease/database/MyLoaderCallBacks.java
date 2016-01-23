package com.technodevil.justease.database;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.technodevil.justease.ui.ChatActivity;
import com.technodevil.justease.ui.EnquiriesFragment;
import com.technodevil.justease.ui.MyCasesFragment;
import com.technodevil.justease.util.Constants;

/**
 * Loader Callbacks
 */
public class MyLoaderCallBacks implements LoaderManager.LoaderCallbacks<Cursor> {
    //Debugging
    private static final String TAG = "MyLoaderCallBacks";
    private static final boolean D = true;

    Context context;

    public MyLoaderCallBacks(Context context){
        this.context = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (D) Log.d(TAG, "onCreateLoader():" + id);
        switch (id) {
            case 0:
                return new CursorLoader(context, Constants.CONTENT_URI_ENQUIRIES,
                        new String[]{Constants.ENQUIRY_ID, Constants.ENQUIRY_TITLE, Constants.ENQUIRY_DATE_TIME,
                                Constants.ENQUIRY_NEW_MESSAGE_COUNT, Constants.ENQUIRY_ACCEPTED},
                        Constants.ENQUIRY_ACCEPTED + "=?",
                        new String[]{Constants.MY_CLIENT},
                        Constants.ENQUIRY_ID + " desc");
            case 1:
                return new CursorLoader(context, Constants.CONTENT_URI_ENQUIRIES,
                        new String[]{Constants.ENQUIRY_ID, Constants.ENQUIRY_TITLE, Constants.ENQUIRY_DATE_TIME,
                                Constants.ENQUIRY_NEW_MESSAGE_COUNT, Constants.ENQUIRY_ACCEPTED},
                        null,
                        null,
                        Constants.ENQUIRY_ID + " desc");
            case 2:
                return new CursorLoader(context, Constants.CONTENT_URI_MESSAGES,
                        new String[]{Constants.MESSAGE_ID, Constants.MESSAGE, Constants.MESSAGE_DATE_TIME, Constants.MESSAGE_DIRECTION},
                        Constants.MESSAGE_ENQUIRY_ID + "=?",
                        new String[]{ChatActivity.enquiryID},
                        Constants.MESSAGE_ID + " asc");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (D) Log.d(TAG, "onLoadFinished():" + loader.getId());
        switch (loader.getId()) {
            case 0:
                MyCasesFragment.myCasesCursorAdapter.swapCursor(data);
                break;
            case 1:
                EnquiriesFragment.enquiryCursorAdapter.swapCursor(data);
                break;
            case 2:
                ChatActivity.messageCursorAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        if (D) Log.d(TAG, "onLoaderReset():" + loader.getId());
        switch (loader.getId()) {
            case 0:
                MyCasesFragment.myCasesCursorAdapter.swapCursor(null);
                break;
            case 1:
                EnquiriesFragment.enquiryCursorAdapter.swapCursor(null);
                break;
            case 2:
                ChatActivity.messageCursorAdapter.swapCursor(null);
                break;
        }
    }

}
