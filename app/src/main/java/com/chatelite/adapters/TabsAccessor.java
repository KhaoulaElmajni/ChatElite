package com.chatelite.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.chatelite.fragments.Groups;
import com.chatelite.fragments.Requests;
import com.chatelite.fragments.Chats;
import com.chatelite.fragments.Contacts;

public class TabsAccessor extends FragmentPagerAdapter {
    public TabsAccessor(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                Chats chatsFragment = new Chats();
                return chatsFragment;
            case 1:
                Groups groupsFragment = new Groups();
                return groupsFragment;
            case 2:
                Contacts contactsFragment = new Contacts();
                return contactsFragment;
            case 3:
                Requests requestsFragment = new Requests();
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
                return "Contacts";
            case 3:
                return "Requests";
            default:
                return null;
        }
    }
}
