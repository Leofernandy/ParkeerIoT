package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.parkeeriotapp.model.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private ListView listView;
    private DatabaseReference bookingsRef;
    private FirebaseAuth auth;
    private ValueEventListener bookingsListener;

    private List<Booking> historyList;
    private HistoryAdapter adapter;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        historyList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        listView = view.findViewById(R.id.listHistory);

        adapter = new HistoryAdapter(requireContext(), historyList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Booking selected = historyList.get(position);
            Intent intent = new Intent(requireContext(), BookDetailsActivity.class);
            intent.putExtra("bookingId", selected.getBookingId());
            startActivity(intent);
        });

        return view;
    }

    private void loadHistoryBookings() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Query historyQuery = bookingsRef.orderByChild("userId").equalTo(uid);

        if (bookingsListener != null) {
            historyQuery.removeEventListener(bookingsListener);
        }

        bookingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot bookingSnap : snapshot.getChildren()) {
                    Booking booking = bookingSnap.getValue(Booking.class);

                    if (booking != null && booking.getStatus() != null) {
                        String status = booking.getStatus().toLowerCase();
                        // Masukkan semua riwayat yang sudah selesai/batal ke dalam list
                        if (status.equals("done") || status.equals("cancelled") || status.equals("expired")) {
                            historyList.add(booking);
                        }
                    }
                }

                // ---> INI KUNCI SORTINGNYA (Terbaru ke Terlama) <---
                Collections.sort(historyList, new Comparator<Booking>() {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
                    @Override
                    public int compare(Booking b1, Booking b2) {
                        try {
                            if (b1.getJamMasuk() == null || b2.getJamMasuk() == null) return 0;
                            Date d1 = sdf.parse(b1.getJamMasuk());
                            Date d2 = sdf.parse(b2.getJamMasuk());
                            if (d1 != null && d2 != null) {
                                return d2.compareTo(d1); // Descending
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        historyQuery.addValueEventListener(bookingsListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryBookings();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bookingsListener != null) {
            bookingsRef.removeEventListener(bookingsListener);
        }
    }
}