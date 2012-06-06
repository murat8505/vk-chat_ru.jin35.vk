package com.jin35.vk.net;

import java.io.IOException;
import java.net.URL;

import android.graphics.Bitmap;

public interface IPhotoRequest {
    Bitmap executeRequest(URL photoUrl) throws IOException;

    Bitmap executeRequest(String photoUrl) throws IOException;
}
