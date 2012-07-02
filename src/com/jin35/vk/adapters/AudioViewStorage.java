package com.jin35.vk.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jin35.vk.R;
import com.jin35.vk.model.AudioAttach;
import com.jin35.vk.utils.BitmapUtils;

public class AudioViewStorage {

    private static AudioViewStorage instance;

    private final Map<Long, MediaPlayer> players = new HashMap<Long, MediaPlayer>();
    private final List<TimerTask> tasks = new ArrayList<TimerTask>();

    private boolean needSetController = true;

    private AudioViewStorage() {
    }

    public static synchronized AudioViewStorage getInstance() {
        if (instance == null) {
            instance = new AudioViewStorage();
        }
        return instance;
    }

    public void clear() {
        for (TimerTask tt : tasks) {
            tt.cancel();
        }
        tasks.clear();
        List<Long> trash = new ArrayList<Long>();
        for (final Entry<Long, MediaPlayer> entry : players.entrySet()) {
            MediaPlayer mp = entry.getValue();
            if (!mp.isPlaying()) {
                mp.release();
                trash.add(entry.getKey());
            } else {
                mp.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        players.remove(entry.getKey());
                    }
                });
            }
        }
        for (Long id : trash) {
            players.remove(id);
        }
    }

    public void fillView(final AudioAttach attach, final View baseView) {
        ((TextView) baseView.findViewById(R.id.audio_author_tv)).setText(attach.getPerformer());
        ((TextView) baseView.findViewById(R.id.audio_name_tv)).setText(attach.getTitle());
        ImageView playBtn = (ImageView) baseView.findViewById(R.id.play_audio_iv);

        MediaPlayer player = players.get(attach.getId());
        if (player != null) {
            System.out.println("find existing player for attach " + attach.getTitle());
            player.setOnBufferingUpdateListener(getOnBufferingUpdateListener(baseView, player));
            setLength(baseView, R.id.loaded_iv, 100);
            setLength(baseView, R.id.played_iv, getPlayerPosition(player));
            setController(baseView, getPlayerPosition(player));
            if (player.isPlaying()) {
                playBtn.setImageResource(R.drawable.ic_pause_audio);
            } else {
                playBtn.setImageResource(R.drawable.ic_play_audio);
            }
            playBtn.setOnClickListener(getPlayerBtnListener(player, playBtn));
            baseView.findViewById(R.id.audio_playback_rl).setOnTouchListener(getPlaybackTouchListener(player));
        } else {
            playBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("create new player for attach " + attach.getTitle());
                    v.setOnClickListener(null);
                    createPlayer(attach, baseView);
                }
            });
        }
    }

    private OnTouchListener getPlaybackTouchListener(final MediaPlayer player) {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalWidth = v.getWidth();
                View controller = v.findViewById(R.id.control_iv);
                int controllerWidth = controller.getWidth();
                int x = (int) event.getX();
                if (x < controllerWidth / 2) {
                    x = controllerWidth / 2;
                }
                if (x > (totalWidth - controllerWidth / 2)) {
                    x = totalWidth - controllerWidth / 2;
                }

                int percent = ((x - (controllerWidth / 2)) * 100) / (totalWidth - controllerWidth);
                setController(v, percent);

                if ((event.getAction() & MotionEvent.ACTION_DOWN) != 0) {
                    needSetController = false;
                }
                if ((event.getAction() & MotionEvent.ACTION_UP) != 0) {
                    needSetController = true;
                    player.seekTo((player.getDuration() * percent) / 100);
                }
                return true;
            }
        };
    }

    private OnBufferingUpdateListener getOnBufferingUpdateListener(final View v, final MediaPlayer player) {
        return new OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, final int percent) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("onBufferingUpdate");
                        try {
                            setLength(v, R.id.loaded_iv, percent);
                            setLength(v, R.id.played_iv, getPlayerPosition(player));
                            if (needSetController) {
                                setController(v, getPlayerPosition(player));
                            }
                        } catch (Exception e) {
                            System.out.println("error in onBufferingUpdate");
                        }
                    }
                });
            }
        };
    }

    private void createPlayer(AudioAttach attach, final View v) {
        final ImageView playBtn = (ImageView) v.findViewById(R.id.play_audio_iv);
        final MediaPlayer player = new MediaPlayer();
        players.put(attach.getId(), player);
        // player.setOnErrorListener(new OnErrorListener() {
        // @Override
        // public boolean onError(MediaPlayer mp, int what, int extra) {
        // Toast.makeText(v.getContext(), R.string.error_in_playing_audio, 5000).show();
        // return false;
        // }
        // });
        playBtn.setImageResource(R.drawable.loader_blue);
        v.post(new Runnable() {
            @Override
            public void run() {
                ((AnimationDrawable) playBtn.getDrawable()).start();
            }
        });
        player.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                player.start();
                playBtn.setImageResource(R.drawable.ic_pause_audio);
                playBtn.setOnClickListener(getPlayerBtnListener(player, playBtn));
                v.findViewById(R.id.audio_playback_rl).setOnTouchListener(getPlaybackTouchListener(player));
            }
        });
        player.setOnBufferingUpdateListener(getOnBufferingUpdateListener(v, player));
        // TimerTask updatePlayedStateTask = new TimerTask() {
        // @Override
        // public void run() {
        // v.post(new Runnable() {
        // @Override
        // public void run() {
        // try {
        // setLength(v, R.id.played_iv, getPlayerPosition(player));
        // setController(v, getPlayerPosition(player));
        // } catch (Exception e) {
        // System.out.println("error in updatePlayedStateTask");
        // }
        // }
        // });
        // }
        // };
        // Token.getInstance().getTimer().schedule(updatePlayedStateTask, 1000, 1000);
        // tasks.add(updatePlayedStateTask);
        try {
            player.setDataSource(attach.getUrl());
            player.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(v.getContext(), R.string.error_in_playing_audio, 5000).show();
        }
    }

    private OnClickListener getPlayerBtnListener(final MediaPlayer player, final ImageView playBtn) {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (player.isPlaying()) {
                        player.pause();
                        playBtn.setImageResource(R.drawable.ic_play_audio);
                    } else {
                        player.start();
                        playBtn.setImageResource(R.drawable.ic_pause_audio);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), R.string.error_in_playing_audio, 5000).show();
                }
            }
        };
    }

    private int getPlayerPosition(MediaPlayer player) {
        return (player.getCurrentPosition() * 100) / player.getDuration();
    }

    private void setController(View v, int percent) {
        View control = v.findViewById(R.id.control_iv);
        RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) control.getLayoutParams();
        int fullPlayedWidth = v.findViewById(R.id.audio_playback_rl).getMeasuredWidth();
        if (fullPlayedWidth <= 0) {
            fullPlayedWidth = BitmapUtils.pxFromDp(150, v.getContext());
        }
        params.leftMargin = ((fullPlayedWidth - params.width) * percent) / 100;
        control.setLayoutParams(params);
    }

    private void setLength(View v, int viewId, int percent) {
        System.out.println("set lenght vor view: " + viewId + " " + percent + "%");
        View played = v.findViewById(viewId);
        RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) played.getLayoutParams();
        int fullPlayedWidth = v.findViewById(R.id.audio_playback_rl).getMeasuredWidth();
        if (fullPlayedWidth <= 0) {
            fullPlayedWidth = BitmapUtils.pxFromDp(150, v.getContext());
        }
        fullPlayedWidth = fullPlayedWidth - params.rightMargin - params.leftMargin;
        System.out.print("original: " + params.width);
        params.width = (fullPlayedWidth * percent) / 100;
        System.out.println(", set: " + params.width);
        played.setLayoutParams(params);
    }
}
