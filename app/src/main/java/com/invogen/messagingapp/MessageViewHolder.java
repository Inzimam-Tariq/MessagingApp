package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

public class MessageViewHolder extends ViewHolder {

    public TextView senderNameTV, msgBodyTV, msgTimeTV;
    public ImageView msgIV;
    public VideoView msgVV;

    public RelativeLayout playAudioLayout, messageMainLayout;
    public FloatingActionButton playBtn;
    public SeekBar seekBar;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);

        senderNameTV = itemView.findViewById(R.id.name_tv);
        msgBodyTV = itemView.findViewById(R.id.message_body_tv);
        msgTimeTV = itemView.findViewById(R.id.message_time_tv);

        msgIV = itemView.findViewById(R.id.message_img_iv);
        msgVV = itemView.findViewById(R.id.message_vv);
        playAudioLayout = itemView.findViewById(R.id.audio_play_layout);
        messageMainLayout = itemView.findViewById(R.id.message_main_layout);
        playBtn = itemView.findViewById(R.id.play_button);
        seekBar = itemView.findViewById(R.id.seek_bar);

    }
}
