package com.invogen.messagingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private Context mContext;
    private final String TAG = "DashboardNavActivity";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mProfilesDBReference, mUsersDBReference;


    //UI elements
    CardView messagingCardView, AddProfilesCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.mContext = this;

        initViews();
        initFirebaseObjects();
        registerListener();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        checkPermissions();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initViews() {
        messagingCardView = findViewById(R.id.messaging_card_id);
    }

    private void initFirebaseObjects() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void registerListener(){
        messagingCardView.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        int id = item.getItemId();

        super.onOptionsItemSelected(item);
        if (id == R.id.menu_logout) {
//            AuthUI.getInstance()
//                    .signOut(mContext)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        public void onComplete(@NonNull Task<Void> task) {
//                            // user is now signed out
//                            startActivity(new Intent(mContext,
//                                    UserTypeActivity.class));
//                            AppConstants.setCurrentUserUid("");
//                            finish();
//                        }
//                    });
        }
        if (id == R.id.menu_scan_qr) {
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
//            startActivity(new Intent(getApplicationContext(), ScanQRCodeActivity.class));
        }
        if (id == R.id.menu_generate_qr) {
            startActivity(new Intent(getApplicationContext(), GenerateQRCodeActivity.class));
        }
        if (id == R.id.menu_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gen_qr_code) {
            startActivity(new Intent(getApplicationContext(), GenerateQRCodeActivity.class));
        } else if (id == R.id.nav_scan_qr_code) {
            startActivity(new Intent(getApplicationContext(), ScanQRCodeActivity.class));
        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        } else if (id == R.id.nav_profiles) {
            startActivity(new Intent(getApplicationContext(), ProfilesActivity.class));
        } else if (id == R.id.nav_logout) {
//            AuthUI.getInstance()
//                    .signOut(mContext)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        public void onComplete(@NonNull Task<Void> task) {
//                            // user is now signed out
//                            startActivity(new Intent(mContext,
//                                    UserTypeActivity.class));
//                            AppConstants.setCurrentUserUid("");
//                            finish();
//                        }
//                    });
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.messaging_card_id:{
                startActivity(new Intent(getApplicationContext(), MainActivityControllerDrawer.class));
                break;
            }
            case R.id.profiles_card_id:{

                break;
            }
        }
    }
}
