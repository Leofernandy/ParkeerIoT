package com.example.parkeeriotapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import com.example.parkeeriotapp.EditVehicleActivity;
import com.example.parkeeriotapp.R;
import com.example.parkeeriotapp.model.Vehicle;

import java.util.List;

public class VehicleAdapter extends BaseAdapter {

    private Context context;
    private List<Vehicle> vehicleList;

    public VehicleAdapter(Context context, List<Vehicle> vehicleList) {
        this.context = context;
        this.vehicleList = vehicleList;
    }

    @Override
    public int getCount() {
        return vehicleList.size();
    }

    @Override
    public Object getItem(int position) {
        return vehicleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView textPlate, textCarName, textColor;
        ImageView imageVehicle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_item_vehicles, parent, false);
            holder = new ViewHolder();
            holder.textPlate = convertView.findViewById(R.id.textPlate);
            holder.textCarName = convertView.findViewById(R.id.textCarName);
            holder.textColor = convertView.findViewById(R.id.textColor);
            holder.imageVehicle = convertView.findViewById(R.id.imageVehicle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Vehicle vehicle = vehicleList.get(position);
        holder.textPlate.setText(vehicle.getPlate());
        holder.textCarName.setText(vehicle.getBrand() + " " + vehicle.getModel());
        holder.textColor.setText(vehicle.getColor());

        // Gambar berdasarkan kombinasi model + color
        String model = vehicle.getModel().toLowerCase();
        String color = vehicle.getColor().toLowerCase();
        String drawableName = model + "_" + color;

        int imageResId = context.getResources().getIdentifier(
                drawableName,
                "drawable",
                context.getPackageName()
        );

        if (imageResId != 0) {
            holder.imageVehicle.setImageResource(imageResId);
        } else {
            holder.imageVehicle.setImageResource(R.drawable.default_car);
        }

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditVehicleActivity.class);
            intent.putExtra("plate", vehicle.getPlate());
            context.startActivity(intent);
        });

        return convertView;
    }
}