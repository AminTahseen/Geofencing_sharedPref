package com.masterandroid.geofencing_sharedpref.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.masterandroid.geofencing_sharedpref.R;
import com.masterandroid.geofencing_sharedpref.models.locResponse;
import com.masterandroid.geofencing_sharedpref.models.nearbyPlace;
import com.masterandroid.geofencing_sharedpref.retrofit.PHPApiClient;
import com.masterandroid.geofencing_sharedpref.retrofit.PHPApiInterface;
import com.masterandroid.geofencing_sharedpref.show_nearby;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class nearbyAdapter  extends RecyclerView.Adapter<nearbyAdapter.ViewHolder> {

    public interface onItemClickListener{
        void onItemClick(int position);
    }
    private List<nearbyPlace> nearbyList;
    private Activity activity;
    private onItemClickListener mListener;


    public nearbyAdapter(List<nearbyPlace> nearbyList, Activity activity) {
        this.nearbyList = nearbyList;
        this.activity = activity;
    }
    public void setOnItemClickListener(onItemClickListener listener){
        mListener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.nearby_item,parent,false);
        return new ViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        nearbyPlace data=nearbyList.get(position);
        holder.name.setText(data.getPlaceName());
        holder.latLng.setText(data.getTypes().toString());
        holder.yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PHPApiInterface apiInterface = PHPApiClient.getClient().create(PHPApiInterface.class);

                Call<locResponse> locResponseCall= apiInterface.saveLocation(
                        "Amin123",
                        Double.toString(data.getPlaceLatitude()),
                        Double.toString(data.getPlaceLongitude()),
                        data.getMainPlaceAddress(),
                        data.getPlaceName(),
                        data.getTypes().toString(),
                        "Visited",
                        "time"
                );

                locResponseCall.enqueue(new Callback<locResponse>() {
                    @Override
                    public void onResponse(Call<locResponse> call, Response<locResponse> response) {
                        if(response.isSuccessful())
                        {
                            Log.d("Retrofit",response.message());
                            Toast.makeText(activity, "Record successfully saved", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Log.d("Retrofit err",response.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<locResponse> call, Throwable t) {
                        Log.d("Retrofit Failed",t.getMessage());

                    }
                });
                Toast.makeText(activity,data.getPlaceName(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return nearbyList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder  {
        TextView name,latLng;
        Button yesBtn;
        public ViewHolder(@NonNull View itemView,final onItemClickListener listener ) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            latLng=itemView.findViewById(R.id.latlng);
            yesBtn=itemView.findViewById(R.id.yesBtn);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null)
                    {
                        int position=getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

}
