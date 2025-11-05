package com.example.parkeeriotapp;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.parkeeriotapp.model.Mall;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap gMap;
    private ImageView imvToWallet;
    private TextView tvName, tvSaldo;
    private EditText searchBar;
    private ListView listViewMall;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;
    private MallAdapter mallAdapter;
    private List<Mall> mallList = new ArrayList<>();

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imvToWallet = view.findViewById(R.id.imvToWallet);
        tvName = view.findViewById(R.id.textViewNama);
        tvSaldo = view.findViewById(R.id.textViewSaldo);
        searchBar = view.findViewById(R.id.search_bar);
        listViewMall = view.findViewById(R.id.listViewMall);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mallAdapter = new MallAdapter(requireContext(), mallList);
        listViewMall.setAdapter(mallAdapter);

        loadUserProfile();
        loadMallData();

        imvToWallet.setOnClickListener(v -> toWallet());

        // 🔹 Filter tiap huruf diketik
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mallAdapter.filter(s.toString().trim());
                updateMapMarkers(); // update marker tiap search
            }
        });

        SupportMapFragment mapFragment = new SupportMapFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

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

        userListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) {
                tvName.setText("Hi Guest");
                tvSaldo.setText("IDR 0");
                return;
            }

            String fullname = snapshot.getString("fullname");
            Long saldo = snapshot.getLong("saldo");

            tvName.setText(fullname != null ? "Hi " + fullname : "Hi Guest");
            tvSaldo.setText("IDR " + String.format("%,d", saldo != null ? saldo : 0).replace(',', '.'));
        });
    }

    private void loadMallData() {
        db.collection("malls")
                .get()
                .addOnSuccessListener(query -> {
                    List<Mall> tempList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Mall mall = new Mall();
                        mall.setId(doc.getString("id"));
                        mall.setName(doc.getString("name"));
                        mall.setAddress(doc.getString("address"));
                        mall.setDistance(doc.getString("distance"));
                        mall.setImageUrl(doc.getString("imageUrl"));
                        mall.setPricePerHour(doc.getDouble("pricePerHour") != null ? doc.getDouble("pricePerHour") : 0);
                        mall.setLatitude(doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0.0);
                        mall.setLongitude(doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0.0);
                        tempList.add(mall);
                    }
                    mallAdapter.updateData(tempList); // update adapter dengan data penuh
                    updateMapMarkers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Gagal memuat data mall", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateMapMarkers() {
        if (gMap == null) return;
        gMap.clear();

        for (int i = 0; i < mallAdapter.getCount(); i++) {
            Mall mall = (Mall) mallAdapter.getItem(i);
            if (mall.getLatitude() != 0 && mall.getLongitude() != 0) {
                LatLng loc = new LatLng(mall.getLatitude(), mall.getLongitude());
                gMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .title(mall.getName())
                        .snippet(mall.getAddress()));
            }
        }

        if (mallAdapter.getCount() > 0) {
            Mall firstMall = (Mall) mallAdapter.getItem(0);
            LatLng firstLoc = new LatLng(firstMall.getLatitude(), firstMall.getLongitude());
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, 14));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setMapToolbarEnabled(false);
        updateMapMarkers();
    }

    private void toWallet() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new WalletFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) userListener.remove();
    }
}