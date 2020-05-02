package com.kamrujjamanjoy.hello.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
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
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {
    private ActivityChatMessageBinding binding;
    QBChatDialog qbChatDialog;
    ChatMessageAdapter adapter;
    int contextMenuItemClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;
    static final int SELECT_PICTURE = 7171;


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
                if (!binding.edtContent.getText().toString().isEmpty()) {
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
            }
        });
    }

    private void init() {
        registerForContextMenu(binding.listOfMessage);
        binding.dialogAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectImage = new Intent();
                selectImage.setType("image/*");
                selectImage.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(selectImage,"Select Image"),SELECT_PICTURE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectImageUri = data.getData();
                final ProgressDialog mDialog = new ProgressDialog(ChatMessageActivity.this);
                mDialog.setMessage("please wait...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();


                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    File file = new File(Environment.getExternalStorageDirectory() + "/image.png");
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(outputStream.toByteArray());
                    fileOutputStream.flush();
                    fileOutputStream.close();


                    int imageSizeKb = (int) (file.length() / 1024);
                    if (imageSizeKb >= 1024 * 100) {
                        Toast.makeText(this, "Error size", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // upload file


                    QBContent.uploadFileTask(file,true,null).performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                            qbChatDialog.setPhoto(qbFile.getId().toString());

                            //update
                            QBRequestUpdateBuilder updateBuilder = new QBRequestUpdateBuilder();
                            QBRestChatService.updateGroupChatDialog(qbChatDialog,updateBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                    mDialog.dismiss();
                                    binding.dialogAvatar.setImageBitmap(bitmap);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Toast.makeText(ChatMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });


                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        if (qbChatDialog.getPhoto()!=null){
            QBContent.getFile(Integer.parseInt(qbChatDialog.getPhoto())).performAsync(new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle bundle) {
                    String fileUrl = qbFile.getPublicUrl();
                    Picasso.get()
                            .load(fileUrl)
                            .resize(50,50)
                            .centerCrop()
                            .into(binding.dialogAvatar);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("ERROR", e.getMessage() );

                }
            });
        }
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


        final QBChatDialogParticipantListener participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String s, QBPresence qbPresence) {
                if (s == qbChatDialog.getDialogId()) {
                    QBRestChatService.getChatDialogById(s).performAsync(new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                            try {
                                Collection<Integer> onlineList = qbChatDialog.getOnlineUsers();
                                TextDrawable.IBuilder builder = TextDrawable.builder()
                                        .beginConfig()
                                        .withBorder(4)
                                        .endConfig()
                                        .round();
                                TextDrawable online = builder.build("", Color.RED);
                                binding.imageOnlineCount.setImageDrawable(online);
                                binding.txtOnlineCount.setText(String.format("%d/%d online", onlineList.size(), qbChatDialog.getOccupants().size()));
                            } catch (XMPPException e) {
                                e.printStackTrace();
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });


                }
            }
        };
        qbChatDialog.addParticipantListener(participantListener);

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
