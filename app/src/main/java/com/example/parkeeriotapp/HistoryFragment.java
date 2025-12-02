package com.example.parkeeriotapp;

// Ganti nama paket (package) di atas sesuai dengan project Anda

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

import com.example.parkeeriotapp.model.Booking; // <-- Import model baru Anda
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private ListView listView;
    private DatabaseReference bookingsRef;
    private FirebaseAuth auth;
    private ValueEventListener bookingsListener;

    private List<Booking> historyList;
    private HistoryAdapter adapter; // Asumsi Anda punya adapter ini

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inisialisasi Firebase
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

        // Setup adapter
        adapter = new HistoryAdapter(requireContext(), historyList); // Pastikan adapter Anda bisa menerima List<Booking>
        listView.setAdapter(adapter);

        // Tambahkan click listener untuk pindah ke BookDetailsActivity
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Booking selected = historyList.get(position);

            Intent intent = new Intent(requireContext(), BookDetailsActivity.class);
            // BookDetailsActivity sudah kita perbaiki, jadi hanya perlu kirim bookingId
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

        // Query untuk mengambil booking milik user ini
        Query historyQuery = bookingsRef.orderByChild("userId").equalTo(uid);

        if (bookingsListener != null) {
            historyQuery.removeEventListener(bookingsListener); // Hapus listener lama
        }

        bookingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear(); // Kosongkan list
                for (DataSnapshot bookingSnap : snapshot.getChildren()) {
                    Booking booking = bookingSnap.getValue(Booking.class);

                    if (booking != null && booking.getStatus() != null) {
                        // Status "History" = done (sesuai kode ESP32)
                        // Anda bisa tambahkan "cancelled" jika ada
                        if (booking.getStatus().equals("done") /* || booking.getStatus().equals("cancelled") */ ) {
                            historyList.add(booking);
                        }
                    }
                }
                adapter.notifyDataSetChanged(); // Update ListView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        historyQuery.addValueEventListener(bookingsListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryBookings(); // Muat data setiap kali fragment terlihat
    }

    @Override
    public void onPause() {
        super.onPause();
        // Hapus listener saat fragment tidak terlihat
        if (bookingsListener != null) {
            bookingsRef.removeEventListener(bookingsListener);
        }
    }
}