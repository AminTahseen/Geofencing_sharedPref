package com.masterandroid.geofencing_sharedpref;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.masterandroid.geofencing_sharedpref.adapters.nearbyAdapter;
import com.masterandroid.geofencing_sharedpref.models.Example;
import com.masterandroid.geofencing_sharedpref.models.nearbyPlace;
import com.masterandroid.geofencing_sharedpref.retrofit.ApiClient;
import com.masterandroid.geofencing_sharedpref.retrofit.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class show_nearby extends AppCompatActivity {
    RecyclerView nearbyRecycler;
    List<nearbyPlace> nearbyPlaceList;
    ApiClient retrofit;
    String addr;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_nearby);
        nearbyPlaceList=new ArrayList<>();
        nearbyRecycler=findViewById(R.id.nearbyRecycler);
        nearbyRecycler.setLayoutManager(new LinearLayoutManager(this));

        Intent intent=getIntent();
        Double lat=intent.getDoubleExtra("lat",0.0);
        Double lon=intent.getDoubleExtra("lon",0.0);
        addr=intent.getStringExtra("addr");

        String mainLatlng=lat.toString()+","+lon.toString();
        getDetailsFromAPI(mainLatlng,"AIzaSyDazjxsJFdohTwZllHdMsacB4P9luVjqyE");

        Toast.makeText(this, Double.toString(lat)+", "+Double.toString(lon), Toast.LENGTH_SHORT).show();
    }

    private void getDetailsFromAPI(String location, String api_key)
    {
        progressDialog = ProgressDialog.show(show_nearby.this, "Loading","Finding Nearby...", true);
        final ApiInterface apiInterface= ApiClient.getClient().create(ApiInterface.class);
        Call<Example> call= apiInterface.getDetails(location,200,api_key);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                if(response.isSuccessful())
                {
                    try
                    {
                        progressDialog.dismiss();
                        for(int i=0;i<response.body().getResults().size();i++)
                        {
                            String placeName=response.body().getResults().get(i).getName();
                            Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                            Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                            List<String> placeType=response.body().getResults().get(i).getTypes();
                            nearbyPlace nearby=new nearbyPlace(placeName,lat,lng,placeType,addr);
                            nearbyPlaceList.add(nearby);
                            nearbyAdapter adapter=new nearbyAdapter(nearbyPlaceList, show_nearby.this);
                            nearbyRecycler.setAdapter(adapter);
                            adapter.setOnItemClickListener(new nearbyAdapter.onItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    Toast.makeText(show_nearby.this, nearbyPlaceList.get(position).getPlaceName(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }catch (Exception er)
                    {

                    }
                }else
                    {

                    }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {

            }
        });
    }
}