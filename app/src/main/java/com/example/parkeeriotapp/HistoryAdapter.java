package com.example.parkeeriotapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.parkeeriotapp.model.Booking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends BaseAdapter {
    private final Context context;
    private final List<Booking> historyList;
    private final LayoutInflater inflater;

    public HistoryAdapter(Context context, List<Booking> historyList) {
        this.context = context;
        this.historyList = historyList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String formatTanggal(String datetime) {
        if (datetime == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(datetime);
            if (date != null) {
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                return output.format(date).toUpperCase();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return datetime;
    }

    private String formatJam(String datetime) {
        if (datetime == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(datetime);
            if (date != null) {
                SimpleDateFormat output = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return output.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return datetime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_item_history, parent, false);
        }

        Booking booking = historyList.get(position);

        TextView tvTanggalHeader = convertView.findViewById(R.id.tvTanggalHeader);
        tvTanggalHeader.setText(formatTanggal(booking.getJamMasuk()));

        TextView tvTotalHarga = convertView.findViewById(R.id.tvTotalHarga);
        tvTotalHarga.setText(String.format("%,.0f", (double) booking.getTotalHarga()).replace(",", "."));

        ((TextView) convertView.findViewById(R.id.tvMallName)).setText(booking.getMallName());
        ((TextView) convertView.findViewById(R.id.tvPlate)).setText(booking.getPlate());

        ((TextView) convertView.findViewById(R.id.tvJamMasuk)).setText(formatJam(booking.getJamMasuk()));
        ((TextView) convertView.findViewById(R.id.tvTglMasuk)).setText(formatTanggal(booking.getJamMasuk()));

        ((TextView) convertView.findViewById(R.id.tvJamKeluar)).setText(formatJam(booking.getJamKeluar()));
        ((TextView) convertView.findViewById(R.id.tvTglKeluar)).setText(formatTanggal(booking.getJamKeluar()));

        TextView tvStatus = convertView.findViewById(R.id.tvStatus);

        String status = booking.getStatus();
        if (status != null) {
            if (status.equalsIgnoreCase("done")) {
                tvStatus.setText("COMPLETED");
            } else if (status.equalsIgnoreCase("cancelled") || status.equalsIgnoreCase("expired")) {
                tvStatus.setText("CANCELLED");
            } else {
                tvStatus.setText(status.toUpperCase());
            }
        } else {
            tvStatus.setText("UNKNOWN");
        }

        return convertView;
    }
}