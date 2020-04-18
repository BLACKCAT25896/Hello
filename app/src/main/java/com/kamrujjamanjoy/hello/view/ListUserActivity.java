package com.kamrujjamanjoy.hello.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.kamrujjamanjoy.hello.R;
import com.kamrujjamanjoy.hello.adapter.ListUserAdapter;
import com.kamrujjamanjoy.hello.common.Common;
import com.kamrujjamanjoy.hello.databinding.ActivityListUserBinding;
import com.kamrujjamanjoy.hello.holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class ListUserActivity extends AppCompatActivity {
    private ActivityListUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list_user);
        init();

        retrieveAllUsers();
        binding.lstUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        binding.createChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int countChoice = binding.lstUsers.getCount();
                if (binding.lstUsers.getCheckedItemPositions().size() == 1)
                    createPrivateChat(binding.lstUsers.getCheckedItemPositions());
                else if (binding.lstUsers.getCheckedItemPositions().size()>1) {
                    createGroupChat(binding.lstUsers.getCheckedItemPositions());
                } else
                    Toast.makeText(ListUserActivity.this, "Please Select Friend to Chat", Toast.LENGTH_SHORT).show();


            }
        });
    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog progressDialog = new ProgressDialog(ListUserActivity.this);
        progressDialog.setMessage("please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        int countChoice = binding.lstUsers.getCount();
        ArrayList<Integer> occupantIdList =  new ArrayList<>();
        for (int i = 0; i<countChoice; i++){
            if (checkedItemPositions.get(i)){
                QBUser user = (QBUser) binding.lstUsers.getItemAtPosition(i);
                occupantIdList.add(user.getId());

            }
        }
        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(Common.createChatDialogName(occupantIdList));
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdList);
        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                progressDialog.dismiss();
                Toast.makeText(ListUserActivity.this, "Create Private Chat Dialog Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage() );

            }
        });


    }

    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {
        final ProgressDialog progressDialog = new ProgressDialog(ListUserActivity.this);
        progressDialog.setMessage("please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        int countChoice = binding.lstUsers.getCount();
        ArrayList<Integer> occupantIdList =  new ArrayList<>();
        for (int i = 0; i<countChoice; i++){
            if (checkedItemPositions.get(i)){
                QBUser user = (QBUser) binding.lstUsers.getItemAtPosition(i);
                QBChatDialog dialog = DialogUtils.buildPrivateDialog(user.getId());
                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        progressDialog.dismiss();
                        Toast.makeText(ListUserActivity.this, "Create Chat Dialog Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", e.getMessage() );

                    }
                });

            }
        }


    }

    private void retrieveAllUsers() {

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);

                ArrayList<QBUser> qbUserWithoutCurrent = new ArrayList<QBUser>();
                for (QBUser user : qbUsers) {
                    if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin())) {
                        qbUserWithoutCurrent.add(user);
                    }
                }
                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), qbUserWithoutCurrent);
                binding.lstUsers.setAdapter(adapter);
                adapter.notifyDataSetChanged();


            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());

            }
        });

    }

    private void init() {

    }
}
