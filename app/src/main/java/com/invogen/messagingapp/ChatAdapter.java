package com.invogen.messagingapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {
//        implements View.OnClickListener {

//    private AdapterLongClickCallback mAdapterLongClickCallback;
//
//    public interface AdapterLongClickCallback {
//        void onMethodLongClickCallback(int position);
//    }


    private String TAG = "MessageAdapter";
    private List<Chats> chatList;
    private Context mContext;

    public ChatAdapter(List<Chats> chatList) {
        this.chatList = chatList;
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

        Chats chat = chatList.get(position);
        String chatTitle = chat.getChatName();
        holder.chatTitleTV.setText(chatTitle);
        holder.avatarTV.setText(chatTitle);

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

}
