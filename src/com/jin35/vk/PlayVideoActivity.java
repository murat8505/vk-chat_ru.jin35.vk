package com.jin35.vk;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.VKRequestFactory;

public class PlayVideoActivity extends Activity {

    private static final String VIDEO_ID_EXTRA = "video id";

    public static void start(Context context, String videoId) {
        Intent i = new Intent(context, PlayVideoActivity.class);
        i.putExtra(VIDEO_ID_EXTRA, videoId);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);

        final ImageView loader = (ImageView) findViewById(R.id.loader_iv);
        // final VideoView vView = (VideoView) findViewById(R.id.video_vv);

        loader.post(new Runnable() {
            @Override
            public void run() {
                ((AnimationDrawable) loader.getDrawable()).start();
            }
        });
        final String id = getIntent().getStringExtra(VIDEO_ID_EXTRA);

        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
            @Override
            public void execute() {
                try {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("videos", id);
                    JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("video.get", params);
                    JSONArray array = response.getJSONArray(IDataRequest.responseParam);
                    // 0 - count
                    JSONObject oneVideo = array.getJSONObject(1);
                    final Uri videoUri = Uri.parse(oneVideo.getString("player"));

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(videoUri);
                    startActivity(i);
                    // runOnUiThread(new Runnable() {
                    // @Override
                    // public void run() {
                    //
                    // vView.setVideoURI(videoUri);
                    // vView.setVisibility(View.VISIBLE);
                    // loader.setVisibility(View.GONE);
                    // vView.start();
                    // }
                    // });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PlayVideoActivity.this, R.string.error_in_playing_video, 5000).show();
                        }
                    });
                    finish();
                }
            }
        }));
    }
}
