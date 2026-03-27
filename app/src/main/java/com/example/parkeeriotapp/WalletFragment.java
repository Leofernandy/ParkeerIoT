package com.example.parkeeriotapp;

import android.content.Intent; // Tambahkan ini
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView; // Tambahkan ini
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.parkeeriotapp.model.WalletHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot; // Tambahkan ini
import com.google.firebase.database.DatabaseError; // Tambahkan ini
import com.google.firebase.database.FirebaseDatabase; // Tambahkan ini
import com.google.firebase.database.ValueEventListener; // Tambahkan ini
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList; // Tambahkan ini
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class WalletFragment extends Fragment {

    private TextView txvSaldo, txvPhone;
    private ImageView imvTopup;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // --- TAMBAHAN BARU: Variabel untuk History ---
    private ListView listWalletHistory;
    private WalletHistoryAdapter adapter;
    private ArrayList<WalletHistory> historyList;
    // --- END TAMBAHAN BARU ---

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

        // --- TAMBAHAN BARU: Inisialisasi ListView History ---
        listWalletHistory = view.findViewById(R.id.listWalletHistory);
        historyList = new ArrayList<>();
        adapter = new WalletHistoryAdapter(requireContext(), historyList);
        listWalletHistory.setAdapter(adapter);
        // --- END TAMBAHAN BARU ---

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        // 📥 Ganti .get() menjadi .addSnapshotListener agar Real-time!
        userRef.addSnapshotListener((document, e) -> {
            if (e != null) {
                Toast.makeText(requireContext(), "Gagal memonitor saldo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (document != null && document.exists()) {
                String phone = document.getString("phone");
                Long saldo = document.getLong("saldo");

                // Masking nomor HP
                if (phone != null && phone.length() >= 10) {
                    String masked = phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
                    txvPhone.setText(masked);
                } else {
                    txvPhone.setText(phone != null ? phone : "-");
                }

                // UPDATE SALDO INSTAN! Begitu Firebase berubah, TV ini langsung berubah
                txvSaldo.setText("IDR " + String.format("%,d", saldo != null ? saldo : 0).replace(',', '.'));
            }
        });

        // --- TAMBAHAN BARU: Load History dari Realtime Database ---
        loadTransactionHistory(uid);
        // --- END TAMBAHAN BARU ---

        // 🔹 Perbesar area klik tombol Top-up
        imvTopup.post(() -> expandClickArea(imvTopup, 24)); // tambah 24dp area sentuhan

        // 💰 Tombol Top-up (Sekarang buka TopUpActivity agar bayar lewat Xendit)
        imvTopup.setOnClickListener(v -> {
            // Ubah logika tombol ini agar membuka halaman Top Up Xendit yang baru
            Intent intent = new Intent(requireContext(), TopUpActivity.class);
            startActivity(intent);
        });
    }

    // --- TAMBAHAN BARU: Fungsi untuk menarik data History ---
    private void loadTransactionHistory(String uid) {
        FirebaseDatabase.getInstance().getReference("topups")
                .orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String keyId = ds.getKey();
                            String channel = ds.child("payment_channel").getValue(String.class);
                            Long amount = ds.child("amount").getValue(Long.class);
                            String dbTitle = ds.child("title").getValue(String.class);
                            Long timestamp = ds.child("timestamp").getValue(Long.class);

                            long rawTimestamp = (timestamp != null) ? timestamp : 0; // Ambil angka mentahnya

                            String dateStr = "-";
                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                                dateStr = sdf.format(new Date(timestamp));
                            }

                            String displayTitle = "";
                            String displayAmount = "";
                            int typeInt = 1;

                            if (keyId != null && keyId.startsWith("RF")) {
                                displayTitle = (dbTitle != null) ? dbTitle : "Refund Booking";
                                displayAmount = "+ IDR " + String.format("%,d", amount != null ? amount : 0).replace(',', '.');
                                typeInt = 3;
                            } else if (keyId != null && keyId.startsWith("PY")) {
                                displayTitle = (dbTitle != null) ? dbTitle : "Payment Booking";
                                displayAmount = "- IDR " + String.format("%,d", amount != null ? amount : 0).replace(',', '.');
                                typeInt = 2;
                            } else {
                                displayTitle = "Top Up via " + (channel != null ? channel : "Online");
                                displayAmount = "+ IDR " + String.format("%,d", amount != null ? amount : 0).replace(',', '.');
                                typeInt = 1;
                            }

                            // Tambahkan pakai .add biasa (tidak perlu add(0) lagi) beserta rawTimestamp-nya
                            historyList.add(new WalletHistory(
                                    displayTitle,
                                    displayAmount,
                                    dateStr,
                                    typeInt,
                                    rawTimestamp // <-- Lempar rawTimestamp kesini
                            ));
                        }

                        // === LOGIKA PENGURUTAN (SORTING) TERBARU KE TERLAMA ===
                        Collections.sort(historyList, new Comparator<WalletHistory>() {
                            @Override
                            public int compare(WalletHistory h1, WalletHistory h2) {
                                // h2 dibanding h1 supaya yang paling besar (terbaru) ada di atas
                                return Long.compare(h2.getTimestamp(), h1.getTimestamp());
                            }
                        });

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    // --- END TAMBAHAN BARU ---

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