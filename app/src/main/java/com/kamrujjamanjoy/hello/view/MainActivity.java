package com.kamrujjamanjoy.hello.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kamrujjamanjoy.hello.R;
import com.kamrujjamanjoy.hello.databinding.ActivityMainBinding;
import com.kamrujjamanjoy.hello.adapter.ChatDialogAdapter;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private String user, password;

    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        createSeasonForChat();
        loadChatDialog();

        binding.chatDialogAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ListUserActivity.class));
            }
        });
    }

    private void loadChatDialog() {
        QBRequestGetBuilder builder = new QBRequestGetBuilder();
        builder.setLimit(100);
        QBRestChatService.getChatDialogs(null,builder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {

                ChatDialogAdapter adapter = new ChatDialogAdapter(getBaseContext(),qbChatDialogs);
                binding.lstChatDialog.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage() );

            }
        });
    }

    private void createSeasonForChat() {
        final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage("Please wait..");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");

        final QBUser qbUser = new QBUser(user,password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }
                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", e.getMessage() );

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });



    }
}
