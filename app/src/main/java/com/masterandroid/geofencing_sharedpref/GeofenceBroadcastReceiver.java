package com.masterandroid.geofencing_sharedpref;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    public static String StartTime = "";
    String EndTime = "";
    int min;
    String address;

    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context,"Geofence triggered...",Toast.LENGTH_SHORT).show();

        geofencingClient = LocationServices.getGeofencingClient(context);
        geofenceHelper = new GeofenceHelper(context);

        NotificationHelper notificationHelper= new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()){
            Log.d("GeofenceBroadcastReceiv",":onReceive error receiving geofencing event..");
        }

        List<Geofence>geofenceList= geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList){
            Log.d("GeofenceBroadcastReceiv",":onReceive:"+ geofence.getRequestId());
        }
        int transitionType= geofencingEvent.getGeofenceTransition();

        switch (transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context,"Entering on selected zone",Toast.LENGTH_SHORT).show();
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:00"));
                Date currentLocalTime = cal.getTime();
                DateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date.setTimeZone(TimeZone.getTimeZone("GMT+5:00"));
                String localTimeNow = date.format(currentLocalTime);
                StartTime = localTimeNow;
                    notificationHelper.sendHighPriorityNotification("Entry","Entering on selected zone",MainActivity.class);
              //  getCurrentLocation(context);


                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context,"In the selected zone",Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Dwell","In the selected zone",MainActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:

                onExit(context);
                notificationHelper.sendHighPriorityNotification("Exit","Exit from the selected zone",MainActivity.class);
                Toast.makeText(context,"Exit from the selected zone",Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void onExit(Context context){
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:00"));
        Date currentLocalTime2 = cal2.getTime();
        DateFormat date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date2.setTimeZone(TimeZone.getTimeZone("GMT+5:00"));
        String localTimeLater = date2.format(currentLocalTime2);
        EndTime = localTimeLater;
        min = getMinutes(context, StartTime, EndTime);
        checkCondition(context, min);
    }

    private void checkCondition(Context context, int myMin)
    {
        if (myMin < 3) {
            Toast.makeText(context, "No nearby...", Toast.LENGTH_SHORT).show();
            getCurrentLocation(context);

        } else {
            Toast.makeText(context, "Nearby Success...", Toast.LENGTH_SHORT).show();
            //   getNearByDetails(context,myLatlng,"@string/google_maps_key");
            getCurrentLocationSharedPref(context);
        }
    }

    private int getMinutes(Context context, String startTime, String endTime)
    {
        int myMin = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+5:00"));
        try {
            Date date1 = simpleDateFormat.parse(startTime);
            Date date2 = simpleDateFormat.parse(endTime);
            long difference = date2.getTime() - date1.getTime();
            int days = (int) (difference / (1000 * 60 * 60 * 24));
            int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
            int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
            hours = (hours < 0 ? -hours : hours);
            myMin = min;
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("Error ", e.getMessage());
        }
        return myMin;
    }

    private void getCurrentLocation(final Context context) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(context)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int locationindex = locationResult.getLocations().size() - 1;
                            double current_lat = locationResult.getLocations().get(locationindex).getLatitude();
                            double current_long = locationResult.getLocations().get(locationindex).getLongitude();
                           LatLng myLatlng = new LatLng(current_lat, current_long);
                   //         MapsActivity.mMap.clear();
                         //   MapsActivity.addMarker(myLatlng);
                         //   MapsActivity.addCircle(myLatlng,200);
                         //   MapsActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatlng,16));
                            addGeofence(context,myLatlng,200);

                            Log.d("Receiver", String.valueOf(current_lat) + "," + String.valueOf(current_long));
                        }
                    }
                }, Looper.getMainLooper());
    }

    private void getCurrentLocationSharedPref(final Context context) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(context)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int locationindex = locationResult.getLocations().size() - 1;
                            double current_lat = locationResult.getLocations().get(locationindex).getLatitude();
                            double current_long = locationResult.getLocations().get(locationindex).getLongitude();
                            LatLng myLatlng = new LatLng(current_lat, current_long);
                            addGeofence(context,myLatlng,200);
                           // MapsActivity.mMap.clear();
                          //  MapsActivity.addMarker(myLatlng);
                          //  MapsActivity.addCircle(myLatlng,200);
                         //   MapsActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatlng,16));
                            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                            if (!sharedPrefs.contains("mylist")) {
                                Log.d("Dont Exists", "creating");
                                ArrayList<LatLng> latlongList = new ArrayList<>();
                                latlongList.add(myLatlng);
                                saveArrayList(context, latlongList, "mylist");
                            } else {
                                Log.d("Exists", "updating");
                                ArrayList<LatLng> latlongList = getArrayList(context, "mylist");
                                latlongList.add(myLatlng);
                                saveArrayList(context, latlongList, "mylist");

                            }

                            Log.d("Receiver", String.valueOf(current_lat) + "," + String.valueOf(current_long));
                        }
                    }
                }, Looper.getMainLooper());
    }
    public String getAddress(Context context, LatLng latLng) {

        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            Toast.makeText(context, "loc getting 2", Toast.LENGTH_SHORT).show();
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String time = formatter.format(date).toString();

            //completeDetails = new place("GuzFS0EjtBSwuRXBuRfhFN8ZSfm1", latitude, longitude, address, "pending", time);
            //Log.d("LOCATION_DETAILS", completeDetails.toString());
            Toast.makeText(context, "Got loc 2", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    public void saveArrayList(Context context, ArrayList<LatLng> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    public ArrayList<LatLng> getArrayList(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<LatLng>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
    private void addGeofence(Context context, LatLng latLng, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER
                | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofenceRequest = geofenceHelper.getGeofenceRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        geofencingClient.addGeofences(geofenceRequest, pendingIntent).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("MainActivity","onSuccess..");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage= geofenceHelper.getErrorCode(e);
                        Log.d("MainActivity","onFailure:" + errorMessage);
                    }
                });
    }
}
