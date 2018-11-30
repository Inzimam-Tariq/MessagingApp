package com.invogen.messagingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class ScanQRCodeActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mFirebaseDBRoot, mSenderUserFriendListDBReference,
            mReceiverUserFriendListDBReference;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);
        this.mContext = this;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDBRoot = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    new com.google.zxing.integration.android.IntentIntegrator(ScanQRCodeActivity.this).initiateScan();
                } else {
                    Toast.makeText(mContext, "Not SignedIn", Toast.LENGTH_LONG).show();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                final String scannedId = result.getContents();
                Toast.makeText(this, "Scanned Code = " + scannedId, Toast.LENGTH_LONG).show();
                progressDialog.setTitle("Connecting...");
                progressDialog.setMessage("Please wait while connection!");
                progressDialog.show();
                mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if (currentUser != null) {
                            mReceiverUserFriendListDBReference = mFirebaseDBRoot.child
                                    (AppConstants.USERS_NODE)
                                    .child(currentUser.getUid()).child("friend_list");
                            mReceiverUserFriendListDBReference.child(scannedId).setValue(true)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mSenderUserFriendListDBReference = mFirebaseDBRoot.child
                                                        ("users").child(scannedId).child
                                                        ("friend_list");
                                                mSenderUserFriendListDBReference.child
                                                        (currentUser.getUid()).setValue(true)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    progressDialog.dismiss();
                                                                    finish();
                                                                    Toast.makeText(mContext,
                                                                            "Request " +
                                                                                    "Successful!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(mContext, "Not SignedIn", Toast.LENGTH_LONG).show();
                        }
                    }
                };

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
