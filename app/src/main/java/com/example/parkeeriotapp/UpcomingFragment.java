package com.example.parkeeriotapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.BaseAdapter;

public class UpcomingFragment extends Fragment {

    private ListView listView;

    public UpcomingFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);
        listView = view.findViewById(R.id.listUpcoming);

        // Pakai adapter langsung dengan hardcode data
        listView.setAdapter(new BaseAdapter() {
            String[] malls = {"Mall Example 1", "Mall Example 2"};
            String[] slots = {"Slot A1", "Slot B2"};
            String[] plates = {"B1234XYZ", "C5678ABC"};
            String[] times = {"10:00 - 12:00", "13:00 - 15:00"};

            @Override
            public int getCount() { return malls.length; }

            @Override
            public Object getItem(int position) { return null; }

            @Override
            public long getItemId(int position) { return position; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.layout_item_incoming, parent, false);
                }
                TextView tvMallName = convertView.findViewById(R.id.tvMallName);
                TextView tvBookingDate = convertView.findViewById(R.id.tvBookingDate);
                TextView tvPlate = convertView.findViewById(R.id.tvPlate);
                TextView tvStatus = convertView.findViewById(R.id.tvStatus);

                tvMallName.setText(malls[position]);
                tvBookingDate.setText(times[position]);
                tvPlate.setText(plates[position]);
                tvStatus.setText("RESERVED");

                return convertView;
            }
        });

        return view;
    }
}
