package com.invogen.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {
//        implements View.OnClickListener {

//    private AdapterLongClickCallback mAdapterLongClickCallback;
//
//    public interface AdapterLongClickCallback {
//        void onMethodLongClickCallback(int position);
//    }


    private String TAG = "ChatAdapter";
    private List<Chats> chatRoomChatsList;
    private Context mContext;
    private List<String> chatRoomChatsKeyList;
    private ArrayList<FriendlyMessage> messageList;

    public ChatAdapter(List<Chats> chatList, List<String> chatRoomChatsKeyList) {
        this.chatRoomChatsList = chatList;
        this.chatRoomChatsKeyList = chatRoomChatsKeyList;
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

        int pos = holder.getAdapterPosition();
        setupLastMessageData(holder);

        Chats chat = chatRoomChatsList.get(pos);
        final String chatTitle = chat.getChatName();
        holder.chatTitleTV.setText(chatTitle);
        holder.avatarTV.setText(chatTitle);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pushkey = chatRoomChatsKeyList.get(holder.getAdapterPosition());
                Log.e(TAG, "pushKey = " + pushkey);
                Intent intent = new Intent(mContext, ChatMessagesActivity.class);
                intent.putExtra("chatTitle", chatTitle);
                intent.putExtra("pushKey", pushkey);
                intent.putExtra("messages", messageList);


                mContext.startActivity(intent);
            }
        });

    }

    private void setupLastMessageData(final ChatViewHolder holder) {
        DatabaseReference mSingleChatReference =
                FirebaseDatabase.getInstance().getReference()
                        .child(AppConstants.CHATS_NODE)
                        .child(chatRoomChatsKeyList.get(holder.getAdapterPosition()))
                        .child("messages");
        Log.e(TAG, "ChatPath = " + mSingleChatReference);
        mSingleChatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        FriendlyMessage msg = ds.getValue(FriendlyMessage.class);

                        Log.e(TAG, "Message Key = " + ds.getKey());
                        messageList.add(msg);
                    }
                }
                holder.lastMessageTV.setText(messageList.get(messageList.size() - 1).getMsgText());
                holder.timeTV.setText(messageList.get(messageList.size() - 1).getMsgDate());
//                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomChatsList.size();
    }

}
