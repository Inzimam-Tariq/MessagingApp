package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private List<FriendlyMessage> msgList;
    private String userId;

    public MessageAdapter(List<FriendlyMessage> objects, String userId) {
        this.msgList = objects;
        this.userId = userId;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                .item_message, viewGroup, false);

        Log.e("MainActivity", "onCreateViewHolder");

        return new MessageViewHolder(view1);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {


    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
