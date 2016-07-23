package application.com.krise.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import application.com.krise.R;
import application.com.krise.utils.CommonLib;
import application.com.krise.utils.UploadManager;
import application.com.krise.utils.UploadManagerCallback;

public class FormActivity extends Activity  implements UploadManagerCallback {

    TextView username;
    EditText dob,mob,num1,num2,num3;
    String name;
    private SharedPreferences prefs;
    String picturePath;

    private static int RESULT_LOAD_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_layout);

        prefs = getSharedPreferences("application_settings",0);
        username = (TextView)findViewById(R.id.loginName);
        name = prefs.getString("user_name","");
        username.setText(name);
        dob = (EditText)findViewById(R.id.dob);
        mob = (EditText)findViewById(R.id.phoneNumber);
        num1 = (EditText)findViewById(R.id.number1);
        num2 = (EditText)findViewById(R.id.number2);
        num3 = (EditText)findViewById(R.id.number3);
        UploadManager.addCallback(this);
        final SharedPreferences.Editor editor = prefs.edit();

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String DOB = dob.getText().toString();
                String MOB = mob.getText().toString();
                String contact1 = num1.getText().toString();
                String contact2 = num2.getText().toString();
                String contact3 = num3.getText().toString();
                editor.putBoolean("filled",true);
                editor.apply();
                UploadManager.register(name,DOB,MOB,contact1,contact2,contact3);

            }
        });

        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            SharedPreferences prefs = getSharedPreferences("application_settings",0);

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("picture", picturePath);
            editor.apply();
            cursor.close();
        }
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.REGISTER){
            if(status){
                if (getIntent().getStringExtra("form").equalsIgnoreCase("yes"))
                {
                    Intent intent = new Intent(FormActivity.this,Home.class);
                    intent.putExtra("flag","home");
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}
