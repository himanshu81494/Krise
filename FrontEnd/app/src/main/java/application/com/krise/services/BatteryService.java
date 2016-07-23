package application.com.krise.services;


import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import application.com.krise.KriseApplication;
import application.com.krise.utils.CommonLib;
import application.com.krise.utils.UploadManager;
import application.com.krise.utils.UploadManagerCallback;

public class BatteryService extends IntentService implements UploadManagerCallback{

    public Context context;
    private SharedPreferences prefs;
    private KriseApplication zapp;

    public BatteryService() {
        super("BatteryService");
        context = this;
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if (level<=5)
            {
                UploadManager.sendEmergencyMessage(zapp.lat, zapp.lon);
            }
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {

        prefs = getSharedPreferences("application_settings",0);
        context.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        zapp = (KriseApplication) getApplication();

        UploadManager.addCallback(this);

    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.REGISTER){
            if(status){
                stopSelf();
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}
