package com.invogen.messagingapp;

public class Users {

    private String user_name, user_status, user_image, user_email, user_type;//, user_circle;
//    private List<String> friend_list;

    public Users() {

    }

    public Users(String user_name, String user_status, String user_image) {
        this.user_name = user_name;
        this.user_status = user_status;
        this.user_image = user_image;
    }

    public Users(String user_name, String user_status, String user_image,
                 String user_email, String user_type){//}, List<String> friend_list) {
        this.user_name = user_name;
        this.user_status = user_status;
        this.user_image = user_image;
        this.user_email = user_email;
        this.user_type = user_type;
//        this.friend_list = friend_list;
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

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

//    public List<String> getFriend_list() {
//        return friend_list;
//    }

//    public void setFriend_list(List<String> friend_list) {
//        this.friend_list = friend_list;
//    }
}
