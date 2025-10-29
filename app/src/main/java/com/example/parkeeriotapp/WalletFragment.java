package com.example.parkeeriotapp;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class WalletFragment extends Fragment {

    private TextView txvSaldo, txvPhone;
    private ImageView imvTopup;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public WalletFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🌈 Ubah warna status bar jadi putih
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
            );
        }

        // 🔧 Inisialisasi komponen UI
        txvSaldo = view.findViewById(R.id.txvSaldo);
        txvPhone = view.findViewById(R.id.txvPhone);
        imvTopup = view.findViewById(R.id.imvTopup);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        // 📥 Load data user dari Firestore
        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                String phone = document.getString("phone");
                Long saldo = document.getLong("saldo");

                // Masking nomor HP
                if (phone != null && phone.length() >= 10) {
                    String masked = phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
                    txvPhone.setText(masked);
                } else {
                    txvPhone.setText(phone != null ? phone : "-");
                }

                txvSaldo.setText("IDR " + String.format("%,d", saldo != null ? saldo : 0).replace(',', '.'));
            } else {
                Toast.makeText(requireContext(), "Data user tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
        );

        // 🔹 Perbesar area klik tombol Top-up
        imvTopup.post(() -> expandClickArea(imvTopup, 24)); // tambah 24dp area sentuhan

        // 💰 Tombol Top-up Rp10.000
        imvTopup.setOnClickListener(v -> {
            userRef.get().addOnSuccessListener(document -> {
                if (document.exists()) {
                    Long saldoSekarang = document.getLong("saldo");
                    long saldoBaru = (saldoSekarang != null ? saldoSekarang : 0) + 10000;

                    userRef.update("saldo", saldoBaru)
                            .addOnSuccessListener(aVoid -> {
                                txvSaldo.setText("IDR " + String.format("%,d", saldoBaru).replace(',', '.'));
                                Toast.makeText(requireContext(), "Top-up Rp10.000 berhasil", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(), "Gagal update saldo", Toast.LENGTH_SHORT).show()
                            );
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(requireContext(), "Gagal membaca data user", Toast.LENGTH_SHORT).show()
            );
        });
    }

    /**
     * 🧠 Fungsi untuk memperbesar area klik suatu view tanpa mengubah ukuran visualnya
     */
    private void expandClickArea(View view, int dp) {
        View parent = (View) view.getParent();
        parent.post(() -> {
            final Rect rect = new Rect();
            view.getHitRect(rect);
            int extraArea = (int) (dp * getResources().getDisplayMetrics().density);
            rect.top -= extraArea;
            rect.bottom += extraArea;
            rect.left -= extraArea;
            rect.right += extraArea;
            parent.setTouchDelegate(new TouchDelegate(rect, view));
        });
    }
}