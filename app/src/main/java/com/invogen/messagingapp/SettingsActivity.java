package com.invogen.messagingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rohitarya.picasso.facedetection.transformation.FaceCenterCrop;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView usernameTV, statusTV;
    private Button changeProfileImageBtn, changeStatusBtn;
    private FirebaseAuth fbAuth;
    private DatabaseReference dbReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        PicassoFaceDetector.initialize(this);
        fbAuth = FirebaseAuth.getInstance();
        if (fbAuth != null) {
            if (fbAuth.getCurrentUser() != null) {
                String userId = fbAuth.getCurrentUser().getUid();
                dbReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                storageReference = FirebaseStorage.getInstance().getReference().child("Profile_Images");
                dbReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("user_name").getValue().toString();
                        String status = dataSnapshot.child("user_status").getValue().toString();
                        String imageUrl = dataSnapshot.child("user_image").getValue().toString();
                        String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                        usernameTV.setText(name);
                        statusTV.setText(status);
                        Picasso.get()
                                .load(imageUrl)
                                .resize(320, 320)
                                .fit() // use fit() and centerInside() for making it memory efficient.
                                .centerInside()
                                .transform(new FaceCenterCrop(10, 10))
                                .placeholder(R.drawable.default_user_img)
                                .error(R.drawable.default_user_img)
                                .into(profileImageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                changeProfileImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent galleryIntent = new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, 100);
                    }
                });
            }
        }

    }

    private void initViews() {
        profileImageView = findViewById(R.id.iv_user_pic);
        usernameTV = findViewById(R.id.tv_username);
        statusTV = findViewById(R.id.tv_user_status);
        changeProfileImageBtn = findViewById(R.id.change_pic_btn);
        changeStatusBtn = findViewById(R.id.change_status_btn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoFaceDetector.releaseDetector();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (fbAuth != null) {
                if (fbAuth.getCurrentUser() != null) {
                    String userId = fbAuth.getCurrentUser().getUid();
                    final StorageReference imagePath = storageReference.child(userId + ".jpg");
                    if (imageUri != null) {
                        imagePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Uploading your profile image to firebase...",
                                            Toast.LENGTH_LONG).show();
                                    imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String downloadUrl = uri.toString();
                                            dbReference.child("user_image").setValue(downloadUrl)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(getApplicationContext(), "Profile image uploaded!",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    });
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error occurred while storing profile in database",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        }

    }
}
