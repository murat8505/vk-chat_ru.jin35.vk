package com.jin35.vk.net.impl;

import com.jin35.vk.net.IAuthFailedHandler;
import com.jin35.vk.net.ICaptchaHandler;
import com.jin35.vk.net.IVKRequest;

public class VKRequestFactory {

    private static VKRequestFactory instance;

    private final ICaptchaHandler captchaHandler;
    private final IAuthFailedHandler authFailHandler;

    public static synchronized void init(ICaptchaHandler captchaHandler, IAuthFailedHandler authFailHandler) {
        if (instance == null) {
            instance = new VKRequestFactory(captchaHandler, authFailHandler);
        }
    }

    private VKRequestFactory(ICaptchaHandler captchaHandler, IAuthFailedHandler authFailHandler) {
        this.captchaHandler = captchaHandler;
        this.authFailHandler = authFailHandler;
    }

    public static VKRequestFactory getInstance() {
        return instance;
    }

    public IVKRequest getRequest() {
        return new VKRequestHTTPS(captchaHandler);
    }

}
