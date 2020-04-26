package com.kamrujjamanjoy.hello.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.kamrujjamanjoy.hello.R;
import com.kamrujjamanjoy.hello.adapter.ChatMessageAdapter;
import com.kamrujjamanjoy.hello.common.Common;
import com.kamrujjamanjoy.hello.databinding.ActivityChatMessageBinding;
import com.kamrujjamanjoy.hello.holder.QBChatMessageHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {
    private ActivityChatMessageBinding binding;
    QBChatDialog qbChatDialog;
    ChatMessageAdapter adapter;
    int contextMenuItemClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP)
            getMenuInflater().inflate(R.menu.chat_message_group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_group_edit_name:
                editNameGroup();
                break;
            case R.id.chat_group_add_user:
                addUser();
                break;
            case R.id.chat_group_remove_user:
                removeUser();
                break;
        }
        return true;
    }

    private void removeUser() {


        Intent intent = new Intent(ChatMessageActivity.this, ListUserActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    private void addUser() {
        Intent intent = new Intent(ChatMessageActivity.this, ListUserActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_ADD_MODE);
        startActivity(intent);


    }

    private void editNameGroup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group_layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        final EditText newName = view.findViewById(R.id.edit_new_group_name);

        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                qbChatDialog.setName(newName.getText().toString());
                QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        Toast.makeText(ChatMessageActivity.this, "Group Name Edited", Toast.LENGTH_SHORT).show();
                        binding.messageToolbar.setTitle(qbChatDialog.getName());

                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        contextMenuItemClicked = info.position;
        switch (item.getItemId()) {
            case R.id.chat_message_edit:
                updateMessage();
                break;
            case R.id.chat_message_delete:
                deleteMessage();
                break;
            default:
                break;
        }
        return true;
    }

    private void deleteMessage() {
        final ProgressDialog deleteProgress = new ProgressDialog(ChatMessageActivity.this);
        deleteProgress.setMessage("Please wait..");
        deleteProgress.show();
        editMessage = QBChatMessageHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId()).get(contextMenuItemClicked);
        QBRestChatService.deleteMessage(editMessage.getId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retrieveMessage();
                deleteProgress.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ChatMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void updateMessage() {

        editMessage = QBChatMessageHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuItemClicked);

        binding.edtContent.setText(editMessage.getBody());
        isEditMode = true;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_contex_menu, menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_message);

        init();


        initChatDialogs();
        retrieveMessage();
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditMode) {
                    QBChatMessage message = new QBChatMessage();
                    message.setBody(binding.edtContent.getText().toString());
                    message.setSenderId(QBChatService.getInstance().getUser().getId());
                    message.setSaveToHistory(true);
                    try {
                        qbChatDialog.sendMessage(message);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }


                    //put message to cache
                    if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                        QBChatMessageHolder.getInstance().putMessage(qbChatDialog.getDialogId(), message);
                        ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId());
                        adapter = new ChatMessageAdapter(getBaseContext(), messages);
                        binding.listOfMessage.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    binding.edtContent.setText("");
                    binding.edtContent.setFocusable(true);

                } else {
                    final ProgressDialog updateProgress = new ProgressDialog(ChatMessageActivity.this);
                    updateProgress.setMessage("Please wait..");
                    updateProgress.show();

                    QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                    messageUpdateBuilder.updateText(binding.edtContent.getText().toString()).markDelivered().markRead();
                    QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder).performAsync(new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            retrieveMessage();
                            isEditMode = false;
                            updateProgress.dismiss();

                            binding.edtContent.setText("");
                            binding.edtContent.setFocusable(true);

                        }

                        @Override
                        public void onError(QBResponseException e) {

                            Toast.makeText(ChatMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    private void init() {
        registerForContextMenu(binding.listOfMessage);
    }

    private void retrieveMessage() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500);
        if (qbChatDialog != null) {
            QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    //put message to cache
                    QBChatMessageHolder.getInstance().putMessages(qbChatDialog.getDialogId(), qbChatMessages);
                    adapter = new ChatMessageAdapter(getBaseContext(), qbChatMessages);
                    binding.listOfMessage.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    private void initChatDialogs() {
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        qbChatDialog.initForChat(QBChatService.getInstance());

        //register inComing message
        QBIncomingMessagesManager incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessagesManager.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //add join group to enable group chat

        if (qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP) {

            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);
            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {

                    Log.d("ERROR", " " + e.getMessage());

                }
            });

        }

        qbChatDialog.addMessageListener(this);


        //toolbar title

        binding.messageToolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(binding.messageToolbar);


    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

        //cache message

        QBChatMessageHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessagesByDialogId(qbChatMessage.getDialogId());

        adapter = new ChatMessageAdapter(getBaseContext(), messages);
        binding.listOfMessage.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

        Log.e("ERROR", " " + e.getMessage());
    }
}
