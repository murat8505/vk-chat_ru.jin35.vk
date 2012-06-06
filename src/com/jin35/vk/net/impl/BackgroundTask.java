package com.jin35.vk.net.impl;

public abstract class BackgroundTask<T> implements Runnable, Comparable<BackgroundTask<T>> {

    private final long time;
    private final int priority;

    protected BackgroundTask(int priority) {
        this.priority = priority;
        time = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            onSuccess(execute());
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                onError();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public abstract T execute() throws Throwable;

    public abstract void onSuccess(T result);

    public abstract void onError();

    @Override
    public int compareTo(BackgroundTask<T> another) {
        int result = priority - another.priority;
        if (result != 0) {
            return result;
        }
        return (int) (time - another.time);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
