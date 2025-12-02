package com.example.parkeeriotapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // <-- Import sudah ada

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// --- IMPORT BARU UNTUK REFUND & UPDATE ---
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
// --- IMPORT BARU UNTUK TRANSAKSI FIRESTORE ---
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
// --- END IMPORT BARU ---

import java.util.HashMap;
import java.util.Map;
// --- END IMPORT BARU ---

import java.util.HashSet;
import java.util.Set;

public class BookDetailsActivity extends AppCompatActivity {
    ImageView imvLeftArrow;
    DatabaseReference bookingsRef;
    FirebaseAuth mAuth; // <-- BARU: Untuk User ID
    FirebaseFirestore firestore; // <-- TAMBAHKAN INI
    String bookingId;
    private static final int SCAN_QR_REQUEST = 1002;

    private TextView btnCancel;
    private TextView btnDownloadReceipt;
    private Set<String> scannedSet;

    // --- VARIABEL BARU UNTUK MENYIMPAN DATA BOOKING ---
    private int bookingTotalHarga = 0;
    private String bookingSlotId = ""; // cth: "S1", "S2"
    private String bookingMallId = ""; // cth: "mall01"
    // --- END VARIABEL BARU ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> navigateToUpcoming());

        // --- INISIALISASI BARU ---
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // <-- TAMBAHKAN INI
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        // --- END ---

        bookingId = getIntent().getStringExtra("bookingId");
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Booking ID tidak ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ... (findViewByid TextViews sama) ...
        TextView textViewBookingId = findViewById(R.id.textViewBookingId);
        TextView textViewMallName = findViewById(R.id.textViewMallName);
        TextView textViewSlot = findViewById(R.id.textViewSlot);
        TextView textViewPlate = findViewById(R.id.textViewPlate);
        TextView textViewMasuk = findViewById(R.id.textViewMasuk);
        TextView textViewKeluar = findViewById(R.id.textViewKeluar);
        TextView textViewDurasi = findViewById(R.id.textViewDurasi);
        TextView textViewHarga = findViewById(R.id.textViewHarga);

        btnCancel = findViewById(R.id.btnCancel);
        btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);

        bookingsRef.child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(BookDetailsActivity.this, "Booking tidak ditemukan", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        // ... (Ambil data display seperti biasa) ...
                        String mallName = dataSnapshot.child("mallName").getValue(String.class);
                        String slot = dataSnapshot.child("slot").getValue(String.class);
                        String plate = dataSnapshot.child("plate").getValue(String.class);
                        String jamMasuk = dataSnapshot.child("jamMasuk").getValue(String.class);
                        String jamKeluar = dataSnapshot.child("jamKeluar").getValue(String.class);

                        // --- SIMPAN DATA UNTUK PROSES CANCEL ---
                        Long totalHargaLong = dataSnapshot.child("totalHarga").getValue(Long.class);
                        bookingTotalHarga = (totalHargaLong != null) ? totalHargaLong.intValue() : 0;

                        // ASUMSI: "slot" berisi "S1", "S2", dll.
                        bookingSlotId = dataSnapshot.child("slot").getValue(String.class);
                        // ASUMSI: Data booking Anda menyimpan "mallId" (cth: "mall01")
                        bookingMallId = dataSnapshot.child("mallId").getValue(String.class);
                        // --- END SIMPAN DATA ---

                        Long durasiMenitLong = dataSnapshot.child("durasiMenit").getValue(Long.class);
                        long durasiMenit = (durasiMenitLong != null) ? durasiMenitLong : 0;

                        Boolean readonlyBool = dataSnapshot.child("readonly").getValue(Boolean.class);
                        boolean readonly = (readonlyBool != null) ? readonlyBool : false;

                        Boolean qrScannedBool = dataSnapshot.child("qrScanned").getValue(Boolean.class);
                        boolean qrScanned = (qrScannedBool != null) ? qrScannedBool : false;

                        long hours = durasiMenit / 60;
                        long minutes = durasiMenit % 60;
                        String durasiFormatted = String.format("%02d hours %02d minutes", hours, minutes);

                        // Set ke TextView (Display)
                        textViewBookingId.setText(bookingId);
                        textViewMallName.setText(mallName);
                        // Tampilkan slot yang "cantik" (A-01), tapi kita simpan "S1" untuk logika
                        // Jika slot Anda *sudah* "A-01" di Firebase, Anda perlu logika konversi
                        textViewSlot.setText(slot); // Asumsi slot ini "S1" atau "A-01"
                        textViewPlate.setText(plate);
                        textViewMasuk.setText(jamMasuk);
                        textViewKeluar.setText(jamKeluar);
                        textViewDurasi.setText(durasiFormatted);
                        // === PERBAIKAN DI BARIS INI ===
                        // 'totalHarga' tidak ada, ganti ke 'bookingTotalHarga'
                        textViewHarga.setText("IDR " + String.format("%,d", bookingTotalHarga).replace(",", "."));

                        scannedSet = getSharedPreferences("qr_status", MODE_PRIVATE)
                                .getStringSet("scannedBookings", new HashSet<>());

                        // ... (Logika status tombol sama) ...
                        if (readonly || qrScanned || scannedSet.contains(bookingId)) {
                            btnCancel.setEnabled(false);
                            btnCancel.setAlpha(0.4f);
                            btnDownloadReceipt.setEnabled(false);
                            btnDownloadReceipt.setAlpha(0.4f);
                            if(qrScanned || scannedSet.contains(bookingId)) {
                                btnDownloadReceipt.setText("QR SCANNED");
                            }
                        } else {
                            btnCancel.setEnabled(true);
                            btnCancel.setAlpha(1.0f);
                            btnDownloadReceipt.setEnabled(true);
                            btnDownloadReceipt.setAlpha(1.0f);
                            btnDownloadReceipt.setText("SCAN QR");
                        }


                        // === LOGIKA TOMBOL CANCEL BARU (3 LANGKAH) ===
                        btnCancel.setOnClickListener(v -> {
                            if (qrScanned || scannedSet.contains(bookingId)) {
                                Toast.makeText(BookDetailsActivity.this, "Cannot cancel, already scanned.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Validasi data sebelum memproses
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser == null) {
                                Toast.makeText(BookDetailsActivity.this, "User not authenticated. Cannot refund.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (bookingMallId == null || bookingSlotId == null || bookingMallId.isEmpty() || bookingSlotId.isEmpty()) {
                                Toast.makeText(BookDetailsActivity.this, "Booking data corrupt (Mall ID/Slot ID missing). Cannot update slot.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (bookingTotalHarga <= 0) {
                                Toast.makeText(BookDetailsActivity.this, "Cannot refund 0.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Nonaktifkan tombol untuk mencegah klik ganda
                            btnCancel.setEnabled(false);
                            btnCancel.setAlpha(0.4f);

                            Toast.makeText(BookDetailsActivity.this, "Processing cancellation...", Toast.LENGTH_SHORT).show();

                            // LANGKAH 1: Kembalikan Saldo (Transaction)
                            String userId = currentUser.getUid();
                            // DatabaseReference userSaldoRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("saldo"); // <-- HAPUS

                            // === PERBAIKAN: Gunakan Transaksi FIRESTORE ===
                            final DocumentReference userDocRef = firestore.collection("users").document(userId);

                            firestore.runTransaction(new com.google.firebase.firestore.Transaction.Function<Void>() {
                                @Override
                                public Void apply(@NonNull com.google.firebase.firestore.Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot snapshot = transaction.get(userDocRef);
                                    Long currentSaldo = snapshot.getLong("saldo");
                                    if (currentSaldo == null) {
                                        currentSaldo = 0L;
                                    }

                                    // Tambahkan saldo kembali
                                    long newSaldo = currentSaldo + bookingTotalHarga;
                                    transaction.update(userDocRef, "saldo", newSaldo);
                                    return null; // Sukses
                                }
                            }).addOnSuccessListener(aVoid -> {
                                // Saldo berhasil dikembalikan, lanjut ke Langkah 2
                                updateSlotStatus();
                            }).addOnFailureListener(e -> {
                                // Gagal refund, batalkan proses
                                Toast.makeText(BookDetailsActivity.this, "Failed to refund balance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                btnCancel.setEnabled(true); // Aktifkan tombol lagi
                                btnCancel.setAlpha(1.0f);
                            });

                            /* <-- HAPUS SEMUA LOGIKA TRANSAKSI REALTIME DATABASE YANG LAMA -->
                            userSaldoRef.runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    Long currentSaldo = mutableData.getValue(Long.class);
                                    if (currentSaldo == null) {
                                        currentSaldo = 0L;
                                    }
                                    // Tambahkan saldo kembali
                                    long newSaldo = currentSaldo + bookingTotalHarga;
                                    mutableData.setValue(newSaldo);
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                                    if (committed) {
                                        // Saldo berhasil dikembalikan, lanjut ke Langkah 2
                                        updateSlotStatus();
                                    } else {
                                        // Gagal refund, batalkan proses
                                        Toast.makeText(BookDetailsActivity.this, "Failed to refund balance. Cancellation aborted.", Toast.LENGTH_SHORT).show();
                                        btnCancel.setEnabled(true); // Aktifkan tombol lagi
                                        btnCancel.setAlpha(1.0f);
                                    }
                                }
                            });
                            */
                        });
                        // === END LOGIKA TOMBOL CANCEL BARU ===


                        // === Logika Tombol "SCAN QR" (Tidak berubah) ===
                        btnDownloadReceipt.setOnClickListener(v -> {
                            Intent intent = new Intent(BookDetailsActivity.this, ScanQR.class);
                            startActivityForResult(intent, SCAN_QR_REQUEST);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(BookDetailsActivity.this, "Gagal load data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- FUNGSI BARU (LANGKAH 2) ---
    private void updateSlotStatus() {
        // Path ke slot di Firebase, cth: /slots/mall01/S1
        DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference("slots")
                .child(bookingMallId).child(bookingSlotId);

        Map<String, Object> slotUpdate = new HashMap<>();
        slotUpdate.put("status", "available");
        slotUpdate.put("bookingId", ""); // Kosongkan bookingId di slot

        slotRef.updateChildren(slotUpdate).addOnSuccessListener(aVoid -> {
            // Slot berhasil di-update, lanjut ke Langkah 3
            deleteBookingEntry();
        }).addOnFailureListener(e -> {
            // Ini adalah status yang buruk (refund berhasil, tapi slot gagal update)
            Toast.makeText(BookDetailsActivity.this, "Refund success, but failed to update slot. Please contact support.", Toast.LENGTH_LONG).show();
            // Tetap hapus bookingnya agar tidak "menggantung"
            deleteBookingEntry();
        });
    }

    // --- FUNGSI BARU (LANGKAH 3) ---
    private void deleteBookingEntry() {
        bookingsRef.child(bookingId).removeValue().addOnSuccessListener(aVoid -> {
            // SUKSES TOTAL
            Toast.makeText(BookDetailsActivity.this, "Booking successfully cancelled. Refund processed.", Toast.LENGTH_SHORT).show();

            // Hapus dari SharedPreferences lokal
            if (scannedSet != null) {
                scannedSet.remove(bookingId);
                getSharedPreferences("qr_status", MODE_PRIVATE)
                        .edit()
                        .putStringSet("scannedBookings", scannedSet)
                        .apply();
            }

            // Kembali ke halaman utama
            navigateToUpcoming();

        }).addOnFailureListener(e -> {
            Toast.makeText(BookDetailsActivity.this, "Refund/Slot update success, but failed to delete booking entry.", Toast.LENGTH_LONG).show();
            // Tetap navigasi kembali
            navigateToUpcoming();
        });
    }


    // ... (Fungsi onActivityResult tidak berubah) ...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_QR_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String qrText = data.getStringExtra("QR_RESULT");
                if (qrText != null && qrText.equals("PARKEERIOT_GATE_01")) {
                    Toast.makeText(this, "QR Code Match! Gate Valid.", Toast.LENGTH_LONG).show();
                    handleScanSuccess();
                } else {
                    Toast.makeText(this, "Wrong QR Code. Please scan the correct one.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "QR Scan cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ... (Fungsi handleScanSuccess tidak berubah) ...
    private void handleScanSuccess() {
        btnCancel.setEnabled(false);
        btnCancel.setAlpha(0.4f);
        btnDownloadReceipt.setEnabled(false);
        btnDownloadReceipt.setAlpha(0.4f);
        btnDownloadReceipt.setText("QR SCANNED");

        if (scannedSet == null) {
            scannedSet = getSharedPreferences("qr_status", MODE_PRIVATE)
                    .getStringSet("scannedBookings", new HashSet<>());
        }
        scannedSet.add(bookingId);
        getSharedPreferences("qr_status", MODE_PRIVATE)
                .edit()
                .putStringSet("scannedBookings", scannedSet)
                .apply();

        bookingsRef.child(bookingId).child("qrScanned").setValue(true);
    }


    // ... (Fungsi navigateToUpcoming tidak berubah) ...
    private void navigateToUpcoming() {
        Intent intent = new Intent(BookDetailsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("GOTO_FRAGMENT", "UPCOMING");
        startActivity(intent);
        finish();
    }
}