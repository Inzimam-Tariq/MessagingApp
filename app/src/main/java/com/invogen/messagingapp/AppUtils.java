package com.invogen.messagingapp;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AppUtils {
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean userLoggedStatus;

    public static void finishActivity(Activity activity) {
        activity.finish();
    }

    public boolean isUserLoggedIn() {

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    userLoggedStatus = true;
                }
            }
        };
        return userLoggedStatus;
    }
}
