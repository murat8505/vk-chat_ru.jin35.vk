package com.jin35.vk;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.VKRequestFactory;

public class LoginActivity extends Activity {

    private static final int MAIN = 34534;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        final View loginBtn = findViewById(R.id.login_btn);
        TextWatcher btnEnabler = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    loginBtn.setEnabled(false);
                } else {
                    loginBtn.setEnabled(true);
                }
            }
        };

        findViewById(R.id.phone_iv).setSelected(true);
        EditText login = (EditText) findViewById(R.id.login_et);
        login.addTextChangedListener(btnEnabler);
        findViewById(R.id.login_et).setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                findViewById(R.id.phone_iv).setSelected(hasFocus);
            }
        });
        EditText pass = (EditText) findViewById(R.id.pass_et);
        pass.addTextChangedListener(btnEnabler);
        pass.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                findViewById(R.id.pass_iv).setSelected(hasFocus);
            }
        });

        loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) LoginActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                final ImageView loginWaiter = (ImageView) findViewById(R.id.login_waiter_iv);
                loginWaiter.setVisibility(View.VISIBLE);
                ((AnimationDrawable) loginWaiter.getDrawable()).start();

                new Thread(new Runnable() {
                    int toastResource = 0;

                    @Override
                    public void run() {
                        String login = ((EditText) findViewById(R.id.login_et)).getText().toString();
                        String password = ((EditText) findViewById(R.id.pass_et)).getText().toString();
                        try {
                            JSONObject answer = VKRequestFactory.getInstance().getRequest().executeLoginRequest(login, password);
                            if (answer.has("access_token")) {
                                Token.getInstance().setNewToken(answer.getString("access_token"));
                                startActivityForResult(new Intent(LoginActivity.this, VkChatActivity.class), MAIN);
                            } else {
                                toastResource = R.string.login_auth_error;
                            }
                        } catch (Throwable e) {
                            toastResource = R.string.login_fatal_error;
                            e.printStackTrace();
                        } finally {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((AnimationDrawable) loginWaiter.getDrawable()).stop();
                                    loginWaiter.setVisibility(View.GONE);
                                    if (toastResource != 0) {
                                        Toast toast = Toast.makeText(LoginActivity.this, toastResource, 5000);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                }
                            });
                        }
                    }
                }, "login").start();

                // показать оч красивый диалог
                // послать запрос
                // распарсить ответ
            }
        });

        findViewById(R.id.signup_rl).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "not implemented yet :(", 1000).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case MAIN:
            if (data == null || !data.hasExtra(PreferencesActivity.LOGOUT)) {
                finish();
            } else {
                // обнулить данные??
            }
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }
}
