package com.invogen.messagingapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> implements View.OnClickListener {

    private String TAG = "MessageAdapter";
    private List<FriendlyMessage> msgList;
    private MediaPlayer player;
    private Handler seekHandler;
    private boolean isPlaying, playReady;
    private SeekBar seek_bar;
    private FloatingActionButton play_button;
    private Context mContext;
    private RelativeLayout mainLayout;

    public MessageAdapter(List<FriendlyMessage> messageList) {
        this.msgList = messageList;

    }

    @Override
    public int getItemViewType(int position) {

//        String currentUserUid = AppConstants.getCurrentUserUid();
        FriendlyMessage message = msgList.get(position);
//        String senderId = message.getSenderId();
        boolean isSenderCurrentUser =
                AppConstants.getCurrentUserUid().equals(message.getSenderId());
        String msgType = message.getMsgType();

        if (isSenderCurrentUser) {
            if (msgType.equals(AppConstants.DOC_MESSAGE)) return R.layout.my_message_plain;
            else if (msgType.equals(AppConstants.VIDEO_MESSAGE)) return R.layout.my_message_video;
            else if (msgType.equals(AppConstants.IMAGE_MESSAGE)) return R.layout.my_message_image;
            else if (msgType.equals(AppConstants.AUDIO_MESSAGE)) return R.layout.my_message_audio;
            else return R.layout.my_message_plain;
        } else {
            if (msgType.equals(AppConstants.DOC_MESSAGE)) return R.layout.their_message_plain;
            else if (msgType.equals(AppConstants.VIDEO_MESSAGE))
                return R.layout.their_message_video;
            else if (msgType.equals(AppConstants.IMAGE_MESSAGE))
                return R.layout.their_message_image;
            else if (msgType.equals(AppConstants.AUDIO_MESSAGE))
                return R.layout.their_message_audio;
            else return R.layout.their_message_plain;
        }

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemViewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemViewType, viewGroup, false);
        Log.e(TAG, "onCreateView LayoutId = " + itemViewType + " Inside MyLayout");

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int position) {

        String currentUserUid = AppConstants.getCurrentUserUid();
        mContext = messageViewHolder.msgTimeTV.getContext();
        mainLayout = messageViewHolder.messageMainLayout;

        FriendlyMessage message = msgList.get(position);
        String senderId = message.getSenderId();
        String msgType = message.getMsgType();

        boolean isSenderCurrentUser = senderId.equals(currentUserUid);
        Log.e(TAG,
                "onBindView, Message = " + position + "\nsenderId = " + senderId + "\ncurUserId = " + currentUserUid);

        if (!isSenderCurrentUser) {
            messageViewHolder.senderNameTV.setText(message.getSenderName());
        }
        messageViewHolder.msgTimeTV.setText(message.getMsgDate());
        Log.e(TAG, "msgType = " + msgType);
        if (msgType.equals("plain")) {
            messageViewHolder.msgBodyTV.setText(message.getMsgText());
        } else {

            FileMessageAttributes fileMessageAttributes = message
                    .getFileMessageAttributesMap().get("fileProperties");
            String remoteFilePath = fileMessageAttributes.getFilePath();
            switch (msgType) {
                case "image":
                    if (remoteFilePath != null) {
                        Log.e("ImagePathDB", remoteFilePath);
                        Picasso.get().load(remoteFilePath)//.networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.loading)
                                .into(messageViewHolder.msgIV);
                    }
                    break;
                case "video":
                    if (remoteFilePath != null) {
                        Log.e("VideoPathDB", remoteFilePath);
                        VideoView videoView = messageViewHolder.msgVV;

                        try {
                            // Start the MediaController
                            MediaController mediacontroller =
                                    new MediaController(mContext);
                            mediacontroller.setAnchorView(videoView);

                            Uri videoUri = Uri.parse(remoteFilePath);
                            videoView.setMediaController(mediacontroller);
                            videoView.setVideoURI(videoUri);
                            videoView.seekTo(10);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "doc":

                    break;
                case "audio":
                    seek_bar = messageViewHolder.seekBar;
                    play_button = messageViewHolder.playBtn;
                    setupMediaPlayer(remoteFilePath);
                    play_button.setOnClickListener(this);
                    break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    private void setupMediaPlayer(String filePath) {
        try {
            player = new MediaPlayer();
            seekHandler = new Handler();
            player.setDataSource(filePath);
            player.setLooping(false);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seek_bar.setMax(player.getDuration());
                    playReady = true;
                }
            });
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Runnable run = new Runnable() {

        @Override
        public void run() {
            updateSeekProgress();
        }
    };

    public void updateSeekProgress() {
        seek_bar.setProgress(player.getCurrentPosition());
        seekHandler.postDelayed(run, 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_button:
                Log.e(TAG, "Inside Play Clicked");
                if (!isPlaying) {
                    if (playReady) {
                        isPlaying = true;
                        player.start();
                        play_button.setImageResource(R.drawable.ic_pause_circle_white);
                        updateSeekProgress();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                seekHandler.removeCallbacks(run);
                                play_button.setImageResource(R.drawable.ic_play_circle_white);
                                isPlaying = false;
                            }
                        });
                    } else
                        Snackbar.make(mainLayout, "Preparing Media Please click again after some " +
                                "seconds", Snackbar.LENGTH_LONG).show();
                } else {
                    isPlaying = false;
                    play_button.setImageResource(R.drawable.ic_play_circle_white);
                    player.pause();
                }
                break;
        }
    }
}
