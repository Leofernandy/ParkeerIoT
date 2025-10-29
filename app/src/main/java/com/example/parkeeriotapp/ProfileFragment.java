package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileFragment extends Fragment {

    private ImageView imvEditProfile;
    private LinearLayout llyMyVehicles, llyLogout, llyCS, llyHowUse, llyLanguage, llySetPin;
    private TextView txvFullname, txvEmail, txvPhone;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // === Inisialisasi View ===
        imvEditProfile = view.findViewById(R.id.imvEditProfile);
        llyMyVehicles = view.findViewById(R.id.llyMyVehicles);
        llyCS = view.findViewById(R.id.llyCS);
        llySetPin = view.findViewById(R.id.llySetPin);
        llyLanguage = view.findViewById(R.id.llyLanguage);
        llyHowUse = view.findViewById(R.id.llyHowUse);
        llyLogout = view.findViewById(R.id.llyLogout);
        txvFullname = view.findViewById(R.id.txvFullname);
        txvEmail = view.findViewById(R.id.txvEmail);
        txvPhone = view.findViewById(R.id.txvPhone);

        // === Firebase ===
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // === Ambil data user ===
        loadUserProfile();

        // === Aksi tombol ===
        imvEditProfile.setOnClickListener(v -> toEditProfile());
        llyMyVehicles.setOnClickListener(v -> toMyVehicles());

        View.OnClickListener toastListener = v ->
                Toast.makeText(getContext(), "Fitur ini sedang dalam pengembangan!", Toast.LENGTH_SHORT).show();

        llySetPin.setOnClickListener(toastListener);
        llyLanguage.setOnClickListener(toastListener);
        llyHowUse.setOnClickListener(toastListener);
        llyCS.setOnClickListener(toastListener);

        llyLogout.setOnClickListener(v -> logout());

        // === Ubah warna status bar ===
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
            );
        }
    }

    private void loadUserProfile() {
        txvFullname.setText("Loading...");
        txvEmail.setText("-");
        txvPhone.setText("-");

        if (auth.getCurrentUser() == null) {
            txvFullname.setText("Guest");
            txvEmail.setText("-");
            txvPhone.setText("-");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        // === Realtime Firestore Listener ===
        userListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) {
                txvFullname.setText("Guest");
                txvEmail.setText("-");
                txvPhone.setText("-");
                return;
            }

            String fullname = snapshot.getString("fullname");
            String email = snapshot.getString("email");
            String phone = snapshot.getString("phone");

            txvFullname.setText(fullname != null ? fullname : "Guest");
            txvEmail.setText(email != null ? email : "-");
            txvPhone.setText(phone != null ? phone : "-");
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finishAffinity();
    }

    private void toEditProfile() {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        }
    }

    private void toMyVehicles() {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), MyVehiclesActivity.class));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // === Hentikan listener Firestore untuk mencegah memory leak ===
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }
}