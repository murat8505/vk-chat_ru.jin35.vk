package com.jin35.vk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;

public class CapthcaActivity extends Activity {

    public static final String PREF_NAME = "captca prefs";

    public static final String RESULT_PREF_NAME = "captca result";

    public static final String IMG_URL_EXTRA = "img url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.captcha_input);
        final ImageView image = (ImageView) findViewById(R.id.captcha_iv);
        final String imgUrl = getIntent().getStringExtra(IMG_URL_EXTRA);
        NotificationCenter.getInstance().addObjectListener(-42, new IModelListener() {
            @Override
            public void dataChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageDrawable(PhotoStorage.getInstance().getPhoto(imgUrl, -42, true, false, false));
                    }
                });
            }
        });
        image.setImageDrawable(PhotoStorage.getInstance().getPhoto(imgUrl, -42, true, false, false));
        findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(CapthcaActivity.PREF_NAME, Activity.MODE_PRIVATE).edit()
                        .putString(RESULT_PREF_NAME, ((EditText) findViewById(R.id.captcha_et)).getText().toString()).commit();
                finish();
            }
        });
    }
}
