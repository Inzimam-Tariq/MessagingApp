package com.invogen.messagingapp;

import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AppUtils {
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean userLoggedStatus;

    public static void finishActivity(Activity activity) {
        activity.finish();
    }

    public static String getTime() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH-mm");
        return df.format(c);
    }
}
