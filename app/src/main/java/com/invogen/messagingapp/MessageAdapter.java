package com.invogen.messagingapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private String TAG = "MessageAdapter";
    private List<FriendlyMessage> msgList;

    public MessageAdapter(List<FriendlyMessage> messageList) {
        this.msgList = messageList;

    }

    @Override
    public int getItemViewType(int position) {

        String userId = AppConstants.getCurrentUserUid();
        FriendlyMessage message = msgList.get(position);
        String userUid = message.getSenderId();
        Log.e(TAG,
                "getItemViewType, Message = " + position + "\nsenderId = " + userUid +
                        "\ncurUserId = " + userId);
        if (userUid != null && userUid.equals(userId)) {
            return R.layout.my_message;
        } else {
            return R.layout.their_message;
        }

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemViewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemViewType, viewGroup, false);
        Log.e(TAG, "onCreateView LayoutId = " + itemViewType + " Inside MyLayout");
//        switch (itemViewType) {
//            case R.layout.my_message: {
//                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
//                        .my_message, viewGroup, false);
//                Log.e(TAG, "onCreateView LayoutId = " + itemViewType + " Inside MyLayout");
//                break;
//            }
//            case R.layout.their_message:{
//                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
//                        .their_message, viewGroup, false);
//                Log.e(TAG, "onCreateView LayoutId = " + itemViewType + " Inside MyLayout");
//                break;
//            }
//            default:{
//                view = null;
//            }
//        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int position) {

        String userId = AppConstants.getCurrentUserUid();

        FriendlyMessage message = msgList.get(position);
        String userUid = message.getSenderId();
        Log.e(TAG,
                "onBindView, Message = " + position + "\nsenderId = " + userUid + "\ncurUserId = " + userId);
        String msgType = message.getMsgType();
        Log.e(TAG, "msgType = " + msgType);
        if (msgType.equals("plain")) {
            if (userUid != null && userUid.equals(userId)) {
                String msgTxt = message.getMsgText();
                if (msgTxt != null && !msgTxt.trim().isEmpty()) {
                    messageViewHolder.msgBodyTV.setText(msgTxt);
                } else {
                    messageViewHolder.msgBodyTV.setText(msgTxt);
                }
                messageViewHolder.msgTimeTV.setText(message.getMsgDate());

            } else {
                messageViewHolder.senderNameTV.setText(message.getSenderName());
                String msgTxt = message.getMsgText();
                if (msgTxt != null && !msgTxt.trim().isEmpty()) {
                    messageViewHolder.msgBodyTV.setText(msgTxt);
                } else {
                    messageViewHolder.msgBodyTV.setText(msgTxt);
                }
                messageViewHolder.msgTimeTV.setText(message.getMsgDate());
            }
//        else {
//            messageViewHolder.msgTVRight.setVisibility(View.GONE);
//            messageViewHolder.msgTVLeft.setVisibility(View.GONE);
//            if (msgType.equals("image")) {
//                FileMessageAttributes fileMessageAttributes = message
//                        .getFileMessageAttributesMap().get("fileProperties");
//                if (userUid != null && userUid.equals(userId)) {
//                    messageViewHolder.linearLayoutLeft.setVisibility(View.GONE);
//                    messageViewHolder.linearLayoutRight.setVisibility(View.VISIBLE);
//
//                    messageViewHolder.nameTVRight.setText(message.getSenderName());
//                    messageViewHolder.timeTVRight.setText(message.getMsgDate());
////                    progressBar.setVisibility(View.GONE);
//
//                    if (fileMessageAttributes.getFilePath() != null) {
//                        Log.e("ImagePathDB", fileMessageAttributes.getFilePath());
//                        Picasso.get().load(fileMessageAttributes.getFilePath())
//                                .into(messageViewHolder.imageViewRight);
//                    }
//                } else {
//                    messageViewHolder.linearLayoutLeft.setVisibility(View.VISIBLE);
//                    messageViewHolder.linearLayoutRight.setVisibility(View.GONE);
//                    messageViewHolder.nameTVLeft.setText(message.getSenderName());
////                            String msgTxt = message.getMsgText();
////                            if (msgTxt != null && !msgTxt.trim().isEmpty()) {
////                                messageViewHolder.msgTVLeft.setText(msgTxt);
////                            } else {
////                                messageViewHolder.msgTVLeft.setVisibility(View.GONE);
////                            }
//                    messageViewHolder.timeTVLeft.setText(message.getMsgDate());
////                    progressBar.setVisibility(View.GONE);
//
//                    if (fileMessageAttributes.getFilePath() != null) {
//                        Log.e(TAG, "ImagePathDB" + fileMessageAttributes.getFilePath());
//                        Picasso.get().load(fileMessageAttributes.getFilePath())
//                                .into(messageViewHolder.imageViewLeft);
//                    }
//                }
//            }
        }

    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }
}
