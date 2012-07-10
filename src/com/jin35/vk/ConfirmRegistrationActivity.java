package com.jin35.vk;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.VKRequestFactory;

public class ConfirmRegistrationActivity extends Activity {

    public static final String PHONE_EXTRA = "phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.registration_confirm);

        final String phone = getIntent().getStringExtra(PHONE_EXTRA);

        final EditText codeText = (EditText) findViewById(R.id.code_et);

        final ImageView statusImage = (ImageView) findViewById(R.id.pass_status_iv);
        final EditText passText = (EditText) findViewById(R.id.pass_et);
        passText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (TextUtils.isEmpty(string) || string.length() < 6) {
                    statusImage.setImageResource(R.drawable.ic_data_error);
                } else {
                    statusImage.setImageResource(R.drawable.ic_data_ok);
                }
            }
        });

        findViewById(R.id.register_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
                    @Override
                    public void execute() {
                        try {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("code", codeText.getText().toString());
                            params.put("client_id", Token.appId());
                            params.put("client_secret", Token.appSecret());
                            params.put("password", passText.getText().toString());
                            params.put("phone", phone);

                            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("auth.confirm", params);
                            if (response.has(responseParam)) {
                                finish();
                            } else {
                                showError();
                            }
                        } catch (Exception e) {
                            showError();
                        }
                    }

                    private void showError() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgress();
                                Toast.makeText(ConfirmRegistrationActivity.this, R.string.error_in_registration, 3000).show();
                            }
                        });
                    }
                }));
            }
        });
    }

    private void showProgress() {
        final ImageView iv = (ImageView) findViewById(R.id.loader_iv);
        iv.setVisibility(View.VISIBLE);
        iv.post(new Runnable() {
            @Override
            public void run() {
                ((AnimationDrawable) iv.getDrawable()).start();
            }
        });
    }

    private void hideProgress() {
        findViewById(R.id.loader_iv).setVisibility(View.GONE);
    }
}
