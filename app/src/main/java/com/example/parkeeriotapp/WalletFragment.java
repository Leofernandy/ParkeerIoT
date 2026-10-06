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
    private View btnTopup;
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
        btnTopup = view.findViewById(R.id.btnTopup);

        // Inisialisasi ListView History
        listWalletHistory = view.findViewById(R.id.listWalletHistory);
        historyList = new ArrayList<>();
        adapter = new WalletHistoryAdapter(requireContext(), historyList);
        listWalletHistory.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        // 📥 Real-time listener saldo & profil
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

                // Update teks saldo
                txvSaldo.setText("IDR " + String.format("%,d", saldo != null ? saldo : 0).replace(',', '.'));
            }
        });

        // Load History dari Realtime Database (dengan pengelompokan tanggal rapi)
        loadTransactionHistory(uid);

        // 💰 Tombol Top Up
        btnTopup.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TopUpActivity.class);
            startActivity(intent);
        });
    }

    // --- FUNGSI LOAD & GROUP HISTORY TRANSAKSI (GAYA OVO / E-WALLET) ---
    private void loadTransactionHistory(String uid) {
        FirebaseDatabase.getInstance().getReference("topups")
                .orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<WalletHistory> rawItems = new ArrayList<>();
                        SimpleDateFormat headerDateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("in", "ID"));

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String keyId = ds.getKey();
                            String channel = ds.child("payment_channel").getValue(String.class);
                            Long amount = ds.child("amount").getValue(Long.class);
                            String dbTitle = ds.child("title").getValue(String.class);
                            Long timestamp = ds.child("timestamp").getValue(Long.class);

                            long rawTimestamp = (timestamp != null) ? timestamp : System.currentTimeMillis();

                            // Format tanggal banner (misal: "30 AGU 2026")
                            String headerDate = headerDateFormat.format(new Date(rawTimestamp)).toUpperCase();

                            String displayTitle;
                            String displaySubtitle;
                            String displayAmount;
                            int typeInt;

                            long amt = (amount != null) ? amount : 0;
                            String formattedAmount = String.format("%,d", amt).replace(',', '.');

                            if (keyId != null && keyId.startsWith("RF")) {
                                displayTitle = (dbTitle != null) ? dbTitle : "Parkeer Refund";
                                displaySubtitle = "Pengembalian Dana";
                                displayAmount = "+Rp" + formattedAmount;
                                typeInt = 3;
                            } else if (keyId != null && keyId.startsWith("PY")) {
                                displayTitle = (dbTitle != null) ? dbTitle : "Pembayaran Parkir";
                                displaySubtitle = "Pembayaran";
                                displayAmount = "-Rp" + formattedAmount;
                                typeInt = 2;
                            } else {
                                String cleanChannel = (channel != null && !channel.isEmpty()) ? channel : "Xendit";
                                if (!cleanChannel.toLowerCase().startsWith("bank") && !cleanChannel.toLowerCase().contains("qris") && !cleanChannel.toLowerCase().contains("ovo") && !cleanChannel.toLowerCase().contains("dana")) {
                                    displayTitle = "Bank " + cleanChannel;
                                } else {
                                    displayTitle = cleanChannel;
                                }
                                displaySubtitle = "Top Up";
                                displayAmount = "+Rp" + formattedAmount;
                                typeInt = 1;
                            }

                            rawItems.add(new WalletHistory(
                                    displayTitle,
                                    displaySubtitle,
                                    displayAmount,
                                    headerDate,
                                    typeInt,
                                    rawTimestamp
                            ));
                        }

                        // Urutkan transaksi dari yang paling baru ke paling lama
                        Collections.sort(rawItems, (h1, h2) -> Long.compare(h2.getTimestamp(), h1.getTimestamp()));

                        // Sisipkan Header Tanggal otomatis saat tanggal berganti
                        historyList.clear();
                        String lastHeader = "";
                        for (WalletHistory item : rawItems) {
                            String itemHeader = item.getDate();
                            if (!itemHeader.equals(lastHeader)) {
                                historyList.add(new WalletHistory(itemHeader)); // Tambahkan banner tanggal
                                lastHeader = itemHeader;
                            }
                            historyList.add(item); // Tambahkan item transaksi
                        }

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