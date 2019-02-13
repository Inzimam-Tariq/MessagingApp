package com.invogen.messagingapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivityControllerDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "MainActControllerNav";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mProfilesDBReference, mUsersDBReference;

    private Toolbar mToolbar;

    private NavigationView navigationView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private Context mContext;
    private boolean backClicked;

    private int[] tabIcons = {
            R.drawable.ic_chat_white,
            R.drawable.ic_users_white,
            R.drawable.ic_attach_file
    };
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_controller_drawer);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.app_name);
        this.mContext = this;

        initViews();
        initFirebaseObjects();
        setupAuth();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        checkPermissions();


    }

    private void initViews() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        mToolbar = findViewById(R.id.main_toolbar);
        navigationView = findViewById(R.id.nav_view);
        mViewPager = findViewById(R.id.viewpager);
        mTabLayout = findViewById(R.id.tabs);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
        setUpTabIcons();
    }

    private void initFirebaseObjects() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
//            findViewById(R.id.default_title).setVisibility(View.VISIBLE);
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backClicked) {
                super.onBackPressed();
            } else {
                backClicked = true;
                Toast.makeText(mContext, "Press again to exit!", Toast.LENGTH_SHORT).show();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backClicked = false;
                }
            }, 3000);
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
        getMenuInflater().inflate(R.menu.menu_main_controller, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    //some operation
                    return true;
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //some operation
                }
            });
            EditText searchPlate = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchPlate.setHint("Search");
            View searchPlateView = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
            searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            // use this method for search process
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // use this method when query submitted
                    Toast.makeText(mContext, query, Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // use this method for auto complete search process
                    return false;
                }
            });
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        super.onOptionsItemSelected(item);
        if (id == R.id.menu_logout) {
            AuthUI.getInstance()
                    .signOut(mContext)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(mContext,
                                    UserTypeActivity.class));
                            AppConstants.setCurrentUserUid("");
                            finish();
                        }
                    });
        }
        if (id == R.id.menu_scan_qr) {
            startActivity(new Intent(getApplicationContext(), ScanQRCodeActivity.class));
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerNavActivityAdapter adapter = new ViewPagerNavActivityAdapter(getSupportFragmentManager());
        adapter.addFragment(new GroupChatFragment(), "Chat Room");
        adapter.addFragment(new FriendsFragment(), "Users");
        adapter.addFragment(new Fragment(), "Empty");
        viewPager.setAdapter(adapter);
    }

    private void setUpTabIcons() {
        mTabLayout.getTabAt(0).setIcon(tabIcons[0]);
        mTabLayout.getTabAt(1).setIcon(tabIcons[1]);
        mTabLayout.getTabAt(2).setIcon(tabIcons[2]);
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

    private void setupAuth() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Toast.makeText(mContext, "Listener Active", Toast.LENGTH_SHORT).show();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    AppConstants.setCurrentUserUid(user.getUid());

                    FirebaseUserMetadata metadata = firebaseAuth.getCurrentUser().getMetadata();
                    Log.e(TAG, "\ncreation timestamp = " + metadata
                            .getCreationTimestamp() + "\nlast sign in timestamp = "
                            + metadata.getLastSignInTimestamp());
                    if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                        // The user is new, show them a fancy intro screen!
                        mProfilesDBReference = mFirebaseDatabase.getReference()
                                .child(AppConstants.PROFILES_NODE).child(user.getUid());

                        Uri profileImageUri = user.getPhotoUrl();
                        String username = user.getDisplayName();
                        String userImage = null;
                        String userEmail = user.getEmail();

                        if (profileImageUri != null) {
                            userImage = profileImageUri.toString();
                        }

                        mProfilesDBReference.child("user_name").setValue(username);
                        mProfilesDBReference.child("user_image").setValue(userImage);
                        mProfilesDBReference.child("user_circle").setValue("Worker");

                        mUsersDBReference = mFirebaseDatabase.getReference()
                                .child(AppConstants.USERS_NODE).child(user.getUid());

                        mUsersDBReference.child("user_name").setValue(username);
                        mUsersDBReference.child("user_status").setValue("online");
                        mUsersDBReference.child("user_image").setValue(userImage);
                        mUsersDBReference.child("user_email").setValue(userEmail);
                        mUsersDBReference.child("user_type").setValue(AppConstants.getUserType());
                        mUsersDBReference.child("friend_list").child("");

                        View view = navigationView.getHeaderView(0);

                        CircleImageView profileImageView = view.findViewById(R.id.user_profile_iv);
                        TextView usernameTV = view.findViewById(R.id.username_tv);
                        TextView emailTV = view.findViewById(R.id.email_tv);

                        usernameTV.setText(username);
                        emailTV.setText(userEmail);
                        if (userImage != null)
                            Picasso.get()
                                    .load(userImage)
                                    .placeholder(R.drawable.default_user_img)
                                    .error(R.drawable.default_user_img)
                                    .into(profileImageView);

                        Log.e("NewUserInfo",
                                "username = " + username + ", user email = " + userEmail
                                        + ", profile url = " + userImage);

                    } else {
                        // This is an existing user, show them a welcome back screen.
                        mUsersDBReference = mFirebaseDatabase.getReference()
                                .child(AppConstants.USERS_NODE).child(user.getUid());

                        mUsersDBReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String name = dataSnapshot.child("user_name").getValue().toString();
                                String status = dataSnapshot.child("user_status").getValue().toString();
                                String imageUrl = dataSnapshot.child("user_image").getValue().toString();
                                String email = dataSnapshot.child("user_email").getValue().toString();

                                View view = navigationView.getHeaderView(0);

                                CircleImageView profileImageView = view.findViewById(R.id.user_profile_iv);
                                TextView usernameTV = view.findViewById(R.id.username_tv);
                                TextView emailTV = view.findViewById(R.id.email_tv);

                                usernameTV.setText(name);
                                emailTV.setText(email);


                                if (!imageUrl.isEmpty())
                                    Picasso.get()
                                            .load(imageUrl)
                                            .placeholder(R.drawable.default_user_img)
                                            .error(R.drawable.default_user_img)
                                            .into(profileImageView);
                                Log.e("NewUserInfo",
                                        "username = " + name + ", user email = " + email
                                                + ", profile url = " + imageUrl);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
}
