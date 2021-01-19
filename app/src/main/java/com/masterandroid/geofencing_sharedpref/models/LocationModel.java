package com.masterandroid.geofencing_sharedpref.models;

import com.google.android.gms.maps.model.LatLng;

public class LocationModel {
    private LatLng Latlong;
    private String Address;

    public LocationModel(LatLng latlong, String address) {
        Latlong = latlong;
        Address = address;
    }

    public LatLng getLatlong() {
        return Latlong;
    }

    public void setLatlong(LatLng latlong) {
        Latlong = latlong;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
