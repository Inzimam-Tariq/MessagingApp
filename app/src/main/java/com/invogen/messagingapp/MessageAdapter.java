package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private List<FriendlyMessage> msgDtoList;
    private String userId;

    public MessageAdapter(List<FriendlyMessage> objects, String userId) {
        this.msgDtoList = objects;
        this.userId = userId;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
