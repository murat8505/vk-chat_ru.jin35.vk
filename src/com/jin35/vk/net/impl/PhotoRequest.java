package com.jin35.vk.net.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jin35.vk.net.IPhotoRequest;

public class PhotoRequest implements IPhotoRequest {

    PhotoRequest() {
    }

    @Override
    public Bitmap executeRequest(URL photoUrl) throws IOException {
        InputStream input = photoUrl.openStream();
        return BitmapFactory.decodeStream(input);
    }

    @Override
    public Bitmap executeRequest(String photoUrl) throws IOException {
        return executeRequest(new URL(photoUrl));
    }

}
