package com.example.parkeeriotapp;

// Pastikan nama paket (package) di atas sesuai dengan project Anda

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

    // Fungsi formatTanggal() dan formatJam() Anda sudah benar
    // karena BookActivity menyimpan dalam format "dd-MMM-yyyy HH:mm"
    private String formatTanggal(String datetime) {
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
        return "";
    }

    private String formatJam(String datetime) {
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
        return "";
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.layout_item_history, parent, false);

        Booking booking = historyList.get(position);

        TextView tvTanggalHeader = view.findViewById(R.id.tvTanggalHeader);
        tvTanggalHeader.setText(formatTanggal(booking.getJamMasuk()));

        TextView tvTotalHarga = view.findViewById(R.id.tvTotalHarga);
        tvTotalHarga.setText(String.format("%,.0f", (double) booking.getTotalHarga()).replace(",", "."));

        ((TextView) view.findViewById(R.id.tvMallName)).setText(booking.getMallName());
        ((TextView) view.findViewById(R.id.tvPlate)).setText(booking.getPlate());

        ((TextView) view.findViewById(R.id.tvJamMasuk)).setText(formatJam(booking.getJamMasuk()));
        ((TextView) view.findViewById(R.id.tvTglMasuk)).setText(formatTanggal(booking.getJamMasuk()));

        ((TextView) view.findViewById(R.id.tvJamKeluar)).setText(formatJam(booking.getJamKeluar()));
        ((TextView) view.findViewById(R.id.tvTglKeluar)).setText(formatTanggal(booking.getJamKeluar()));

        TextView tvStatus = view.findViewById(R.id.tvStatus);

        // --- PENYESUAIAN: Ganti isExpired() dengan getStatus() ---
        // Logika lama: tvStatus.setText(booking.isExpired() ? "EXPIRED" : "ACTIVE");

        // Logika baru:
        String status = booking.getStatus();
        if (status.equals("done")) {
            tvStatus.setText("COMPLETED");
        } else if (status.equals("cancelled")) {
            tvStatus.setText("CANCELLED");
        } else {
            tvStatus.setText(status.toUpperCase()); // Fallback
        }

        return view;
    }
}