package com.qualisys.parkassist.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.qualisys.parkassist.data.ParkingContract.LocationEntry;
import com.qualisys.parkassist.data.ParkingContract.ParkingEntry;


/**
 * Created by diego on 11/24/2014.
 */
public class ParkingProvider extends ContentProvider {

    private static final int PARKING = 100;
    private static final int PARKING_WITH_LOCATION= 101;
    private static final int LOCATION = 200;
    private static final int LOCATION_ID = 201;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sParkingByLocationSettingQueryBuilder;

    static {
        sParkingByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sParkingByLocationSettingQueryBuilder.setTables(ParkingEntry.TABLE_NAME +
                        " INNER JOIN " + LocationEntry.TABLE_NAME + " ON " +
                        getTableColumnString(ParkingEntry.TABLE_NAME, ParkingEntry.COLUMN_LOC_KEY)
                        + " = " +
                        getTableColumnString(LocationEntry.TABLE_NAME, LocationEntry._ID)
        );
    }
    private static final String sLocationSettingSelection =
            getTableColumnStringForQuery(LocationEntry.TABLE_NAME, LocationEntry.COLUMN_LOCATION_SETTING, " = ");

    private static ParkingDbHelper mOpenHelper;

    private static String getTableColumnString(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    private static String getTableColumnStringForQuery(String tableName, String columnName, String comparator) {
        return tableName + "." + columnName + " " + comparator + " " + " ? ";
    }
    private static UriMatcher buildUriMatcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(ParkingContract.CONTENT_AUTHORITY, ParkingContract.PATH_PARKING, PARKING );
        matcher.addURI(ParkingContract.CONTENT_AUTHORITY, ParkingContract.PATH_PARKING+"/*", PARKING_WITH_LOCATION);
        matcher.addURI(ParkingContract.CONTENT_AUTHORITY, ParkingContract.PATH_LOCATION, LOCATION );
        matcher.addURI(ParkingContract.CONTENT_AUTHORITY, ParkingContract.PATH_LOCATION+"/#", LOCATION_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ParkingDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "parking/*"
            case PARKING_WITH_LOCATION: {
                retCursor = getParkingByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "parking"
            case PARKING: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ParkingEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location/*"
            case LOCATION_ID: {
                Long id = ContentUris.parseId(uri);
                selectionArgs = new String[1];
                selectionArgs[0] = id.toString();
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        LocationEntry._ID +" = ? ",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PARKING_WITH_LOCATION:
                return ParkingEntry.CONTENT_TYPE;
            case PARKING:
                return ParkingEntry.CONTENT_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI "+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PARKING: {
                long _id = db.insert(ParkingEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ParkingEntry.buildParkingUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int nRowsAffected = 0;

        switch (match) {
            case PARKING: {
                nRowsAffected = db.delete(ParkingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                nRowsAffected = db.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (nRowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return nRowsAffected;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int nRowsAffected = 0;

        switch (match) {
            case PARKING: {
                nRowsAffected = db.update(ParkingEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            }
            case LOCATION: {
                nRowsAffected = db.update(LocationEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (nRowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return nRowsAffected;
    }


    private Cursor getParkingByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = ParkingEntry.getLocationSettingFromUri(uri);

        return sParkingByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingSelection,
                new String[]{locationSetting},
                null,
                null,
                sortOrder
        );
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PARKING:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ParkingEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

}
