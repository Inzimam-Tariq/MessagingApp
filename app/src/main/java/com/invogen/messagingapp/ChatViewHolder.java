package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class ChatViewHolder extends RecyclerView.ViewHolder {

    public TextView chatTitleTV, timeTV, avatarTV, lastMessageTV;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);

        chatTitleTV = itemView.findViewById(R.id.chat_title_tv);
        timeTV = itemView.findViewById(R.id.chat_time_tv);
        lastMessageTV = itemView.findViewById(R.id.chat_last_message_tv);
        avatarTV = itemView.findViewById(R.id.avatar_tv);
    }
}
