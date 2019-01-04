package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageViewHolder extends ViewHolder {

    public TextView msgTV, nameTV, timeTV;
    public ImageView imageView;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        msgTV = itemView.findViewById(R.id.messageTextView);
        nameTV = itemView.findViewById(R.id.nameTextView);
        imageView = itemView.findViewById(R.id.photoImageView);
        timeTV = itemView.findViewById(R.id.tv_time);
    }
}
