package com.leuradu.android.bikeapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.utils.LoadingCallback;
import com.leuradu.android.bikeapp.utils.Validator;

/**
 * Created by radu on 19.03.2016.
 */
public class RegistrationFragment extends Fragment{

    private static String TAG = "RegistrationFragment";

    private EditText mTextName;
    private EditText mTextEmail;
    private EditText mTextPassword;
    private EditText mTextConfirmPassword;
    private Button mButtonRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_registration,container,false);
        mTextName = (EditText) v.findViewById(R.id.text_name);
        mTextEmail = (EditText) v.findViewById(R.id.text_email);
        mTextPassword = (EditText) v.findViewById(R.id.text_password);
        mTextConfirmPassword = (EditText) v.findViewById(R.id.text_confirm_password);
        mButtonRegister = (Button) v.findViewById(R.id.button_register);
        mButtonRegister.setOnClickListener(createListenerRegisterButton());

        return v;
    }

    private View.OnClickListener createListenerRegisterButton() {

        return new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String name = mTextName.getText().toString();
                String email = mTextEmail.getText().toString();
                String password = mTextPassword.getText().toString();
                String confirmPassword = mTextConfirmPassword.getText().toString();

                if (isRegistrationDataValid(name, email, password, confirmPassword)) {

                    LoadingCallback<BackendlessUser> callback = createRegistrationCallback();
                    callback.showProgressDialog();
                    registerUser(name, email, password, callback);
                }
            }
        };
    }

    private boolean isRegistrationDataValid(String name, String email, String password, String confirmPassword) {

        return Validator.isNameValid(getContext(), name) &&
                Validator.isEmailValid(getContext(), email) &&
                Validator.isPasswordValid(getContext(), password) &&
                Validator.isPasswordValid(getContext(), confirmPassword) &&
                Validator.isPasswordMatch(getContext(), password, confirmPassword);
    }

    public LoadingCallback<BackendlessUser> createRegistrationCallback()
    {
        return new LoadingCallback<BackendlessUser>( getContext(), getString( R.string.loading_register ) )
        {
            @Override
            public void handleResponse( BackendlessUser registeredUser )
            {
                super.handleResponse( registeredUser );
                Toast.makeText(getContext(), String.format(getString(R.string.info_registered),
                        registeredUser.getObjectId()), Toast.LENGTH_LONG).show();
            }
        };
    }

    public void registerUser( String name, String email, String password,
                              AsyncCallback<BackendlessUser> registrationCallback ) {
        Log.d(TAG, "Name: " + name);

        BackendlessUser user = new BackendlessUser();
        user.setEmail(email);
        user.setPassword(password);
        user.setProperty("name", name);

        Backendless.UserService.register(user, registrationCallback);
    }
}
