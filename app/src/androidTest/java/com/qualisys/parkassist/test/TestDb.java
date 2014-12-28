package com.qualisys.parkassist.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.qualisys.parkassist.data.ParkingContract.LocationEntry;
import com.qualisys.parkassist.data.ParkingContract.ParkingEntry;
import com.qualisys.parkassist.data.ParkingDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by diego on 21/11/14.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    static ContentValues getLocationValues() {
        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "Montevideo";
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        return values;
    }

    static ContentValues getParkingValues(Long locationRowId) {
        // Fantastic.  Now that we have a location, add some weather!
        ContentValues parkingValues = new ContentValues();
        parkingValues.put(ParkingEntry.COLUMN_LOC_KEY, locationRowId);
        parkingValues.put(ParkingEntry.COLUMN_FORMATTED_ADDRESS,"Francisco Aguilar 863, Montevideo 11300, Uruguay");
        parkingValues.put(ParkingEntry.COLUMN_LAT, "-34.914819");
        parkingValues.put(ParkingEntry.COLUMN_LON, "-56.155044");
        parkingValues.put(ParkingEntry.COLUMN_ICON,"http://maps.gstatic.com/mapfiles/place_api/icons/generic_business-71.png");
        parkingValues.put(ParkingEntry.COLUMN_NAME,"Parking Aguilar");
        parkingValues.put(ParkingEntry.COLUMN_PLACE_ID,"ChIJ0an_w3SBn5URQGt5pc5VroM");
        parkingValues.put(ParkingEntry.COLUMN_REFERENCE,"CoQBcgAAAPNeQt4ckOQV0xNiDCnEiCOsAZjVIYmTOKGxe5i6xyG3Skod0OV_0i4bMBLbcUZkYkVADGswRlTUuAGH0jha0W5oE8OfVYf4wizPnF8axhvW_-HLLmZ76IaHyp5TZ0l4irr015Cp5i-TjJA5VidRt3jG06Cvi7uRzLZp4LCrLpYiEhDI-OBeg9FkMZCX_BMFu_ZkGhS5z6nrn5KIIVAml-gXOKZ0EGEjoQ");
        parkingValues.put(ParkingEntry.COLUMN_PHOTO_URL, "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=CpQBiQAAAFCrjpvxmFXVPWccQcouudiiWrFL60dVaFaCkIe3M5WRXz4Wlk1IBSTxt-hQWolukx3530IAZPQXJVpNHZth39EwAIIz41fwvM1r4VSEjWS5SgO-7qvqNP4_voVIBOCJjIawjJmHkZwEfdEOi5e-AGXoE8wL0ERphD4Vq85nPxjelW67U03eNloXHcpHyiwQfxIQrZI9buvKhaD-DDySSoVxnhoU50pDWGwkFOeVJRlsPHkRELZxBtY&key=AIzaSyCXBD3uUobhxkI4ce9ofskgFL-aj4JF_WU");
        parkingValues.put(ParkingEntry.COLUMN_PHONE, "+598 2710 4925")  ;
        parkingValues.put(ParkingEntry.COLUMN_WEBSITE,"http://www.parkingaguilar.com/");
        parkingValues.put(ParkingEntry.COLUMN_RATING,1.4);
        return parkingValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(ParkingDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new ParkingDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ParkingDbHelper dbHelper = new ParkingDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Long rowId = testInsertRead(LocationEntry.TABLE_NAME, getLocationValues(), db);
        testInsertRead(ParkingEntry.TABLE_NAME, getParkingValues(rowId), db);

        dbHelper.close();
    }

    public Long testInsertRead(String tableName, ContentValues values, SQLiteDatabase db) {
        long locationRowId;
        locationRowId = db.insert(tableName, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        String[] key_string = new String[values.size()];
        Set<Map.Entry<String, Object>> keys = values.valueSet();

        int i = 0;
        for (Map.Entry<String, Object> key : keys) {
            key_string[i++] = key.getKey();
        }

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                tableName,  // Table to Query
                key_string,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index.

            for (String column : key_string) {
                String dbValue = cursor.getString(cursor.getColumnIndex(column));
                assertEquals(dbValue, values.get(column).toString());
            }

        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }

        cursor.close();
        return locationRowId;
    }
}