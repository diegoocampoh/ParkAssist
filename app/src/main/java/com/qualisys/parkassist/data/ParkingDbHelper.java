package com.qualisys.parkassist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qualisys.parkassist.R;
import com.qualisys.parkassist.data.ParkingContract.LocationEntry;
import com.qualisys.parkassist.data.ParkingContract.ParkingEntry;

/**
 * Created by diego on 20/11/14.
 */
public class ParkingDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static String DATABASE_NAME = "parkings.db";

    public ParkingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        DATABASE_NAME = context.getResources().getString(R.string.parkingsDatabaseName);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude

        // TBD

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + ParkingEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                ParkingEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                // the ID of the location entry associated with this weather data
                ParkingEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                ParkingEntry.COLUMN_FORMATTED_ADDRESS + " TEXT NOT NULL, " +
                ParkingEntry.COLUMN_ICON + " TEXT NOT NULL, " +
                ParkingEntry.COLUMN_LAT + " REAL NOT NULL," +
                ParkingEntry.COLUMN_LON+ " REAL NOT NULL, " +
                ParkingEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ParkingEntry.COLUMN_PHONE+ " TEXT NOT NULL, " +
                ParkingEntry.COLUMN_PHOTO_REFERENCE + " TEXT NOT NULL, " +
                ParkingEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " +
                ParkingEntry.COLUMN_REFERENCE + " TEXT NOT NULL, " +
                ParkingEntry.COLUMN_WEBSITE + " TEXT NOT NULL, " +
                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + ParkingEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "); ";


        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT NOT NULL, " +
                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE);";
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ParkingEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
