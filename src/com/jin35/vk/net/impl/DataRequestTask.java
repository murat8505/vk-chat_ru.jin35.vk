package com.jin35.vk.net.impl;

import com.jin35.vk.net.IDataRequest;

public class DataRequestTask extends BackgroundTask<Object> {

    private final IDataRequest request;

    public DataRequestTask(IDataRequest request) {
        super(0);
        this.request = request;
    }

    @Override
    public Object execute() throws Throwable {
        request.execute();
        return null;
    }

    @Override
    public void onError() {
    }

    @Override
    public void onSuccess(Object result) {
    }

}
