package com.jin35.vk.net.impl;

import com.jin35.vk.net.IVKRequest;

public class VKRequestFactory {

    private static VKRequestFactory instance;

    private VKRequestFactory() {
    }

    public static VKRequestFactory getInstance() {
        if (instance == null) {
            instance = new VKRequestFactory();
        }
        return instance;
    }

    public IVKRequest getRequest() {
        return new VKRequestHTTPS();
    }

}
