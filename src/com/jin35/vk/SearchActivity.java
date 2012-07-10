package com.jin35.vk;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jin35.vk.adapters.Adapter;
import com.jin35.vk.adapters.SearchAdapter;

public class SearchActivity extends ListActivity {

    private View searchPanel;
    private EditText searchEt;
    private String searchPattern;
    private Runnable onSearchChanged;
    private boolean isSearchShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        final Adapter<?> adapter = new SearchAdapter(this);
        getListView().setAdapter(adapter);

        ((TextView) findViewById(R.id.top_bar_tv)).setText(R.string.search_top_text);

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
                searchPattern = s.toString();
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
    }

    public void showSearchBox() {
        if (!isSearchShowing()) {
            searchPanel.setVisibility(View.VISIBLE);
            searchEt.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEt, 0);
            searchEt.setText(getSearchPattern());
            isSearchShowing = true;
            notifySearchChanged();
        }
    }

    private void hideSearchBox() {
        if (isSearchShowing()) {
            searchPanel.setVisibility(View.GONE);
            isSearchShowing = false;
            searchPattern = "";
            notifySearchChanged();
        }
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public boolean isSearchShowing() {
        return isSearchShowing;
    }

    public void setOnSearchChanged(Runnable onSearchChanged) {
        this.onSearchChanged = onSearchChanged;
    }

    private void notifySearchChanged() {
        try {
            onSearchChanged.run();
        } catch (Throwable e) {
        }
    }
}
