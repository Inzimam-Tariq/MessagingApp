package com.invogen.messagingapp;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.AudioPickActivity;
import com.vincent.filepicker.activity.ImagePickActivity;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.activity.VideoPickActivity;
import com.vincent.filepicker.filter.entity.AudioFile;
import com.vincent.filepicker.filter.entity.ImageFile;
import com.vincent.filepicker.filter.entity.NormalFile;
import com.vincent.filepicker.filter.entity.VideoFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.vincent.filepicker.activity.AudioPickActivity.IS_NEED_RECORDER;
import static com.vincent.filepicker.activity.VideoPickActivity.IS_NEED_CAMERA;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupChatFragment extends Fragment implements View.OnClickListener, MessageAdapter.AdapterLongClickCallback {

    @Override
    public void onMethodLongClickCallback(int position) {
        this.itemPosition = position;
        messageSelectedOptionBarLayout.setVisibility(View.VISIBLE);
        Log.e(TAG, "You selected item at position = " + position);
    }


    private final String TAG = "GroupChatsFragment";
    private DatabaseReference mMessagesDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatDocsStorageReference, mChatVideosStorageReference,
            mChatPhotosStorageReference, mChatAudiosStorageReference;

    private RecyclerView mRecyclerView;
//    private ProgressBar progressBar;
    private EditText mMessageEditText;
    private FloatingActionButton mSendButton;
    private ImageButton attachmentBtn, cameraBtn;
    private List<FriendlyMessage> messageList;
    private List<String> messageKeyList;

    private Intent intent;
    private ImageButton btnDocument, btnCamera, btnGallery, btnAudio, btnLocation, btnContact;
    private RelativeLayout attachmentContainer;
    private boolean reveal;

    private Context mContext;
    private String mUsername, userId;
    private String phoneNo, name;
    private int i, itemPosition;

    // messageSelectedOptionBar
    private RelativeLayout messageSelectedOptionBarLayout;
    private ImageView closeBtnIV, forwardBtnIV, deleteBtnIV;

    private boolean isInSelectionMode;


    public GroupChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_group_chat, container, false);
        this.mContext = view.getContext();

        initViews(view);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mChatDocsStorageReference = mFirebaseStorage.getReference().child(AppConstants.CHAT_DOCS_NODE);
        mChatVideosStorageReference = mFirebaseStorage.getReference().child(AppConstants.CHAT_VIDEOS_NODE);
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child(AppConstants.CHAT_IMAGES_NODE);
        mChatAudiosStorageReference = mFirebaseStorage.getReference().child(AppConstants.CHAT_AUDIOS_NODE);
        mMessagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.COMMON_CHAT_ROOM_NODE);
        mMessagesDatabaseReference.keepSynced(true);

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

        authenticateUser();

        return view;
    }

    private void authenticateUser() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedInInitialize(user.getDisplayName());
                    userId = user.getUid();
                    Log.e(TAG, "User Id = " + userId);
                    if (!AppConstants.getCurrentUserUid().equals(""))
                        AppConstants.setCurrentUserUid(userId);
                    setChatData();
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
//        progressBar.setVisibility(View.GONE);
        mMessageEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(AppConstants.DEFAULT_MSG_LENGTH_LIMIT)});

    }

    private void setChatData() {
        messageList = new ArrayList<>();
        messageKeyList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new MessageAdapter(messageList, this));
        mMessagesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                if (dataSnapshot.exists()) {
                    Log.e(TAG, "Snapshot Exits");
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        FriendlyMessage msg = ds.getValue(FriendlyMessage.class);
                        messageKeyList.add(ds.getKey());

                        Log.e(TAG, "Message Key = " + ds.getKey());
                        messageList.add(msg);
                    }
                }
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.recycle_view);
//        progressBar = view.findViewById(R.id.progressBar);
        attachmentBtn = view.findViewById(R.id.btn_attachment);
        cameraBtn = view.findViewById(R.id.btn_camera_in_et);
        mMessageEditText = view.findViewById(R.id.messageEditText);
        mSendButton = view.findViewById(R.id.sendButton);

        btnDocument = view.findViewById(R.id.btn_document);
        btnCamera = view.findViewById(R.id.btn_video);
        btnGallery = view.findViewById(R.id.btn_gallery);
        btnAudio = view.findViewById(R.id.btn_audio);
        btnLocation = view.findViewById(R.id.btn_location);
        btnContact = view.findViewById(R.id.btn_contact);
        attachmentContainer = view.findViewById(R.id.attachment_container);

        messageSelectedOptionBarLayout = view.findViewById(R.id.selection_option_layout);
        closeBtnIV = view.findViewById(R.id.close_ib);
        forwardBtnIV = view.findViewById(R.id.forward_ib);
        deleteBtnIV = view.findViewById(R.id.delete_ib);

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

    private void registerClickListener() {
        attachmentBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        btnDocument.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnAudio.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        btnContact.setOnClickListener(this);
        mSendButton.setOnClickListener(this);

        closeBtnIV.setOnClickListener(this);
        forwardBtnIV.setOnClickListener(this);
        deleteBtnIV.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAuthStateListener != null) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
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

    private void createDirectoryAndOpenCamera() {
        Date mDate = new Date();
        File newfile;
        final String dirPath;
        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        if (StorageHelper.isExternalStorageReadableAndWritable()) {
            dirPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/AI Eye Pics/";
        } else {
            dirPath = "" + mContext.getCacheDir() + "/AI Eye Pics/";
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.btn_attachment: {
                Log.e(TAG, "Clicked attachment");
                revealAttachments();
                break;
            }
            case R.id.btn_camera_in_et: {
                Log.e(TAG, "Clicked camera");
                createDirectoryAndOpenCamera();

                reveal = true;
                revealAttachments();
                break;
            }
            case R.id.btn_document: {
                Log.e(TAG, "Clicked 2");
//                openItemPicker("doc/*", AppConstants.RC_PICK_IMAGE);
                intent = new Intent(mContext, NormalFilePickActivity.class);
                intent.putExtra(Constant.MAX_NUMBER, 9);
                intent.putExtra(NormalFilePickActivity.SUFFIX, new String[]{"xlsx", "xls", "doc", "docx", "ppt", "pptx", "pdf"});
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_FILE);
                reveal = true;
                revealAttachments();
                break;
            }
            case R.id.btn_video: {
                Log.e(TAG, "Clicked 3");
//                createDirectoryAndOpenCamera();
                intent = new Intent(mContext, VideoPickActivity.class);
                intent.putExtra(IS_NEED_CAMERA, true);
                intent.putExtra(Constant.MAX_NUMBER, 9);
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_VIDEO);
                reveal = true;
                revealAttachments();
                break;
            }
            case R.id.btn_gallery: {
                intent = new Intent(mContext, ImagePickActivity.class);
                intent.putExtra(IS_NEED_CAMERA, true);
                intent.putExtra(Constant.MAX_NUMBER, 9);
                startActivityForResult(intent, Constant.REQUEST_CODE_PICK_IMAGE);
                Log.e(TAG, "Clicked 4");
                reveal = true;
                revealAttachments();
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
                reveal = true;
                revealAttachments();
                break;
            }
            case R.id.btn_location: {
                Log.e(TAG, "Clicked 6");
//                openItemPicker("location/*", AppConstants.RC_PICK_IMAGE);
                reveal = true;
                revealAttachments();
                break;
            }
            case R.id.btn_contact: {
                Log.e(TAG, "Clicked 7");
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, AppConstants.RC_PICK_CONTACT);
                reveal = true;
                revealAttachments();
//                openItemPicker("contact/*", AppConstants.RC_PICK_IMAGE);
                break;
            }
            case R.id.sendButton: {
                Log.e("InsideSendBtn", "true");
                String msgText = mMessageEditText.getText().toString();

                FriendlyMessage friendlyMessage =
                        new FriendlyMessage(userId, mUsername, "plain", msgText);

                mMessagesDatabaseReference.push().setValue(friendlyMessage);
                // Clear input box
                mMessageEditText.setText("");
                break;
            }
            case R.id.close_ib: {
                Log.e("InsideCloseBtn", "true");
                messageSelectedOptionBarLayout.setVisibility(View.GONE);

                break;
            }
            case R.id.forward_ib: {
                Log.e("InsideCloseBtn", "true");
                messageSelectedOptionBarLayout.setVisibility(View.GONE);
                break;
            }
            case R.id.delete_ib: {

                Log.e("InsideDeleteBtn",
                        "true position = " + itemPosition + "messageId = " + messageKeyList.get(itemPosition));
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(itemPosition);
                        if (null != holder) {
                            holder.itemView.setBackgroundColor(Color.parseColor("#00f49828"));
                        }
                    }
                }, 50);
                messageSelectedOptionBarLayout.setVisibility(View.GONE);
//                mMessagesDatabaseReference.child(messageKeyList.get(itemPosition)).removeValue();

                mMessagesDatabaseReference.child(messageKeyList.get(itemPosition)).child(
                        "removedBy").setValue(AppConstants.getCurrentUserUid());
                mMessagesDatabaseReference.child(messageKeyList.get(itemPosition)).child(
                        "isRemoved").setValue(true);
                mRecyclerView.getAdapter().notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null)
            if (resultCode == RESULT_OK) {
                if (requestCode == Constant.REQUEST_CODE_PICK_IMAGE) {
                    final ArrayList<ImageFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_IMAGE);
                    Log.e(TAG, "image list size = " + list.size());

                    for (i = 0; i < list.size(); i++) {
                        final StorageReference photoRef = mChatPhotosStorageReference.child(list.get(i).getName());
                        Uri file = Uri.fromFile(new File(list.get(i).getPath()));
                        final String fileName = list.get(i).getName();
                        final float fileSize = list.get(i).getSize();

                        // upload file to Firebase Storage
                        photoRef.putFile(file).addOnCompleteListener(new OnCompleteListener<UploadTask
                                .TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {

                                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String downloadUrl = uri.toString();

                                            Map<String, FileMessageAttributes> fileMessageAttributesMap
                                                    = new HashMap<>();
                                            fileMessageAttributesMap.put("fileProperties", new
                                                    FileMessageAttributes(fileName,
                                                    downloadUrl, fileSize));
                                            FriendlyMessage friendlyMessage =
                                                    new FriendlyMessage(userId, mUsername, "image",
                                                            fileMessageAttributesMap);
                                            mMessagesDatabaseReference.push().setValue(friendlyMessage);

                                            Log.e("DownloadURL", downloadUrl);
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "Error occurred while storing profile in database");
                                }
                            }
                        });
                    }
                } else if (requestCode == Constant.REQUEST_CODE_PICK_FILE) {
                    final ArrayList<NormalFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
                    Log.e(TAG, "docs list size = " + list.size());

                    for (i = 0; i < list.size(); i++) {
                        final String fileName = list.get(i).getName();
                        final float fileSize = list.get(i).getSize();
                        final StorageReference docsRef = mChatDocsStorageReference.child(fileName);
                        Uri file = Uri.fromFile(new File(list.get(i).getPath()));

                        // upload file to Firebase Storage
                        docsRef.putFile(file).addOnCompleteListener(new OnCompleteListener<UploadTask
                                .TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {

                                    docsRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String downloadUrl = uri.toString();

                                            Map<String, FileMessageAttributes> fileMessageAttributesMap
                                                    = new HashMap<>();
                                            fileMessageAttributesMap.put("fileProperties", new
                                                    FileMessageAttributes(fileName,
                                                    downloadUrl, fileSize));
                                            FriendlyMessage friendlyMessage =
                                                    new FriendlyMessage(userId, mUsername, "docs",
                                                            fileMessageAttributesMap);
                                            mMessagesDatabaseReference.push().setValue(friendlyMessage);

                                            Log.e("DownloadURL", downloadUrl);
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "Error occurred while storing profile in database");
                                }
                            }
                        });
                    }

                } else if (requestCode == Constant.REQUEST_CODE_PICK_VIDEO) {
                    final ArrayList<VideoFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_VIDEO);
                    Log.e(TAG, "videos list size = " + list.size());

                    for (i = 0; i < list.size(); i++) {
                        final String fileName = list.get(i).getName();
                        final float fileSize = list.get(i).getSize();
                        final StorageReference videosRef = mChatVideosStorageReference.child(fileName);
                        Uri file = Uri.fromFile(new File(list.get(i).getPath()));

                        // upload file to Firebase Storage
                        videosRef.putFile(file).addOnCompleteListener(new OnCompleteListener<UploadTask
                                .TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {

                                    videosRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String downloadUrl = uri.toString();

                                            Map<String, FileMessageAttributes> fileMessageAttributesMap
                                                    = new HashMap<>();
                                            fileMessageAttributesMap.put("fileProperties", new
                                                    FileMessageAttributes(fileName,
                                                    downloadUrl, fileSize));
                                            FriendlyMessage friendlyMessage =
                                                    new FriendlyMessage(userId, mUsername, "video",
                                                            fileMessageAttributesMap);
                                            mMessagesDatabaseReference.push().setValue(friendlyMessage);

                                            Log.e("DownloadURL", downloadUrl);
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "Error occurred while storing video in database");
                                }
                            }
                        });
                    }

                } else if (requestCode == Constant.REQUEST_CODE_PICK_AUDIO) {
                    final ArrayList<AudioFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_AUDIO);
                    Log.e(TAG, "videos list size = " + list.size());

                    for (i = 0; i < list.size(); i++) {
                        final String fileName = list.get(i).getName();
                        final float fileSize = list.get(i).getSize();
                        final StorageReference audiosRef = mChatAudiosStorageReference.child(fileName);
                        Uri file = Uri.fromFile(new File(list.get(i).getPath()));

                        // upload file to Firebase Storage
                        audiosRef.putFile(file).addOnCompleteListener(new OnCompleteListener<UploadTask
                                .TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {

                                    audiosRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String downloadUrl = uri.toString();

                                            Map<String, FileMessageAttributes> fileMessageAttributesMap
                                                    = new HashMap<>();
                                            fileMessageAttributesMap.put("fileProperties", new
                                                    FileMessageAttributes(fileName,
                                                    downloadUrl, fileSize));
                                            FriendlyMessage friendlyMessage =
                                                    new FriendlyMessage(userId, mUsername, "audio",
                                                            fileMessageAttributesMap);
                                            mMessagesDatabaseReference.push().setValue(friendlyMessage);

                                            Log.e("DownloadURL", downloadUrl);
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "Error occurred while storing audio in database");
                                }
                            }
                        });
                    }

                } else if (requestCode == AppConstants.RC_PICK_CONTACT) {

                    Uri uri = data.getData();
                    Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

                    if (cursor.moveToFirst()) {
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                        phoneNo = cursor.getString(phoneIndex);
                        name = cursor.getString(nameIndex);

                        FriendlyMessage friendlyMessage =
                                new FriendlyMessage(userId, mUsername, "plain",
                                        "Name: " + name + "\nCell  #: " + phoneNo);
                        mMessagesDatabaseReference.push().setValue(friendlyMessage);

                        Log.e(TAG,
                                "onActivityResult(), " + phoneIndex + ", " + phoneNo + ", " + nameIndex + ", " + name);
                    }
                    cursor.close();
                }
            }

    }

    public static void onBackPressed() {


    }

}
