package com.jin35.vk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jin35.vk.adapters.Adapter;
import com.jin35.vk.adapters.IListItem;
import com.jin35.vk.model.Chat;
import com.jin35.vk.model.ChatStorage;
import com.jin35.vk.model.IModelListener;
import com.jin35.vk.model.NotificationCenter;
import com.jin35.vk.model.PhotoStorage;
import com.jin35.vk.model.UserInfo;
import com.jin35.vk.model.UserStorageFactory;
import com.jin35.vk.net.IDataRequest;
import com.jin35.vk.net.Token;
import com.jin35.vk.net.impl.BackgroundTasksQueue;
import com.jin35.vk.net.impl.DataRequestTask;
import com.jin35.vk.net.impl.VKRequestFactory;

public class EditChatActivity extends ListActivity {

    private static final int USER_SELECT = 45345;

    private static final String ID_EXTRA = "id";
    private static final String EDIT_EXTRA = "edit";
    public static final String CREATED_CHAT_ID = "chatId";

    private Chat chat;

    public static void startChatEdit(long chatId, Context context) {
        Intent i = new Intent(context, EditChatActivity.class);
        i.putExtra(ID_EXTRA, chatId);
        i.putExtra(EDIT_EXTRA, true);
        context.startActivity(i);
    }

    public static void startNewChatCreation(long firstUid, Activity context, int requestCode) {
        Intent i = new Intent(context, EditChatActivity.class);
        i.putExtra(ID_EXTRA, firstUid);
        i.putExtra(EDIT_EXTRA, false);
        context.startActivityForResult(i, requestCode);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra(EDIT_EXTRA, true)) {
            super.onBackPressed();
        } else {// попытка создания чата
            if (chat.getId() != 0 && chat.getUsers().size() > 2) {// чат создан
                Intent i = new Intent();
                i.putExtra(CREATED_CHAT_ID, chat.getId());
                setResult(RESULT_OK, i);
                finish();
            } else {// чат не создан
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_chat);

        long id = getIntent().getLongExtra(ID_EXTRA, 0);
        if (getIntent().getBooleanExtra(EDIT_EXTRA, true)) {
            chat = ChatStorage.getInstance().getChat(id);
            if (chat == null) {
                showProgress();
                Token.getInstance().getTimer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (chat != null) {
                            this.cancel();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgress();
                                    chat.notifyChanges();
                                }
                            });
                        }
                    }
                }, 100, 100);
            }
        } else {
            chat = new Chat(0L);
            chat.addUser(id);
            chat.addUser(Token.getInstance().getCurrentUid());
        }

        findViewById(R.id.back_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addChatListener();
        updateTopPanel();

        getListView().setAdapter(new UsersAdapter(this));

        final Button editChat = (Button) findViewById(R.id.save_name_btn);
        final EditText chatName = (EditText) findViewById(R.id.chat_name_et);

        editChat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String oldName = chat.getChatName();
                chat.setChatName(chatName.getText().toString());
                if (chat.getId() != 0) {
                    showProgress();
                    BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
                        @Override
                        public void execute() {
                            boolean success;
                            try {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("chat_id", String.valueOf(chat.getId()));
                                params.put("title", chat.getChatName());
                                JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.editChat", params);
                                success = answer.has("response");
                            } catch (Exception e) {
                                success = false;
                            }
                            if (!success) {
                                chat.setChatName(oldName);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(EditChatActivity.this, R.string.error_in_chat_edit, 3000).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatName.setText("");
                                    }
                                });
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgress();
                                    chat.notifyChanges();
                                }
                            });
                        }
                    }));
                }
            }
        });

        findViewById(R.id.add_user_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(EditChatActivity.this, FriendsActivity.class).putExtra(FriendsActivity.NEED_RETURN_UID_EXTRA, true),
                        USER_SELECT);
            }
        });
    }

    protected void addChatListener() {
        NotificationCenter.getInstance().addObjectListener(chat.getId(), new IModelListener() {
            @Override
            public void dataChanged() {
                updateTopPanel();
                ((Adapter<?>) getListView().getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void updateTopPanel() {
        TextView tv = ((TextView) findViewById(R.id.name_tv));
        if (chat == null || TextUtils.isEmpty(chat.getChatName())) {
            tv.setText(R.string.not_dowanloaded_name);
        } else {
            tv.setText(chat.getChatName());
        }

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
        case USER_SELECT:
            long uid = 0;
            if (data != null) {
                uid = data.getLongExtra(FriendsActivity.UID_EXTRA, 0);
            }
            if (uid > 0) {
                showProgress();
                final long newUserId = uid;
                if (chat.getId() > 0) {// add user
                    BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
                        @Override
                        public void execute() {
                            boolean success;
                            try {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("chat_id", String.valueOf(chat.getId()));
                                params.put("uid", String.valueOf(newUserId));
                                JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.addChatUser", params);
                                success = answer.has("response");
                                if (success) {
                                    chat.addUser(newUserId);
                                }
                            } catch (Exception e) {
                                success = false;
                            }
                            if (!success) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(EditChatActivity.this, R.string.error_in_chat_edit, 3000).show();
                                    }
                                });
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgress();
                                }
                            });
                            chat.notifyChanges();
                        }
                    }));
                } else {// create chat
                    BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
                        @Override
                        public void execute() {
                            boolean success;
                            try {
                                Map<String, String> params = new HashMap<String, String>();

                                String uids = "";
                                for (Long id : chat.getUsers()) {
                                    uids += id + ",";
                                }
                                uids += String.valueOf(newUserId);
                                params.put("uids", uids);
                                if (!TextUtils.isEmpty(chat.getChatName())) {
                                    params.put("title", chat.getChatName());
                                }
                                JSONObject answer = VKRequestFactory.getInstance().getRequest().executeRequestToAPIServer("messages.createChat", params);
                                success = answer.has("response");
                                if (success) {
                                    chat.setId(answer.getLong("response"));
                                    chat.addUser(newUserId);
                                    ChatStorage.getInstance().addChat(chat);
                                    addChatListener();
                                }
                            } catch (Exception e) {
                                success = false;
                            }
                            if (!success) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(EditChatActivity.this, R.string.error_in_chat_edit, 3000).show();
                                    }
                                });
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgress();
                                }
                            });
                            chat.notifyChanges();
                        }
                    }));
                }
            }
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }

    private class UsersAdapter extends Adapter<IListItem> {
        public UsersAdapter(ListActivity activity) {
            super(activity);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        protected int getModelListenerMask() {
            return 0;
        }

        @Override
        protected List<IListItem> getList() {
            List<IListItem> result = new ArrayList<IListItem>();
            for (long uid : chat.getUsers()) {
                if (uid != Token.getInstance().getCurrentUid()) {
                    result.add(new ListItem(uid));
                }
            }
            return result;
        }
    }

    private class ListItem implements IListItem {
        private final long uid;

        public ListItem(long uid) {
            this.uid = uid;
        }

        @Override
        public long getId() {
            return uid;
        }

        @Override
        public int getViewId() {
            return R.layout.edit_chat_user_item;
        }

        @Override
        public void updateView(View view) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chat.getUsers().size() <= 3) {
                        Toast.makeText(EditChatActivity.this, R.string.error_in_chat_edit, 3000).show();
                    } else {
                        showProgress();
                        BackgroundTasksQueue.getInstance().execute(new DataRequestTask(new IDataRequest() {
                            @Override
                            public void execute() {
                                boolean success;
                                try {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("chat_id", String.valueOf(chat.getId()));
                                    params.put("uid", String.valueOf(uid));
                                    JSONObject answer = VKRequestFactory.getInstance().getRequest()
                                            .executeRequestToAPIServer("messages.removeChatUser", params);
                                    success = answer.has("response");
                                    if (success) {
                                        chat.removeUser(uid);
                                    }
                                } catch (Exception e) {
                                    success = false;
                                }
                                if (!success) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(EditChatActivity.this, R.string.error_in_chat_edit, 3000).show();
                                        }
                                    });
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgress();
                                    }
                                });
                                chat.notifyChanges();
                            }
                        }));
                    }
                }
            });
            UserInfo user = UserStorageFactory.getInstance().getUserStorage().getUser(uid, true);
            if (user == null) {
                ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getDefaultPhoto());
                ((TextView) view.findViewById(R.id.name_tv)).setText(R.string.not_dowanloaded_name);
                view.findViewById(R.id.online_indicator_iv).setVisibility(View.GONE);
            } else {
                ((ImageView) view.findViewById(R.id.photo_iv)).setImageDrawable(PhotoStorage.getInstance().getPhoto(user));
                ((TextView) view.findViewById(R.id.name_tv)).setText(user.getFullName());
                view.findViewById(R.id.online_indicator_iv).setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public boolean needListener() {
            return true;
        }

        @Override
        public void subsribeListenerForObject(IModelListener listener) {
            NotificationCenter.getInstance().addObjectListener(uid, listener);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

    }
}
