package com.jin35.vk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jin35.vk.ConversationActivity;
import com.jin35.vk.R;
import com.jin35.vk.TimeUtils;
import com.jin35.vk.model.AttachmentPack;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class MessageListItem extends ModelObjectListItem<Message> {

    private final Context context;

    private static final int MESSAGE_CONTENT_VIEW_ID = 63234;

    public MessageListItem(Message object, Context context) {
        super(object);
        this.context = context;
    }

    @Override
    public int getViewId() {
        return R.layout.message_list_item;
    }

    protected View getMessageContentView(Context context, ViewGroup root) {
        CharSequence msgText = getObject().getText().replace("<br>", "\n");

        if (getObject().hasAnyAttaches()) {
            msgText = AttachmentPack.addSpans(msgText, context, getObject().hasFwd(), getObject().hasLoc(), getObject().getAttachmentPack());
        }

        TextView result = (TextView) LayoutInflater.from(context).inflate(R.layout.simple_message_content, null);
        result.setText(msgText);
        return result;
    }

    @Override
    public void updateView(View view) {
        UserInfo correspondent = UserStorageFactory.getInstance().getUserStorage().getUser(getObject().getCorrespondentId(), true);
        if (correspondent == null) {
            ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getDefaultPhoto());
            view.findViewById(R.id.online_indicator_iv).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.name_tv)).setText(R.string.not_dowanloaded_name);
        } else {
            ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(correspondent.getPhoto());
            int onlineVisibility = correspondent.isOnline() ? View.VISIBLE : View.GONE;
            view.findViewById(R.id.online_indicator_iv).setVisibility(onlineVisibility);
            ((TextView) view.findViewById(R.id.name_tv)).setText(correspondent.getFullName());
        }

        ((TextView) view.findViewById(R.id.time_tv)).setText(TimeUtils.getMessageTime(context, getObject().getTime()));

        if (view instanceof ViewGroup) {
            View old = view.findViewById(MESSAGE_CONTENT_VIEW_ID);
            if (old != null) {
                ((ViewGroup) view).removeView(old);
            }

            View messageContent = getMessageContentView(context, (ViewGroup) view);
            if (messageContent != null) {
                messageContent.setId(MESSAGE_CONTENT_VIEW_ID);
                // messageContent.setLayoutParams(view.findViewById(R.id.message_v).getLayoutParams());
                ((ViewGroup) view).addView(messageContent, view.findViewById(R.id.message_v).getLayoutParams());
            }
        }
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ConversationActivity.start(context, getObject().getCorrespondentId());
            }
        });

        if (MessageStorage.getInstance().hasUnreadMessagesFromUser(getObject().getCorrespondentId())) {
            view.setBackgroundResource(R.drawable.conversation_unread_bckg);
        } else {
            view.setBackgroundResource(R.drawable.conversation_read_bckg);
        }
    }

    @Override
    public boolean needListener() {
        return true;
    }

    @Override
    public void subsribeListenerForObject(IModelListener listener) {
        NotificationCenter.getInstance().addObjectListener(getObject().getId(), listener);
        NotificationCenter.getInstance().addObjectListener(getObject().getCorrespondentId(), listener);
    }

}
