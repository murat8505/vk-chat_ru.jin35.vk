package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;

import com.jin35.vk.R;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class SearchAdapter extends Adapter<IListItem> {

    public SearchAdapter(Activity activity) {
        super(activity);
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

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_REQUESTS | NotificationCenter.MODEL_SUGGESTIONS;
    }

    @Override
    protected List<IListItem> getList() {
        List<IListItem> result = new ArrayList<IListItem>();
        List<UserInfo> requests = UserStorageFactory.getInstance().getUserStorage().getReguests();
        if (requests.size() > 0) {
            result.add(new SeparatorListItem(activity.getString(R.string.requests)));
        }
        Collections.sort(requests, UserInfo.getFriendComparator());
        for (UserInfo user : requests) {
            result.add(new SimpleUserListItem(user));
        }

        List<UserInfo> possibleFriends = UserStorageFactory.getInstance().getUserStorage().getSuggestions();
        if (possibleFriends.size() > 0) {
            result.add(new SeparatorListItem(activity.getString(R.string.suggestions)));
        }
        Collections.sort(possibleFriends, UserInfo.getFriendComparator());
        for (UserInfo user : possibleFriends) {
            result.add(new SimpleUserListItem(user));
        }
        return result;
    }

}
