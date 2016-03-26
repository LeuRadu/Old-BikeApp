package com.leuradu.android.bikeapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.activities.RegistrationActivity;
import com.leuradu.android.bikeapp.utils.LoadingCallback;
import com.leuradu.android.bikeapp.utils.Validator;

/**
 * Created by radu on 21.03.2016.
 */
public class LoginFragment extends Fragment {

    private TextView mTextEmail;
    private TextView mTextPassword;
    private Button mButtonLogin;
    private Button mButtonRegister;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        mTextEmail = (TextView) v.findViewById(R.id.login_text_email);
        mTextPassword = (TextView) v.findViewById(R.id.login_text_password);
        mButtonLogin = (Button) v.findViewById(R.id.button_login);
        mButtonRegister = (Button) v.findViewById(R.id.button_login_register);

        mButtonLogin.setOnClickListener(createListenerLoginButton());

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(RegistrationActivity.newIntent(getContext()));
            }
        });

        return v;
    }

    public void loginUser( String email, String password, AsyncCallback<BackendlessUser> loginCallback )
    {
        Backendless.UserService.login(email, password, loginCallback);
    }

    private View.OnClickListener createListenerLoginButton() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mTextEmail.getText().toString();
                String password = mTextPassword.getText().toString();

                if (isLoginDataValid(email, password)) {
                    LoadingCallback<BackendlessUser> callback = createLoginCallback();
                    callback.showProgressDialog();

                    loginUser(email,password,callback);
                }
            }
        };
    }

//    TODO: register response (when registering, mail and pass move to login activity)

    private boolean isLoginDataValid(String email, String password) {
        return Validator.isEmailValid(getContext(), email) &&
                Validator.isPasswordValid(getContext(),password);
    }

    private LoadingCallback<BackendlessUser> createLoginCallback() {
        return new LoadingCallback<BackendlessUser>(getContext(),
                getContext().getString(R.string.login_wait)){
            @Override
            public void handleResponse(BackendlessUser response) {
                super.handleResponse(response);
                Toast.makeText(getContext(), String.format(getString(R.string.info_logging),
                        response.getObjectId()), Toast.LENGTH_LONG).show();
            }
        };
    }
}
