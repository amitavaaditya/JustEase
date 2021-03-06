package com.technodevil.justease.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.technodevil.justease.R;
import com.technodevil.justease.util.Constants;
import com.technodevil.justease.util.OnFragmentChangedListener;

public class AdministratorActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnFragmentChangedListener {

    //Debugging
    private static final String TAG = "AdministratorActivity";

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.D) Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_administrator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        TextView toolbarSubtitleView = (TextView) toolbar.findViewById(R.id.toolbarSubtitleView);
        toolbarSubtitleView.setVisibility(View.GONE);

        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Constants.NOTIFICATION_ID, 0).apply();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_48dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup the viewPager
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        // Setup the Tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        // Setup the Tabs
        // By using this method the tabs will be populated according to viewPager's count and
        // with the name from the pagerAdapter getPageTitle()
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        // This method ensures that tab selection events update the ViewPager and page changes update the selected tab.
        tabLayout.setupWithViewPager(viewPager);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Constants.D) Log.d(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Constants.D) Log.d(TAG, "onOptionsItemSelected():" + item.getTitle());
        switch (item.getItemId()) {
            case R.id.my_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (Constants.D) Log.d(TAG, "onNavigationItemSelected():" + item.getTitle());
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.logout:
                new AlertDialog.Builder(AdministratorActivity.this)
                        .setMessage(R.string.logout_confirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(AdministratorActivity.this).edit();
                                editor.putString(Constants.USERNAME,"");
                                editor.putString(Constants.PASSWORD,"");
                                editor.putString(Constants.USER_TYPE,"");
                                editor.apply();
                                startActivity(new Intent(AdministratorActivity.this, MainActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            case R.id.my_profile:
                startActivity(new Intent(this, ProfileActivity.class));
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
        if (Constants.D) Log.d(TAG, "onBackPressed()");
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (Constants.D) Log.d(TAG, "onSaveInstanceState()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Constants.D) Log.d(TAG, "onDestroy()");
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int pos) {
            if (Constants.D) Log.d(TAG, "getItem():" + pos);
            switch(pos) {
                case Constants.ADMINISTRATOR_FRAGMENT_ENQUIRIES: return new EnquiriesFragment();
                case Constants.ADMINISTRATOR_FRAGMENT_CHAT_LIST: return new MyCasesFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)   return getResources().getString(R.string.enquiries);
            else                return getResources().getString(R.string.my_cases);
        }
    }

    @Override
    public void onFragmentChanged(String subtitle) {
        if (Constants.D) Log.d(TAG,"onFragmentChanged():" + subtitle);
    }
}
