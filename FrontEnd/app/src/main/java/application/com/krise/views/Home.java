package application.com.krise.views;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import application.com.krise.R;
import application.com.krise.KriseApplication;
import application.com.krise.ZLocationCallback;
import application.com.krise.services.DisasterService;
import application.com.krise.utils.UploadManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.plus.PlusOneButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class Home extends ActionBarActivity implements ZLocationCallback {

    private KriseApplication zapp;
    private SharedPreferences prefs;
    private int width;
    String mode;
    private final int MAKE_CALL_INTENT = 20;


    private boolean destroyed = false;

    // rate us on the play store
    boolean rateDialogShow = false;
    private PlusOneButton mPlusOneButton;

    int START_LOCATION_CODE = 10000, DROP_LOCATION_CODE = 10002;
    private static final int REQUEST_INVITE = 1010;

    TextView startLocation, dropLocation;
    LinearLayout startLocationLayout;
    TextView title, all_text, cab_text, auto_text, bike_text, all_icon, cab_icon, auto_icon, bike_icon;
    View view1;
    double latitudeStart, longitudeStart, latitudeEnd, longitudeEnd;
    boolean onHome = false;
    boolean onBookingStatus = false;

    HomeFragment homeFragment;
    DisasterFragment disasterFragment;
    String showView = "all";

    private Bitmap mapBitmap;

    private android.support.v4.app.FragmentManager.OnBackStackChangedListener getListner() {

        android.support.v4.app.FragmentManager.OnBackStackChangedListener result = new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                if (manager != null) {
                    int backStackEntryCount = manager.getBackStackEntryCount();

                    if (manager.getFragments() != null && backStackEntryCount == 0) {
                        if (homeFragment != null) {
                            homeFragment.onResume();

                            prefs = getSharedPreferences("application_settings", 0);
                            title = (TextView) actionBarCustomView.findViewById(R.id.title);
                            title.setPadding(0, 0, width / 40, 0);

                            String userName = prefs.getString("username", "");
                        }
                    }
                }
            }


        };

        return result;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflater = LayoutInflater.from(this);
        prefs = getSharedPreferences("application_settings", 0);
        zapp = (KriseApplication) getApplication();
        width = getWindowManager().getDefaultDisplay().getWidth();

        zapp.zll.forced = true;
        zapp.zll.addCallback(Home.this);
        zapp.startLocationCheck();

        setContentView(R.layout.home_layout);
        getWindow().setBackgroundDrawable(null);
        mode ="";

        Intent serviceIntent = new Intent(Home.this, DisasterService.class);
        startService(serviceIntent);

        // UI Related stuff
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {
        }



        zapp.zll.addCallback(Home.this);
        LocationCheck(this);

        rateDialogShow = prefs.getBoolean("rate_dialog_show", true);

        view1 = findViewById(R.id.view1);
        startLocation = (TextView) findViewById(R.id.start_location);
        startLocationLayout = (LinearLayout) findViewById(R.id.start_location_container);
        all_text = (TextView) findViewById(R.id.all_text);
        cab_text = (TextView) findViewById(R.id.car_text);
        auto_text = (TextView) findViewById(R.id.auto_text);

        startLocation.setText(zapp.getAddressString());

        latitudeStart = zapp.lat;
        longitudeStart = zapp.lon;


        startLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startLocationIntent = new Intent(Home.this, Login.class);
                startLocationIntent.putExtra("result", true);
                startActivityForResult(startLocationIntent, START_LOCATION_CODE);
            }
        });
        if(getIntent().getStringExtra("flag").equalsIgnoreCase("disaster")){
            disasterFragment = DisasterFragment.newInstance(getIntent().getExtras());
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, disasterFragment, "home")
                    .commit();
            mode = "disaster";

        }

         else{
            homeFragment = HomeFragment.newInstance(getIntent().getExtras());
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, homeFragment, "home")
                    .commit();
        }


        //call for help
        //sos
        //ring and alert



        setupActionBar();
        getSupportFragmentManager().addOnBackStackChangedListener(getListner());

        findViewById(R.id.all_container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                showView = "all";
                changeViews(Home.TYPE_ALL);
                call("+919810831525");

            }
        });
        findViewById(R.id.car_container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showView = "cab";
                changeViews(Home.TYPE_CAR);
                UploadManager.sendEmergencyMessage(zapp.lat, zapp.lon);
            }
        });
        findViewById(R.id.auto_container).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showView = "auto";
                changeViews(Home.TYPE_AUTO);
                Intent intent = new Intent(Home.this, MyBroadcastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), 234324243, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                        + (100), pendingIntent);
            }
        });
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
// Vibrate the mobile phone
            Vibrator vibrator = (Vibrator)

                    context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(2000);
        }
    }


    View actionBarCustomView;

    LayoutInflater inflater;

    private void setupActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBarCustomView = inflater.inflate(R.layout.home_action_bar, null);

        if(mode.equalsIgnoreCase("disaster")){
            findViewById(R.id.bottom_container).setVisibility(View.VISIBLE);
            ((TextView)actionBarCustomView.findViewById(R.id.title)).setText("You are in danger");
        }


        actionBarCustomView.findViewById(R.id.drawer_left_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent userSettingsIntent = new Intent(Home.this, UserSettings.class);
                startActivity(userSettingsIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        actionBar.setCustomView(actionBarCustomView);
    }
    public JSONObject getLocationInfo(Double lat, Double lng) {

        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=true");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public void onCoordinatesIdentified(Location loc) {
        if (loc != null) {
            locationAvailable = true;

            latitudeStart = loc.getLatitude();
            longitudeStart = loc.getLongitude();

            float lat = (float) loc.getLatitude();
            float lon = (float) loc.getLongitude();
            Editor editor = prefs.edit();
            editor.putFloat("lat1", lat);
            editor.putFloat("lon1", lon);
            editor.commit();

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses != null && addresses.size() > 0 && addresses.get(0) != null) {
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    address = address + ", " + city + ", " + state + ", " + country;
                    zapp.setLocationString(city);
                    zapp.setCountryString(country);
                    zapp.setAddressString(address);
                } else {
                    JSONObject ret = getLocationInfo(loc.getLatitude(), loc.getLongitude());
                    JSONObject location;
                    String location_string;
                    try {
                        location = ret.getJSONArray("results").getJSONObject(0);
                        location_string = location.getString("formatted_address");
                        zapp.setAddressString(location_string);
                    } catch (JSONException e1) {
                        e1.printStackTrace();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            startLocation.setText(zapp.getAddressString());

            if (locationDisabled && homeFragment != null) {
                try {
                    homeFragment.refreshMap();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    public void onLocationIdentified() {

    }

    @Override
    public void onLocationNotIdentified() {

    }

    @Override
    public void onDifferentCityIdentified() {

    }

    @Override
    public void locationNotEnabled() {

    }

    @Override
    public void onLocationTimedOut() {

    }

    @Override
    public void onNetworkError() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    zapp.zll.forced = true;
                    zapp.startLocationCheck();
                    locationDisabled = true;
                    break;
                case Activity.RESULT_CANCELED:
                    //LocationCheck(this);
                    locationAvailable = false;
                    Toast.makeText(this, "Please enable location services",
                            Toast.LENGTH_LONG).show();//keep asking if imp or do whatever
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean locationDisabled = false;

    boolean locationAvailable = false;

    public void LocationCheck(final Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        locationAvailable = true;
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            if (!locationAvailable) {
                                status.startResolutionForResult(Home.this, 1000);
                            }
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        finish();
                        break;
                }
            }
        });
    }

    public TextView getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(TextView startLocation) {
        this.startLocation = startLocation;
    }

    public TextView getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(TextView dropLocation) {
        this.dropLocation = dropLocation;
    }

    public double getLatitudeStart() {
        return latitudeStart;
    }

    public void setLatitudeStart(double latitudeStart) {
        this.latitudeStart = latitudeStart;
    }

    public double getLongitudeStart() {
        return longitudeStart;
    }

    public void setLongitudeStart(double longitudeStart) {
        this.longitudeStart = longitudeStart;
    }

    public double getLatitudeEnd() {
        return latitudeEnd;
    }

    public void setLatitudeEnd(double latitudeEnd) {
        this.latitudeEnd = latitudeEnd;
    }

    public double getLongitudeEnd() {
        return longitudeEnd;
    }

    public void setLongitudeEnd(double longitudeEnd) {
        this.longitudeEnd = longitudeEnd;
    }

    public String getStartLocationText() {
        return startLocation.getText().toString();
    }

    public String getEndLocationText() {
        return dropLocation.getText().toString();
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        zapp.zll.removeCallback(this);
        super.onDestroy();
    }


    public static int selectedType = Home.TYPE_CAR;
    public static final int TYPE_ALL = 1;
    public static final int TYPE_CAR = 2;
    public static final int TYPE_AUTO = 3;
    public static final int TYPE_BIKE = 4;

    public void changeViews(int type) {

        if (destroyed)
            return;

        switch (type) {
            case TYPE_ALL:
                findViewById(R.id.all_selector).setVisibility(View.VISIBLE);
               // ((TextView) findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView) findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.active_color));

                findViewById(R.id.car_selector).setVisibility(View.INVISIBLE);
               // ((TextView) findViewById(R.id.car_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.car_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.auto_selector).setVisibility(View.INVISIBLE);
               // ((TextView) findViewById(R.id.auto_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.auto_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                break;
            case TYPE_CAR:
                findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
               // ((TextView) findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.car_selector).setVisibility(View.VISIBLE);
             //   ((TextView) findViewById(R.id.car_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView) findViewById(R.id.car_text)).setTextColor(getResources().getColor(R.color.active_color));

                findViewById(R.id.auto_selector).setVisibility(View.INVISIBLE);
              //  ((TextView) findViewById(R.id.auto_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.auto_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                break;
            case TYPE_AUTO:
                findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
              //  ((TextView) findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.car_selector).setVisibility(View.INVISIBLE);
              //  ((TextView) findViewById(R.id.car_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.car_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.auto_selector).setVisibility(View.VISIBLE);
              //  ((TextView) findViewById(R.id.auto_icon)).setTextColor(getResources().getColor(R.color.active_color));
                ((TextView) findViewById(R.id.auto_text)).setTextColor(getResources().getColor(R.color.active_color));

                break;
            case TYPE_BIKE:
                findViewById(R.id.all_selector).setVisibility(View.INVISIBLE);
              //  ((TextView) findViewById(R.id.all_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.all_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.car_selector).setVisibility(View.INVISIBLE);
              //  ((TextView) findViewById(R.id.car_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.car_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                findViewById(R.id.auto_selector).setVisibility(View.INVISIBLE);
              //  ((TextView) findViewById(R.id.auto_icon)).setTextColor(getResources().getColor(R.color.all_icon_color));
                ((TextView) findViewById(R.id.auto_text)).setTextColor(getResources().getColor(R.color.all_icon_color));

                break;

        }
    }

    public void call(String contact) {

        StringTokenizer st = new StringTokenizer(contact, ":");
        String phoneNumberToDisplay = "";
        if (st.hasMoreTokens()) {
            phoneNumberToDisplay = st.nextToken();
        }

        if (contact.length() > 3) {
            final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberToDisplay));
            AlertDialog.Builder builder_loc = new AlertDialog.Builder(Home.this, AlertDialog.THEME_HOLO_DARK);

            Dialog dialog = null;
            builder_loc.setMessage(phoneNumberToDisplay).setCancelable(true).setPositiveButton(
                    getResources().getString(R.string.dialog_call), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            try {
                                startActivityForResult(intent, MAKE_CALL_INTENT);
                            } catch (ActivityNotFoundException e) {
                            }
                        }
                    }).setNegativeButton(getResources().getString(R.string.dialog_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            dialog = builder_loc.create();
            dialog.show();

        }
    }
    public void setMapBitmap(Bitmap mapBitmap) {
        this.mapBitmap = mapBitmap;
    }

    public Bitmap getMapBitmap() {
        return mapBitmap;
    }
}

