package com.jin35.vk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.db.DB;
import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.LongPollServerConnection;
import com.jin35.vk.net.impl.VKRequestFactory;

public class PreferencesActivity extends Activity {

    private Uri cameraURI;

    public static final String LOGOUT = "logout";

    private static final String PREFS = "prefs";
    private static final String SOUND_PREF = "sound";
    private static final String PUSH_PREF = "push";

    private static final int MAX_PICTURE_SIZE = 1000;

    private static final int ACTIVITY_CAMERA = 324;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs);

        NotificationCenter.getInstance().addObjectListener(Token.getInstance().getCurrentUid(), new IModelListener() {
            @Override
            public void dataChanged() {
                updatePhoto();
            }
        });
        updatePhoto();

        findViewById(R.id.change_photo_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhotoWithCamera();
            }
        });

        findViewById(R.id.switch_sound_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSound(PreferencesActivity.this, !soundOn(PreferencesActivity.this));
                updatePrefTexts();
            }
        });

        findViewById(R.id.switch_push_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setPush(PreferencesActivity.this, !pushOn(PreferencesActivity.this));
                updatePrefTexts();
            }
        });
        updatePrefTexts();

        findViewById(R.id.logout_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLongPoll();
                DB.getInstance().clearCache();
                Token.getInstance().removeToken();
                finish();
            }
        });

        findViewById(R.id.exit_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLongPoll();
                finish();
            }
        });
    }

    private void updatePhoto() {
        ((ImageView) findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getPhoto(Token.getInstance().getCurrentUserPhoto(),
                Token.getInstance().getCurrentUid()));
    }

    private void updatePrefTexts() {
        ((Button) findViewById(R.id.switch_sound_btn)).setText(soundOn(this) ? R.string.switch_off_sound : R.string.switch_on_sound);
        ((Button) findViewById(R.id.switch_push_btn)).setText(pushOn(this) ? R.string.switch_off_push : R.string.switch_on_push);
    }

    private void stopLongPoll() {
        LongPollServerConnection.getInstance().stopConnection();
    }

    private static void setSound(Context context, boolean value) {
        context.getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(SOUND_PREF, value).commit();
    }

    private static void setPush(Context context, boolean value) {
        context.getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(PUSH_PREF, value).commit();
    }

    public static boolean soundOn(Context context) {
        return context.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(SOUND_PREF, true);
    }

    public static boolean pushOn(Context context) {
        return context.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PUSH_PREF, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case ACTIVITY_CAMERA: {
            if (resultCode != Activity.RESULT_CANCELED) {
                final Bitmap picture = getBitmapFromByUri(cameraURI);
                cameraURI = null;
                ImageView iv = ((ImageView) findViewById(R.id.photo_iv));
                iv.setImageResource(R.drawable.loader_blue);
                ((AnimationDrawable) iv.getDrawable()).start();
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
                    @Override
                    public void execute() {
                        try {
                            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("photos.getProfileUploadServer", null);
                            String serverUrl = response.getJSONObject(responseParam).getString("upload_url");

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            picture.compress(CompressFormat.PNG, 100, baos);
                            JSONObject photoUploadResponse = VKRequestFactory.getInstance().getRequest()
                                    .executePost(serverUrl, "photo", "image/png", baos.toByteArray());
                            Map<String, String> paramsForSave = new HashMap<String, String>();
                            paramsForSave.put("server", photoUploadResponse.getString("server"));
                            paramsForSave.put("photo", photoUploadResponse.getString("photo"));
                            paramsForSave.put("hash", photoUploadResponse.getString("hash"));
                            JSONObject photoSaveResponse = VKRequestFactory.getInstance().getRequest()
                                    .executeRequestToAPIServer("photos.saveProfilePhoto", paramsForSave);

                            if (photoSaveResponse.has(responseParam)) {
                                DataRequestFactory.getInstance().getCurrentUserPhotoRequest().execute();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updatePhoto();
                                    }
                                });
                            } else {
                                showError();
                            }
                        } catch (Exception e) {
                            showError();
                        }
                    }

                    private void showError() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PreferencesActivity.this, R.string.error_in_uploading_photo, Toast.LENGTH_LONG).show();
                                updatePhoto();
                            }
                        });
                    }
                }));
            }
            break;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }

    private Bitmap getBitmapFromByUri(Uri pictureUri) {
        InputStream pictureInputStream = null;
        Bitmap newPicture = null;
        try {
            pictureInputStream = getContentResolver().openInputStream(pictureUri);
            byte[] data = new byte[pictureInputStream.available()];
            pictureInputStream.read(data);
            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            // int opts.outHeight;

            int scale = 1;
            while (opts.outWidth / scale / 2 >= MAX_PICTURE_SIZE || opts.outHeight / scale / 2 >= MAX_PICTURE_SIZE) {
                scale *= 2;
            }

            opts = new Options();
            opts.inSampleSize = scale;

            newPicture = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        } catch (Throwable th) {
            Toast.makeText(this, R.string.error_in_adding_attach, 3000).show();
        } finally {
            if (pictureInputStream != null) {
                try {
                    pictureInputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return newPicture;
    }

    private void makePhotoWithCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraURI = getPhotoUri(this);
        i.putExtra(MediaStore.EXTRA_OUTPUT, cameraURI);
        startActivityForResult(Intent.createChooser(i, getString(R.string.camera_choice)), ACTIVITY_CAMERA);
    }

    private Uri getPhotoUri(Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "tmp.vk.jpg");
        Uri res = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return res;
    }
}
