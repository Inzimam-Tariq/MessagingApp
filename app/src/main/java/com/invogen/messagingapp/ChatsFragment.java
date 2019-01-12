package com.invogen.messagingapp;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {


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
    private FloatingActionButton mSendButton, mPhotoPickerButton;

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
                } else {
                    Toast.makeText(mContext, "You can't send empty message!", Toast.LENGTH_SHORT).show();
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Select Image
        pickImage();

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("InsideSendBtn", "true");
                FriendlyMessage friendlyMessage = new FriendlyMessage(
                        userId, mUsername, mMessageEditText.getText().toString(), "urlhere");
                mMessagesDatabaseReference.push().setValue(friendlyMessage);
                // Clear input box
                Log.e("ChatFrag SendBtn", "User Id = " + userId);
                mMessageEditText.setText("");
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
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
//                mRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
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

//        adapter.startListening();
        mRecyclerView.setAdapter(adapter);
//mRecyclerView.smoothScrollToPosition(options);
        return view;
    }

    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.recycle_view);
        progressBar = view.findViewById(R.id.progressBar);
        mPhotoPickerButton = view.findViewById(R.id.photoPickerButton);
        mMessageEditText = view.findViewById(R.id.messageEditText);
        mSendButton = view.findViewById(R.id.sendButton);
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


    private void pickImage() {
        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"),
                        AppConstants.RC_PICK_IMAGE);
            }
        });
    }


}