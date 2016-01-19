package com.technodevil.justease;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
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


/**
 * Fragment for Login Activity
 */
@SuppressWarnings("ConstantConditions")
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG,"onViewCreated()");
        //Initialise UI elements
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayout);
        usernameLayout = (TextInputLayout)view.findViewById(R.id.usernameLayout);
        passwordLayout = (TextInputLayout)view.findViewById(R.id.passwordLayout);
        signInButton = (Button)view.findViewById(R.id.signInButton);
        registerButton = (Button)view.findViewById(R.id.registerButton);
        loginProgressBar = (ProgressBar)view.findViewById(R.id.loginProgressBar);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button clicked: signInButton");

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
                Log.d(TAG, "Button clicked: registerButton");
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
        Log.d(TAG, "onResume()");
        Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.login, Snackbar.LENGTH_INDEFINITE);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.BLUE);
        ((TextView) view.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public void onPause(){
        super.onResume();
        Log.d(TAG, "onPause()");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(loginBroadcastReceiver);
        Log.i(TAG, "Login BroadcastReceiver unregistered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
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
            Log.i(TAG, "Login BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                Log.e(TAG,e.getClass().toString());
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
            Log.i(TAG, "Login BroadcastReceiver unregistered");
            Toast.makeText(getActivity(), R.string.login_failure, Toast.LENGTH_SHORT).show();
        }
    }

    BroadcastReceiver loginBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(Constants.LOGIN_STATUS);
            Log.d(TAG, "onReceive():" + status);
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
                if(userType.equals(Constants.ADMINISTRATOR))
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
            Log.i(TAG, "Login BroadcastReceiver unregistered");
        }
    };
}