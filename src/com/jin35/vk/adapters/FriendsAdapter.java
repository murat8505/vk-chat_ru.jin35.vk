package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.jin35.vk.FriendsActivity;
import com.jin35.vk.R;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class FriendsAdapter extends Adapter<IListItem> {

    public FriendsAdapter(FriendsActivity a) {
        super(a);

        a.addOnSearchChanged(this, new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    private FriendsActivity getActivity() {
        return (FriendsActivity) activity;
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

        if (!getActivity().isSearchShowing()) {
            result.add(new SearchItem());
        }

        for (UserInfo friend : friends) {
            if (!TextUtils.isEmpty(getActivity().getSearchPattern())
                    && !friend.getFullName().toLowerCase().contains(getActivity().getSearchPattern().toLowerCase())) {
                continue;
            }
            FriendListItem friendItem = new FriendListItem(friend, !getActivity().isUserSelection());
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
        if (item instanceof SearchItem) {
            return 2;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    private class SearchItem implements IListItem {

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public int getViewId() {
            return R.layout.search_panel;
        }

        @Override
        public void updateView(View view) {
            EditText et = (EditText) view.findViewById(R.id.search_et);
            // et.setText(filterText);
            et.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        ((FriendsActivity) activity).showSearchBox();
                    }
                }
            });
        }

        @Override
        public boolean needListener() {
            return false;
        }

        @Override
        public void subsribeListenerForObject(IModelListener listener) {
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
