package com.masterandroid.geofencing_sharedpref.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.masterandroid.geofencing_sharedpref.R;
import com.masterandroid.geofencing_sharedpref.models.LocationModel;
import com.masterandroid.geofencing_sharedpref.show_nearby;

import java.util.List;


public class placesAdapter extends RecyclerView.Adapter<placesAdapter.ViewHolder>{

    public interface onItemClickListener{
        void onItemClick(int position);
    }

    private List<LocationModel> placeList;
    private Activity activity;
    private onItemClickListener mListener;

    public placesAdapter(List<LocationModel> placeList, Activity activity) {
        this.placeList = placeList;
        this.activity = activity;
    }

    public void onItemClickListener(onItemClickListener listener){
        mListener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.loc_item,parent,false);
        return new ViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationModel obj=placeList.get(position);
        holder.Address.setText(obj.getAddress());
        holder.latlng.setText("["+obj.getLatlong().latitude+", "+obj.getLatlong().longitude+"]");
        holder.show_nearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent show_nearby=new Intent(activity, show_nearby.class);
                show_nearby.putExtra("lat",obj.getLatlong().latitude);
                show_nearby.putExtra("lon",obj.getLatlong().longitude);
                show_nearby.putExtra("addr",obj.getAddress());

                activity.startActivity(show_nearby);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView Address,latlng;
        Button show_nearbyButton;
        public ViewHolder(@NonNull View itemView,final onItemClickListener listener) {
            super(itemView);
            Address=itemView.findViewById(R.id.Address);
            latlng=itemView.findViewById(R.id.latlng);
            show_nearbyButton=itemView.findViewById(R.id.nearbyBtn);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null)
                    {
                        int position=getAdapterPosition();
                        if(position!= RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
