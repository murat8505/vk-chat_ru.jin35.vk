package com.jin35.vk;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
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

public class RegistrationActivity extends Activity {

    private static final int CONFIRM_ACTIVITY = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.registration);

        findViewById(R.id.back_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ImageView phoneStatusImage = (ImageView) findViewById(R.id.phone_status_iv);
        final EditText phoneText = (EditText) findViewById(R.id.phone_et);
        phoneText.addTextChangedListener(new TextWatcher() {

            TimerTask checkPhoneTask;
            String phoneForTask = "";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentPhone = s.toString();
                if (!TextUtils.isEmpty(currentPhone)) {
                    phoneStatusImage.setImageResource(R.drawable.loader_blue);
                    ((AnimationDrawable) phoneStatusImage.getDrawable()).start();
                    if (!phoneForTask.equalsIgnoreCase(currentPhone)) {
                        if (checkPhoneTask != null) {
                            checkPhoneTask.cancel();
                        }
                        phoneForTask = currentPhone;

                        checkPhoneTask = new TimerTask() {
                            @Override
                            public void run() {
                                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
                                    @Override
                                    public void execute() {
                                        try {
                                            Map<String, String> params = new HashMap<String, String>();
                                            params.put("phone", phoneForTask);
                                            final String sendedPhone = phoneForTask;
                                            JSONObject response = VKRequestFactory.getInstance().getRequest()
                                                    .executeRequestToAPIServer("auth.checkPhone", params);
                                            if (sendedPhone.equalsIgnoreCase(phoneText.getText().toString())) {
                                                if (response.has(responseParam)) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            phoneStatusImage.setImageResource(R.drawable.ic_data_ok);
                                                        }
                                                    });
                                                } else {
                                                    setErrorImage();
                                                }
                                            }
                                        } catch (Exception e) {
                                            setErrorImage();
                                        }
                                    }

                                    private void setErrorImage() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                phoneStatusImage.setImageResource(R.drawable.ic_data_error);
                                            }
                                        });
                                    }
                                }));
                            }
                        };
                        Token.getInstance().getTimer().schedule(checkPhoneTask, 500);
                    }
                } else {
                    phoneStatusImage.setImageResource(R.drawable.ic_data_error);
                    if (checkPhoneTask != null) {
                        checkPhoneTask.cancel();
                    }
                }

            }
        });

        addNameWatcher(R.id.name_et, R.id.name_status_iv);
        addNameWatcher(R.id.surname_et, R.id.surname_status_iv);

        final EditText nameEt = (EditText) findViewById(R.id.name_et);
        final EditText surnameEt = (EditText) findViewById(R.id.surname_et);

        findViewById(R.id.register_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
                    @Override
                    public void execute() {
                        try {
                            Map<String, String> params = new HashMap<String, String>();
                            String phone = phoneText.getText().toString();
                            params.put("phone", phone);
                            params.put("client_id", Token.appId());
                            params.put("client_secret", Token.appSecret());
                            params.put("first_name", nameEt.getText().toString());
                            params.put("last_name", surnameEt.getText().toString());

                            JSONObject response = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("auth.signup", params);
                            if (response.has(responseParam)) {
                                startActivityForResult(new Intent(RegistrationActivity.this, ConfirmRegistrationActivity.class).putExtra(
                                        ConfirmRegistrationActivity.PHONE_EXTRA, phone), CONFIRM_ACTIVITY);
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
                                Toast.makeText(RegistrationActivity.this, R.string.error_in_registration, Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case CONFIRM_ACTIVITY:
            finish();
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }

    private void addNameWatcher(int nameEditTextId, int nameStatusImageViewId) {
        final ImageView statusImage = (ImageView) findViewById(nameStatusImageViewId);
        EditText nameText = (EditText) findViewById(nameEditTextId);
        nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkName(s.toString(), statusImage);
            }
        });
    }

    private void checkName(String string, ImageView nameStatusImage) {
        if (TextUtils.isEmpty(string) || string.length() < 3 || TextUtils.isDigitsOnly(string)) {
            nameStatusImage.setImageResource(R.drawable.ic_data_error);
        } else {
            nameStatusImage.setImageResource(R.drawable.ic_data_ok);
        }
    }
}
