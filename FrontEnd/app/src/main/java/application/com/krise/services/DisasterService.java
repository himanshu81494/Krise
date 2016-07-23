package application.com.krise.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import application.com.krise.KriseApplication;
import application.com.krise.R;
import application.com.krise.utils.CommonLib;
import application.com.krise.utils.UploadManager;
import application.com.krise.utils.UploadManagerCallback;
import application.com.krise.views.SplashScreen;

public class DisasterService extends IntentService implements UploadManagerCallback {

    public Context context;
    private SharedPreferences prefs;
    private KriseApplication zapp;

    public DisasterService() {
        super("DisasterService");
        context = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        prefs = getSharedPreferences("application_settings",0);
        zapp = (KriseApplication) getApplication();

        UploadManager.addCallback(this);
        UploadManager.disaster();
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.DISASTER){
            if (!status) {

                try {
                    Thread.sleep(10000);
                    UploadManager.disaster();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

                if (status){
                Uri alarmSound = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        context)
                        // Set Icon
                        .setSmallIcon(R.drawable.ic_launcher)
                                // Set Ticker Message
                        .setTicker("ticker")


                        .setSound(alarmSound).setLights(Color.BLUE, 1000, 500)
                        .setVibrate(new long[] { 1000, 1000 })
                                // Set Title
                                //.setContentTitle("new Notification")
                                // Set Text
                                .setContentText("you are in danger")
                        //.setContent("You are in danger")
                                // Add an Action Button below Notification
                                // .addAction(R.drawable.ic_launcher, "Action Button", pIntent)
                                // Set PendingIntent into Notification
                                // .setContentIntent(pIntent)
                                // Dismiss Notification
                        .setAutoCancel(false);

              /**  Intent buttonIntent = new Intent(context, ButtonReceiver.class);
                buttonIntent.putExtra("notificationId",notificationId);
                buttonIntent2.putExtra("notificationId",notificationId);
                buttonIntent2.putExtra("details",detail);
**/
/**
                PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, notificationId, buttonIntent,0);
                PendingIntent btPendingIntent2 = PendingIntent.getBroadcast(context, notificationId, buttonIntent2,0);
                remoteViews.setOnClickPendingIntent(R.id.button1,btPendingIntent );
                remoteViews.setOnClickPendingIntent(R.id.button2,btPendingIntent2 );
**/
                Intent notificationIntent = new Intent(getApplicationContext(), SplashScreen.class);
                PendingIntent intent = PendingIntent.getActivity(context, 0,
                        notificationIntent, 0);

                Notification notification = builder.build();

                notification.setLatestEventInfo(context, "You are in danger", "Stay safe", intent);
                SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("mode","disaster");
                    editor.apply();
                NotificationManager notificationmanager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                // Build Notification with Notification Manager
                notificationmanager.notify(0, builder.build());
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}
