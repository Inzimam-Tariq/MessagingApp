package com.invogen.messagingapp;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
    private FloatingActionButton fabCreate;
    private RecyclerView mRecyclerView;
    private List<Chats> chatRoomList = new ArrayList<>();
    private Context mContext;
    private String chatName;
    private DatabaseReference mDBReferenceChats;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        this.mContext = view.getContext();
        initViews(view);

        mDBReferenceChats = FirebaseDatabase.getInstance().getReference().child(AppConstants.CHATS_NODE);
        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext,
                        R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle("Create Chat!");

                final EditText input_et = new EditText(mContext);
                builder.setView(input_et);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chatName = input_et.getText().toString();
                        Chats chats = new Chats(chatName);

                        String pushKey = mDBReferenceChats.push().getKey();
                        mDBReferenceChats.child(pushKey).setValue(chats);
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
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new ChatAdapter(chatRoomList));
        mDBReferenceChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatRoomList.clear();
                if (dataSnapshot.exists()) {
                    Log.e(TAG, "Snapshot Exits\nValue = " + dataSnapshot.toString());
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Chats chat = ds.getValue(Chats.class);

                        Log.e(TAG, "Message Key = " + ds.getKey());
                        chatRoomList.add(chat);
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
    }

}
