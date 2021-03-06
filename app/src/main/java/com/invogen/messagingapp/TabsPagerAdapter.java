package com.invogen.messagingapp;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {


    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new GroupChatFragment();
            case 1:
                return new FriendsFragment();
//            case 2:
//                return new GroupChatFragment();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chat Room";
            case 1:
                return "Friends";
//            case 2:
//                return "Group Chat";

            default:
                return null;
        }
    }
}
