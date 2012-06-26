package com.jin35.vk.net.impl;

import java.io.IOException;

import com.jin35.vk.net.IDataRequest;

public class MarkAsOnlineRequest implements IDataRequest {

    @Override
    public void execute() {
        try {
            VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("account.setOnline", null);
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        }
    }

}
