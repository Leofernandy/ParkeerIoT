package com.example.parkeeriotapp;

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
        View viewBarcodeButton = convertView.findViewById(R.id.viewBarcodeButton);

        tvEnterAt.setText("ENTER AT " + booking.getJamMasuk());
        tvMallName.setText(booking.getMallName());
        tvBookingDate.setText(booking.getJamMasuk() + " - " + booking.getJamKeluar());
        tvPlate.setText(booking.getPlate());
        tvStatus.setText("RESERVED");

        viewBarcodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailsActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            intent.putExtra("mallName", booking.getMallName());
            intent.putExtra("mallAddress", booking.getMallAddress());
            intent.putExtra("slot", booking.getSlot());
            intent.putExtra("plate", booking.getPlate());
            intent.putExtra("jamMasuk", booking.getJamMasuk());
            intent.putExtra("jamKeluar", booking.getJamKeluar());
            intent.putExtra("totalHarga", booking.getTotalHarga());
            intent.putExtra("durasiMenit", booking.getDurasiMenit());
            intent.putExtra("qrScanned", booking.isQrScanned());

            context.startActivity(intent);
        });

        return convertView;
    }


}

