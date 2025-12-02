package com.example.parkeeriotapp;

// Pastikan nama paket (package) di atas sesuai dengan project Anda

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.parkeeriotapp.model.Booking;

import java.util.List;

public class UpcomingAdapter extends BaseAdapter {
    private Context context;
    private List<Booking> bookingList;

    public UpcomingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @Override
    public int getCount() {
        return bookingList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_item_incoming, parent, false);
        }

        Booking booking = bookingList.get(position);

        TextView tvEnterAt = convertView.findViewById(R.id.tvEnterAt);
        TextView tvMallName = convertView.findViewById(R.id.tvMallName);
        TextView tvBookingDate = convertView.findViewById(R.id.tvBookingDate);
        TextView tvPlate = convertView.findViewById(R.id.tvPlate);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        View viewBarcodeButton = convertView.findViewById(R.id.scanQRButton);

        // --- Logika yang Disesuaikan (Tidak Berubah) ---
        tvEnterAt.setText("ENTER AT " + booking.getJamMasuk());
        tvMallName.setText(booking.getMallName());
        tvBookingDate.setText(booking.getJamMasuk() + " - " + booking.getJamKeluar());
        tvPlate.setText(booking.getPlate());

        // --- PENYESUAIAN 1: Status ---
        // Ganti logika hardcoded "RESERVED" dengan status asli dari Firebase
        String status = booking.getStatus();
        if (status.equals("booked")) {
            tvStatus.setText("RESERVED");
            tvStatus.setTextColor(context.getResources().getColor(R.color.navy)); // Asumsi Anda punya color.xml
        } else if (status.equals("checked-in")) {
            tvStatus.setText("ACTIVE");
            tvStatus.setTextColor(context.getResources().getColor(R.color.navy)); // Asumsi Anda punya color.xml
        } else {
            tvStatus.setText(status.toUpperCase());
        }

        // --- PENYESUAIAN 2: Click Listener ---
        viewBarcodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailsActivity.class);

            // Sesuai BookDetailsActivity_Fixed.java, kita HANYA perlu mengirim bookingId.
            // Activity itu akan mengambil datanya sendiri dari RTDB.
            intent.putExtra("bookingId", booking.getBookingId());

            context.startActivity(intent);
        });

        return convertView;
    }
}