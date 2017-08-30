package com.android.lucia.dribbble.authenticate;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.lucia.dribbble.DribbbleAPI.DribbbleAPI;
import com.android.lucia.dribbble.R;
import com.android.lucia.dribbble.view.MainActivity;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.data;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.activity_login_btn) TextView loginBtn;

    private static final int REQ_CODE_AUTH = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (DribbbleAPI.LoggedIn(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, AuthActivity.class);
                    startActivityForResult(intent, REQ_CODE_AUTH);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_AUTH && resultCode == Activity.RESULT_OK) {
            final String authCode = data.getStringExtra(AuthActivity.KEY_CODE);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        String token = Authenticate.requestAccessToken(authCode);

                        DribbbleAPI.Login(LoginActivity.this, token);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    catch (IOException | JsonSyntaxException exception) {
                        exception.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
