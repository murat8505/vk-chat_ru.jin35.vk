package com.jin35.vk.net.impl;

import android.graphics.Bitmap;

import com.jin35.vk.net.OnPhotoRequestResult;

public class PhotoRequestTask extends BackgroundTask<Bitmap> {

    private final String photoUrl;
    private final OnPhotoRequestResult onPhotoRequestResult;

    public PhotoRequestTask(String photoUrl, OnPhotoRequestResult onPhotoRequestResult) {
        super(1);
        this.onPhotoRequestResult = onPhotoRequestResult;
        this.photoUrl = photoUrl;
    }

    @Override
    public void onSuccess(Bitmap result) {
        onPhotoRequestResult.onPhotoRequestResult(result);
    }

    @Override
    public Bitmap execute() throws Exception {
        return PhotoRequestFactory.getInstance().getPhotoRequest().executeRequest(photoUrl);
    }

    @Override
    public void onError() {
        onPhotoRequestResult.onPhotoRequestFail();
    }
}