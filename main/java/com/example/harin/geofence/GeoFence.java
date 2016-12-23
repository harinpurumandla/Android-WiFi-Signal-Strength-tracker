package com.example.harin.geofence;

/**
 * Created by harin on 10/2/2016.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;


public class GeoFence implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    GoogleMap map;
    LocationRequest locrequest;
    GoogleApiClient api;
    Location currentlocation;
    Context mapcontext;
    MapsActivity gf;
    WifiManager wifi;
    Double distance =0d;
    //Creating a constructor.
    GeoFence(GoogleMap gmap, Context context,MapsActivity geo,WifiManager wifiManager) {
        startLocationRequest();
        map = gmap;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return ;
        }
        mapcontext=context;
        gf=geo;
        api = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        wifi=wifiManager;
    }

    @Override
    public void onConnected(Bundle bundle) {
        requestLocationupdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
// When locaiton is changed this method is triggered.
    @Override
    public void onLocationChanged(Location location) {

if(location!=null) {
    List<ScanResult> mScanResults = wifi.getScanResults();

    String deviceid = gf.deviceid;
    currentlocation = location;
    for(int i=0;i<mScanResults.size();i++)
    {
        String SSID = mScanResults.get(i).SSID.replace("\"","");
        int RSSI = mScanResults.get(i).level;
        long timestamp = System.currentTimeMillis() / 1000;
        gf.database.addTableentry(SSID.replace("\"",""), RSSI, timestamp, deviceid, location.getLatitude(), location.getLongitude());
    }
}

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
// This method creates the fused api connection to request location updates.
    protected void requestLocationupdate() {
        if (ActivityCompat.checkSelfPermission(mapcontext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mapcontext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        api.connect();
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                api, locrequest, this);
    }
    // connecting a api
    public void start()
    {
        api.connect();

    }
    // disconnect the api
    public void stop()
    {
        api.disconnect();
        //DecimalFormat numberFormat = new DecimalFormat("#.00");
        //gf.makeToast("You Have walked "+numberFormat.format(distance)+" miles");
    }
    // remove location updates
    public void pause() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                api, this);
    }
    // restart the location updates
    public void resume()
    {
        if(api.isConnected()==true)
            requestLocationupdate();
        else
            api.connect();
    }
    //creates a 10 sec interval location updates with a fasted interval is 5 sec.
    protected void startLocationRequest() {
        locrequest = new LocationRequest();
        locrequest.setInterval(10000);
        locrequest.setFastestInterval(5000);
        locrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

}
