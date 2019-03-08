package com.invogen.messagingapp;

import java.util.List;

public class Chats {
    private String chatName;
    private String chatDate;
    List<Users> participantList;
    List<FriendlyMessage> messageList;

    public Chats() {
    }

    public Chats(String chatName) {
        this.chatName = chatName;
        this.chatDate = AppUtils.getTime();
    }

    public Chats(String chatName, List<FriendlyMessage> messageList) {
        this.chatName = chatName;
        this.messageList = messageList;
    }

    public Chats(String chatName, List<Users> participantList, List<FriendlyMessage> messageList) {
        this.chatName = chatName;
        this.participantList = participantList;
        this.messageList = messageList;
    }


    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getChatDate() {
        return chatDate;
    }

    public void setChatDate(String chatDate) {
        this.chatDate = chatDate;
    }

    public List<Users> getParticipantList() {
        return participantList;
    }

    public void setParticipantList(List<Users> participantList) {
        this.participantList = participantList;
    }

    public List<FriendlyMessage> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<FriendlyMessage> messageList) {
        this.messageList = messageList;
    }
}
