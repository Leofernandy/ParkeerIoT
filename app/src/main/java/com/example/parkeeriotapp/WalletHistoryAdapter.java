package com.example.parkeeriotapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.parkeeriotapp.model.WalletHistory;

import java.util.ArrayList;

public class WalletHistoryAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<WalletHistory> list;
    private LayoutInflater inflater;

    public WalletHistoryAdapter(Context context, ArrayList<WalletHistory> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return list.size(); }

    @Override
    public Object getItem(int i) { return list.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_wallet_history, viewGroup, false);
        }

        WalletHistory history = list.get(i);

        TextView txtTitle = view.findViewById(R.id.txtTitle);
        TextView txtDate = view.findViewById(R.id.txtDate);
        TextView txtAmount = view.findViewById(R.id.txtAmount);
        ImageView imgIcon = view.findViewById(R.id.imgIcon);

        txtTitle.setText(history.getTitle());
        txtDate.setText(history.getDate());
        txtAmount.setText(history.getAmount());

        // --- LOGIKA 3 WARNA & ICON ---
        if (history.getType() == 1) {
            // 1 = TOP UP (Hijau)
            txtAmount.setTextColor(Color.parseColor("#2E7D32"));
            imgIcon.setImageResource(R.drawable.buttonlink);
            imgIcon.setColorFilter(Color.parseColor("#2E7D32"));

        } else if (history.getType() == 2) {
            // 2 = PAYMENT BOOKING (Merah)
            txtAmount.setTextColor(Color.parseColor("#D32F2F"));
            imgIcon.setImageResource(R.drawable.buttonlink); // Nanti ganti icon panah atas kalo ada
            imgIcon.setColorFilter(Color.parseColor("#D32F2F"));

        } else if (history.getType() == 3) {
            // 3 = REFUND (Kuning/Orange Gelap biar tetap terbaca di background putih)
            txtAmount.setTextColor(Color.parseColor("#F57F17"));
            imgIcon.setImageResource(R.drawable.buttonlink);
            imgIcon.setColorFilter(Color.parseColor("#F57F17"));
        }

        return view;
    }
}