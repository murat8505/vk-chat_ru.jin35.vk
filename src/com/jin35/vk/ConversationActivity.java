package com.jin35.vk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jin35.vk.adapters.Adapter;
import com.jin35.vk.adapters.ConversationAdapter;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.IObjectListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestFactory;
import com.jin35.vk.net.impl.DataRequestTask;

public class ConversationActivity extends ListActivity {

    private Uri cameraURI;
    private final List<Bitmap> attaches = new ArrayList<Bitmap>();
    private Pair<Double, Double> location;

    private static final String USER_ID_EXTRA = "userId";
    private static final int ACTIVITY_CAMERA = 5787;
    private static final int ACTIVITY_GALLERY = 346;

    private static final int MAX_PICTURE_SIZE = 500;

    private long userId;

    public static void start(Context context, long userId) {
        Intent startIntent = new Intent(context, ConversationActivity.class);
        startIntent.putExtra(USER_ID_EXTRA, userId);
        context.startActivity(startIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = getIntent().getLongExtra(USER_ID_EXTRA, 0);

        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getMessagesWithUserRequest(userId)));

        setContentView(R.layout.list);

        LinearLayout listContainer = (LinearLayout) findViewById(R.id.list_container);
        listContainer.addView(LayoutInflater.from(this).inflate(R.layout.msg_send_panel, listContainer, false));

        findViewById(R.id.msg_send_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = ((TextView) findViewById(R.id.msg_send_tv));
                String messageText = tv.getText().toString();
                tv.setText("");
                if (!attaches.isEmpty()) {
                    // TODO
                    attaches.clear();
                }
                if (location != null) {
                    // TODO
                    location = null;
                }
                Message msg = new Message(Message.getUniqueTempId(), userId, messageText, new Date(System.currentTimeMillis()), false);
                msg.setSent(false);
                msg.setRead(false);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
                MessageStorage.getInstance().addMessage(msg);
                NotificationCenter.getInstance().notifyModelListeners(NotificationCenter.MODEL_MESSAGES);
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getSendMessageRequest(msg)));
            }
        });
        findViewById(R.id.msg_attach_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAttachmentClick();
            }
        });
        findViewById(R.id.attach_photo_ll).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAttachMenu();
                makePhotoWithCamera();
            }
        });
        findViewById(R.id.attach_gallery_ll).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAttachMenu();
                selectPhotoFromGallery();
            }
        });

        ViewGroup topBar = (ViewGroup) findViewById(R.id.top_bar_ll);
        topBar.removeAllViews();
        topBar.addView(LayoutInflater.from(this).inflate(R.layout.conversation_top_panel, topBar, false));

        findViewById(R.id.back_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Adapter<?> adapter = new ConversationAdapter(this, userId);
        getListView().setDividerHeight(0);
        getListView().setBackgroundDrawable(null);
        getListView().setStackFromBottom(true);
        getListView().setAdapter(adapter);

        updateTopPanel();
        NotificationCenter.getInstance().addObjectListener(userId, new IObjectListener() {
            @Override
            public void dataChanged(long objectId) {
                updateTopPanel();
            }
        });

        NotificationCenter.getInstance().addModelListener(NotificationCenter.MODEL_SELECTED, new IModelListener() {
            @Override
            public void dataChanged() {
                updateTopPanel();
            }
        });

        findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Message> messages = MessageStorage.getInstance().clearSelected();
                for (Message msg : messages) {
                    msg.notifyChanges();
                }
            }
        });

        // findViewById(R.id.forward_btn).setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // List<Message> messages = MessageStorage.getInstance().getSelected();
        //
        // }
        // });

        markMessagesAsRead();
    }

    private void markMessagesAsRead() {
        String unreadMids = "";
        List<Message> msgs = MessageStorage.getInstance().getMessagesWithUser(userId);
        for (Message msg : msgs) {
            if (!msg.isRead() && msg.isIncome()) {
                unreadMids = unreadMids.concat(String.valueOf(msg.getId())).concat(",");
                msg.setRead(true);
                msg.notifyChanges();
            }
        }
        if (unreadMids.length() > 0) {
            unreadMids = unreadMids.substring(0, unreadMids.length() - 1);
            BackgroundTasksQueue.getInstance().execute(new DataRequestTask(DataRequestFactory.getInstance().getMarkAsReadRequest(unreadMids)));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MessageStorage.getInstance().clearSelected();
        markMessagesAsRead();
    }

    private void updateTopPanel() {
        List<Message> selected = MessageStorage.getInstance().getSelected();
        if (selected.size() == 0) {
            findViewById(R.id.user_ll).setVisibility(View.VISIBLE);
            findViewById(R.id.btns_ll).setVisibility(View.GONE);
            UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(userId, true);
            if (user == null) {
                ((TextView) findViewById(R.id.name_tv)).setText("...");
                findViewById(R.id.online_indicator_iv).setVisibility(View.GONE);
                ((ImageView) findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getDefaultPhoto());
            } else {
                ((TextView) findViewById(R.id.name_tv)).setText(user.getFullName());
                findViewById(R.id.online_indicator_iv).setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
                ((ImageView) findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getPhoto(user));
            }
        } else {
            findViewById(R.id.btns_ll).setVisibility(View.VISIBLE);
            findViewById(R.id.user_ll).setVisibility(View.GONE);
        }
    }

    private void onAttachmentClick() {
        View attachMenu = findViewById(R.id.attach_menu_ll);
        View attachPanel = findViewById(R.id.attachments_panel_hsv);
        if (attachMenu.getVisibility() == View.VISIBLE) {
            hideAttachMenu();
            return;
        }
        if (attachPanel.getVisibility() == View.VISIBLE) {
            hideAttachPanel();
            return;
        }
        findViewById(R.id.msg_attach_iv).setSelected(true);
        if (attaches.size() == 0 && location == null) {
            attachMenu.setVisibility(View.VISIBLE);
        } else {
            updateAttachmentPanel();
            attachPanel.setVisibility(View.VISIBLE);
        }
    }

    private void hideAttachMenu() {
        View attachBtn = findViewById(R.id.msg_attach_iv);
        View attachMenu = findViewById(R.id.attach_menu_ll);
        attachBtn.setSelected(false);
        attachMenu.setVisibility(View.INVISIBLE);
    }

    private void hideAttachPanel() {
        View attachBtn = findViewById(R.id.msg_attach_iv);
        View attachPanel = findViewById(R.id.attachments_panel_hsv);
        attachBtn.setSelected(false);
        attachPanel.setVisibility(View.GONE);
    }

    private void updateAttachmentBtn() {
        ImageView attachBtn = (ImageView) findViewById(R.id.msg_attach_iv);
        if (attaches.size() == 0) {
            attachBtn.setScaleType(ScaleType.FIT_CENTER);
            if (location == null) {
                attachBtn.setImageResource(R.drawable.attach_btn_bckg);
            } else {
                // TODO
                attachBtn.setImageResource(R.drawable.attach_btn_bckg);
            }
        } else {
            attachBtn.setScaleType(ScaleType.CENTER_CROP);
            attachBtn.setImageBitmap(attaches.get(0));
        }
    }

    private void updateAttachmentPanel() {
        ViewGroup attachPanel = (ViewGroup) findViewById(R.id.attachments_panel_hsv);
        LinearLayout ll = (LinearLayout) attachPanel.findViewById(R.id.attachments_panel_container_ll);
        ll.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (final Bitmap bitmap : attaches) {
            ViewGroup attachmentView = (ViewGroup) inflater.inflate(R.layout.attachments_panel_item, ll, false);
            ImageView attachImageView = (ImageView) attachmentView.findViewById(R.id.attachment_iv);
            attachImageView.setScaleType(ScaleType.CENTER_CROP);
            attachImageView.setImageBitmap(bitmap);
            ll.addView(attachmentView);
            attachmentView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    attaches.remove(bitmap);
                    updateAttachmentBtn();
                    updateAttachmentPanel();
                    if (attaches.size() == 0) {
                        hideAttachPanel();
                    }
                }
            });
        }
        ViewGroup attachmentView = (ViewGroup) inflater.inflate(R.layout.attachments_panel_item, ll, false);
        ((ImageView) attachmentView.findViewById(R.id.attachment_iv)).setImageResource(R.drawable.ic_new_attach_photo);
        attachmentView.findViewById(R.id.delete_attachment_iv).setVisibility(View.GONE);
        attachmentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhotoWithCamera();
            }
        });
        ll.addView(attachmentView);
        attachmentView = (ViewGroup) inflater.inflate(R.layout.attachments_panel_item, ll, false);
        ((ImageView) attachmentView.findViewById(R.id.attachment_iv)).setImageResource(R.drawable.ic_new_attach_gallery);
        attachmentView.findViewById(R.id.delete_attachment_iv).setVisibility(View.GONE);
        attachmentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhotoFromGallery();
            }
        });
        ll.addView(attachmentView);

        if (location == null) {
            attachmentView = (ViewGroup) inflater.inflate(R.layout.attachments_panel_item, ll, false);
            ((ImageView) attachmentView.findViewById(R.id.attachment_iv)).setImageResource(R.drawable.ic_new_attach_loc);
            attachmentView.findViewById(R.id.delete_attachment_iv).setVisibility(View.GONE);
            ll.addView(attachmentView);
        } else {
            attachmentView = (ViewGroup) inflater.inflate(R.layout.attachments_panel_item, ll, false);
            ((ImageView) attachmentView.findViewById(R.id.attachment_iv)).setImageResource(R.drawable.ic_attach_loc);
            ll.addView(attachmentView);
            attachmentView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    location = null;
                    updateAttachmentPanel();
                }
            });
        }
    }

    private void selectPhotoFromGallery() {
        Intent i = (new Intent(Intent.ACTION_GET_CONTENT)).setType("image/*");
        startActivityForResult(Intent.createChooser(i, getString(R.string.gallery_choice)), ACTIVITY_GALLERY);
    }

    private void makePhotoWithCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraURI = getPhotoUri(this);
        i.putExtra(MediaStore.EXTRA_OUTPUT, cameraURI);
        startActivityForResult(Intent.createChooser(i, getString(R.string.camera_choice)), ACTIVITY_CAMERA);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case ACTIVITY_CAMERA:
            if (resultCode != Activity.RESULT_CANCELED) {
                attachPictureByUri(cameraURI);
                cameraURI = null;
                updateAttachmentBtn();
                updateAttachmentPanel();
            }
            break;
        case ACTIVITY_GALLERY:
            if (resultCode != Activity.RESULT_CANCELED) {
                attachPictureByUri(data.getData());
                updateAttachmentBtn();
                updateAttachmentPanel();
            }
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    private void attachPictureByUri(Uri pictureUri) {
        InputStream pictureInputStream = null;
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

            Bitmap newPicture = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            if (newPicture != null) {
                attaches.add(newPicture);
            }
        } catch (Throwable th) {
            Toast.makeText(this, R.string.error_in_adding_attach, 3000).show();
            th.printStackTrace();
        } finally {
            if (pictureInputStream != null) {
                try {
                    pictureInputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Generate only one Uri for operation. Save it for reading results
     */
    private Uri getPhotoUri(Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "tmp.vk.jpg");
        Uri res = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return res;
    }
}
