package com.technodevil.justease.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.technodevil.justease.R;
import com.technodevil.justease.service.ServerIntentService;
import com.technodevil.justease.util.Constants;
import com.technodevil.justease.util.OnFragmentChangedListener;


/**
 * Fragment for Login Activity
 */
public class LoginFragment extends Fragment {
    //Debugging
    public static final String TAG = "LoginFragment";

    //UI elements
    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private Button signInButton;
    private Button registerButton;
    private ProgressBar loginProgressBar;

    String username;
    String password;

    LoginTask loginTask;

    OnFragmentChangedListener onFragmentChangedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Constants.D) Log.d(TAG, "onAttach()");
        try {
            onFragmentChangedListener = (OnFragmentChangedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnFragmentChangedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.D) Log.d(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (Constants.D) Log.d(TAG,"onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (Constants.D) Log.d(TAG,"onViewCreated()");
        //Initialise UI elements
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayout);
        usernameLayout = (TextInputLayout)view.findViewById(R.id.usernameLayout);
        passwordLayout = (TextInputLayout)view.findViewById(R.id.passwordLayout);
        signInButton = (Button)view.findViewById(R.id.signInButton);
        registerButton = (Button)view.findViewById(R.id.registerButton);
        loginProgressBar = (ProgressBar)view.findViewById(R.id.loginProgressBar);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View v) {
                if (Constants.D) Log.d(TAG, "Button clicked: signInButton");

                username = usernameLayout.getEditText().getText().toString();
                if (username.equals("")) {
                    usernameLayout.setErrorEnabled(true);
                    usernameLayout.setError(getString(R.string.login_field_blank));
                    return;
                }

                password = passwordLayout.getEditText().getText().toString();
                if (password.equals("")) {
                    passwordLayout.setErrorEnabled(true);
                    passwordLayout.setError(getString(R.string.login_field_blank));
                    return;
                }

                loginTask = new LoginTask();
                loginTask.execute();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.D) Log.d(TAG, "Button clicked: registerButton");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, new RegisterFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if (Constants.D) Log.d(TAG, "onResume()");
        onFragmentChangedListener.onFragmentChanged(getResources().getString(R.string.login));
        Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.login_required, Snackbar.LENGTH_INDEFINITE);
        View view = snackbar.getView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, getActivity().getTheme()));
        } else {
            //noinspection deprecation
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public void onPause(){
        super.onResume();
        if (Constants.D) Log.d(TAG, "onPause()");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(loginBroadcastReceiver);
        if (Constants.D) Log.i(TAG, "Login BroadcastReceiver unregistered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Constants.D) Log.d(TAG, "onDestroy()");
        if(loginTask != null) loginTask.cancel(false);
    }

    class LoginTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            signInButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.GONE);
            loginProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.USERNAME, username);
            bundle.putString(Constants.PASSWORD, password);

            Intent intent = new Intent(getActivity(), ServerIntentService.class);
            intent.setAction(Constants.ACTION_LOGIN);
            intent.putExtra(Constants.DATA, bundle);
            getActivity().startService(intent);

            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(loginBroadcastReceiver, new IntentFilter(Constants.LOGIN_STATUS));
            if (Constants.D) Log.i(TAG, "Login BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                if (Constants.D) Log.e(TAG,e.getClass().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loginProgressBar.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);
            loginProgressBar.setVisibility(View.GONE);
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(loginBroadcastReceiver);
            if (Constants.D) Log.i(TAG, "Login BroadcastReceiver unregistered");
            Toast.makeText(getActivity(), R.string.login_failure, Toast.LENGTH_SHORT).show();
        }
    }

    BroadcastReceiver loginBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(Constants.LOGIN_STATUS);
            if (Constants.D) Log.d(TAG, "onReceive():" + status);
            if(status.equals(Constants.LOGIN_SUCCESS)) {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString(Constants.USERNAME, username);
                editor.putString(Constants.PASSWORD, password);
                Bundle data = intent.getBundleExtra(Constants.DATA);
                editor.putString(Constants.FIRST_NAME, data.getString(Constants.FIRST_NAME));
                editor.putString(Constants.LAST_NAME, data.getString(Constants.LAST_NAME));
                editor.putString(Constants.MOBILE_NO, data.getString(Constants.MOBILE_NO));
                String userType = data.getString(Constants.USER_TYPE);
                editor.putString(Constants.USER_TYPE, userType);
                editor.apply();
                if(userType != null && userType.equals(Constants.ADMINISTRATOR))
                    startActivity(new Intent(getActivity(),AdministratorActivity.class));
                else
                    startActivity(new Intent(getActivity(), UserActivity.class));
                getActivity().finish();
            } else {
                loginProgressBar.setVisibility(View.GONE);
                signInButton.setVisibility(View.VISIBLE);
                registerButton.setVisibility(View.VISIBLE);
                Toast.makeText(context, R.string.invalid_login_credentials, Toast.LENGTH_SHORT).show();
            }
            if(loginTask != null) loginTask.cancel(false);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(loginBroadcastReceiver);
            if (Constants.D) Log.i(TAG, "Login BroadcastReceiver unregistered");
        }
    };
}