package com.example.parkeeriotapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.parkeeriotapp.model.Mall;

import java.util.ArrayList;
import java.util.List;

public class MallAdapter extends BaseAdapter {

    private final Context context;
    private final List<Mall> mallList;      // data yang tampil di ListView
    private final List<Mall> mallListFull;  // salinan lengkap semua mall
    private final LayoutInflater inflater;

    public MallAdapter(Context context, List<Mall> mallList) {
        this.context = context;
        this.mallList = new ArrayList<>(mallList);
        this.mallListFull = new ArrayList<>(mallList);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mallList.size();
    }

    @Override
    public Object getItem(int position) {
        return mallList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView ivMallImage, btnParkHere;
        TextView tvMallName, tvMallAddress, tvMallDistance, tvMallPrice;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_item_mall, parent, false);
            holder = new ViewHolder();
            holder.ivMallImage = convertView.findViewById(R.id.ivMallImage);
            holder.btnParkHere = convertView.findViewById(R.id.btnParkHere);
            holder.tvMallName = convertView.findViewById(R.id.tvMallName);
            holder.tvMallAddress = convertView.findViewById(R.id.tvMallAddress);
            holder.tvMallDistance = convertView.findViewById(R.id.tvMallDistance);
            holder.tvMallPrice = convertView.findViewById(R.id.tvMallPrice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Mall mall = mallList.get(position);

        // Load image dari drawable sesuai Firestore, fallback default
        String imageKey = (mall.getImageUrl() != null) ? mall.getImageUrl().trim().toLowerCase() : "";
        int imageResId = context.getResources().getIdentifier(imageKey, "drawable", context.getPackageName());
        holder.ivMallImage.setImageResource(imageResId != 0 ? imageResId : R.drawable.delipark);

        // Set teks
        holder.tvMallName.setText(mall.getName() != null ? mall.getName() : "Unknown Mall");
        holder.tvMallAddress.setText(mall.getAddress() != null ? mall.getAddress() : "-");
        holder.tvMallDistance.setText(mall.getDistance() != null ? mall.getDistance() : "-");
        holder.tvMallPrice.setText("IDR " + String.format("%,.0f", mall.getPricePerHour()).replace(',', '.') + "/hr");

        // Tombol "Park Here"
        holder.btnParkHere.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookActivity.class);
            intent.putExtra("mallId", mall.getId());
            intent.putExtra("mallName", mall.getName());
            intent.putExtra("mallAddress", mall.getAddress());
            intent.putExtra("mallPricePerHour", mall.getPricePerHour());
            intent.putExtra("mallImage", mall.getImageUrl());
            context.startActivity(intent);
        });

        return convertView;
    }

    // 🔎 Filter realtime tiap huruf diketik
    public void filter(String query) {
        mallList.clear();

        if (query == null || query.trim().isEmpty()) {
            mallList.addAll(mallListFull); // kosong → tampilkan semua mall
        } else {
            String lowerQuery = query.toLowerCase();
            for (Mall mall : mallListFull) {
                if ((mall.getName() != null && mall.getName().toLowerCase().contains(lowerQuery)) ||
                        (mall.getAddress() != null && mall.getAddress().toLowerCase().contains(lowerQuery))) {
                    mallList.add(mall);
                }
            }
        }
        notifyDataSetChanged();
    }

    // 🔄 Update data lengkap dari Firestore
    public void updateData(List<Mall> newList) {
        mallListFull.clear();
        mallListFull.addAll(newList);

        mallList.clear();
        mallList.addAll(newList);
        notifyDataSetChanged();
    }
}