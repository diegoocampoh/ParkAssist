package com.qualisys.parkassist.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.qualisys.parkassist.data.ParkingContract;
import com.qualisys.parkassist.data.ParkingContract.LocationEntry;
import com.qualisys.parkassist.data.ParkingContract.ParkingEntry;
import com.qualisys.parkassist.data.model.Parking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by diego on 20/12/14.
 */
public class SunshineService extends IntentService {

    static public final String LOCATION_QUERY_EXTRA = "lqe";
    private final String LOG_TAG = SunshineService.class.getSimpleName();
    private final String PLACES_API_KEY = "AIzaSyCXBD3uUobhxkI4ce9ofskgFL-aj4JF_WU";

    public SunshineService() {
        super(SunshineService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "Running "+this.getClass().getSimpleName());
        String locationQuery = intent.getStringExtra(LOCATION_QUERY_EXTRA);

        // If there's no location code, there's nothing to look up.  Verify size of params.
        if (locationQuery == null || locationQuery.isEmpty()) {
            return;
        }
        // Will contain the raw JSON response as a string.
        String jsonString = null;
        //https://maps.googleapis.com/maps/api/place/textsearch/json?query=parking%20in%20Montevideo&key=AIzaSyCXBD3uUobhxkI4ce9ofskgFL-aj4JF_WU

        final String FORECAST_BASE_URL =
                "https://maps.googleapis.com/maps/api/place/textsearch/json";
        final String QUERY_PARAM = "query";
        final String KEY_PARAM = "key";
        final String query ="Parking in "+locationQuery;

        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, query)
                .appendQueryParameter(KEY_PARAM, PLACES_API_KEY)
                .build();
        jsonString = getRESTData(builtUri.toString());
        try {
            getParkingsFromJSON(jsonString, locationQuery);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return;
    }

    private String getRESTData(String uri) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            return buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getParkingsFromJSON(String jsonString, String locationSetting)
            throws JSONException, IOException {
        // keys for getting details
        //https://maps.googleapis.com/maps/api/place/details/json?reference=PLACEREFERENCE&key=AddYourOwnKeyHere
        final String PLACE_DETAIL_URL =
                "https://maps.googleapis.com/maps/api/place/details/json";
        final String REFERENCE = "reference";
        final String KEY_PARAM = "key";

        // Basic elements of a place, for further details, see the next group of tags
        final String PLACES_NEXTPAGE = "next_page_token";
        final String PLACES_RESULTS_ROOT = "results";
        final String PLACES_ADDRESS = "formatted_address";
        final String PLACES_GEOMETRY_ROOT = "geometry";
        final String PLACES_LOCATION_ROOT = "location";
        final String PLACES_GEOMETRY_LAT = "lat";
        final String PLACES_GEOMETRY_LON = "lng";
        final String PLACES_ICON = "icon";
        final String PLACES_ID = "id";
        final String PLACES_NAME = "name";
        final String PLACES_PHOTOS_ROOT = "photos";
        final String PLACES_PHOTOS_HEIGHT = "height";
        final String PLACES_PHOTOS_WIDTH = "width";
        final String PLACES_PHOTO_REFERENCE = "photo_reference";
        final String PLACES_PLACE_ID = "place_id";
        final String PLACES_REFERENCE = "reference";

        JSONObject root_json = new JSONObject(jsonString);
        JSONArray places_array = root_json.getJSONArray(PLACES_RESULTS_ROOT);

        // Insert the location into the database.
        long locationID = addLocation(locationSetting);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(places_array.length());

        for (int i = 0; i < places_array.length(); i++) {
            Parking parking = new Parking();
            // Get the JSON object representing the parking
            JSONObject jsonParking = places_array.getJSONObject(i);
            parking.setFormattedAddress(jsonParking.getString(PLACES_ADDRESS));

            //Location json object
            JSONObject location =  jsonParking.getJSONObject(PLACES_GEOMETRY_ROOT).getJSONObject(PLACES_LOCATION_ROOT);
            parking.setLat(location.getDouble(PLACES_GEOMETRY_LAT));
            parking.setLon(location.getDouble(PLACES_GEOMETRY_LON));
            parking.setIcon(jsonParking.getString(PLACES_ICON));
            parking.setId(jsonParking.getString(PLACES_ID));
            parking.setName(PLACES_NAME);

            //Get the photo reference
            try {
                JSONObject jsonPhoto = jsonParking.getJSONArray(PLACES_PHOTOS_ROOT).getJSONObject(0);
                String photoReference = jsonPhoto.getString(PLACES_PHOTO_REFERENCE);
                String photoWidth = jsonPhoto.getString(PLACES_PHOTOS_WIDTH);
                String photoHeight = jsonPhoto.getString(PLACES_PHOTOS_HEIGHT);
                parking.setPhotoURL(obtainPhotoURL(photoReference, photoWidth, photoHeight));
            }catch (JSONException e){
                parking.setPhotoURL(null);
            }
            parking.setPlaceId(jsonParking.getString(PLACES_PLACE_ID));
            parking.setReference(jsonParking.getString(PLACES_REFERENCE));
            obtainParkingDetails(parking);
            cVVector.add(parking.toContentValues());
        }
        ContentValues[] contentValuesToBulkInsert = new ContentValues[cVVector.size()];
        cVVector.toArray(contentValuesToBulkInsert);
        this.getContentResolver().bulkInsert(ParkingEntry.CONTENT_URI, contentValuesToBulkInsert);
        return new String[]{"2"};
    }

    private Long addLocation(String locationSetting) {
        Log.v(LOG_TAG, "inserting " + locationSetting);
        Long existingId = getLocationIdByLocationSetting(locationSetting);
        if (existingId == null) {
            Log.v(LOG_TAG, "Didn't find it in the database, inserting now!");
            ContentValues locationValues = new ContentValues();
            locationValues.put(ParkingContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);

            Uri locationInsertUri = this.getContentResolver()
                    .insert(ParkingContract.LocationEntry.CONTENT_URI, locationValues);

            return ContentUris.parseId(locationInsertUri);
        } else {
            return existingId;
        }


    }

    private Long getLocationIdByLocationSetting(String locationSetting) {
        // A cursor is your primary interface to the query results.
        Cursor cursor = this.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                new String[]{LocationEntry._ID}, // leaving "columns" null just returns all the columns.
                LocationEntry.COLUMN_LOCATION_SETTING + " = ? ", // cols for "where" clause
                new String[]{locationSetting}, // values for "where" clause
                null  // sort order
        );
        if (cursor.moveToFirst()) {
            Log.v(LOG_TAG, "Found it in the database!");
            int locationIdIndex = cursor.getColumnIndex(LocationEntry._ID);
            return cursor.getLong(locationIdIndex);
        }
        return null;
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, SunshineService.class);
            sendIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, intent.getStringExtra(SunshineService.LOCATION_QUERY_EXTRA));
            context.startService(sendIntent);

        }
    }


    private String obtainPhotoURL(String photoReference, String width, String height) throws IOException {

        //https://maps.googleapis.com/maps/api/place/photo?maxwidth=MAXWIDTH&photoreference=PHOTOREFkey=AddYourOwnKeyHere
        final String PLACE_PHOTO_URL =
                "https://maps.googleapis.com/maps/api/place/photo";
        final String REFERENCE = "photoreference";
        final String KEY_PARAM = "key";
        final String MAX_HEIGHT = "maxheight";
        final String MAX_WIDTH = "maxwidth";

        Uri builtUri = Uri.parse(PLACE_PHOTO_URL).buildUpon()
                .appendQueryParameter(MAX_HEIGHT, height)
                .appendQueryParameter(MAX_WIDTH, width)
                .appendQueryParameter(REFERENCE, photoReference)
                .appendQueryParameter(KEY_PARAM, PLACES_API_KEY)
                .build();
        return builtUri.toString();
    }

    private void obtainParkingDetails(Parking parking) throws JSONException {
        //https://maps.googleapis.com/maps/api/place/details/json?reference=REFERENCE&key=AddYourOwnKeyHere
        // Detailed place info
        final String PLACES_DETAIL_PHONE = "international_phone_number";
        final String PLACES_DETAIL_WEBSITE = "website";
        final String PLACES_DETAIL_RATING = "rating";
        final String PLACE_PHOTO_URL = "https://maps.googleapis.com/maps/api/place/details/json";
        final String REFERENCE = "reference";
        final String KEY_PARAM = "key";
        final String PLACES_DETAIL_ROOT = "result";

        Uri builtUri = Uri.parse(PLACE_PHOTO_URL).buildUpon()
                .appendQueryParameter(REFERENCE, parking.getReference())
                .appendQueryParameter(KEY_PARAM, PLACES_API_KEY)
                .build();
        String jsonDetails= getRESTData(builtUri.toString());
        JSONObject details = new JSONObject(jsonDetails).getJSONObject(PLACES_DETAIL_ROOT);
        try{
            parking.setPhone(details.getString(PLACES_DETAIL_PHONE));
        }catch (JSONException e){

        }

        try{
            parking.setRating(details.getDouble(PLACES_DETAIL_RATING));
        }catch(JSONException e){
            parking.setRating(-1d);
        }
        parking.setWebsite(details.getString(PLACES_DETAIL_WEBSITE));

    }



}
