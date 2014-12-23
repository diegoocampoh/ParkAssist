package com.qualisys.parkassist.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by diego on 23/12/14.
 */
public class ParkingContract {

    public static final String CONTENT_AUTHORITY = "com.qualisys.parkassist";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    public static final String PATH_PARKING = "parking";
    public static final String PATH_LOCATION = "location";

    /* Inner class that defines the table contents of the weather table */
    public static final class ParkingEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PARKING).build();
        public static final String TABLE_NAME = "parking";
        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";

        public static final String COLUMN_ID= "id";
        public static final String COLUMN_FORMATTED_ADDRESS = "formattedAddress";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LON = "lon";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PLACE_ID = "placeId";
        public static final String COLUMN_REFERENCE = "reference";
        public static final String COLUMN_PHOTO_REFERENCE = "photoReference";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_WEBSITE = "website";


        public static Uri buildParkingUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildParkingLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PARKING;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PARKING;

    }


    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;


    }
}


