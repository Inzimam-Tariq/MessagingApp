package com.invogen.messagingapp;

public class AppConstants {
    public static int CAPTURE_PHOTO_CODE = 0;
    public static int RC_SIGN_IN = 1;
    public static int RC_WRITE_TO_EXTERNAL_STORAGE = 2;
    public static int RC_READ_FROM_EXTERNAL_STORAGE = 3;
    public static int RC_PICK_IMAGE = 4;
    public static int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static String USER_TYPE = "";
    public static boolean SIGNED_OUT = false;

    // Tables Related
    public static String USERS_NODE = "users";
    public static String PROFILES_NODE = "profiles";
    public static String OPEN_CHAT_NODE = "open_chats";
    public static String MESSAGES_NODE = "messages";
    public static String PROFILE_IMAGES_NODE = "profile_images";
    public static String CHAT_IMAGES_NODE = "chat_images";


    public static String getUserType() {
        return USER_TYPE;
    }

    public static void setUserType(String userType) {
        USER_TYPE = userType;
    }
}
