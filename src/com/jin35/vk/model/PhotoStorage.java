package com.jin35.vk.model;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jin35.vk.R;

public class PhotoStorage {

    private final Context context;
    private final Drawable defaultPhoto;
    private static PhotoStorage instance;
    private static final String[] defaultUrls = new String[] { "http://vkontakte.ru/images/camera_a.gif", "http://vkontakte.ru/images/camera_b.gif",
            "http://vkontakte.ru/images/camera_c.gif" };

    private final Map<String, Drawable> photos = new HashMap<String, Drawable>();

    private PhotoStorage(Context context) {
        this.context = context;
        defaultPhoto = context.getResources().getDrawable(R.drawable.contact_no_photo);
    }

    public static synchronized void init(Context context) {
        instance = new PhotoStorage(context);
    }

    public static PhotoStorage getInstance() {
        return instance;
    }

    public Drawable getPhoto(String photoUrl) {
        for (String defaultUrl : defaultUrls) {
            if (defaultUrl.equalsIgnoreCase(photoUrl))
                return defaultPhoto;
        }
        return defaultPhoto;
    }
}
