package com.qualisys.parkassist.data.model;

import android.content.ContentValues;

import com.qualisys.parkassist.data.ParkingContract;

/**
 * Created by diego on 12/27/2014.
 */
public class Parking {
    private Long location_id;
    private String id;
    private String formattedAddress;
    private Double lat;
    private Double lon;
    private String icon;
    private String name;
    private String placeId;
    private String reference;
    private String photoURL;
    private String phone;
    private String website;
    private Double rating;

    public Long getLocation_id() {
        return location_id;
    }

    public void setLocation_id(Long location_id) {
        this.location_id = location_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        values.put(ParkingContract.ParkingEntry.COLUMN_LOC_KEY, this.getLocation_id());
        values.put(ParkingContract.ParkingEntry.COLUMN_RATING, this.getRating());
        values.put(ParkingContract.ParkingEntry.COLUMN_FORMATTED_ADDRESS, this.getFormattedAddress());
        values.put(ParkingContract.ParkingEntry.COLUMN_ICON, this.getIcon());
        values.put(ParkingContract.ParkingEntry.COLUMN_LAT, this.getLat());
        values.put(ParkingContract.ParkingEntry.COLUMN_LON, this.getLon());
        values.put(ParkingContract.ParkingEntry.COLUMN_NAME, this.getName());
        values.put(ParkingContract.ParkingEntry.COLUMN_PHONE, this.getPhone());
        values.put(ParkingContract.ParkingEntry.COLUMN_PHOTO_URL, this.getPhotoURL());
        values.put(ParkingContract.ParkingEntry.COLUMN_PLACE_ID, this.getPlaceId());
        values.put(ParkingContract.ParkingEntry.COLUMN_REFERENCE, this.getReference());
        values.put(ParkingContract.ParkingEntry.COLUMN_WEBSITE, this.getWebsite());
        return values;

    }
}
