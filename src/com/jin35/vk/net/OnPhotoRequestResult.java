package com.jin35.vk.net;

import android.graphics.Bitmap;

public interface OnPhotoRequestResult {

    void onPhotoRequestResult(Bitmap result);

    void onPhotoRequestFail();
}
