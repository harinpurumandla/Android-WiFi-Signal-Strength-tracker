package com.example.harin.geofence;

/**
 * Created by harin on 12/4/2016.
 */


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WifiDatabase extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "WifiDB";

    // Contacts table name
    private static final String TABLE_NAME = "WifiStrength";

    // Contacts Table Columns names
    private static final String KEY_SSID = "ssid";
    private static final String KEY_RSSI = "rssi";
    private static final String KEY_TIME = "timestamp";
    private static final String KEY_DID = "device_id";
    private static final String KEY_LONG = "longitude";
    private static final String KEY_LAT = "latitude";

    public WifiDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("+ KEY_SSID + " varchar(100) not null," + KEY_RSSI + " int(11) not null," + KEY_TIME + " bigint(20) not null," + KEY_DID + " VARCHAR(1000) not null ," + KEY_LAT + " DOUBLE not null , "+KEY_LONG + " DOUBLE not null )";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }
    public void clearTable() {
        // Drop older table if existed
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }
    public void clearTable(String SSID,long time) {
        // Drop older table if existed
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP FROM TABLE  " + TABLE_NAME+" WHERE "+KEY_SSID+" = '"+SSID+"' AND "+KEY_TIME+" = "+time);

        onCreate(db);
    }
    void addTableentry(String ssid,int rssi,long timestamp,String deviceid,double latitude,double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SSID,ssid);
        values.put(KEY_RSSI,rssi);
        values.put(KEY_TIME,timestamp);
        values.put(KEY_DID,deviceid);
        values.put(KEY_LAT,latitude);
        values.put(KEY_LONG,longitude);
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    public Cursor getAllRecords() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public int getRecordsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        // return count
        int count=cursor.getCount();
        cursor.close();
        return count;

    }

}