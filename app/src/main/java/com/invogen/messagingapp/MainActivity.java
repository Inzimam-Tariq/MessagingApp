package com.invogen.messagingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mProfilesDBReference, mUsersDBReference;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsPagerAdapter mTabsPagerAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mContext = this;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        initViews();
        initFirebaseObjects();
        setupAuth();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        checkPermissions();


        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("AI Eye");

        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initViews() {
        mToolbar = findViewById(R.id.main_toolbar);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);
    }

    private void initFirebaseObjects() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
//        mFirebaseDatabase = FirebaseDatabase.getInstance();
//        mFirebaseStorage = FirebaseStorage.getInstance();

//        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");
//        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");
    }

    private void setupAuth() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Toast.makeText(mContext, "Listener Active", Toast.LENGTH_SHORT).show();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    AppConstants.setUserUid(user.getUid());
                    Toast.makeText(mContext, "Users Active", Toast.LENGTH_SHORT).show();
                    FirebaseUserMetadata metadata = firebaseAuth.getCurrentUser().getMetadata();
                    Log.e("ActivityController", "\ncreation timestamp = " + metadata
                            .getCreationTimestamp() + "\nlast sign in timestamp = "
                            + metadata.getLastSignInTimestamp());
                    if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                        // The user is new, show them a fancy intro screen!
                        mProfilesDBReference = mFirebaseDatabase.getReference()
                                .child(AppConstants.PROFILES_NODE).child(user.getUid());

                        mProfilesDBReference.child("user_name").setValue(user.getDisplayName());
                        mProfilesDBReference.child("user_image").setValue("");
                        mProfilesDBReference.child("user_circle").setValue("Worker");
                        Toast.makeText(mContext, "Listener Active Storing Data", Toast.LENGTH_SHORT)
                                .show();

                        mUsersDBReference = mFirebaseDatabase.getReference()
                                .child("users").child(user.getUid());

                        mUsersDBReference.child("user_name").setValue(user.getDisplayName());
                        mUsersDBReference.child("user_status").setValue("online");
                        mUsersDBReference.child("user_image").setValue("");
                        mUsersDBReference.child("user_email").setValue(user.getEmail());
                        mUsersDBReference.child("user_type").setValue(AppConstants.getUserType());
                        mUsersDBReference.child("friend_list").child("");

                    } else {
                        // This is an existing user, show them a welcome back screen.
                    }
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.AnonymousBuilder().build()))
                                    .build(),
                            AppConstants.RC_SIGN_IN);
                }
            }
        };
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission
                .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
        Permissions.check(this/*context*/, permissions, null/*rationale*/,
                null/*options*/, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        // do your task.
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        String userType = getIntent().getStringExtra("UserType");
//        if (userType.equals("Viewer")) {
//            getMenuInflater().inflate(R.menu.menu_main_viewer, menu);
//        } else {
        getMenuInflater().inflate(R.menu.menu_main_controller, menu);
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_logout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(MainActivity.this,
                                    UserTypeActivity.class));
                            AppConstants.setUserUid("");
                            finish();
                        }
                    });
        }
        if (item.getItemId() == R.id.menu_scan_qr) {
            startActivity(new Intent(getApplicationContext(), ScanQRCodeActivity.class));
        }
        if (item.getItemId() == R.id.menu_generate_qr) {
            startActivity(new Intent(getApplicationContext(), GenerateQRCodeActivity.class));
        }
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }

        return true;
    }
}
