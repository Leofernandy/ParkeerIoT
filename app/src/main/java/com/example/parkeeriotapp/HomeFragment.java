package com.example.parkeeriotapp;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap gMap;
    private ImageView imvToWallet;
    private TextView tvName, tvSaldo;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔧 Init UI
        imvToWallet = view.findViewById(R.id.imvToWallet);
        tvName = view.findViewById(R.id.textViewNama);
        tvSaldo = view.findViewById(R.id.textViewSaldo);

        // 🔧 Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔗 Tombol ke Wallet
        imvToWallet.setOnClickListener(v -> toWallet());

        // 🎯 Ambil data user dari Firestore
        loadUserProfile();

        // 🗺️ Map fragment
        SupportMapFragment mapFragment = new SupportMapFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        // 🎨 Ganti warna status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(requireContext(), R.color.navy)
            );
        }
    }

    private void loadUserProfile() {
        if (auth.getCurrentUser() == null) {
            tvName.setText("Hi Guest");
            tvSaldo.setText("IDR 0");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        // 🔄 Dengarkan perubahan real-time
        userListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) {
                tvName.setText("Hi Guest");
                tvSaldo.setText("IDR 0");
                return;
            }

            String fullname = snapshot.getString("fullname");
            Long balance = snapshot.getLong("balance");

            tvName.setText(fullname != null ? "Hi " + fullname : "Hi Guest");

            if (balance != null) {
                String formattedSaldo = String.format("IDR %,d", balance).replace(',', '.');
                tvSaldo.setText(formattedSaldo);
            } else {
                tvSaldo.setText("IDR 0");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 🔕 Hentikan listener saat fragment ditutup
        if (userListener != null) {
            userListener.remove();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        LatLng jakarta = new LatLng(-6.200000, 106.816666);
        gMap.addMarker(new MarkerOptions().position(jakarta).title("Jakarta"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jakarta, 12));
    }

    public void toWallet() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new WalletFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}