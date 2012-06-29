package com.jin35.vk;

import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.jin35.vk.adapters.Adapter;
import com.jin35.vk.adapters.FriendsAdapter;
import com.jin35.vk.adapters.OnlineFriendsAdapter;

public class FriendsActivity extends ListActivity {

    public static final String UID_EXTRA = "uid";
    public static final String NEED_RETURN_UID_EXTRA = "return uid";

    private final Map<FriendsAdapter, Boolean> isSearchShowing = new HashMap<FriendsAdapter, Boolean>();
    private final Map<FriendsAdapter, String> searchPattern = new HashMap<FriendsAdapter, String>();
    private View searchPanel;
    private EditText searchEt;
    private final Map<FriendsAdapter, Runnable> onSearchChanged = new HashMap<FriendsAdapter, Runnable>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        ViewGroup topPanelHolder = (ViewGroup) findViewById(R.id.top_bar_ll);
        topPanelHolder.removeAllViews();
        topPanelHolder.addView(LayoutInflater.from(this).inflate(R.layout.friend_list_top_bar, topPanelHolder, false));

        ViewGroup vg = (ViewGroup) findViewById(R.id.list_container);
        searchPanel = LayoutInflater.from(this).inflate(R.layout.search_panel, vg, false);
        searchPanel.setVisibility(View.GONE);
        vg.addView(searchPanel, 1);
        searchEt = (EditText) findViewById(R.id.search_et);
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchPattern.put((FriendsAdapter) getListView().getAdapter(), s.toString());
                notifySearchChanged();
            }
        });

        searchEt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideSearchBox();
                }
            }
        });

        final Map<Button, Adapter<?>> adapters = new HashMap<Button, Adapter<?>>(3);

        if (getIntent().getBooleanExtra(NEED_RETURN_UID_EXTRA, false)) {
            getListView().setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent result = new Intent();
                    result.putExtras(getIntent());
                    result.putExtra(UID_EXTRA, id);
                    setResult(RESULT_OK, result);
                    finish();
                }
            });
        }
        adapters.put((Button) findViewById(R.id.all_friends_btn), new FriendsAdapter(this));
        adapters.put((Button) findViewById(R.id.online_friends_btn), new OnlineFriendsAdapter(this));

        for (Button btn : adapters.keySet()) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideSearchBox();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchEt.getWindowToken(), 0);
                    for (Button btn : adapters.keySet()) {
                        btn.setPressed(false);
                    }
                    v.setPressed(true);
                    Adapter<?> adapter = adapters.get(v);
                    getListView().setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            });
        }

        ((Button) findViewById(R.id.all_friends_btn)).performClick();

    }

    public boolean isSearchShowing() {
        Boolean res = isSearchShowing.get(getListView().getAdapter());
        if (res == null) {
            res = false;
        }
        return res;
    }

    public void showSearchBox() {
        if (!isSearchShowing()) {
            searchPanel.setVisibility(View.VISIBLE);
            searchEt.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEt, 0);
            searchEt.setText(getSearchPattern());
            isSearchShowing.put((FriendsAdapter) getListView().getAdapter(), true);
            notifySearchChanged();
        }
    }

    private void hideSearchBox() {
        if (isSearchShowing()) {
            searchPanel.setVisibility(View.GONE);
            isSearchShowing.put((FriendsAdapter) getListView().getAdapter(), false);
            searchPattern.put((FriendsAdapter) getListView().getAdapter(), "");
            notifySearchChanged();
        }
    }

    public String getSearchPattern() {
        String res = searchPattern.get(getListView().getAdapter());
        if (res == null) {
            res = "";
        }
        return res;
    }

    public void addOnSearchChanged(FriendsAdapter adapter, Runnable action) {
        onSearchChanged.put(adapter, action);
    }

    private void notifySearchChanged() {
        try {
            onSearchChanged.get(getListView().getAdapter()).run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
