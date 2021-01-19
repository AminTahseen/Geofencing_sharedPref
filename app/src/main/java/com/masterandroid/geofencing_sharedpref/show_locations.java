package com.masterandroid.geofencing_sharedpref;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.masterandroid.geofencing_sharedpref.adapters.placesAdapter;
import com.masterandroid.geofencing_sharedpref.models.LocationModel;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class show_locations extends AppCompatActivity {

    String address;
    RecyclerView places_list;
    List<LocationModel> locationModelList;
    List<LatLng> latLngArrayList;
    LinearLayout noItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_locations);
        places_list=findViewById(R.id.places_list);
        locationModelList=new ArrayList<>();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        places_list.setLayoutManager(llm);
        noItems=findViewById(R.id.noItems);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPrefs.contains("mylist"))
        {
            noItems.setVisibility(View.VISIBLE);
            places_list.setVisibility(View.GONE);
        }
        else
            {
                noItems.setVisibility(View.GONE);
                places_list.setVisibility(View.VISIBLE);
                    latLngArrayList=getArrayList("mylist");
                    for (LatLng item:latLngArrayList)
                    {
                        String address=getAddress(this,item);
                        LocationModel loc=new LocationModel(item,address);
                        locationModelList.add(loc);
                        placesAdapter adapter=new placesAdapter(locationModelList,this);
                        places_list.setAdapter(adapter);
                        adapter.onItemClickListener(new placesAdapter.onItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Intent show_nearby=new Intent(show_locations.this, show_nearby.class);
                                show_nearby.putExtra("lat",locationModelList.get(position).getLatlong().latitude);
                                show_nearby.putExtra("lon",locationModelList.get(position).getLatlong().longitude);
                                show_nearby.putExtra("addr",locationModelList.get(position).getAddress());
                                startActivity(show_nearby);
                            }
                        });
                    }
            }
    }
    public ArrayList<LatLng> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<LatLng>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public String getAddress(Context context, LatLng latLng) {

        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}