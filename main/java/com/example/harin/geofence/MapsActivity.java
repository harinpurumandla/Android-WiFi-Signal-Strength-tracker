package com.example.harin.geofence;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.database.Cursor;
import android.Manifest;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GeoFence gf=null;
    private Button click=null;
    public WifiDatabase database = new WifiDatabase(this);

    WifiManager wifiManager=null;


public String ssid="";
    public String deviceid="";
    String[] arr ={"wsu-secure","wsu_ez_connect"};
    private AutoCompleteTextView dropdown=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        ToggleGPS();
       deviceid = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        /***********************Task1****************************/
        wifiManager= (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        int RSSI = wifiManager.getConnectionInfo().getRssi();
        String SSID = wifiManager.getConnectionInfo().getSSID();


        /*********************************************************/
        if (wifiManager.isWifiEnabled() == false)
        {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            wifiManager.setWifiEnabled(true);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        click=(Button) findViewById(R.id.button2);
        dropdown=(AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, arr);
        dropdown.setThreshold(2);
        dropdown.setAdapter(adapter);
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if(InternetAvaiable())
                        {
                            makeToast("Found "+wifiManager.getScanResults().size()+" Wi-Fi's Around.");
                            int size = database.getRecordsCount();
                            new UPLOADDATA().execute(new String[]{});
                           makeToast(""+size+" records synced Cloud Succesfully.");
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 60*1000);

        gf=new GeoFence(mMap,this,this,wifiManager); // calling the geoFence with the constructor.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void clicked(View view)
    {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        mMap.clear();
        String SSID = dropdown.getText().toString();
        RETRIVEDATA retrieve = new RETRIVEDATA();
        retrieve.execute(new String[]{SSID});
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true); // location gps marking
        mMap.getUiSettings().setCompassEnabled(true); // enapling compass
        mMap.getUiSettings().setZoomControlsEnabled(true);// enabling the zoom in and out button on map.

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
    // creating the permissions for Marshmellow and above.
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    // call the location permission request
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }

    }
// over written method.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Need Location permission to run this APP", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
    //creating a maketoast method to display toast.
    public void makeToast(String out)
    {
        Toast.makeText(MapsActivity.this,out,
                Toast.LENGTH_LONG).show();
    }
    // when app is paused
    @Override
    protected void onPause() {
        try {

            super.onPause();
            gf.pause();
        }
        catch(Exception e)
        {
            makeToast(e.toString());
        }
    }
    // when app is resumed
    @Override
    public void onResume() {
        try{
            super.onResume();

            gf.resume();
        }
        catch(Exception e)
        {
            makeToast(e.toString());
        }
    }
    //when app is started
    @Override
    public void onStart() {
        try{
            super.onStart();

            gf.start();
        }
        catch(Exception e)
        {
            makeToast(e.toString());
        }
    }
// when app is stopped
    @Override
    public void onStop() {
        try{
            super.onStop();

            gf.stop();
        }
        catch(Exception e)
        {
            makeToast(e.toString());
        }
    }
    // marking on map for a particular map.
    public void addMarker(LatLng loc,int rssi,String SSID)
    {
        if(rssi>=-35)
            mMap.addMarker(new MarkerOptions().position(loc).title(SSID+" (RSSI: "+rssi+")").icon(BitmapDescriptorFactory.fromResource(R.mipmap.strong_signal)));
        else if(rssi>=-60)
            mMap.addMarker(new MarkerOptions().position(loc).title(SSID+"(RSSI: "+rssi+")").icon(BitmapDescriptorFactory.fromResource(R.mipmap.verygood_signal)));
        else if(rssi>=-70)
            mMap.addMarker(new MarkerOptions().position(loc).title(SSID+" (RSSI: "+rssi+")").icon(BitmapDescriptorFactory.fromResource(R.mipmap.good_signal)));
        else if(rssi>=-95)
            mMap.addMarker(new MarkerOptions().position(loc).title(SSID+" (RSSI: "+rssi+")").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bad_signal)));
        else
            mMap.addMarker(new MarkerOptions().position(loc).title(SSID+" (RSSI: "+rssi+")").icon(BitmapDescriptorFactory.fromResource(R.mipmap.verybad_signal)));
    }
    private boolean InternetAvaiable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    private void ToggleGPS(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps"))
        {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
            Toast.makeText(getApplicationContext(), "Starting Location Service.", Toast.LENGTH_SHORT).show();
        }
    }
    public class RETRIVEDATA extends AsyncTask<String, Void, String> {
        String SSID="";
        @Override
        protected String doInBackground(String... params) {

            String data = "";
            try {
                SSID=params[0];
                data =(new HttpRequest()).retriveData(params[0]).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;

        }
        @Override
        protected void onPostExecute(String output) {
            super.onPostExecute(output);
            try {

                JSONObject object = new JSONObject(output);
                JSONArray array = object.getJSONArray("wifi_sense");
                for(int i=0;i<array.length();i++)
                {

                    addMarker(new LatLng(array.getJSONObject(i).getDouble("latitude"),array.getJSONObject(i).getDouble("longitude")),array.getJSONObject(i).getInt("rssi"),SSID);
                }
                LatLng camera = new LatLng(array.getJSONObject(array.length()-1).getDouble("latitude"),array.getJSONObject(array.length()-1).getDouble("longitude"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(camera));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(5));
            } catch (Exception e) {
                makeToast("Data for This SSID is not present/ Server is not responding.");
                e.printStackTrace();
            }
        }
    }
    public class UPLOADDATA extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String data = "";
            try {
                Cursor cur = database.getAllRecords();
                while (cur.moveToNext())
                {
                    data =(new HttpRequest()).uploadData(cur.getString(0),cur.getInt(1),cur.getLong(2),cur.getString(3),cur.getDouble(4),cur.getDouble(5)).toString();

                }
            } catch (Exception e) {
                data=e.toString();
                e.printStackTrace();
            }
            return data;
        }
        @Override
        protected void onPostExecute(String output) {
            super.onPostExecute(output);
            try {
                database.clearTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
