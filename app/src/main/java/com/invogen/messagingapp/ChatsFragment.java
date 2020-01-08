package com.invogen.messagingapp;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private final String TAG = "ChatsFragment";
    private DatabaseReference dbReferenceUsers;
    private FloatingActionButton fabCreate;
    private RecyclerView mRecyclerView;
    private List<Chats> chatRoomChatsList = new ArrayList<>();
    private List<String> chatRoomKeysList = new ArrayList<>();
    private List<String> mSelectedContacts;
    private Context mContext;
    private String chatName;
    private DatabaseReference mChatsDBReference;
    private String pushKey;
    private List<String> usernameList, userUIdList;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        this.mContext = view.getContext();
        initViews(view);

        mChatsDBReference = FirebaseDatabase.getInstance().getReference().child(AppConstants.CHATS_NODE);
        dbReferenceUsers = FirebaseDatabase.getInstance().getReference().child(AppConstants.USERS_NODE);
        getUsersData();
        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                    showContacts();

//                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext,
//                            R.style.Theme_AppCompat_Light_Dialog_Alert);
//                    builder.setTitle("Create Chat!");
//
//                    final EditText input_et = new EditText(mContext);
//                    builder.setView(input_et);
//                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            chatName = input_et.getText().toString();
//
//                            Chats chats = new Chats(chatName);
//
//                            pushKey = mChatsDBReference.push().getKey();
//                            mChatsDBReference.child(pushKey).setValue(chats);
//                            mChatsDBReference.child(pushKey).child("messages").push().setValue(new FriendlyMessage(
//                                    "DP8LZ4XnAPOrb9x5fdgj8SBPrIs2",
//                                    "Inzimam Tariq", AppConstants.PLAIN_MESSAGE, "Hello, this is test " +
//                                    "message"));
//                        }
//                    });
//                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });

//                builder.show();

            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new ChatAdapter(chatRoomChatsList, chatRoomKeysList));
        mChatsDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatRoomChatsList.clear();
                chatRoomKeysList.clear();
                if (dataSnapshot.exists()) {
                    Log.e(TAG, "Snapshot Exits\nValue = " + dataSnapshot.toString());
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Chats chat = ds.getValue(Chats.class);

                        Log.e(TAG, "Chat Key = " + ds.getKey() + "\n DataSnapshot = " + ds.toString());
                        chatRoomChatsList.add(chat);
                        chatRoomKeysList.add(ds.getKey());
                    }
                }
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void initViews(View view) {
        mRecyclerView = view.findViewById(R.id.recycle_view);
        fabCreate = view.findViewById(R.id.fab);

        AppUtils.hideKeyboard(getActivity());
    }

    void showContacts() {
        mSelectedContacts = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,
                R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle("Select Contact/s!");
        builder.setCancelable(false);
        final String usernames[] = usernameList.toArray(new String[0]);
        builder.setMultiChoiceItems(usernames, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                if (isChecked) {
                    mSelectedContacts.add(usernames[which]);
                } else if (mSelectedContacts.contains(usernames[which])) {
                    mSelectedContacts.remove(usernames[which]);
                }
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String finalSelection = "";
                for (String item : mSelectedContacts) {
                    finalSelection += "\n" + item;
                }
                Toast.makeText(mContext, "You Selected: " + finalSelection,
                        Toast.LENGTH_LONG).show();
//                if (mSelectedContacts.size() > 1) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext,
//                            R.style.Theme_AppCompat_Light_Dialog_Alert);
//                    builder.setTitle("Chat Title!");
//                    builder.setCancelable(false);
//                    final EditText input_et = new EditText(mContext);
//                    builder.setView(input_et);
//                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            chatName = input_et.getText().toString().trim();
//                            if (!chatName.isEmpty()) {
//                                Chats chats = new Chats(chatName);
//
//                                pushKey = mChatsDBReference.push().getKey();
//                                mChatsDBReference.child(pushKey).setValue(chats);
////                            mChatsDBReference.child("messages").push().setValue(new FriendlyMessage(
////                                    "hVxwTSi4NES74spM44t9gmJS1lJ3"
////                                    ,"Inzimam Tariq"
////                                    ,"text"
////                                    ,"This is test message!"
////                            ));
////                                for (int i = 0; i < userUIdList.size(); i++) {
////                                    mChatsDBReference.child(pushKey).child("users").push().setValue(
////                                            new Users(userUIdList.get(i)
////                                                    , usernameList.get(i)));
////                                }
//                            }
//                        }
//                    });
//                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    builder.show();
//                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }

    void getUsersData() {
        usernameList = new ArrayList<>();
        userUIdList = new ArrayList<>();
        dbReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot users : dataSnapshot.getChildren()) {
                    Users user = users.getValue(Users.class);
                    usernameList.add(user.getUser_name());
                    userUIdList.add(users.getKey());
                    Log.e(TAG, "Username = " + user.getUser_name());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
