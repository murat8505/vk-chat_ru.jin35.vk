package com.jin35.vk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

import com.jin35.vk.R;
import com.jin35.vk.model.db.DB;
import com.jin35.vk.net.OnPhotoRequestResult;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.PhotoRequestTask;
import com.jin35.vk.utils.BitmapUtils;

public class PhotoStorage {
    private final int roundPx;

    private static final int MAX_ATTEMPTS = 3;

    private final Drawable defaultPhoto;
    private static PhotoStorage instance;
    private static final String[] defaultUrls = new String[] { "http://vkontakte.ru/images/camera_a.gif", "http://vkontakte.ru/images/camera_b.gif",
            "http://vkontakte.ru/images/camera_c.gif" };
    private final Bitmap defaultPhotoBitmap;

    private final LruCache<String, Drawable> photos = new LruCache<String, Drawable>(100) {
        @Override
        protected Drawable create(String key) {
            Bitmap photo = DB.getInstance().getPhoto(key);
            if (photo != null) {
                return new BitmapDrawable(photo);
            }
            return null;
        }
    };

    private final Map<Long, Bitmap> chatPhotos = new HashMap<Long, Bitmap>();

    private PhotoStorage(Context context) {
        defaultPhoto = context.getResources().getDrawable(R.drawable.contact_no_photo);
        defaultPhotoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.contact_no_photo);

        roundPx = BitmapUtils.pxFromDp(3, context);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new PhotoStorage(context);
        }
    }

    public static PhotoStorage getInstance() {
        return instance;
    }

    public synchronized Drawable getPhoto(final String photoUrl, final long notifiedObjectId) {
        return getPhoto(photoUrl, notifiedObjectId, true, true);
    }

    public synchronized Drawable getPhoto(final String photoUrl, final long notifiedObjectId, boolean returnDefaultIfNoImage, final boolean roundCorners) {
        return getPhoto(photoUrl, notifiedObjectId, returnDefaultIfNoImage, roundCorners, true);
    }

    public synchronized Drawable getPhoto(final String photoUrl, final long notifiedObjectId, boolean returnDefaultIfNoImage, final boolean roundCorners,
            final boolean saveToDb) {
        for (String defaultUrl : defaultUrls) {
            if (defaultUrl.equalsIgnoreCase(photoUrl) || photoUrl == null) {
                if (returnDefaultIfNoImage) {
                    return defaultPhoto;
                } else {
                    return null;
                }
            }
        }
        // not default photo

        Drawable result = photos.get(photoUrl);
        if (result != null) {
            return result;
        }

        // photo not downloaded

        BackgroundTasksQueue.getInstance().execute(new PhotoRequestTask(photoUrl, new OnPhotoRequestResult() {
            private int attemts = 0;

            @Override
            public void onPhotoRequestResult(Bitmap result) {

                if (roundCorners) {
                    result = BitmapUtils.getRoundedCornerBitmap(result, roundPx);
                }
                synchronized (photos) {
                    photos.put(photoUrl, new BitmapDrawable(result));
                    NotificationCenter.getInstance().notifyObjectListeners(notifiedObjectId);
                    if (saveToDb) {
                        DB.getInstance().savePhoto(photoUrl, result);
                    }
                }
            }

            @Override
            public void onPhotoRequestFail() {
                if (attemts++ < MAX_ATTEMPTS) {
                    BackgroundTasksQueue.getInstance().execute(new PhotoRequestTask(photoUrl, this));
                }
            }
        }));

        if (returnDefaultIfNoImage) {
            return defaultPhoto;
        }
        return null;
    }

    public Drawable getPhoto(UserInfo user) {
        if (user == null) {
            return getDefaultPhoto();
        }
        return getPhoto(user.getPhotoUrl(), user.getId());
    }

    public Drawable getDefaultPhoto() {
        return defaultPhoto;
    }

    public Bitmap getChatPhoto(Chat chat) {
        Bitmap result;// = chatPhotos.get(chat.getId());
        // if (result != null) {
        // return result;
        // }
        result = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
        Canvas c = new Canvas(result);

        List<Bitmap> bmps = new ArrayList<Bitmap>();
        synchronized (chat.getUsers()) {
            for (int i = 0; i < chat.getUsers().size() && bmps.size() < 4; i++) {
                long uid = chat.getUsers().get(i);
                if (uid == Token.getInstance().getCurrentUid()) {
                    continue;
                }
                UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(uid, true);
                Drawable photo = getPhoto(user);
                if (photo instanceof BitmapDrawable) {
                    bmps.add(((BitmapDrawable) photo).getBitmap());
                }
            }
        }
        switch (bmps.size()) {
        case 1:// wtf?
        case 0:// wtf?
            return defaultPhotoBitmap;
        case 2:
            c.drawBitmap(bmps.get(0), null, new Rect(0, 28, 45, 73), null);
            c.drawBitmap(bmps.get(1), null, new Rect(55, 28, 100, 73), null);
            break;
        case 3:
            c.drawBitmap(bmps.get(0), null, new Rect(0, 0, 45, 45), null);
            c.drawBitmap(bmps.get(1), null, new Rect(55, 0, 100, 45), null);
            c.drawBitmap(bmps.get(2), null, new Rect(28, 55, 73, 100), null);
            break;
        case 4:
            c.drawBitmap(bmps.get(0), null, new Rect(0, 0, 45, 45), null);
            c.drawBitmap(bmps.get(1), null, new Rect(55, 0, 100, 45), null);
            c.drawBitmap(bmps.get(2), null, new Rect(0, 55, 45, 100), null);
            c.drawBitmap(bmps.get(3), null, new Rect(55, 55, 100, 100), null);
            break;
        default:// wtf?
            return defaultPhotoBitmap;
        }
        result = BitmapUtils.getRoundedCornerBitmap(result, roundPx);
        // chatPhotos.put(chat.getId(), result);
        return result;
    }
}
