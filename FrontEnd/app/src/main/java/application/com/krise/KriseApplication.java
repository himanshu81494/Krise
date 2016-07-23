package application.com.krise;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import application.com.krise.utils.CommonLib;
import application.com.krise.utils.UploadManager;


public class KriseApplication extends Application {

    public ZLocationListener zll = new ZLocationListener(this);
    public LocationManager locationManager = null;
    public String location = "";
    public String country = "";
    public double lat = 0;
    public double lon = 0;
    public boolean isNetworkProviderEnabled = false;
    public boolean isGpsProviderEnabled = false;
    public boolean firstLaunch = false;
    public int state = CommonLib.LOCATION_DETECTION_RUNNING;

    private CheckLocationTimeoutAsync checkLocationTimeoutThread;

    public void onCreate() {
        super.onCreate();
        UploadManager.setContext(getApplicationContext());
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        try {
            lat = Double.parseDouble(prefs.getString("lat1", "0"));
            lon = Double.parseDouble(prefs.getString("lon1", "0"));
        } catch (ClassCastException e) {
        } catch (Exception e) {
        }
        location = prefs.getString("location", "");

    }



    /**public boolean isMyServiceRunning(Class<?> serviceClass) {
     ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
     for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
     if (serviceClass.getName().equals(service.service.getClassName())) {
     return true;
     }
     }
     return false;
     }

     private void startCacheCleanerService() {

     Intent intent = new Intent(this, CacheCleanerService.class);
     PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

     Calendar calendar = Calendar.getInstance();
     calendar.set(Calendar.HOUR_OF_DAY, 04);
     calendar.set(Calendar.MINUTE, 00);
     calendar.set(Calendar.SECOND, 00);

     AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
     alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pintent);
     }

     private void startHacking() {

     Intent intent = new Intent(this, ZHackService.class);
     PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

     Calendar calendar = Calendar.getInstance();
     calendar.set(Calendar.HOUR_OF_DAY, 04);
     calendar.set(Calendar.MINUTE, 00);
     calendar.set(Calendar.SECOND, 00);

     AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
     alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pintent);
     }**/

    public void setLocationString(String lstr) {
        location = lstr;
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        Editor editor = prefs.edit();
        editor.putString("location", location);
        editor.commit();
    }

    public void setCountryString(String lstr) {
        country = lstr;
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        Editor editor = prefs.edit();
        editor.putString("country", country);
        editor.commit();
    }

    public void setAddressString(String lstr) {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        Editor editor = prefs.edit();
        editor.putString("address", lstr);
        editor.commit();
    }
    public String getAddressString() {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        String address= prefs.getString("address", "");
        return address;
    }

    public String getLocationString() {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        location = prefs.getString("location", "");
        return location;
    }

    public String getCountryString() {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        location = prefs.getString("country", "");
        return country;
    }

    public void interruptLocationTimeout() {
        // checkLocationTimeoutThread.interrupt();
        if (checkLocationTimeoutThread != null)
            checkLocationTimeoutThread.interrupt = false;
    }

    public void startLocationCheck() {

        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (result == ConnectionResult.SUCCESS) {
            zll.getFusedLocation(this);
        } else {
            getAndroidLocation();
        }
    }

    public void getAndroidLocation() {

        //CommonLib.ZLog("zll", "getAndroidLocation");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        if (providers != null) {
            for (String providerName : providers) {
                if (providerName.equals(LocationManager.GPS_PROVIDER))
                    isGpsProviderEnabled = true;
                if (providerName.equals(LocationManager.NETWORK_PROVIDER))
                    isNetworkProviderEnabled = true;
            }
        }

        if (isNetworkProviderEnabled || isGpsProviderEnabled) {

            if (isGpsProviderEnabled){
                try{
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, zll);
                }catch(SecurityException e){

                }
            }

            if (isNetworkProviderEnabled){
                try{
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 500.0f, zll);

                }catch(SecurityException e){

                }
            }

            if (checkLocationTimeoutThread != null) {
                checkLocationTimeoutThread.interrupt = false;
            }

            checkLocationTimeoutThread = new CheckLocationTimeoutAsync();
            checkLocationTimeoutThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            zll.locationNotEnabled();
        }
    }

    private class CheckLocationTimeoutAsync extends AsyncTask<Void, Void, Void> {
        boolean interrupt = true;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            if (interrupt) {
                zll.interruptProcess();
            }
        }
    }

    public boolean isLocationAvailable() {
        return (isNetworkProviderEnabled || isGpsProviderEnabled);
    }
}