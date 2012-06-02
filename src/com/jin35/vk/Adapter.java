package com.jin35.vk;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.jin35.vk.model.IObjectListener;
import com.jin35.vk.model.ModelObject;
import com.jin35.vk.model.NotificationCenter;

abstract class Adapter<T extends ModelObject> implements ListAdapter {

    protected Context context;
    private List<T> list = new ArrayList<T>();

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    // private final List<DataSetObserver> observers = new ArrayList<DataSetObserver>();

    protected abstract int getListenerMask();

    protected abstract List<T> getList();

    protected abstract View getView(T object, View convertView, ViewGroup parent);

    protected abstract void updateView(T object, View view);

    public Adapter(Context context) {
        this.context = context;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            IObjectListener oldListener = (IObjectListener) convertView.getTag();
            if (oldListener != null)
                NotificationCenter.getInstance().removeListener(oldListener);
        }
        final T object = getItem(position);
        final View result = getView(getItem(position), convertView, parent);
        IObjectListener newListener = new IObjectListener() {
            @Override
            public void dataChanged(long id) {
                updateView(object, result);
                // notifyInvalidated();
            }
        };
        result.setTag(newListener);
        NotificationCenter.getInstance().addObjectListener(getListenerMask(), object.getId(), newListener);
        System.out.println("make view for object " + object + ", view: " + result);
        return result;
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
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
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
        System.out.println("register observer");
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        System.out.println("unregister observer");
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
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}