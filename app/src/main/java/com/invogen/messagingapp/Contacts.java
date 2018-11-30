package com.invogen.messagingapp;

public class Contacts {

    private String user_name, user_status, user_image;

    public Contacts() {

    }

    public Contacts(String user_name, String user_status, String user_image) {
        this.user_name = user_name;
        this.user_status = user_status;
        this.user_image = user_image;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_status() {
        return user_status;
    }

    public void setUser_status(String user_status) {
        this.user_status = user_status;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }
}
