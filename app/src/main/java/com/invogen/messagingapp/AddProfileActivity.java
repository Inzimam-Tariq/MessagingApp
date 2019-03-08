package com.invogen.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Picasso;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.ImagePickActivity;
import com.vincent.filepicker.filter.entity.ImageFile;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.vincent.filepicker.activity.VideoPickActivity.IS_NEED_CAMERA;


public class AddProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "AddProfileActivity";

    private Toolbar mToolbar;
    private CircleImageView profileImageView;
    private TextView progressStatusTV, statusTV;
    private ProgressBar progressBar;
    private Button chooseImageBtn, saveProfileBtn;
    private EditText personNameET;
    //    private FirebaseAuth fbAuth;
    private DatabaseReference profilesDBReference;
    private StorageReference storageReference;
    private Uri imageUri;
    private Context mContext;
    private boolean isNewProfile = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);
        this.mContext = this;

        initViews();
        PicassoFaceDetector.initialize(this);
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_circle_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        profilesDBReference =
                FirebaseDatabase.getInstance().getReference().child(AppConstants.PROFILES_NODE);
        storageReference = FirebaseStorage.getInstance().getReference().child(AppConstants.PROFILE_IMAGES_NODE);
        profilesDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    String name = dataSnapshot.child("user_name").getValue().toString();
//                    String status = dataSnapshot.child("user_status").getValue().toString();
//                    String imageUrl = dataSnapshot.child("user_image").getValue().toString();
//                    String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

//                    usernameTV.setText(name);
//                    statusTV.setText(status);
//                    Picasso.get()
//                            .load(imageUrl)
//                            .resize(320, 320)
//                            .fit() // use fit() and centerInside() for making it memory efficient.
//                            .centerInside()
//                            .transform(new FaceCenterCrop(10, 10))
//                            .placeholder(R.drawable.default_user_img)
//                            .error(R.drawable.default_user_img)
//                            .into(profileImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initViews() {
        mToolbar = findViewById(R.id.main_toolbar);
        profileImageView = findViewById(R.id.iv_user_pic);
        progressStatusTV = findViewById(R.id.progress_bar_status_tv);
        progressBar = findViewById(R.id.progress_bar);
        personNameET = findViewById(R.id.person_name_et);
        chooseImageBtn = findViewById(R.id.choose_img_btn);
        saveProfileBtn = findViewById(R.id.save_btn);

        chooseImageBtn.setOnClickListener(this);
        saveProfileBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoFaceDetector.releaseDetector();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_CODE_PICK_IMAGE
                && resultCode == RESULT_OK && data != null) {
            final ArrayList<ImageFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_IMAGE);
            imageUri = Uri.fromFile(new File(list.get(0).getPath()));

            Log.e(TAG, "image uri = " + imageUri);
            Picasso.get()
                    .load(imageUri).noPlaceholder().centerCrop().fit()
                    .into((CircleImageView) findViewById(R.id.iv_user_pic));
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.choose_img_btn: {
                Intent intent = new Intent(mContext, ImagePickActivity.class);
                intent.putExtra(IS_NEED_CAMERA, true);
                intent.putExtra(Constant.MAX_NUMBER, 1);
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_IMAGE);
                break;
            }
            case R.id.save_btn: {
                progressBar.setVisibility(View.VISIBLE);
                progressStatusTV.setText("Updating Profile...");
                final String personName = personNameET.getText().toString().trim();
                final String pushKey = profilesDBReference.push().getKey();
                final StorageReference imagePath = storageReference.child(pushKey + ".jpg");

                if (imageUri != null) {
                    imagePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                progressStatusTV.setText("Uploading your profile photo to" +
                                        " firebase...");
                                imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        profilesDBReference.child(pushKey).setValue(new Users(personName, downloadUrl, "friends"))
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        progressStatusTV.setText("Profile Updated");
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                });
                                    }
                                });
                            } else {
                                progressStatusTV.setText("Error occurred while storing profile in database");
                            }
                        }
                    });
                }
                break;
            }
//            case R.id.choose_img_btn:{
//
//                break;
//            }
        }
    }
}
