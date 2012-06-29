package com.jin35.vk.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.TypedValue;

import com.jin35.vk.R;
import com.jin35.vk.model.db.DB;
import com.jin35.vk.net.OnPhotoRequestResult;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.PhotoRequestTask;

public class PhotoStorage {
    private final int roundPx;

    private static final int MAX_ATTEMPTS = 3;

    private final Drawable defaultPhoto;
    private static PhotoStorage instance;
    private static final String[] defaultUrls = new String[] { "http://vkontakte.ru/images/camera_a.gif", "http://vkontakte.ru/images/camera_b.gif",
            "http://vkontakte.ru/images/camera_c.gif" };

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

    private PhotoStorage(Context context) {
        defaultPhoto = context.getResources().getDrawable(R.drawable.contact_no_photo);

        roundPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, context.getResources().getDisplayMetrics());
    }

    public static synchronized void init(Context context) {
        instance = new PhotoStorage(context);
    }

    public static PhotoStorage getInstance() {
        return instance;
    }

    public synchronized Drawable getPhoto(final String photoUrl, final long userId) {
        for (String defaultUrl : defaultUrls) {
            if (defaultUrl.equalsIgnoreCase(photoUrl)) {
                return defaultPhoto;
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

                result = getRoundedCornerBitmap(result, roundPx);
                synchronized (photos) {
                    photos.put(photoUrl, new BitmapDrawable(result));
                    NotificationCenter.getInstance().notifyObjectListeners(userId);
                    DB.getInstance().savePhoto(photoUrl, result);
                }
            }

            @Override
            public void onPhotoRequestFail() {
                if (attemts++ < MAX_ATTEMPTS) {
                    BackgroundTasksQueue.getInstance().execute(new PhotoRequestTask(photoUrl, this));
                }
            }
        }));

        return defaultPhoto;
    }

    public Drawable getPhoto(UserInfo user) {
        return getPhoto(user.getPhotoUrl(), user.getId());
    }

    public Drawable getDefaultPhoto() {
        return defaultPhoto;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
