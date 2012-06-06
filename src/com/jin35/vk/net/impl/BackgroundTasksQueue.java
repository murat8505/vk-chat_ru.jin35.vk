package com.jin35.vk.net.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackgroundTasksQueue {

    private static BackgroundTasksQueue instance;
    private final ExecutorService executorService;

    private BackgroundTasksQueue() {
        BlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>();
        executorService = new ThreadPoolExecutor(1, 2, 600, TimeUnit.SECONDS, queue);
    }

    public static BackgroundTasksQueue getInstance() {
        if (instance == null) {
            instance = new BackgroundTasksQueue();
        }
        return instance;
    }

    public void execute(BackgroundTask<?> task) {
        executorService.execute(task);
    }
}