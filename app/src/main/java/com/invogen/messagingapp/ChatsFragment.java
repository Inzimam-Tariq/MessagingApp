package com.invogen.messagingapp;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.AudioPickActivity;
import com.vincent.filepicker.activity.ImagePickActivity;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.activity.VideoPickActivity;
import com.vincent.filepicker.filter.entity.ImageFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.vincent.filepicker.activity.AudioPickActivity.IS_NEED_RECORDER;
import static com.vincent.filepicker.activity.VideoPickActivity.IS_NEED_CAMERA;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements View.OnClickListener {

    private final String TAG = "ChatsFragment";

    //    private DatabaseReference mDatabaseReference;
    private FirebaseRecyclerOptions<FriendlyMessage> options;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> adapter;

    private DatabaseReference mMessagesDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private EditText mMessageEditText;
    private FloatingActionButton mSendButton;
    private ImageButton attachmentBtn, cameraBtn;
    //    private LinearLayout mRevealView;
    private Intent intent;
    private ImageButton btnDocument, btnCamera, btnGallery, btnAudio, btnLocation, btnContact;
    private RelativeLayout attachmentContainer;
    private boolean reveal;

    private Context mContext;
    private String mUsername, userId;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        mContext = view.getContext();

        initViews(view);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mChatPhotosStorageReference = mFirebaseStorage.getReference().child(AppConstants.CHAT_IMAGES_NODE);
//        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.MESSAGES_NODE);
        mMessagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.MESSAGES_NODE);

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                    cameraBtn.setVisibility(View.GONE);
                } else {
                    mSendButton.setEnabled(false);
                    cameraBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedInInitialize(user.getDisplayName());
                    userId = user.getUid();
                    Log.e("ChatFrag", "User Id = " + userId);
                } else {
                    onSignedOutCleanup();
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
        progressBar.setVisibility(View.GONE);
        mMessageEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(AppConstants.DEFAULT_MSG_LENGTH_LIMIT)});


        options = new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                .setQuery(mMessagesDatabaseReference, FriendlyMessage.class).build();

        adapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position,
                                            @NonNull FriendlyMessage model) {
//                Random rnd = new Random();
//                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//                holder.nameTVLeft.setTextColor(color);
                String userUid = model.getUserId();
                Log.e("UserIdFromFirebase", "" + userUid);
                if (userUid != null && userUid.equals(userId)) {
                    holder.linearLayoutLeft.setVisibility(View.GONE);
                    holder.linearLayoutRight.setVisibility(View.VISIBLE);
                    holder.nameTVRight.setText(model.getName());
                    holder.msgTVRight.setText(model.getText());
                    if (model.getDate() != null)
                        holder.timeTVRight.setText(model.getDate());
                    progressBar.setVisibility(View.GONE);

                    if (model.getPhotoUrl() != null) {
                        Log.e("ImagePathDB", model.getPhotoUrl());
                        Picasso.get().load(model.getPhotoUrl())
                                .into(holder.imageViewRight);
                    }
                } else {
                    holder.linearLayoutLeft.setVisibility(View.VISIBLE);
                    holder.linearLayoutRight.setVisibility(View.GONE);
                    holder.nameTVLeft.setText(model.getName());
                    holder.msgTVLeft.setText(model.getText());
                    if (model.getDate() != null)
                        holder.timeTVLeft.setText(model.getDate());
                    progressBar.setVisibility(View.GONE);

                    if (model.getPhotoUrl() != null) {
                        Log.e("ImagePathDB", model.getPhotoUrl());
                        Picasso.get().load(model.getPhotoUrl())
                                .into(holder.imageViewLeft);
                    }
                }
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                        .item_message, viewGroup, false);

                Log.e("MainActivity", "onCreateViewHolder");

                return new MessageViewHolder(view1);
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(),
                1, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(adapter);
        return view;
    }

    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.recycle_view);
        progressBar = view.findViewById(R.id.progressBar);
        attachmentBtn = view.findViewById(R.id.btn_attachment);
        cameraBtn = view.findViewById(R.id.btn_camera_in_tv);
        mMessageEditText = view.findViewById(R.id.messageEditText);
        mSendButton = view.findViewById(R.id.sendButton);

        btnDocument = view.findViewById(R.id.btn_document);
        btnCamera = view.findViewById(R.id.btn_video);
        btnGallery = view.findViewById(R.id.btn_gallery);
        btnAudio = view.findViewById(R.id.btn_audio);
        btnLocation = view.findViewById(R.id.btn_location);
        btnContact = view.findViewById(R.id.btn_contact);
        attachmentContainer = view.findViewById(R.id.attachment_container);

        btnDocument.setVisibility(View.GONE);
        btnCamera.setVisibility(View.GONE);
        btnGallery.setVisibility(View.GONE);
        btnAudio.setVisibility(View.GONE);
        btnLocation.setVisibility(View.GONE);
        btnContact.setVisibility(View.GONE);
        attachmentContainer.setVisibility(View.GONE);

        mSendButton.setEnabled(false);

        registerClickListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.RC_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                ArrayList<ImageFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_IMAGE);
                assert data != null;
                Uri selectedImageUri = data.getData();
                assert selectedImageUri != null;
                String imgName = new File(selectedImageUri.getPath()).getName();
                final StorageReference photoRef = mChatPhotosStorageReference.child(new Date()
                        .getTime() + imgName);

                // upload file to Firebase Storage
                photoRef.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask
                        .TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, "Uploading your profile image to firebase...",
                                    Toast.LENGTH_LONG).show();
                            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    FriendlyMessage friendlyMessage =
                                            new FriendlyMessage(userId, mUsername, null, downloadUrl);
                                    mMessagesDatabaseReference.push().setValue(friendlyMessage);
                                    Toast.makeText(mContext, "DownloadURL " + downloadUrl,
                                            Toast.LENGTH_LONG).show();
                                    Log.e("DownloadURL", downloadUrl);
                                }
                            });
                        } else {
                            Toast.makeText(mContext, "Error occurred while storing profile in " +
                                            "database",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

    }

    private void onSignedInInitialize(String displayName) {
        mUsername = displayName;
    }

    private void onSignedOutCleanup() {
        mUsername = "Anonymous";
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealAttachments() {
        int cx = attachmentContainer.getWidth();
        int cy = attachmentContainer.getHeight();
        float finalRadius = (float) Math.hypot(cx, cy);
        if (!reveal) {
            reveal = true;
            Animator anim = ViewAnimationUtils.createCircularReveal(attachmentContainer, cx, cy, 0, finalRadius);
            attachmentContainer.setVisibility(View.VISIBLE);
            anim.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    btnContact.setVisibility(View.VISIBLE);
                    btnLocation.setVisibility(View.VISIBLE);
                    btnAudio.setVisibility(View.VISIBLE);
                    btnGallery.setVisibility(View.VISIBLE);
                    btnCamera.setVisibility(View.VISIBLE);
                    btnDocument.setVisibility(View.VISIBLE);
                    animateView(btnContact, 100);
                    animateView(btnLocation, 150);
                    animateView(btnAudio, 200);
                    animateView(btnGallery, 250);
                    animateView(btnCamera, 300);
                    animateView(btnDocument, 350);
                }

            });
            anim.start();
        } else {
            reveal = false;
            Animator anim = ViewAnimationUtils.createCircularReveal(attachmentContainer, cx, cy, finalRadius, 0);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    attachmentContainer.setVisibility(View.GONE);
                    btnDocument.setVisibility(View.GONE);
                    btnCamera.setVisibility(View.GONE);
                    btnGallery.setVisibility(View.GONE);
                    btnAudio.setVisibility(View.GONE);
                    btnLocation.setVisibility(View.GONE);
                    btnContact.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.start();
        }
    }

    void animateView(View view, int delay) {
        view.setScaleY(0f);
        view.setScaleX(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setDuration(delay)
                .setStartDelay(delay);
    }

    void registerClickListener() {
        attachmentBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        btnDocument.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnAudio.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        btnContact.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.btn_attachment: {
                Log.e(TAG, "Clicked attachment");
                revealAttachments();
                break;
            }
            case R.id.btn_camera_in_tv: {
                Log.e(TAG, "Clicked camera");
                createDirectoryAndOpenCamera();
                break;
            }
            case R.id.btn_document: {
                Log.e(TAG, "Clicked 2");
//                openItemPicker("doc/*", AppConstants.RC_PICK_IMAGE);
                intent = new Intent(mContext, NormalFilePickActivity.class);
                intent.putExtra(Constant.MAX_NUMBER, 9);
                intent.putExtra(NormalFilePickActivity.SUFFIX, new String[] {"xlsx", "xls", "doc", "docx", "ppt", "pptx", "pdf"});
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_FILE);
                break;
            }
            case R.id.btn_video: {
                Log.e(TAG, "Clicked 3");
                createDirectoryAndOpenCamera();
                intent = new Intent(mContext, VideoPickActivity.class);
                intent.putExtra(IS_NEED_CAMERA, true);
                intent.putExtra(Constant.MAX_NUMBER, 9);
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_VIDEO);
                break;
            }
            case R.id.btn_gallery: {
                intent = new Intent(mContext, ImagePickActivity.class);
                intent.putExtra(IS_NEED_CAMERA, true);
                intent.putExtra(Constant.MAX_NUMBER, 9);
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_IMAGE);
                Log.e(TAG, "Clicked 4");
//                openItemPicker("image/*", AppConstants.RC_PICK_IMAGE);
                break;
            }
            case R.id.btn_audio: {
                Log.e(TAG, "Clicked 5");
//                openItemPicker("audio/*", AppConstants.RC_PICK_IMAGE);
                intent = new Intent(mContext, AudioPickActivity.class);
                intent.putExtra(IS_NEED_RECORDER, true);
                intent.putExtra(Constant.MAX_NUMBER, 9);
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_AUDIO);
                break;
            }
            case R.id.btn_location: {
                Log.e(TAG, "Clicked 6");
                openItemPicker("location/*", AppConstants.RC_PICK_IMAGE);
                break;
            }
            case R.id.btn_contact: {
                Log.e(TAG, "Clicked 7");
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, AppConstants.RC_PICK_IMAGE);
//                openItemPicker("contact/*", AppConstants.RC_PICK_IMAGE);
                break;
            }
            case R.id.sendButton: {
                Log.e(TAG, "Clicked 8");
                Log.e("InsideSendBtn", "true");
                String msgText = mMessageEditText.getText().toString();

                FriendlyMessage friendlyMessage = new FriendlyMessage(
                        userId, mUsername, msgText, "urlhere");
                mMessagesDatabaseReference.push().setValue(friendlyMessage);
                // Clear input box
                Log.e("ChatFrag SendBtn", "User Id = " + userId);
                mMessageEditText.setText("");
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
                break;
            }
        }
    }

    private void createDirectoryAndOpenCamera() {
        Date mDate = new Date();
        File newfile;
        final String dirPath;
        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        if (StorageHelper.isExternalStorageReadableAndWritable()) {
            dirPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/picFolder/";
        } else {
            dirPath = "" + mContext.getCacheDir() + "/picFolder/";
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
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newfile));

        startActivityForResult(cameraIntent, AppConstants.CAPTURE_PHOTO_CODE);
    }

    void openItemPicker(String contentType, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType(contentType);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"),
                requestCode);
    }
}