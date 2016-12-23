package com.example.harin.geofence;

/**
 * Created by harin on 12/4/2016.
 */
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class HttpRequest {
    private static final String GET_DATA = "http://project2website.000webhostapp.com/select_data.php?user=1&ssid=%s";
    private static final String SET_DATA ="http://project2website.000webhostapp.com/insert_data.php?ssid=%s&rssi=%s&lat=%s&long=%s&timestamp=%s&deviceid=%s";
    public static String retriveData(String ssid) {
        try {
            URL url = new URL(String.format(GET_DATA,ssid));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader jsonreader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer jsonobject = new StringBuffer(1024);
            String tmp = "";

            while((tmp = jsonreader.readLine()) != null)
                jsonobject.append(tmp).append("\n");
            jsonreader.close();
            return jsonobject.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
    public static String uploadData(String ssid,int rssi,long timestamp,String deviceid,double latitude,double longitude) {
        try {
            URL url = new URL(String.format(SET_DATA,ssid,""+rssi,""+latitude,""+longitude,""+timestamp,deviceid));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp = "";

            while((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());
            return json.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
}
















