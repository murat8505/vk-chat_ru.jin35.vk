package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;

import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class FriendsAdapter extends Adapter<IListItem> {
    public FriendsAdapter(Activity a) {
        super(a);
    }

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_FRIENDS;
    }

    protected List<UserInfo> getUsers() {
        return UserStorageFactory.getInstance().getUserStorage().getFriends();
    }

    @Override
    protected List<IListItem> getList() {
        List<UserInfo> friends = getUsers();

        Collections.sort(friends, UserInfo.getFriendComparator());
        List<IListItem> result = new ArrayList<IListItem>();
        List<Character> separators = new ArrayList<Character>();
        for (UserInfo friend : friends) {
            FriendListItem friendItem = new FriendListItem(friend);
            if (friend.getImportance() != 0) {
                result.add(friendItem);
                continue;
            }
            char firstLetter = friend.getFullName().charAt(0);
            if (!separators.contains(firstLetter)) {
                result.add(new SeparatorListItem(String.valueOf(firstLetter)));
                separators.add(firstLetter);
            }
            result.add(friendItem);
        }
        return result;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        IListItem item = getItem(position);
        if (item instanceof SeparatorListItem) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
