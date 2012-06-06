package com.jin35.vk.net.impl;

import com.jin35.vk.net.IPhotoRequest;

public class PhotoRequestFactory {

    private static PhotoRequestFactory instance;

    private PhotoRequestFactory() {
    }

    public static synchronized PhotoRequestFactory getInstance() {
        if (instance == null)
            instance = new PhotoRequestFactory();
        return instance;
    }

    public IPhotoRequest getPhotoRequest() {
        return new PhotoRequest();
    }
}
