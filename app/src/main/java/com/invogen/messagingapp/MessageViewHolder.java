package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MessageViewHolder extends ViewHolder {


    public RelativeLayout theirMsgLayout, myMsgLayout;
    public TextView senderNameTV, msgBodyTV, msgTimeTV;
    public ImageView msgIV;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);

        theirMsgLayout = itemView.findViewById(R.id.their_msg_layout);
        myMsgLayout = itemView.findViewById(R.id.my_msg_layout);

        senderNameTV = itemView.findViewById(R.id.name_tv);
        msgBodyTV = itemView.findViewById(R.id.message_body_tv);
        msgTimeTV = itemView.findViewById(R.id.message_time_tv);

        msgIV = itemView.findViewById(R.id.message_img_iv);

    }
}
