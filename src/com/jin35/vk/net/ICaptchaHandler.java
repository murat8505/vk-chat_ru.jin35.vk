package com.jin35.vk.net;

public interface ICaptchaHandler {

    public String onCapchaNeeded(String captchaImageUrl) throws InterruptedException;
}
