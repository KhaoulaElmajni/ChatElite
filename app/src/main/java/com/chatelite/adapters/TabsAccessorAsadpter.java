package com.chatelite.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.chatelite.fragments.GroupsFragment;
import com.chatelite.fragments.RequestsFragment;
import com.chatelite.fragments.ChatsFragment;
import com.chatelite.fragments.ContactsFragment;

public class TabsAccessorAsadpter extends FragmentPagerAdapter {
    public TabsAccessorAsadpter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            case 3:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {

        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contact";
            case 3:
                return "Requests";
            default:
                return null;
        }
    }
}
