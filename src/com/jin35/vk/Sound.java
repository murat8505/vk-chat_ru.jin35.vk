package com.jin35.vk;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class Sound {

    private static Sound instance;
    private final Context context;
    private final SoundPool sPool;
    private final int soundId;

    private Sound(Context context) {
        this.context = context;
        sPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        soundId = sPool.load(context, R.raw.f_4dc7efd744e39, 1);
    }

    public synchronized static void init(Context context) {
        if (instance == null) {
            instance = new Sound(context);
        }
    }

    public static Sound getInstance() {
        return instance;
    }

    public void playNewMessageSound() {
        if (!PreferencesActivity.soundOn(context)) {
            return;
        }
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;// silent or vibro
        }

        sPool.play(soundId, am.getStreamVolume(AudioManager.STREAM_NOTIFICATION), am.getStreamVolume(AudioManager.STREAM_NOTIFICATION), 1, 0, 1f);

    }
}
