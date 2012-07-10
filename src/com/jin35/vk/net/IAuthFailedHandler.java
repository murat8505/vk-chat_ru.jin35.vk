package com.jin35.vk.net;

public interface IAuthFailedHandler {
    public void onInvalidToken();

    public void onAccessDenied();
}
