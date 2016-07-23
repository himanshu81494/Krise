package application.com.krise.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import application.com.krise.R;

public class UserSettings extends Activity {

    SharedPreferences prefs;
    ImageView userImageView;
    String picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersettings);

        prefs = getSharedPreferences("application_settings",0);

        userImageView = (ImageView) findViewById(R.id.drawer_user_image);
        picture = prefs.getString("picture","");
        userImageView.setImageBitmap(BitmapFactory.decodeFile(picture));

        findViewById(R.id.cross).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userSettingsIntent = new Intent(UserSettings.this, Settings.class);
                startActivity(userSettingsIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        findViewById(R.id.about_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userSettingsIntent = new Intent(UserSettings.this, AboutUs.class);
                startActivity(userSettingsIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }
}
