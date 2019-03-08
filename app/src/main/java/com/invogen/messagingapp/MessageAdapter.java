package com.invogen.messagingapp;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.vincan.medialoader.MediaLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder>
        implements View.OnClickListener {

    private AdapterLongClickCallback mAdapterLongClickCallback;

    public interface AdapterLongClickCallback {
        void onMethodLongClickCallback(int position);
    }


    private String TAG = "MessageAdapter";
    private List<FriendlyMessage> msgList;
    private MediaPlayer player;
    private Handler seekHandler;
    private boolean isPlaying, playReady;
    private SeekBar seek_bar;
    private FloatingActionButton play_button;
    private Context mContext;
    private RelativeLayout mainLayout;
    int selectedPosition = -1;
//    private boolean isSourceCommonChatRoom = true;


    public MessageAdapter(List<FriendlyMessage> messageList, AdapterLongClickCallback callback
                          ) {
        this.msgList = messageList;
        this.mAdapterLongClickCallback = callback;
//        this.isSourceCommonChatRoom = isSourceCommonChatRoom;
    }

    @Override
    public int getItemViewType(int position) {

        FriendlyMessage message = msgList.get(position);
        boolean isSenderCurrentUser =
                AppConstants.getCurrentUserUid().equals(message.getSenderId());
        String msgType = message.getMsgType();
        boolean isRemoved = message.getIsRemoved();
        Log.e(TAG, "Message = " + position
                + "\nsenderName = " + message.getSenderName()
                + "\nsenderId = " + message.getSenderId()
                + "\ncurrentUserId = " + AppConstants.getCurrentUserUid()
                + "\nMessageDate = " + message.getMsgDate()
                + "\nmessageType = " + msgType
                + "\nMessageText = " + message.getMsgText()
                + "\nisRemoved = " + message.getIsRemoved()
                + "\nremovedBy = " + message.getRemovedBy()
        );

        if (isSenderCurrentUser) {
            if (isRemoved) {
                Log.e(TAG, " inCondition = Removed, sender = current user");
                return R.layout.my_message_deleted;
            } else {
                if (msgType.equals(AppConstants.DOC_MESSAGE)) {
                    Log.e(TAG, " inCondition = not Removed 1, sender = current user");
                    return R.layout.my_message_plain;
                } else if (msgType.equals(AppConstants.VIDEO_MESSAGE)) {
                    Log.e(TAG, " inCondition = not Removed 2, sender = current user");
                    return R.layout.my_message_video;
                } else if (msgType.equals(AppConstants.IMAGE_MESSAGE)) {
                    Log.e(TAG, " inCondition = not Removed 3, sender = current user");
                    return R.layout.my_message_image;
                } else if (msgType.equals(AppConstants.AUDIO_MESSAGE)) {
                    Log.e(TAG, " inCondition = not Removed 4, sender = current user");
                    return R.layout.my_message_audio;
                } else {
                    Log.e(TAG, " inCondition = not Removed 5, sender = current user");
                    return R.layout.my_message_plain;
                }
            }
        } else {
            if (isRemoved) {
                Log.e(TAG, " inCondition = Removed, sender = not current user");
                return R.layout.their_message_deleted;
            } else {
                if (msgType.equals(AppConstants.DOC_MESSAGE)) {
                    Log.e(TAG, " inCondition = not Removed 1, sender = not current user");
                    return R.layout.their_message_plain;
                } else if (msgType.equals(AppConstants.VIDEO_MESSAGE)) {
                    Log.e(TAG, " inCondition = not Removed 2, sender = not current user");
                    return R.layout.their_message_video;
                } else if (msgType.equals(AppConstants.IMAGE_MESSAGE)) {
                    Log.e(TAG, " inCondition = not Removed 3, sender = not current user");
                    return R.layout.their_message_image;
                } else if (msgType.equals(AppConstants.AUDIO_MESSAGE)) {
                    Log.e(TAG, " inCondition = not Removed 4, sender = not current user");
                    return R.layout.their_message_audio;
                } else {
                    Log.e(TAG, " inCondition = not Removed 5, sender = not current user");
                    return R.layout.their_message_plain;
                }
            }
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
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        mContext = holder.msgTimeTV.getContext();

        String currentUserUid = AppConstants.getCurrentUserUid();
        mainLayout = holder.messageMainLayout;

        FriendlyMessage message = msgList.get(position);
        final String senderId = message.getSenderId();
        String msgType = message.getMsgType();
        Log.e(TAG, "msgType = " + msgType);

        String removedBy = message.getRemovedBy();
        Log.e(TAG, "Message = " + removedBy);
        Log.e(TAG, " isRemoved = " + message.getIsRemoved());

        final boolean isSenderCurrentUser = senderId.equals(currentUserUid);
        Log.e(TAG, "Msg pos = " + position + ", isSender CurrentUser: " + isSenderCurrentUser);

        if (!isSenderCurrentUser) {
            holder.senderNameTV.setText(message.getSenderName());
        }
        holder.msgTimeTV.setText(message.getMsgDate());

        if (!message.getIsRemoved()) {
            if (msgType.equals("plain")) {
                holder.msgBodyTV.setText(message.getMsgText());
            } else {
                FileMessageAttributes fileMessageAttributes = message
                        .getFileMessageAttributesMap().get("fileProperties");
                String remoteFilePath = fileMessageAttributes.getFilePath();

                switch (msgType) {
                    case "image":
                        if (remoteFilePath != null) {
                            Log.e("ImagePathDB", remoteFilePath);

                            File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                                    , "AI Eye Images 1");

                            if (!mediaStorageDir.exists()) {
                                if (!mediaStorageDir.mkdirs()) {
                                    Log.d("App", "failed to create directory");
                                }
                            } else {
                                Log.e(TAG, "Path = " + mediaStorageDir.getAbsolutePath());
                            }

                            File folder = mContext.getFilesDir();
                            File f = new File(folder, "AI Eye Images");
                            boolean isCreated = f.mkdir();

                            if (isCreated) {
                                Log.e(TAG, "Path = " + f.getAbsolutePath());
                            } else {
                                Log.e(TAG, "Path can't be created");
                            }

                            Picasso.get().load(remoteFilePath)//.networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.loading)
                                    .into(holder.msgIV);
                        }
                        break;
                    case "video":
                        if (remoteFilePath != null) {
                            Log.e("VideoPathDB", remoteFilePath);
                            VideoView videoView = holder.msgVV;

                            try {
                                // Start the MediaController
                                MediaController mediacontroller =
                                        new MediaController(mContext);
                                mediacontroller.setAnchorView(videoView);

//                                Uri videoUri = Uri.parse(remoteFilePath);
                                videoView.setMediaController(mediacontroller);
//                                videoView.setVideoURI(videoUri);
                                String proxyUrl = MediaLoader.getInstance(mContext).getProxyUrl(remoteFilePath);
                                videoView.setVideoPath(proxyUrl);
                                videoView.seekTo(10);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "doc":

                        break;
                    case "audio":
                        seek_bar = holder.seekBar;
                        play_button = holder.playBtn;
                        String proxyUrl = MediaLoader.getInstance(mContext).getProxyUrl(remoteFilePath);
                        setupMediaPlayer(proxyUrl);
                        play_button.setOnClickListener(this);
                        break;
                }
            }
        }

        if (selectedPosition == position)
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPurple_trans));
        else
            holder.itemView.setBackgroundColor(Color.parseColor("#00f49828"));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int pos = holder.getAdapterPosition();
                selectedPosition = pos;
                notifyDataSetChanged();
                mAdapterLongClickCallback.onMethodLongClickCallback(pos);
//                return true;
                Log.e(TAG, "Long Clicked at position = " + pos);
                return false;
            }
        });

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
            case R.id.play_button: {
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

//    @Override
//    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//
//        Log.e(TAG, "Long Clicked at position = " + position);
//        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//        builder.setTitle("Confirm");
//        builder.setMessage("Are you sure want to Update Record?");
//        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
//
//                dialog.dismiss();
//            }
//        });
//
//        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                // Do nothing
//                dialog.dismiss();
//            }
//        });
//
//        AlertDialog alert = builder.create();
//        alert.show();
//        return false;
//    }

}
