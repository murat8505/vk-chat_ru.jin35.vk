package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.jin35.vk.R;
import com.jin35.vk.SearchActivity;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;

public class SearchAdapter extends Adapter<IListItem> {

    public SearchAdapter(SearchActivity activity) {
        super(activity);
        activity.setOnSearchChanged(new Runnable() {
            TimerTask searchTask;
            String searchString = "";

            @Override
            public void run() {
                String currentSearchPattern = ((SearchActivity) SearchAdapter.this.activity).getSearchPattern();
                if (!TextUtils.isEmpty(currentSearchPattern) && !MessageStorage.getInstance().hasSearchResults(currentSearchPattern)) {
                    if (!searchString.equalsIgnoreCase(currentSearchPattern)) {
                        if (searchTask != null) {
                            searchTask.cancel();
                        }
                        searchString = currentSearchPattern;
                        searchTask = new TimerTask() {
                            @Override
                            public void run() {
                                BackgroundTasksQueue.getInstance()
                                        .execute(new DataRequestTask(DataRequestFactory.getInstance().getSearchRequest(searchString)));
                            }
                        };
                        Token.getInstance().getTimer().schedule(searchTask, 500);
                    }
                }
                notifyDataSetChanged();
            }
        });
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
        if (item instanceof LoaderListItem) {
            return 3;
        }
        if (item instanceof EmptyListItem) {
            return 4;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    protected int getModelListenerMask() {
        return NotificationCenter.MODEL_REQUESTS | NotificationCenter.MODEL_SUGGESTIONS | NotificationCenter.MODEL_SEARCH;
    }

    @Override
    protected List<IListItem> getList() {
        List<IListItem> result = new ArrayList<IListItem>();

        String pattern = ((SearchActivity) activity).getSearchPattern();
        if (((SearchActivity) activity).isSearchShowing() && !TextUtils.isEmpty(pattern)) {
            if (MessageStorage.getInstance().hasSearchResults(pattern)) {
                List<UserInfo> users = MessageStorage.getInstance().getSearchResults(pattern);
                if (users.size() == 0) {
                    result.add(new EmptyListItem(R.string.search_empty));
                } else {
                    for (UserInfo user : users) {
                        result.add(new SimpleUserListItem(user, true));
                    }
                }
            } else {
                result.add(new LoaderListItem());
            }

        } else {
            if (!((SearchActivity) activity).isSearchShowing()) {
                result.add(new SearchItem());
            }

            List<UserInfo> requests = UserStorageFactory.getInstance().getUserStorage().getReguests();
            if (requests.size() > 0) {
                result.add(new SeparatorListItem(activity.getString(R.string.requests)));
            }
            Collections.sort(requests, UserInfo.getFriendComparator());
            for (UserInfo user : requests) {
                result.add(new SimpleUserListItem(user, true));
            }

            List<UserInfo> possibleFriends = UserStorageFactory.getInstance().getUserStorage().getSuggestions();
            if (possibleFriends.size() > 0) {
                result.add(new SeparatorListItem(activity.getString(R.string.suggestions)));
            }
            Collections.sort(possibleFriends, UserInfo.getFriendComparator());
            for (UserInfo user : possibleFriends) {
                result.add(new SimpleUserListItem(user, true));
            }
        }
        return result;
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
                        ((SearchActivity) activity).showSearchBox();
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
