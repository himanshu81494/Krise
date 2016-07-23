package application.com.krise.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import application.com.krise.R;
import application.com.krise.utils.CommonLib;
import application.com.krise.utils.UploadManager;
import application.com.krise.utils.UploadManagerCallback;

public class Login extends Activity implements UploadManagerCallback {

    TextView button;
    EditText username,email,password;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("application_settings", 0);

        String check = getIntent().getStringExtra("log");
        UploadManager.addCallback(this);

        if (check.equalsIgnoreCase("login"))
        {
            setContentView(R.layout.login_layout);
            ((TextView) findViewById(R.id.submit_button)).setText(getResources().getString(R.string.login));

            email = (EditText) findViewById(R.id.login_email);
            password = (EditText) findViewById(R.id.login_password);
            findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text1 = email.getText().toString();
                String text2 = password.getText().toString();
                UploadManager.login("",text1,text2);
            }
            });

        }
        else if (check.equalsIgnoreCase("signup"))
        {
            setContentView(R.layout.signup_layout);
            ((TextView) findViewById(R.id.submit_button)).setText(getResources().getString(R.string.signup));

            username = (EditText)findViewById(R.id.login_username);
            email = (EditText) findViewById(R.id.login_email);
            password = (EditText) findViewById(R.id.login_password);

            ((TextView) findViewById(R.id.login_page_already_have_an_account)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setContentView(R.layout.login_layout);
                }
            });
            findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text1 = email.getText().toString();
                    String text2 = password.getText().toString();
                    String text3 = username.getText().toString();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("user_name", username.getText().toString());
                    editor.apply();
                    UploadManager.signup(text3, text1, text2);
                    SplashScreen.splash.finish();
                }
            });
        }
        else {
            setContentView(R.layout.login_layout);
            ((TextView) findViewById(R.id.submit_button)).setText(getResources().getString(R.string.login));

            email = (EditText) findViewById(R.id.login_email);
            password = (EditText) findViewById(R.id.login_password);
            findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text1 = email.getText().toString();
                    String text2 = password.getText().toString();
                    UploadManager.login("", text1, text2);
                    SplashScreen.splash.finish();
                }
            });
        }
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.LOGIN){
            if(status){
                String token = (String) data;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("access_token",token);
                editor.apply();
                Intent intent = new Intent(Login.this,FormActivity.class);
                intent.putExtra("form","yes");
                startActivity(intent);
                finish();
            }
        }else if(requestType == CommonLib.SIGNUP){
            if(status){
                Toast.makeText(getBaseContext(),"Success",Toast.LENGTH_LONG).show();
                setContentView(R.layout.login_layout);
                ((TextView) findViewById(R.id.submit_button)).setText(getResources().getString(R.string.login));

                email = (EditText) findViewById(R.id.login_email);
                password = (EditText) findViewById(R.id.login_password);
                findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text1 = email.getText().toString();
                        String text2 = password.getText().toString();
                        UploadManager.login("", text1, text2);
                    }
                });
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}
