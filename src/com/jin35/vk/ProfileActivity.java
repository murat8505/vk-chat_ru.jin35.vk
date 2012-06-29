package com.jin35.vk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;

public class ProfileActivity extends Activity {

    private static final String UID_EXTRA = "uid";
    private long uid;

    public static void start(Context context, long uid) {
        Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra(UID_EXTRA, uid);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uid = getIntent().getLongExtra(UID_EXTRA, -1);
        if (uid < 0) {
            finish();
            return;
        }
        setContentView(R.layout.profile);

        findViewById(R.id.back_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateTopPanel();
        NotificationCenter.getInstance().addObjectListener(uid, new IModelListener() {
            @Override
            public void dataChanged() {
                updateTopPanel();
            }
        });

        LinearLayout btnContainer = (LinearLayout) findViewById(R.id.btn_container_ll);
        LayoutInflater inflater = LayoutInflater.from(this);
        Button btn = (Button) inflater.inflate(R.layout.blue_btn, btnContainer, false);
        btn.setText(R.string.send_message);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ConversationActivity.start(ProfileActivity.this, uid);
            }
        });
        btnContainer.addView(btn);
        if (UserStorageFactory.getInstance().getUserStorage().isFriend(uid)) {
            View v = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
            params.weight = 1;
            v.setLayoutParams(params);
            btnContainer.addView(v);
            btn = (Button) inflater.inflate(R.layout.transparent_btn, btnContainer, false);
            btn.setText(R.string.remove_from_friends);
            btnContainer.addView(btn);
            btn.setOnClickListener(getRefuseOnClickListener());
        } else if (UserStorageFactory.getInstance().getUserStorage().isRequest(uid)) {
            btn = (Button) inflater.inflate(R.layout.blue_btn, btnContainer, false);
            btn.setText(R.string.add_to_friends);
            btn.setOnClickListener(getAddOnClickListener());
            btnContainer.addView(btn);
            btn = (Button) inflater.inflate(R.layout.transparent_btn, btnContainer, false);
            btn.setText(R.string.refuse_request);
            btnContainer.addView(btn);
            btn.setOnClickListener(getRefuseOnClickListener());
        } else {
            btn = (Button) inflater.inflate(R.layout.blue_btn, btnContainer, false);
            btn.setText(R.string.add_to_friends);
            btnContainer.addView(btn);
            btn.setOnClickListener(getAddOnClickListener());
        }
    }

    private OnClickListener getRefuseOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserStorageFactory.getInstance().getUserStorage().removeFriend(uid);
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getDeleteUserRequest(uid)));
            }
        };
    }

    private OnClickListener getAddOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserStorageFactory.getInstance().getUserStorage().markAsFriend(uid);
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getAddUserRequest(uid)));
            }
        };
    }

    private void updateTopPanel() {
        TextView nameText = (TextView) findViewById(R.id.name_tv);
        ImageView photo = (ImageView) findViewById(R.id.photo_iv);
        UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(uid, true);
        if (user != null) {
            photo.setImageDrawable(PhotoStorage.getInstance().getPhoto(user));
            nameText.setText(user.getFullName());
        } else {
            photo.setImageDrawable(PhotoStorage.getInstance().getDefaultPhoto());
            nameText.setText(R.string.not_dowanloaded_name);
        }
    }
}
