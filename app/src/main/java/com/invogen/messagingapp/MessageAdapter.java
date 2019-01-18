package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private String TAG = "MessageAdapter";
    private List<FriendlyMessage> msgList;

    public MessageAdapter(List<FriendlyMessage> objects) {
        this.msgList = objects;

    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                .item_message, viewGroup, false);

        Log.e(TAG, "onCreateViewHolder Size = " + msgList.size());

        return new MessageViewHolder(view1);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {

        String userId = AppConstants.getUserUid();

        FriendlyMessage message = msgList.get(i);
        String userUid = message.getSenderId();
        Log.e(TAG, "senderId = " + userUid + "\nCurrentUserId = " + userId);
        String msgType = message.getMsgType();
        Log.e(TAG, "msgType = " + msgType);
        if (msgType.equals("plain")) {
            if (userUid != null && userUid.equals(userId)) {
                messageViewHolder.linearLayoutLeft.setVisibility(View.GONE);
                messageViewHolder.linearLayoutRight.setVisibility(View.VISIBLE);
                messageViewHolder.nameTVRight.setText(message.getSenderName());
                String msgTxt = message.getMsgText();
                if (msgTxt != null && !msgTxt.trim().isEmpty()) {
                    messageViewHolder.msgTVRight.setText(msgTxt);
                } else {
                    messageViewHolder.msgTVRight.setVisibility(View.GONE);
                }
                messageViewHolder.timeTVRight.setText(message.getMsgDate());

            } else {
                messageViewHolder.linearLayoutLeft.setVisibility(View.VISIBLE);
                messageViewHolder.linearLayoutRight.setVisibility(View.GONE);
                messageViewHolder.nameTVLeft.setText(message.getSenderName());
                String msgTxt = message.getMsgText();
                if (msgTxt != null && !msgTxt.trim().isEmpty()) {
                    messageViewHolder.msgTVLeft.setText(msgTxt);
                } else {
                    messageViewHolder.msgTVLeft.setVisibility(View.GONE);
                }
                if (message.getMsgDate() != null)
                    messageViewHolder.timeTVLeft.setText(message.getMsgDate());

            }
        } else {
            messageViewHolder.msgTVRight.setVisibility(View.GONE);
            messageViewHolder.msgTVLeft.setVisibility(View.GONE);
            if (msgType.equals("image")) {
                FileMessageAttributes fileMessageAttributes = message
                        .getFileMessageAttributesMap().get("fileProperties");
                if (userUid != null && userUid.equals(userId)) {
                    messageViewHolder.linearLayoutLeft.setVisibility(View.GONE);
                    messageViewHolder.linearLayoutRight.setVisibility(View.VISIBLE);

                    messageViewHolder.nameTVRight.setText(message.getSenderName());
                    messageViewHolder.timeTVRight.setText(message.getMsgDate());
//                    progressBar.setVisibility(View.GONE);

                    if (fileMessageAttributes.getFilePath() != null) {
                        Log.e("ImagePathDB", fileMessageAttributes.getFilePath());
                        Picasso.get().load(fileMessageAttributes.getFilePath())
                                .into(messageViewHolder.imageViewRight);
                    }
                } else {
                    messageViewHolder.linearLayoutLeft.setVisibility(View.VISIBLE);
                    messageViewHolder.linearLayoutRight.setVisibility(View.GONE);
                    messageViewHolder.nameTVLeft.setText(message.getSenderName());
//                            String msgTxt = message.getMsgText();
//                            if (msgTxt != null && !msgTxt.trim().isEmpty()) {
//                                messageViewHolder.msgTVLeft.setText(msgTxt);
//                            } else {
//                                messageViewHolder.msgTVLeft.setVisibility(View.GONE);
//                            }
                    messageViewHolder.timeTVLeft.setText(message.getMsgDate());
//                    progressBar.setVisibility(View.GONE);

                    if (fileMessageAttributes.getFilePath() != null) {
                        Log.e(TAG, "ImagePathDB" + fileMessageAttributes.getFilePath());
                        Picasso.get().load(fileMessageAttributes.getFilePath())
                                .into(messageViewHolder.imageViewLeft);
                    }
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }
}
