package com.technodevil.justease;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {
    //Debugging
    public static final String TAG = "ProfileActivity";
    private static final boolean D = true;
    //UI elements
    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout mobileNoLayout;
    private TextInputLayout emailIDLayout;
    private Button editButton;
    private Button changePasswordButton;
    private Button savePasswordButton;
    private ProgressBar updateProgressBar;
    private ProgressBar passwordChangeProgressBar;
    private Dialog dialog;

    private String firstName;
    private String lastName;
    private String mobileNo;
    private String emailID;
    private String password;
    private String newPassword;

    private boolean editing = false;

    private UpdateTask updateTask;
    private UpdatePasswordTask updatePasswordTask;

    SharedPreferences sharedPreferences;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_profile);
        setSupportActionBar((Toolbar) findViewById(R.id.toolBar));

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        firstNameLayout = (TextInputLayout) findViewById(R.id.firstNameLayout);
        lastNameLayout = (TextInputLayout) findViewById(R.id.lastNameLayout);
        mobileNoLayout = (TextInputLayout) findViewById(R.id.mobileNumberLayout);
        emailIDLayout = (TextInputLayout) findViewById(R.id.emailIDLayout);
        editButton = (Button) findViewById(R.id.editButton);
        changePasswordButton = (Button) findViewById(R.id.changePasswordButton);
        updateProgressBar = (ProgressBar) findViewById(R.id.updateProgressBar);
        emailID = sharedPreferences.getString(Constants.USERNAME,"");
        firstName = sharedPreferences.getString(Constants.FIRST_NAME,"");
        lastName = sharedPreferences.getString(Constants.LAST_NAME,"");
        mobileNo = sharedPreferences.getString(Constants.MOBILE_NO,"");

        firstNameLayout.getEditText().setText(firstName);
        lastNameLayout.getEditText().setText(lastName);
        emailIDLayout.getEditText().setText(emailID);
        mobileNoLayout.getEditText().setText(mobileNo);

        editButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View v) {
                if (D) Log.d(TAG, "register()");
                if (!editing) {
                    editing = true;
                    firstNameLayout.getEditText().setEnabled(true);
                    lastNameLayout.getEditText().setEnabled(true);
                    mobileNoLayout.getEditText().setEnabled(true);
                    editButton.setText(R.string.save_changes);
                } else {
                    firstName = firstNameLayout.getEditText().getText().toString();
                    lastName = lastNameLayout.getEditText().getText().toString();
                    mobileNo = mobileNoLayout.getEditText().getText().toString();
                    emailID = emailIDLayout.getEditText().getText().toString();
                    updateTask = new UpdateTask();
                    updateTask.execute();
                }
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(ProfileActivity.this);
                dialog.setContentView(R.layout.dialog_change_password);
                passwordChangeProgressBar = (ProgressBar) dialog.findViewById(R.id.updatePasswordProgressBar);
                savePasswordButton = (Button) dialog.findViewById(R.id.savePasswordButton);
                savePasswordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        password = sharedPreferences.getString(Constants.PASSWORD, "");
                        TextInputLayout oldPasswordLayout = (TextInputLayout) dialog.findViewById(R.id.oldPasswordLayout);
                        String oldPassword = oldPasswordLayout.getEditText().getText().toString();
                        if (!oldPassword.equals(password)) {
                            oldPasswordLayout.setErrorEnabled(true);
                            oldPasswordLayout.setError(getString(R.string.incorrect_password));
                        } else {
                            TextInputLayout newPasswordLayout = (TextInputLayout) dialog.findViewById(R.id.setPasswordLayout);
                            TextInputLayout newPasswordAgainLayout = (TextInputLayout) dialog
                                    .findViewById(R.id.setPasswordAgainLayout);
                            newPassword = newPasswordLayout.getEditText().getText().toString();
                            String newPasswordAgain = newPasswordAgainLayout.getEditText().getText().toString();
                            if (!newPassword.equals(newPasswordAgain)) {
                                oldPasswordLayout.setErrorEnabled(false);
                                newPasswordLayout.setErrorEnabled(true);
                                newPasswordLayout.setError(getString(R.string.password_mismatch));
                                newPasswordAgainLayout.setErrorEnabled(true);
                                newPasswordAgainLayout.setError(getString(R.string.password_mismatch));
                            } else {
                                updatePasswordTask = new UpdatePasswordTask();
                                updatePasswordTask.execute();
                            }
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState()");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.d(TAG, "onViewCreated()");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(D) Log.d(TAG, "onPause()");
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(updateBroadcastReceiver);
        Log.i(TAG, "Update BroadcastReceiver unregistered");
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(passwordBroadcastReceiver);
        Log.i(TAG, "Password BroadcastReceiver unregistered");
    }

    class UpdateTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            editButton.setVisibility(View.GONE);
            changePasswordButton.setVisibility(View.GONE);
            updateProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.EMAIL_ID, emailID);
            bundle.putString(Constants.FIRST_NAME, firstName);
            bundle.putString(Constants.LAST_NAME, lastName);
            bundle.putString(Constants.MOBILE_NO, mobileNo);

            Intent intent = new Intent(ProfileActivity.this, ServerIntentService.class);
            intent.setAction(Constants.ACTION_UPDATE);
            intent.putExtra(Constants.DATA, bundle);
            ProfileActivity.this.startService(intent);

            LocalBroadcastManager.getInstance(ProfileActivity.this)
                    .registerReceiver(updateBroadcastReceiver, new IntentFilter(Constants.UPDATE_STATUS));
            Log.i(TAG, "Update BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getClass().toString());
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            editButton.setVisibility(View.VISIBLE);
            changePasswordButton.setVisibility(View.VISIBLE);
            updateProgressBar.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(ProfileActivity.this)
                    .unregisterReceiver(updateBroadcastReceiver);
            Log.i(TAG, "Update BroadcastReceiver unregistered");
            Toast.makeText(ProfileActivity.this, R.string.update_failure, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    class UpdatePasswordTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            savePasswordButton.setVisibility(View.GONE);
            passwordChangeProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String username = sharedPreferences.getString(Constants.USERNAME, "");
            Bundle bundle = new Bundle();
            bundle.putString(Constants.USERNAME, username);
            bundle.putString(Constants.PASSWORD, newPassword);

            Intent intent = new Intent(ProfileActivity.this, ServerIntentService.class);
            intent.setAction(Constants.ACTION_UPDATE_PASSWORD);
            intent.putExtra(Constants.DATA, bundle);
            ProfileActivity.this.startService(intent);

            LocalBroadcastManager.getInstance(ProfileActivity.this)
                    .registerReceiver(passwordBroadcastReceiver, new IntentFilter(Constants.PASSWORD_CHANGE_STATUS));
            Log.i(TAG, "Password BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                Log.e(TAG,e.getClass().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            savePasswordButton.setVisibility(View.VISIBLE);
            passwordChangeProgressBar.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(ProfileActivity.this)
                    .unregisterReceiver(passwordBroadcastReceiver);
            Log.i(TAG, "Password BroadcastReceiver unregistered");
            Toast.makeText(ProfileActivity.this, R.string.password_change_failure, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile, menu);
        return true;
    }

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
                                        .getDefaultSharedPreferences(ProfileActivity.this).edit();
                                editor.putString(Constants.USERNAME,"");
                                editor.putString(Constants.PASSWORD,"");
                                editor.putString(Constants.USER_TYPE,"");
                                editor.apply();
                                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            case R.id.my_home:
                if(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.USER_TYPE,"").equals(Constants.USER))
                    startActivity(new Intent(this, UserActivity.class));
                else
                    startActivity(new Intent(this, AdministratorActivity.class));
                finish();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed()");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "onSaveInstanceState()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if(updateTask != null) updateTask.cancel(false);
        if(updatePasswordTask != null) updatePasswordTask.cancel(false);
    }

    BroadcastReceiver updateBroadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive()");
            if(updateTask != null) updateTask.cancel(false);
            String status = intent.getStringExtra(Constants.UPDATE_STATUS);
            if(status.equals(Constants.UPDATE_SUCCESS)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.USERNAME, emailID);
                editor.putString(Constants.FIRST_NAME, firstName);
                editor.putString(Constants.LAST_NAME, lastName);
                editor.putString(Constants.MOBILE_NO, mobileNo);
                editor.apply();

                editing = false;
                firstNameLayout.getEditText().setEnabled(false);
                lastNameLayout.getEditText().setEnabled(false);
                mobileNoLayout.getEditText().setEnabled(false);
                firstNameLayout.getEditText().setText(firstName);
                lastNameLayout.getEditText().setText(lastName);
                emailIDLayout.getEditText().setText(emailID);
                mobileNoLayout.getEditText().setText(mobileNo);
                editButton.setVisibility(View.VISIBLE);
                editButton.setText(R.string.action_edit_profile);

                changePasswordButton.setVisibility(View.VISIBLE);
                updateProgressBar.setVisibility(View.GONE);

                Toast.makeText(context,R.string.update_success,Toast.LENGTH_SHORT).show();
            } else {
                editButton.setVisibility(View.VISIBLE);
                changePasswordButton.setVisibility(View.VISIBLE);
                updateProgressBar.setVisibility(View.GONE);
                Toast.makeText(context, R.string.update_failure, Toast.LENGTH_SHORT)
                        .show();
            }
            LocalBroadcastManager.getInstance(context)
                    .unregisterReceiver(updateBroadcastReceiver);
            Log.i(TAG, "Update BroadcastReceiver unregistered");
        }
    };

    BroadcastReceiver passwordBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive()");
            if(updatePasswordTask != null) updatePasswordTask.cancel(false);
            String status = intent.getStringExtra(Constants.PASSWORD_CHANGE_STATUS);
            if(status.equals(Constants.PASSWORD_CHANGE_SUCCESS)) {
                sharedPreferences.edit().putString(Constants.PASSWORD, newPassword).apply();
                dialog.dismiss();
                Toast.makeText(context,R.string.password_change_successful,Toast.LENGTH_SHORT).show();
            } else {
                savePasswordButton.setVisibility(View.VISIBLE);
                passwordChangeProgressBar.setVisibility(View.GONE);
                Toast.makeText(context,R.string.password_change_failure,Toast.LENGTH_SHORT).show();
            }
            LocalBroadcastManager.getInstance(context)
                    .unregisterReceiver(passwordBroadcastReceiver);
            Log.i(TAG, "Password BroadcastReceiver unregistered");
        }
    };
}
