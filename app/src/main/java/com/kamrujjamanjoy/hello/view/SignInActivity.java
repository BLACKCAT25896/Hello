package com.kamrujjamanjoy.hello.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kamrujjamanjoy.hello.R;
import com.kamrujjamanjoy.hello.databinding.ActivitySignInBinding;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    static final String APP_ID = "81666";
    static final String AUTH_KEY = "RbPyezjmwKJCePK";
    static final String AUTH_SECRET = "PzJzAX2HG4VtQVv";
    static final String ACCOUNT_KEY = "ndVxhCznY7L4L2tduQv9";
    private String userName, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_in);

        init();

        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = binding.signInEmailET.getText().toString().trim();
                password = binding.signInPasswordET.getText().toString().trim();
                QBUser qbUser = new QBUser(userName,password);
                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(SignInActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        intent.putExtra("user",userName);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();;
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();



                    }
                });

            }
        });

        binding.signUpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
                finish();
            }
        });

    }

    private void init() {

        QBSettings.getInstance().init(getApplicationContext(),APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
