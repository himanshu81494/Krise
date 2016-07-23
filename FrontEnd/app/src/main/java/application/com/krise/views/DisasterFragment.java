package application.com.krise.views;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import application.com.krise.KriseApplication;
import application.com.krise.R;
import application.com.krise.services.DisasterService;

public class DisasterFragment extends Fragment implements OnMapReadyCallback {

    private Activity activity;
    private View getView;
    private SharedPreferences prefs;
    private int width, height;
    private LayoutInflater vi;
    private boolean destroyed = false;
    private KriseApplication zapp;

    // map object
    private GoogleMap googleMap;

    Bundle bundle;

    public static DisasterFragment newInstance(Bundle bundle) {
        DisasterFragment fragment = new DisasterFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_disaster, null);
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                view = inflater.inflate(R.layout.fragment_disaster, null);
            } catch (Exception e1) {
                e1.printStackTrace();
                if(view==null)
                    view = inflater.inflate(R.layout.fragment_disaster, null);
            }
            return view;
        }

        return view;
    }

    private boolean isLocationChanged = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (KriseApplication) activity.getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        vi = LayoutInflater.from(activity.getApplicationContext());

        getActivity().stopService(new Intent(getActivity(), DisasterService.class));

        String instructions = "";

        // load data from activity
        try {// Loading map

            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        getView.findViewById(R.id.map).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mMapIsTouched = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        mMapIsTouched = false;
                        break;
                }
                return false;
            }
        });

    }


    /**
     * function to load map. If map is not created it will create it for you
     */

    private void initilizeMap() {
        if (googleMap == null) {
            if (!destroyed) {
                try {
                    ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(DisasterFragment.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private AsyncTask mAsyncRunning;

    public void refreshView(double latitude, double longitude) {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetLocation(latitude, longitude).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private class GetLocation extends AsyncTask<Object, Void, Object> {

        private double latitude;
        private double longitude;

        public GetLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(activity, Locale.getDefault());
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = null;
                if(addresses != null && addresses.get(0) != null) {
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    address = address + ", " + city + ", " + state + ", " + country;

                    zapp.setLocationString(city);
                    zapp.setCountryString(country);
                    zapp.setAddressString(address);
                }

                return address;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;
            /** if (activity != null && ((Home) activity).getStartLocation() != null) {
             if (result == null) {
             ((Home) activity).startLocation.setText("Start Location ");
             ((Home) activity).startLocation.setTextColor(Color.parseColor("#7F7F7F"));
             }
             else
             ((Home) activity).getStartLocation().setText(String.valueOf(result));**/
        }
        /**if(getView != null) {
         if (result == null)
         result = "No Internet";
         }**/
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }


    @Override
    public void onMapReady(final GoogleMap mMap) {
        this.googleMap = mMap;

        if (googleMap != null) {

            // Changing map type
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Showing / hiding your current location
            try{
                googleMap.setMyLocationEnabled(true);
            }catch(SecurityException e){

            }

            // Enable / Disable zooming controls
            googleMap.getUiSettings().setZoomControlsEnabled(false);

            // Enable / Disable my location button
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Enable / Disable Compass icon
            googleMap.getUiSettings().setCompassEnabled(false);

            // Enable / Disable Rotate gesture
            googleMap.getUiSettings().setRotateGesturesEnabled(false);

//            googleMap.setTrafficEnabled(true);

            // Enable / Disable zooming functionality
            googleMap.getUiSettings().setZoomGesturesEnabled(true);

            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (mMapIsTouched) {
                        return;
                    }

                    LatLng bounds = cameraPosition.target;

                    final double latitude = bounds.latitude;
                    final double longitude = bounds.longitude;
                    refreshView(latitude, longitude);
                }
            });

            refreshMap();
        }
    }

    protected void refreshMap() {

        if(activity != null && googleMap != null) {
            LatLng location = new LatLng(((Home) activity).getLatitudeStart(),
                    ((Home) activity).getLongitudeStart());
            googleMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(location, (float) 15.0));
        }
    }

    private boolean mMapIsTouched;
}
