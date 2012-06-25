package com.jin35.vk;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.text.format.DateUtils;

public abstract class TimeUtils {

    public static String getMessageTime(Context context, Date messageDate) {
        String result = null;
        if (DateUtils.isToday(messageDate.getTime())) {
            result = new SimpleDateFormat("hh:mm").format(messageDate);
        } else if (DateUtils.isToday(messageDate.getTime() + 24 * 60 * 60 * 1000)) {
            result = context.getString(R.string.yesterday);
        } else {
            result = new SimpleDateFormat("dd.MM").format(messageDate);
        }
        return result;
    }
}
