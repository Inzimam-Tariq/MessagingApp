package com.invogen.messagingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class MainActivityViewer extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mProfilesDBReference, mUsersDBReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    private Toolbar mToolbar;
    private Button startCapturingBtn;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_viewer);
        this.mContext = this;

        initViews();
        initFirebaseObjects();

        setupAuth();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        checkPermissions();

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("AI Eye");

        startCapturingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri outputImageFileUri = createDirectoryForImageFiles();
                startCameraActivity(outputImageFileUri);
            }
        });
    }

    private void initViews() {
        mToolbar = findViewById(R.id.main_toolbar);
        startCapturingBtn = findViewById(R.id.btn_start_capture);
    }

    private void initFirebaseObjects() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void setupAuth() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Toast.makeText(mContext, "Listener Active", Toast.LENGTH_SHORT).show();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(mContext, "User Active", Toast.LENGTH_SHORT).show();
                    FirebaseUserMetadata metadata = firebaseAuth.getCurrentUser().getMetadata();
                    Log.e("ActivityViewer", "\ncreation timestamp = " + metadata
                            .getCreationTimestamp() + "\nlast sign in timestamp = "
                            + metadata.getLastSignInTimestamp());
                    if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                        // The user is new, show them a fancy intro screen!
                        mProfilesDBReference = mFirebaseDatabase.getReference()
                                .child(AppConstants.PROFILE_NODE).child(user.getUid());

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
                        mUsersDBReference.child("user_type").setValue(AppConstants.USER_TYPE);
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
        }

        ;
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission
                .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        Permissions.check(this/*context*/, permissions, null/*rationale*/,
                null/*options*/, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        // do your task.
                    }
                });
    }

    private Uri createDirectoryForImageFiles() {
        Date mDate = new Date();
        File newfile;
        final String dirPath;
        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        if (StorageHelper.isExternalStorageReadableAndWritable()) {
            dirPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/picFolder/";
        } else {
            dirPath = "" + getCacheDir() + "/picFolder/";
        }
        File newdir = new File(dirPath);
        boolean mkdirs = newdir.mkdirs();
        String file = dirPath + mDate.getTime() + ".jpg";
        newfile = new File(file);
        try {
            boolean newFile = newfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(newfile);
    }


    private void startCameraActivity(Uri outputFileUri) {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, AppConstants.CAPTURE_PHOTO_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_controller, menu);
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
                            startActivity(new Intent(MainActivityViewer.this,
                                    UserTypeActivity.class));
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
        return true;//super.onOptionsItemSelected(item);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.CAPTURE_PHOTO_CODE && resultCode == RESULT_OK) {
            Log.d("CameraDemo", "Pic saved");
        }
    }

}
