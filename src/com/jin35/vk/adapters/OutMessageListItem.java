package com.jin35.vk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jin35.vk.R;
import com.jin35.vk.model.AttachmentPack;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.Message;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.net.Token;

public class OutMessageListItem extends MessageListItem {

    public OutMessageListItem(Message object, Context context) {
        super(object, context);
    }

    @Override
    public void subsribeListenerForObject(IModelListener listener) {
        super.subsribeListenerForObject(listener);
        NotificationCenter.getInstance().addObjectListener(Token.getInstance().getCurrentUid(), listener);
    }

    @Override
    protected View getMessageContentView(Context context, ViewGroup root) {
        LinearLayout result = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.simple_out_message_content, root, false);
        ((ImageView) result.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getPhoto(Token.getInstance().getCurrentUserPhoto(),
                Token.getInstance().getCurrentUid()));
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
}
