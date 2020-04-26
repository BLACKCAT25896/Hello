package com.kamrujjamanjoy.hello.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kamrujjamanjoy.hello.R;
import com.kamrujjamanjoy.hello.common.Common;
import com.kamrujjamanjoy.hello.databinding.ActivityUserProfileBinding;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;
    private String edtFullName, edtEmail, edtPhone, oldPassword, newPassword;
    private ProgressDialog mDialog;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_update_logout:
                logOut();
                break;
            default:
                break;
        }
        return true;
    }

    private void logOut() {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, "You Are Logout", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserProfileActivity.this, SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // remove all previous activity
                        startActivity(intent);
                        finish();

                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);

        init();
        binding.toolbar.setTitle(QBChatService.getInstance().getUser().getFullName());
        setSupportActionBar(binding.toolbar);

        loadUserProfile();


        binding.profileUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                edtFullName = binding.fullName.getText().toString();
                edtEmail = binding.email.getText().toString();
                edtPhone = binding.phone.getText().toString();
                oldPassword = binding.oldPassword.getText().toString();
                newPassword = binding.newPassword.getText().toString();
                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());
                if (!Common.isNullOrEmptyString(oldPassword))
                    user.setOldPassword(oldPassword);
                if (!Common.isNullOrEmptyString(newPassword))
                    user.setPassword(newPassword);
                if (!Common.isNullOrEmptyString(edtFullName))
                    user.setFullName(edtFullName);
                if (!Common.isNullOrEmptyString(edtEmail))
                    user.setEmail(edtEmail);
                if (!Common.isNullOrEmptyString(edtPhone))
                    user.setPhone(edtPhone);

                mDialog.setMessage("Please wait...");
                mDialog.show();
                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, "User: " + qbUser.getLogin() + "updated", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


            }
        });

    }

    private void loadUserProfile() {
        QBUser currentUser = QBChatService.getInstance().getUser();
        String fullName = currentUser.getFullName();
        String email = currentUser.getEmail();
        String phone = currentUser.getPhone();

        binding.fullName.setText(fullName);
        binding.email.setText(email);
        binding.phone.setText(phone);
    }

    private void init() {
        mDialog = new ProgressDialog(UserProfileActivity.this);
    }
}
