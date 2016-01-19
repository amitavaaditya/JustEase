package com.technodevil.justease;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {
    //Debugging
    private static final String TAG = "ChatActivity";

    public static boolean running;
    public static String enquiryID;
    private ArrayList<Integer> messageIDs;

    private EditText messageEditText;
    private Button sendButton;
    private ProgressBar messageProgressBar;

    private String message;
    private String messageDateTime;

    public static MessageCursorAdapter messageCursorAdapter;

    private SendMessageTask sendMessageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        Intent intent = getIntent();
        enquiryID = intent.getStringExtra(Constants.MESSAGE_ENQUIRY_ID);

        messageEditText = (EditText) findViewById(R.id.messageEditText);
        sendButton = (Button) findViewById(R.id.sendButton);
        messageProgressBar = (ProgressBar) findViewById(R.id.messageProgressBar);
        ListView list = (ListView) findViewById(R.id.list);

        messageCursorAdapter = new MessageCursorAdapter(this, null);
        list.setAdapter(messageCursorAdapter);
        list.setDivider(null);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = messageEditText.getText().toString();
                if (!message.equals("")) {
                    sendMessageTask = new SendMessageTask();
                    sendMessageTask.execute();
                }
            }
        });

        Cursor cursor = getContentResolver().query(Constants.CONTENT_URI_ENQUIRIES, new String[]{Constants.ENQUIRY_TITLE},
                Constants.ENQUIRY_ID + "=?", new String[]{enquiryID}, null);
        assert cursor != null;
        cursor.moveToFirst();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setSubtitle("Enquiry: " + cursor.getString(cursor.getColumnIndex(Constants.ENQUIRY_TITLE)));
        cursor.close();

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(ChatActivity.this)
                        .setMessage(R.string.delete_message)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e(TAG, "onItemLongClick():" + position);
                                getContentResolver().delete(Constants.CONTENT_URI_MESSAGES,
                                        Constants.MESSAGE_ID + "=?",
                                        new String[]{Integer.toString(messageIDs.get(messageIDs.size()-1-position))});
                                messageIDs.remove(messageIDs.size()-1-position);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return false;
            }
        });
        messageIDs = new ArrayList<>();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume()");
        MyLoaderCallBacks myLoaderCallBacks = new MyLoaderCallBacks(this);
        getSupportLoaderManager().initLoader(2, null, myLoaderCallBacks);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.ENQUIRY_NEW_MESSAGE_COUNT, 0);
        getContentResolver().update(Constants.CONTENT_URI_ENQUIRIES, contentValues,
                Constants.ENQUIRY_ID + "=?", new String[]{enquiryID});
        running = true;
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause()");
        running = false;
    }

    private static class ViewHolder {
        TextView messageView;
        TextView messageDateTimeView;
    }

    public class MessageCursorAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public MessageCursorAdapter(Context context, Cursor c) {
            super(context, c, 0);
            this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override public int getCount() {
            return getCursor() == null ? 0 : super.getCount();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Cursor cursor = (Cursor) getItem(position);
            return cursor.getString(cursor.getColumnIndex(Constants.MESSAGE_DIRECTION))
                    .equals(Constants.INCOMING) ? 0 : 1;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View itemLayout;
            Log.d(TAG, "newView():" + getItemViewType(cursor.getPosition()));
            if(getItemViewType(cursor.getPosition()) == 0)
                itemLayout = mInflater.inflate(R.layout.message_incoming_item, parent, false);
            else
                itemLayout = mInflater.inflate(R.layout.message_outgoing_item, parent, false);

            Log.d(TAG, "newView():" + cursor.getString(cursor.getColumnIndex(Constants.MESSAGE)));
            messageIDs.add(cursor.getInt(cursor.getColumnIndex(Constants.MESSAGE_ID)));

            ViewHolder holder = new ViewHolder();
            itemLayout.setTag(holder);
            holder.messageView = (TextView) itemLayout.findViewById(R.id.messageView);
            holder.messageDateTimeView = (TextView) itemLayout.findViewById(R.id.messageDateTimeView);
            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.messageView.setText(cursor.getString(cursor.getColumnIndex(Constants.MESSAGE)));
            holder.messageDateTimeView.setText(cursor.getString(cursor.getColumnIndex(Constants.MESSAGE_DATE_TIME)));
            Log.i(TAG, "adding:" + cursor.getInt(cursor.getColumnIndex(Constants.ENQUIRY_ID)));
            Log.i(TAG, "array:" + messageIDs);
        }
    }

    class SendMessageTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            sendButton.setVisibility(View.GONE);
            messageProgressBar.setVisibility(View.VISIBLE);
            Toast.makeText(ChatActivity.this, R.string.sending_message, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.MESSAGE_ENQUIRY_ID, enquiryID);
            bundle.putString(Constants.USER_TYPE,
                    PreferenceManager.getDefaultSharedPreferences(ChatActivity.this).getString(Constants.USER_TYPE,""));
            bundle.putString(Constants.MESSAGE, message);
            Calendar calendar = Calendar.getInstance();
            messageDateTime = String.format("%d-%d-%d%3s%02d:%02d%s%s",
                            calendar.get(Calendar.DAY_OF_MONTH),
                            (1 + calendar.get(Calendar.MONTH)),
                            calendar.get(Calendar.YEAR),
                            " ",
                            calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR),
                            calendar.get(Calendar.MINUTE),
                            " ",
                            (calendar.get(Calendar.AM_PM) == 0) ? "AM" : "PM");
            bundle.putString(Constants.MESSAGE_DATE_TIME, messageDateTime);

            Intent intent = new Intent(ChatActivity.this, ServerIntentService.class);
            intent.setAction(Constants.ACTION_SEND_MESSAGE);
            intent.putExtra(Constants.DATA, bundle);
            ChatActivity.this.startService(intent);

            LocalBroadcastManager.getInstance(ChatActivity.this)
                    .registerReceiver(messageBroadcastReceiver, new IntentFilter(Constants.SEND_MESSAGE_STATUS));
            Log.i(TAG, "Message BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                Log.e(TAG,e.getClass().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            sendButton.setVisibility(View.VISIBLE);
            messageProgressBar.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(ChatActivity.this)
                    .unregisterReceiver(messageBroadcastReceiver);
            Log.i(TAG, "Message BroadcastReceiver unregistered");
            Toast.makeText(ChatActivity.this, R.string.message_send_failure, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    BroadcastReceiver messageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive()");
            if(sendMessageTask != null) sendMessageTask.cancel(false);
            String status = intent.getStringExtra(Constants.SEND_MESSAGE_STATUS);
            if(status.equals(Constants.SEND_MESSAGE_SUCCESS)) {
                messageEditText.setText("");
                sendButton.setVisibility(View.VISIBLE);
                messageProgressBar.setVisibility(View.GONE);

                ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.MESSAGE_ENQUIRY_ID, Integer.parseInt(enquiryID));
                contentValues.put(Constants.MESSAGE, message);
                contentValues.put(Constants.MESSAGE_DATE_TIME, messageDateTime);
                contentValues.put(Constants.MESSAGE_DIRECTION, Constants.OUTGOING);
                context.getContentResolver().insert(Constants.CONTENT_URI_MESSAGES, contentValues);
            } else {
                sendButton.setVisibility(View.VISIBLE);
                messageProgressBar.setVisibility(View.GONE);
                Toast.makeText(context, R.string.message_send_failure, Toast.LENGTH_SHORT)
                        .show();
            }
            LocalBroadcastManager.getInstance(context)
                    .unregisterReceiver(messageBroadcastReceiver);
            Log.i(TAG, "Message BroadcastReceiver unregistered");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected():" + item.getTitle());
        switch (item.getItemId()) {
            case R.id.logout:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.logout_confirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(ChatActivity.this).edit();
                                editor.putString(Constants.USERNAME,"");
                                editor.putString(Constants.PASSWORD,"");
                                editor.putString(Constants.USER_TYPE,"");
                                editor.apply();
                                startActivity(new Intent(ChatActivity.this, MainActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            case R.id.my_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            case R.id.change_wait_time:
                final EditText waitTimeEditText = new EditText(this);
                waitTimeEditText.setText(Integer.toString(Constants.BACKOFF_TIME));
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.wait_time_changed))
                        .setView(waitTimeEditText)
                        .setPositiveButton(Constants.CONFIRM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Constants.BACKOFF_TIME = Integer.parseInt(waitTimeEditText.getText().toString());
                                } catch (Exception e) {
                                    Log.e(TAG, e.toString());
                                }
                                Toast.makeText(getApplicationContext(), getString(R.string.wait_time_changed), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }).show();
                return true;
            case R.id.change_server_url:
                final EditText serverEditText = new EditText(this);
                serverEditText.setText(Constants.SERVER_ADDRESS);
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.change_server_url))
                        .setView(serverEditText)
                        .setPositiveButton(Constants.CONFIRM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Constants.SERVER_ADDRESS = serverEditText.getText().toString();
                                Toast.makeText(getApplicationContext(), getString(R.string.url_changed), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }).show();
                return true;
            case R.id.about_us:
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.logo)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.about_us_message)
                        .show();
                return true;
            case R.id.exit:
                finish();
                return true;
        }
        return false;
    }
}
