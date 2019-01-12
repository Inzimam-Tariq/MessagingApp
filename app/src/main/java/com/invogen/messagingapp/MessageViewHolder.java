package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageViewHolder extends ViewHolder {

    public TextView msgTVLeft, nameTVLeft, timeTVLeft, msgTVRight, nameTVRight, timeTVRight;
    public ImageView imageViewLeft, imageViewRight;
    public LinearLayout linearLayoutLeft, linearLayoutRight;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        linearLayoutLeft = itemView.findViewById(R.id.layout_left);
        msgTVLeft = itemView.findViewById(R.id.tv_message_left);
        nameTVLeft = itemView.findViewById(R.id.tv_name_left);
        imageViewLeft = itemView.findViewById(R.id.iv_left);
        timeTVLeft = itemView.findViewById(R.id.tv_time_left);

        linearLayoutRight = itemView.findViewById(R.id.layout_right);
        msgTVRight = itemView.findViewById(R.id.tv_message_right);
        nameTVRight = itemView.findViewById(R.id.tv_name_right);
        imageViewRight = itemView.findViewById(R.id.iv_right);
        timeTVRight = itemView.findViewById(R.id.tv_time_right);
    }
}
