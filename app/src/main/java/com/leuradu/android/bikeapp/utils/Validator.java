package com.leuradu.android.bikeapp.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by radu on 20.03.2016.
 */
public class Validator {

//    TODO: create proper validation
    public static boolean isNameValid(Context context, String name) {
        if (name.isEmpty()) {
            Toast.makeText(context, "Name field cannot be empty", Toast.LENGTH_LONG);
            return false;
        }
        return true;
    }

    public static boolean isTextValid(Context context, String name) {
        if (name.isEmpty()) {
            Toast.makeText(context, "Name field cannot be empty", Toast.LENGTH_LONG);
            return false;
        }
        return true;
    }

    public static boolean isEmailValid(Context context, String email){
        if (email.isEmpty()) {
            Toast.makeText(context, "Email field cannot be empty", Toast.LENGTH_LONG);
            return false;
        }
        return true;
    }

    public static boolean isPasswordValid(Context context, String password){
        if (password.isEmpty()) {
            Toast.makeText(context, "Password field cannot be empty", Toast.LENGTH_LONG);
            return false;
        }
        return true;
    }

    public static boolean isPasswordMatch(Context context, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            Toast.makeText(context, "Password fields do not match", Toast.LENGTH_LONG);
            return false;
        }
        return true;
    }

}
