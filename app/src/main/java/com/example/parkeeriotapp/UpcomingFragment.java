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

public class UpcomingFragment extends Fragment {

    private ListView listView;
    private DatabaseReference bookingsRef;
    private FirebaseAuth auth;
    private ValueEventListener bookingsListener;

    private List<Booking> upcomingList;
    private UpcomingAdapter adapter; // Asumsi Anda punya adapter ini

    public UpcomingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        upcomingList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);
        listView = view.findViewById(R.id.listUpcoming);

        // Setup adapter
        adapter = new UpcomingAdapter(requireContext(), upcomingList); // Pastikan adapter Anda bisa menerima List<Booking>
        listView.setAdapter(adapter);

        // Tambahkan click listener untuk pindah ke BookDetailsActivity
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Booking selected = upcomingList.get(position);

            Intent intent = new Intent(requireContext(), BookDetailsActivity.class);
            // BookDetailsActivity sudah kita perbaiki, jadi hanya perlu kirim bookingId
            intent.putExtra("bookingId", selected.getBookingId());
            startActivity(intent);
        });

        return view;
    }

    private void loadUpcomingBookings() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query untuk mengambil booking milik user ini
        Query upcomingQuery = bookingsRef.orderByChild("userId").equalTo(uid);

        if (bookingsListener != null) {
            upcomingQuery.removeEventListener(bookingsListener); // Hapus listener lama jika ada
        }

        bookingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upcomingList.clear(); // Kosongkan list
                for (DataSnapshot bookingSnap : snapshot.getChildren()) {
                    Booking booking = bookingSnap.getValue(Booking.class);

                    if (booking != null && booking.getStatus() != null) {
                        // Status "Upcoming" = booked ATAU checked-in
                        if (booking.getStatus().equals("booked") || booking.getStatus().equals("checked-in")) {
                            upcomingList.add(booking);
                        }
                    }
                }
                adapter.notifyDataSetChanged(); // Update ListView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        upcomingQuery.addValueEventListener(bookingsListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUpcomingBookings(); // Muat data setiap kali fragment terlihat
    }

    @Override
    public void onPause() {
        super.onPause();
        // Hapus listener saat fragment tidak terlihat untuk hemat baterai
        if (bookingsListener != null) {
            bookingsRef.removeEventListener(bookingsListener);
        }
    }
}