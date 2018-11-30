package com.invogen.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.mContext = this;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Toast.makeText(mContext, "Listener Active", Toast.LENGTH_SHORT).show();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    String userId = user.getUid();
                    mUserDatabaseReference = mFirebaseDatabase.getReference().child(AppConstants.USERS_NODE)
                            .child(userId);
                    mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String userType = dataSnapshot.child("user_type").getValue().toString();
                            AppConstants.USER_TYPE = userType;
                            if (userType.equals("controller")) {
                                startActivity(new Intent(mContext, MainActivity.class));
                                AppUtils.finishActivity(SplashActivity.this);
                            } else {
                                startActivity(new Intent(mContext, MainActivityViewer.class));
                                AppUtils.finishActivity(SplashActivity.this);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Toast.makeText(getApplicationContext(), "Already Logged In!", Toast.LENGTH_LONG).show();
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), UserTypeActivity.class));
                            finish();
                        }
                    }, 1000);
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthStateListener != null) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
//        finish();
    }
}
