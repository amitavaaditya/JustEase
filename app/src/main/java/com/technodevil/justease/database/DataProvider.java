package com.technodevil.justease.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.technodevil.justease.util.Constants;

/**
 * Class to handle SQLite database interactions and activities
 */
@SuppressWarnings("ConstantConditions")
public class DataProvider extends ContentProvider{

    private DbHelper dbHelper;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.technodevil.justease.provider", "enquiries", Constants.ENQUIRIES_ALL_ROWS);
        uriMatcher.addURI("com.technodevil.justease.provider", "enquiries/#", Constants.ENQUIRIES_SINGLE_ROW);
        uriMatcher.addURI("com.technodevil.justease.provider", "messages", Constants.MESSAGES_ALL_ROWS);
        uriMatcher.addURI("com.technodevil.justease.provider", "messages/#", Constants.MESSAGES_SINGLE_ROW);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch(uriMatcher.match(uri)) {
            case Constants.ENQUIRIES_ALL_ROWS:
                qb.setTables(Constants.TABLE_ENQUIRIES);
                break;

            case Constants.ENQUIRIES_SINGLE_ROW:
                qb.setTables(Constants.TABLE_ENQUIRIES);
                qb.appendWhere(Constants.ENQUIRY_ID + " = " + uri.getLastPathSegment());
                break;
            case Constants.MESSAGES_ALL_ROWS:
                qb.setTables(Constants.TABLE_MESSAGES);
                break;

            case Constants.MESSAGES_SINGLE_ROW:
                qb.setTables(Constants.TABLE_MESSAGES);
                qb.appendWhere(Constants.MESSAGE_ID + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id;
        switch(uriMatcher.match(uri)) {
            case Constants.ENQUIRIES_ALL_ROWS:
                id = db.insertOrThrow(Constants.TABLE_ENQUIRIES, null, values);
                break;

            case Constants.MESSAGES_ALL_ROWS:
                id = db.insertOrThrow(Constants.TABLE_MESSAGES, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri insertUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(insertUri, null);
        return insertUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count;
        switch(uriMatcher.match(uri)) {

            case Constants.ENQUIRIES_ALL_ROWS:
                count = db.delete(Constants.TABLE_ENQUIRIES, selection, selectionArgs);
                break;

            case Constants.ENQUIRIES_SINGLE_ROW:
                count = db.delete(Constants.TABLE_ENQUIRIES, Constants.ENQUIRY_ID + " = ?", new String[]{uri.getLastPathSegment()});
                break;
            case Constants.MESSAGES_ALL_ROWS:
                count = db.delete(Constants.TABLE_MESSAGES, selection, selectionArgs);
                break;

            case Constants.MESSAGES_SINGLE_ROW:
                count = db.delete(Constants.TABLE_MESSAGES, Constants.MESSAGE_ID + " = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count;
        switch(uriMatcher.match(uri)) {
            case Constants.ENQUIRIES_ALL_ROWS:
                count = db.update(Constants.TABLE_ENQUIRIES, values, selection, selectionArgs);
                break;

            case Constants.ENQUIRIES_SINGLE_ROW:
                count = db.update(Constants.TABLE_ENQUIRIES, values, Constants.ENQUIRY_ID + " = ?", new String[]{uri.getLastPathSegment()});
                break;

            case Constants.MESSAGES_ALL_ROWS:
                count = db.update(Constants.TABLE_MESSAGES, values, selection, selectionArgs);
                break;

            case Constants.MESSAGES_SINGLE_ROW:
                count = db.update(Constants.TABLE_MESSAGES, values, Constants.MESSAGE_ID + " = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class DbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "justease.db";
        private static final int DATABASE_VERSION = 1;
        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table enquiries("
                    + Constants.ENQUIRY_ID 	                + " integer primary key, "
                    + Constants.ENQUIRY_CHANNEL             + " varchar(20), "
                    + Constants.ENQUIRY_TITLE               + " varchar(50), "
                    + Constants.ENQUIRY                     + " text, "
                    + Constants.ENQUIRY_DATE_TIME           + " varchar(20), "
                    + Constants.ENQUIRY_NEW_MESSAGE_COUNT   + " integer default 0, "
                    + Constants.ENQUIRY_ACCEPTED            + " varchar(10) default 'pending');");

            db.execSQL("create table messages ("
                    + Constants.MESSAGE_ID 	        + " integer primary key autoincrement, "
                    + Constants.MESSAGE_ENQUIRY_ID  + " varchar(20), "
                    + Constants.MESSAGE             + " text, "
                    + Constants.MESSAGE_DIRECTION   + " varchar(10), "
                    + Constants.MESSAGE_DATE_TIME   + " varchar(20));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
