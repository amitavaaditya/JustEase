package com.technodevil.justease.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.technodevil.justease.R;
import com.technodevil.justease.util.Constants;
import com.technodevil.justease.util.OnFragmentChangedListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnFragmentChangedListener {
    //Logging
    private static final String TAG = "MainActivity";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private DrawerLayout drawerLayout;
    private TextView toolbarSubtitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.D) Log.d(TAG, "onCreate()");setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarSubtitleView = (TextView) toolbar.findViewById(R.id.toolbarSubtitleView);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_48dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        checkNetwork();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Constants.D) Log.d(TAG, "onStart()");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (Constants.D) Log.d(TAG, "onRestoreInstanceState()");
    }

    @Override
    public void onResume(){
        super.onResume();
        if (Constants.D) Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause(){
        super.onPause();
        if (Constants.D) Log.d(TAG, "onPause()");
    }

    /*
     * Check if Internet is enabled. Otherwise exit.
     */
    private void checkNetwork() {
        if (Constants.D) Log.d(TAG, "checkNetwork()");
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            if (Constants.D) Log.d(TAG, "Not Connected");
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_internet_title)
                    .setMessage(R.string.no_internet_message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            if (Constants.D) Log.d(TAG, "Connected");
            checkPlayServices();
        }
    }

    /*
     * Check if user is already logged in
     */
    private void checkLogin() {
        if (Constants.D) Log.d(TAG, "checkLogin()");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPreferences.getString(Constants.USERNAME, "");
        if(!username.equals("")) {
            if(sharedPreferences.getString(Constants.USER_TYPE,"").equals(Constants.USER)) {
                startActivity(new Intent(this, UserActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, AdministratorActivity.class));
                finish();
            }
        }
        else
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, new LoginFragment())
                    .commit();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                if (Constants.D) Log.i(TAG, "This device is not supported.");
                finish();
            }
        }
        checkLogin();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (Constants.D) Log.d(TAG, "onNavigationItemSelected():" + item.getTitle());
        item.setChecked(false);
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.change_wait_time:
                final EditText waitTimeEditText = new EditText(this);
                waitTimeEditText.setText(String.format("%d", Constants.BACKOFF_TIME));
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.wait_time_changed))
                        .setView(waitTimeEditText)
                        .setPositiveButton(Constants.CONFIRM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Constants.BACKOFF_TIME = Integer.parseInt(waitTimeEditText.getText().toString());
                                } catch (Exception e) {
                                    if (Constants.D) Log.e(TAG, e.toString());
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
                        .setMessage(R.string.change_server_url)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Constants.D) Log.d(TAG, "onOptionsItemSelected():" + item.getTitle());
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (Constants.D) Log.d(TAG, "onBackPressed()");
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        // do something when the positive button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
                            //close application;
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (Constants.D) Log.d(TAG, "onSaveInstanceState()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Constants.D) Log.d(TAG,"onDestroy()");
    }

    @Override
    public void onFragmentChanged(String subtitle) {
        if (Constants.D) Log.d(TAG,"onFragmentChanged():" + subtitle);
        toolbarSubtitleView.setText(subtitle);
    }
}
