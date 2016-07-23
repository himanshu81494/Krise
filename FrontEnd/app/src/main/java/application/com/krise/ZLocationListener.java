package application.com.krise;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import application.com.krise.utils.CommonLib;


public class ZLocationListener implements com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ArrayList<ZLocationCallback> callbacks;
    private KriseApplication zapp;
    public boolean forced = false;

    private GoogleApiClient mGoogleApiClient;

    public ZLocationListener(KriseApplication zapp) {
        callbacks = new ArrayList<ZLocationCallback>();
        this.zapp = zapp;
    }

    public void addCallback(ZLocationCallback callback) {
        callbacks.add(callback);
    }

    public void removeCallback(ZLocationCallback callback) {
        if (callbacks.contains(callback))
            callbacks.remove(callback);
    }

    /**
     * Called when the location has changed.
     * <p/>
     * <p>
     * There are no restrictions on the use of the supplied Location object.
     *
     * @param loc
     *            The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location loc) {
        if (loc != null) {

            boolean callToBeFired = false;
            if (forced || CommonLib.distFrom(zapp.lat, zapp.lon, loc.getLatitude(), loc.getLongitude()) > .2) {
                zapp.lat = loc.getLatitude();
                zapp.lon = loc.getLongitude();
                callToBeFired = true;
            }

            zapp.interruptLocationTimeout();
            zapp.state = CommonLib.LOCATION_DETECTED;

            for (ZLocationCallback callback : callbacks) {
                // zapp.currentCity = zapp.getCityIdFromLocation(loc);
                callback.onCoordinatesIdentified(loc);
            }


        }

        if (zapp.locationManager != null) {
            //zapp.locationManager.removeUpdates(this);
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider
     *            the name of the location provider associated with this update.
     */
    @Override
    public void onProviderEnabled(String provider) {
    }

    /**
     * Called when the provider is disabled by the user. If
     * requestLocationUpdates is called on an already disabled provider, this
     * method is called immediately.
     *
     * @param provider
     *            the name of the location provider associated with this update.
     */
    @Override
    public void onProviderDisabled(String provider) {

    }


    public void getFusedLocation(KriseApplication zapp) {
        PackageManager pm = zapp.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
            this.zapp = zapp;
            mGoogleApiClient = new GoogleApiClient.Builder(zapp)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Location currentLocation = null;
        try {
            PackageManager pm = zapp.getPackageManager();
            if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)
                    || pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
                    || pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)) {
            } else {
                return;
            }
        } catch (Exception e) {
            // Crashlytics.logException(e);
        }

        if (mGoogleApiClient.isConnected()) {
            try{
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }catch(SecurityException e){

            }

        }
        if (currentLocation != null) {
            onLocationChanged(currentLocation);
        } else {
            zapp.getAndroidLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        zapp.getAndroidLocation();
    }

    public void locationNotEnabled() {
        if (forced) {
            zapp.state = CommonLib.LOCATION_NOT_ENABLED;
            zapp.lat = 0;
            zapp.lon = 0;
            zapp.location = "";
            for (ZLocationCallback callback : callbacks)
                callback.locationNotEnabled();
        }
    }

    public void interruptProcess() {

        zapp.state = CommonLib.LOCATION_NOT_DETECTED;
        zapp.lat = 0;
        zapp.lon = 0;
        zapp.location = "";
        try{
            zapp.locationManager.removeUpdates(this);
        }catch(SecurityException e){}

        for (ZLocationCallback callback : callbacks) {
            callback.onLocationTimedOut();
        }
    }
}