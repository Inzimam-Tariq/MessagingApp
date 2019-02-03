package com.invogen.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class UserTypeActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);
        mContext = this;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("AI Eye");
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Toast.makeText(mContext, "Listener Active", Toast.LENGTH_SHORT).show();
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    String userId = user.getUid();
                    AppConstants.setCurrentUserUid(user.getUid());
                    mUserDatabaseReference = mFirebaseDatabase.getReference().child(AppConstants.USERS_NODE)
                            .child(userId);
                    mUserDatabaseReference.keepSynced(true);
                    mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String userType = dataSnapshot.child("user_type").getValue().toString();
                            AppConstants.setUserType(userType);
                            if (userType.equals("controller")) {
                                controllerClicked(new View(mContext));
                            } else {
                                viewerClicked(new View(mContext));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Toast.makeText(getApplicationContext(), "Already Logged In!", Toast.LENGTH_LONG).show();
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
    }


    public void controllerClicked(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivityControllerDrawer.class));
        AppConstants.setUserType("controller");
        Toast.makeText(mContext, "Users Type Controller!", Toast.LENGTH_LONG).show();
        AppUtils.finishActivity(UserTypeActivity.this);
    }

    public void viewerClicked(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivityViewer.class));
        AppConstants.setUserType("viewer");
        Toast.makeText(mContext, "Users Type Viewer!", Toast.LENGTH_LONG).show();
        AppUtils.finishActivity(UserTypeActivity.this);
    }

}
