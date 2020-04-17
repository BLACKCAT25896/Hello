package com.kamrujjamanjoy.hello.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kamrujjamanjoy.hello.R;
import com.kamrujjamanjoy.hello.databinding.ActivitySignUpBinding;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private String name,userName, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up);

        init();
        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = binding.signUpEmailET.getText().toString().trim();
                password = binding.signUpPasswordET.getText().toString().trim();
                name = binding.signUpNameET.getText().toString().trim();
                QBUser qbUser = new QBUser(userName,password);
                qbUser.setFullName(name);
                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(SignUpActivity.this, "Successfully SignUp Hoo!!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        intent.putExtra("user",userName);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();;

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

        binding.signInTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,SignInActivity.class));
                finish();
            }
        });
    }

    private void init() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());

            }
        });

    }
}
