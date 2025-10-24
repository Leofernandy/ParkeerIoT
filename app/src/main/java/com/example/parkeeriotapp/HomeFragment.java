package com.example.parkeeriotapp;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.parkeeriotapp.utils.UserSessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap gMap;
    private ImageView imvToWallet;

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
        imvToWallet.setOnClickListener(v -> toWallet());

        TextView tvName = view.findViewById(R.id.textViewNama);
        TextView tvSaldo = view.findViewById(R.id.textViewSaldo);

        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String fullname = sessionManager.getFullname();
        tvName.setText(fullname != null ? "Hi " + fullname : "Hi Guest");

        int saldo = sessionManager.getSaldo();
        String formattedSaldo = String.format("IDR %,d", saldo).replace(',', '.');
        tvSaldo.setText(formattedSaldo);

        // Map fragment
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

    @Override
    public void onResume() {
        super.onResume();
        TextView tvSaldo = getView().findViewById(R.id.textViewSaldo);
        int saldo = new UserSessionManager(requireContext()).getSaldo();
        String formattedSaldo = String.format("IDR %,d", saldo).replace(',', '.');
        tvSaldo.setText(formattedSaldo);
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
