package com.technodevil.justease.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.technodevil.justease.R;
import com.technodevil.justease.service.ServerIntentService;
import com.technodevil.justease.util.Constants;
import com.technodevil.justease.util.OnFragmentChangedListener;


/**
 * Fragment for the registration form
 */
public class RegisterFragment extends Fragment {
    //Debugging
    public static final String TAG = "RegisterFragment";
    private static final boolean D = true;
    //UI elements
    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout contactNumberLayout;
    private TextInputLayout emailIDLayout;
    private TextInputLayout setPasswordLayout;
    private TextInputLayout setPasswordAgainLayout;
    private Button signUpButton;
    private ProgressBar registerProgressBar;

    String firstName;
    String lastName;
    String password;
    String passwordAgain;
    String mobileNo;
    String emailID;

    RegisterTask registerTask;
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
        onFragmentChangedListener.onFragmentChanged(getResources().getString(R.string.register));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(D) Log.d(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(D) Log.d(TAG,"onViewCreated()");
        //Initialise UI elements
        firstNameLayout = (TextInputLayout)view.findViewById(R.id.firstNameLayout);
        lastNameLayout = (TextInputLayout)view.findViewById(R.id.lastNameLayout);
        contactNumberLayout = (TextInputLayout)view.findViewById(R.id.mobileNumberLayout);
        emailIDLayout = (TextInputLayout)view.findViewById(R.id.emailIDLayout);
        setPasswordLayout = (TextInputLayout)view.findViewById(R.id.setPasswordLayout);
        setPasswordAgainLayout = (TextInputLayout)view.findViewById(R.id.setPasswordAgainLayout);
        registerProgressBar = (ProgressBar) view.findViewById(R.id.registerProgressBar);

        signUpButton = (Button) view.findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View v) {
                if (D) Log.d(TAG, "register()");
                firstName = firstNameLayout.getEditText().getText().toString();
                lastName = lastNameLayout.getEditText().getText().toString();
                mobileNo = contactNumberLayout.getEditText().getText().toString();
                emailID = emailIDLayout.getEditText().getText().toString();
                password = setPasswordLayout.getEditText().getText().toString();
                passwordAgain = setPasswordAgainLayout.getEditText().getText().toString();
                emailIDLayout.setErrorEnabled(false);
                setPasswordLayout.setErrorEnabled(false);
                setPasswordAgainLayout.setErrorEnabled(false);
                //Check if email id valid
                if(!Patterns.EMAIL_ADDRESS.matcher(emailID).matches()) {
                    emailIDLayout.setErrorEnabled(true);
                    emailIDLayout.setError(getString(R.string.not_valid_email));
                } else {
                    //Check if password length greater than 8 characters
                    if (!(password.length() >= 8)) {
                        setPasswordLayout.setErrorEnabled(true);
                        setPasswordLayout.setError(getResources().getString(R.string.password_error));
                    }
                    //Check if passwords entered match
                    else if (!password.equals(passwordAgain)) {
                        setPasswordLayout.setErrorEnabled(true);
                        setPasswordAgainLayout.setErrorEnabled(true);
                        setPasswordLayout.setError(getResources().getString(R.string.password_mismatch));
                        setPasswordAgainLayout.setError(getResources().getString(R.string.password_mismatch));
                    }
                    //Attempt to register
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.EMAIL_ID, emailID);
                        bundle.putString(Constants.PASSWORD, password);
                        bundle.putString(Constants.FIRST_NAME, firstName);
                        bundle.putString(Constants.LAST_NAME, lastName);
                        bundle.putString(Constants.MOBILE_NO, mobileNo);
                        registerTask = new RegisterTask();
                        registerTask.execute();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.d(TAG, "onPause()");
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(registerBroadcastReceiver);
        Log.i(TAG, "Register BroadcastReceiver unregistered");
    }

    class RegisterTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            signUpButton.setVisibility(View.GONE);
            registerProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.EMAIL_ID, emailID);
            bundle.putString(Constants.PASSWORD, password);
            bundle.putString(Constants.FIRST_NAME, firstName);
            bundle.putString(Constants.LAST_NAME, lastName);
            bundle.putString(Constants.MOBILE_NO, mobileNo);

            Intent intent = new Intent(getActivity(), ServerIntentService.class);
            intent.setAction(Constants.ACTION_REGISTER);
            intent.putExtra(Constants.DATA, bundle);
            getActivity().startService(intent);

            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(registerBroadcastReceiver, new IntentFilter(Constants.REGISTER_STATUS));
            Log.i(TAG, "Register BroadcastReceiver registered");

            try {
                Thread.sleep(Constants.BACKOFF_TIME);
            } catch (InterruptedException e) {
                Log.e(TAG,e.getClass().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            registerProgressBar.setVisibility(View.GONE);
            signUpButton.setVisibility(View.VISIBLE);
            LocalBroadcastManager.getInstance(getActivity())
                    .unregisterReceiver(registerBroadcastReceiver);
            Log.i(TAG, "Register BroadcastReceiver unregistered");
            Toast.makeText(getActivity(), R.string.register_failure, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()");
        if(registerTask != null) registerTask.cancel(false);
    }

    BroadcastReceiver registerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(Constants.REGISTER_STATUS);
            Log.d(TAG, "onReceive():" + status);
            if(status.equals(Constants.REGISTER_SUCCESS)) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.USERNAME, emailID);
                editor.putString(Constants.PASSWORD, password);
                editor.putString(Constants.FIRST_NAME, firstName);
                editor.putString(Constants.LAST_NAME, lastName);
                editor.putString(Constants.MOBILE_NO, mobileNo);
                editor.putString(Constants.USER_TYPE, Constants.USER);
                editor.apply();
                startActivity(new Intent(getActivity(), UserActivity.class));
                getActivity().finish();
            } else {
                registerProgressBar.setVisibility(View.GONE);
                signUpButton.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), R.string.username_exists, Toast.LENGTH_SHORT).show();
            }
            if(registerTask != null) registerTask.cancel(false);
            LocalBroadcastManager.getInstance(context)
                    .unregisterReceiver(registerBroadcastReceiver);
            Log.i(TAG, "Register BroadcastReceiver unregistered");
        }
    };
}
