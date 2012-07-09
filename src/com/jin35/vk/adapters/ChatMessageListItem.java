package com.jin35.vk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jin35.vk.ChatConversationActivity;
import com.jin35.vk.R;
import com.jin35.vk.model.AttachmentPack;
import com.jin35.vk.model.Chat;
import com.jin35.vk.model.ChatMessage;
import com.jin35.vk.model.ChatStorage;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.MessageStorage;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;

public class ChatMessageListItem extends MessageListItem {

    public ChatMessageListItem(ChatMessage object, Context context) {
        super(object, context);
    }

    @Override
    public int getViewId() {
        return R.layout.message_list_item;
    }

    @Override
    protected void fillHeader(View view) {
        view.findViewById(R.id.online_indicator_iv).setVisibility(View.GONE);
        view.findViewById(R.id.group_chat_indicator_iv).setVisibility(View.VISIBLE);
        Chat chat = ChatStorage.getInstance().getChat(getObject().getCorrespondentId());
        if (chat == null) {
            ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getDefaultPhoto());
            ((TextView) view.findViewById(R.id.name_tv)).setText(R.string.not_dowanloaded_name);
        } else {
            // ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(correspondent.getPhoto());
            // TODO
            ((ImageView) view.findViewById(R.id.photo_iv)).setImageBitmap(PhotoStorage.getInstance().getChatPhoto(chat));
            ((TextView) view.findViewById(R.id.name_tv)).setText(chat.getChatName());
        }
    }

    @Override
    protected View getMessageContentView(Context context, ViewGroup root) {
        LinearLayout result = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.simple_out_message_content, root, false);
        UserInfo author = UserStorageFactory.getInstance().getUserStorage().getUser(((ChatMessage) getObject()).getAuthorId(), true);
        if (author != null) {
            ((ImageView) result.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getPhoto(author));
        } else {
            ((ImageView) result.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getDefaultPhoto());
        }
        TextView tv = (TextView) result.findViewById(R.id.text_tv);
        CharSequence msgText = getObject().getText().replace("<br>", "\n");
        if (getObject().hasAnyAttaches()) {
            msgText = AttachmentPack.addSpans(msgText, context, getObject().hasFwd(), getObject().hasLoc(), getObject().getAttachmentPack());
        }
        tv.setText(msgText);
        if (!getObject().isRead()) {
            tv.setBackgroundResource(R.drawable.msg_out_unread_bckg);
        }
        return result;
    }

    @Override
    protected OnClickListener getOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatConversationActivity.start(getObject().getCorrespondentId(), v.getContext());
            }
        };
    }

    @Override
    protected boolean hasUnread() {
        return MessageStorage.getInstance().hasUnreadMessagesFromChat(getObject().getCorrespondentId());
    }

    @Override
    public void subsribeListenerForObject(IModelListener listener) {
        super.subsribeListenerForObject(listener);
        NotificationCenter.getInstance().addObjectListener(((ChatMessage) getObject()).getAuthorId(), listener);
    }

}
