package com.jin35.vk.adapters;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jin35.vk.LocationSelectActivity;
import com.jin35.vk.R;
import com.jin35.vk.TimeUtils;
import com.jin35.vk.model.Attachment;
import com.jin35.vk.model.AttachmentPack;
import com.jin35.vk.model.ForwardedMsg;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.utils.BitmapUtils;

public abstract class ConversationListItem extends ModelObjectListItem<Message> {
    private final Adapter<?> adapter;

    public ConversationListItem(Adapter<?> adapter, Message object) {
        super(object);
        this.adapter = adapter;
    }

    @Override
    public boolean needListener() {
        return true;
    }

    @Override
    public void subsribeListenerForObject(IModelListener listener) {
        NotificationCenter.getInstance().addObjectListener(getObject().getId(), listener);
        if (getObject().hasAttaches()) {
            for (Attachment a : getObject().getAttachmentPack()) {
                NotificationCenter.getInstance().addObjectListener(a.getId(), listener);
            }
        }
        if (getObject().hasFwd()) {
            for (ForwardedMsg msg : getObject().getForwarded()) {
                NotificationCenter.getInstance().addObjectListener(msg.getAuthorId(), listener);
            }
        }
    }

    @Override
    public void updateView(final View view) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = !MessageStorage.getInstance().isSelected(getObject());
                view.setSelected(selected);
                MessageStorage.getInstance().setSelection(getObject(), selected);
            }
        });
        // далее идет суровый костыль,
        // но если напрямую вызывать view.setSelected
        // то отрисовка не отработает
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setSelected(MessageStorage.getInstance().isSelected(getObject()));
            }
        }, 1);
    }

    protected void addContent(View view) {
        LinearLayout msgContentLayout = (LinearLayout) view.findViewById(R.id.msg_content_ll);
        msgContentLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        if (!TextUtils.isEmpty(getObject().getText())) {
            TextView msgTextView = (TextView) inflater.inflate(R.layout.simple_conversation_content, msgContentLayout, false);
            msgTextView.setText(getObject().getText().replace("<br>", "\n"));
            msgContentLayout.addView(msgTextView);
        }
        if (getObject().hasAnyAttaches()) {
            if (getObject().hasLoc()) {
                View v = inflater.inflate(R.layout.loc_conversation_content, msgContentLayout, false);
                String size = BitmapUtils.pxFromDp(240, v.getContext()) + "x" + BitmapUtils.pxFromDp(180, v.getContext());
                String location = String.valueOf(getObject().getLocation().first) + "," + String.valueOf(getObject().getLocation().second);
                String photoUrl = "http://maps.google.com/maps/api/staticmap?center=" + location + "&zoom=14&size=" + size + "&sensor=false";
                Drawable map = PhotoStorage.getInstance().getPhoto(photoUrl, getObject().getId(), false, false);
                ImageView mapIv = (ImageView) v.findViewById(R.id.map_iv);
                if (map != null) {
                    mapIv.setImageDrawable(map);
                } else {
                    mapIv.setImageDrawable(new ColorDrawable(0xDD777777));
                }
                mapIv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), LocationSelectActivity.class);
                        i.putExtra(LocationSelectActivity.NEED_SELECT_BTN_EXTRA, false);
                        i.putExtra(LocationSelectActivity.LOC_EXTRA, new double[] { getObject().getLocation().first, getObject().getLocation().second });
                        v.getContext().startActivity(i);
                    }
                });
                msgContentLayout.addView(v);
            }
            if (getObject().hasAttaches()) {
                AttachmentPack attaches = getObject().getAttachmentPack();
                for (Attachment a : attaches) {
                    View v = inflater.inflate(a.getConversationViewId(), msgContentLayout, false);
                    a.fillConversationView(v);
                    msgContentLayout.addView(v);
                }
            }
            if (getObject().hasFwd()) {
                for (ForwardedMsg fwdMsg : getObject().getForwarded()) {
                    View v = inflater.inflate(R.layout.fwd_conversation_content, msgContentLayout, false);
                    UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(fwdMsg.getAuthorId(), true);
                    TextView nameTv = (TextView) v.findViewById(R.id.name_tv);
                    if (user == null) {
                        nameTv.setText(R.string.not_dowanloaded_name);
                    } else {
                        nameTv.setText(user.getFullName());
                    }
                    ((TextView) v.findViewById(R.id.time_tv)).setText(TimeUtils.getMessageTime(v.getContext(), fwdMsg.getTime()));
                    ((ImageView) v.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getPhoto(user));
                    CharSequence text = fwdMsg.getMsgText().replace("<br>", "\n");
                    if (fwdMsg.hasAnyAttaches()) {
                        text = AttachmentPack.addSpans(text, v.getContext(), fwdMsg.isHasFrw(), fwdMsg.isHasLoc(), fwdMsg.getAttaches());
                    }
                    ((TextView) v.findViewById(R.id.text_tv)).setText(text);
                    msgContentLayout.addView(v);
                }
            }
        }
    }
}
