package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.IObjectListener;
import com.jin35.vk.model.NotificationCenter;

public abstract class Adapter<T extends IListItem> implements ListAdapter {

    protected final Activity activity;
    private List<T> list = new ArrayList<T>();

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    protected abstract int getModelListenerMask();

    protected abstract List<T> getList();

    public Adapter(final Activity activity) {
        this(activity, true);
    }

    protected Adapter(final Activity activity, boolean doOnCreate) {
        this.activity = activity;
        if (doOnCreate) {
            onCreate();
        }
    }

    protected void onCreate() {
        list = getList();
        NotificationCenter.getInstance().addModelListener(getModelListenerMask(), new IModelListener() {
            @Override
            public void dataChanged() {
                notifyDataSetChanged();
                onDataSetChanged();
            }
        });
    }

    protected void onDataSetChanged() {
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            IObjectListener oldListener = (IObjectListener) convertView.getTag();
            if (oldListener != null) {
                NotificationCenter.getInstance().removeListener(oldListener);
            }
        }
        final T object = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(object.getViewId(), parent, false);
        }
        final View viewForUpdate = convertView;
        object.updateView(viewForUpdate);
        if (object.needListener()) {
            IObjectListener newListener = new IObjectListener() {
                @Override
                public void dataChanged(long id) {
                    object.updateView(viewForUpdate);
                }
            };
            viewForUpdate.setTag(newListener);
            object.subsribeListenerForObject(newListener);
        }
        return viewForUpdate;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        list = getList();
        mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }
}