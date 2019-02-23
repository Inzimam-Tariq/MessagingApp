package com.invogen.messagingapp;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
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

public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {
//        implements View.OnClickListener {

//    private AdapterLongClickCallback mAdapterLongClickCallback;
//
//    public interface AdapterLongClickCallback {
//        void onMethodLongClickCallback(int position);
//    }


    private String TAG = "MessageAdapter";
    private List<String> chatNameList;
    private Context mContext;

    public ChatAdapter(List<String> chatNameList) {
        this.chatNameList = chatNameList;
    }


    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemViewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chats_layout,
                viewGroup, false);
        Log.e(TAG, "onCreateView LayoutId = " + itemViewType + " Inside MyLayout");

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder holder, int position) {
        mContext = holder.chatTitleTV.getContext();

        String chatTitle = chatNameList.get(position);
        holder.chatTitleTV.setText(chatTitle);
        holder.avatarTV.setText(chatTitle);

    }

    @Override
    public int getItemCount() {
        return chatNameList.size();
    }

}
