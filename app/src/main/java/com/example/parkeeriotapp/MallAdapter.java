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

    private Context context;
    private List<Mall> mallList;
    private List<Mall> mallListFull;
    private LayoutInflater inflater;

    public MallAdapter(Context context, List<Mall> mallList) {
        this.context = context;
        this.mallList = new ArrayList<>(mallList);
        this.mallListFull = new ArrayList<>(mallList);
        this.inflater = LayoutInflater.from(context);
    }

    public List<Mall> getFilteredMallList() {
        return mallList;
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

        holder.ivMallImage.setImageResource(mall.getImageResId());
        holder.tvMallName.setText(mall.getName());
        holder.tvMallAddress.setText(mall.getAddress());
        holder.tvMallDistance.setText(mall.getDistance());
        holder.tvMallPrice.setText("IDR " + mall.getPricePerHour() + "/HR");

        holder.btnParkHere.setOnClickListener(view -> {
            Intent intent = new Intent(context, BookActivity.class);
            intent.putExtra("mallId", mall.getId());
            context.startActivity(intent);
        });

        return convertView;
    }

    public void filter(String query) {
        mallList.clear();
        if (query.isEmpty()) {
            mallList.addAll(mallListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Mall mall : mallListFull) {
                if (mall.getName().toLowerCase().contains(lowerCaseQuery) ||
                        mall.getAddress().toLowerCase().contains(lowerCaseQuery)) {
                    mallList.add(mall);
                }
            }
        }
        notifyDataSetChanged();
    }
}
