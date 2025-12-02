package com.example.parkeeriotapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat; // <-- Import yang benar

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
// Hapus import MutableData dan Transaction RTDB yang tidak terpakai
// import com.google.firebase.database.MutableData;
// import com.google.firebase.database.Transaction;
import com.google.firebase.firestore.*;
// --- IMPORT BARU UNTUK TRANSAKSI FIRESTORE ---
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
// --- END IMPORT BARU ---


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BookActivity extends AppCompatActivity {

    Button btnBook;
    String mallId;
    private FirebaseFirestore firestore; // Tetap dipakai untuk 'malls', 'users'
    private DatabaseReference slotsRef; // Untuk /slots/{mallId}
    private DatabaseReference bookingsRef; // <-- BARU: Untuk /bookings
    private FirebaseAuth auth;

    private long durasiMenitTerakhir = 0;
    private int totalHargaTerakhir = 0;
    private int hargaPerJam = 10000; // default, nanti ditimpa Firestore
    private String bookingId;
    private String selectedSlotId = null;

    GridLayout gridSlotContainer;
    TextView tvSelectedSlot, tvMallName, tvMallAddress, tvMallPrice;
    Spinner sprPlate;
    ImageView imvLeftArrow;
    EditText edtTglMsk, edtTglKlr, edtJamMsk, edtJamKlr;
    final Calendar myCalendar = new Calendar.Builder().setCalendarType("gregory").build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book);

        // Sembunyikan navigation bar & status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        mallId = getIntent().getStringExtra("mallId"); // ✅ ex: "mall01"
        firestore = FirebaseFirestore.getInstance(); // Untuk 'malls', 'users'
        auth = FirebaseAuth.getInstance();

        // ===== PERUBAHAN DATABASE =====
        DatabaseReference rtdbRoot = FirebaseDatabase.getInstance().getReference();
        slotsRef = rtdbRoot.child("slots").child(mallId);
        bookingsRef = rtdbRoot.child("bookings"); // <-- Path baru untuk bookings

        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());

        gridSlotContainer = findViewById(R.id.gridSlotContainer);
        tvSelectedSlot = findViewById(R.id.tvSelectedSlot);
        sprPlate = findViewById(R.id.sprPlate);

        tvMallName = findViewById(R.id.tvMallName);
        tvMallAddress = findViewById(R.id.tvMallAddress);
        tvMallPrice = findViewById(R.id.tvMallPrice);

        edtTglMsk = findViewById(R.id.edtTglMsk);
        edtTglKlr = findViewById(R.id.edtTglKlr);
        edtJamMsk = findViewById(R.id.edtJamMsk);
        edtJamKlr = findViewById(R.id.edtJamKlr);

        edtTglMsk.setOnClickListener(v -> showDatePicker(edtTglMsk));
        edtTglKlr.setOnClickListener(v -> showDatePicker(edtTglKlr));
        edtJamMsk.setOnClickListener(v -> showTimePicker(edtJamMsk));
        edtJamKlr.setOnClickListener(v -> showTimePicker(edtJamKlr));

        btnBook = findViewById(R.id.btnBook);

        loadMallInfo();
        loadUserVehicles();
        loadSlotsFromRTDB(); // Ini sudah benar (membaca RTDB)

        btnBook.setOnClickListener(v -> handleBooking());

        TextView txvTerms = findViewById(R.id.txvTerms);
        String fullText = "By paying, you agree to Parkeer’s Terms & Conditions";
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf("Parkeer’s Terms & Conditions");
        int end = start + "Parkeer’s Terms & Conditions".length();
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#001F54")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txvTerms.setText(spannable);
    }

    // loadMallInfo() tidak berubah, karena data 'malls' tetap di Firestore
    private void loadMallInfo() {
        firestore.collection("malls")
                .document(mallId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String address = documentSnapshot.getString("address");
                        Long price = documentSnapshot.getLong("pricePerHour");

                        if (name != null) tvMallName.setText(name);
                        if (address != null) tvMallAddress.setText(address);
                        if (price != null) {
                            hargaPerJam = price.intValue();
                            tvMallPrice.setText("Rp " + hargaPerJam + " / jam");
                        }

                    } else {
                        Toast.makeText(this, "Data mall tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal load mall: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // loadUserVehicles() tidak berubah, karena data 'users' tetap di Firestore
    private void loadUserVehicles() {
        FirebaseUser currentUser = auth.getCurrentUser();
        // === PENGECEKAN KEAMANAN ===
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            // Anda mungkin ingin menonaktifkan tombol atau mengarahkan ke login
            btnBook.setEnabled(false);
            return;
        }
        String uid = currentUser.getUid();

        firestore.collection("users")
                .document(uid)
                .collection("vehicles")
                .get()
                .addOnSuccessListener(query -> {
                    List<String> plates = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String plate = doc.getString("plate");
                        if (plate != null) plates.add(plate);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, plates);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sprPlate.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal load kendaraan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // loadSlotsFromRTDB() dengan PERBAIKAN WARNA & TEKS
    private void loadSlotsFromRTDB() {
        gridSlotContainer.removeAllViews();
        slotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gridSlotContainer.removeAllViews();
                for (DataSnapshot slotSnap : snapshot.getChildren()) {
                    String slotId = slotSnap.getKey();

                    String status = slotSnap.child("status").getValue(String.class);

                    TextView slotView = new TextView(BookActivity.this);

                    slotView.setTextSize(16);
                    slotView.setGravity(Gravity.CENTER);
                    slotView.setPadding(8, 8, 8, 8);
                    slotView.setWidth(dpToPx(70));
                    slotView.setHeight(dpToPx(70));

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    int margin = dpToPx(8); // Gunakan 8dp
                    params.setMargins(margin, margin, margin, margin);
                    slotView.setLayoutParams(params);

                    if (status == null) {
                        status = "available";
                    }

                    switch (status) {
                        case "booked":
                            slotView.setText("BOOKED"); // Teks baru
                            slotView.setBackgroundResource(R.drawable.bg_slot_booked);
                            slotView.setTextColor(ContextCompat.getColor(BookActivity.this, R.color.kuning)); // Warna dari colors.xml
                            slotView.setEnabled(false);
                            break;
                        case "occupied":
                            slotView.setText("OCCUPIED"); // Teks baru
                            slotView.setBackgroundResource(R.drawable.bg_slot_occupied);
                            slotView.setTextColor(ContextCompat.getColor(BookActivity.this, R.color.merah)); // Warna dari colors.xml
                            slotView.setEnabled(false);
                            break;
                        default: // Ini adalah status "available"
                            slotView.setText(slotId); // Tampilkan nomor slot
                            slotView.setBackgroundResource(R.drawable.bg_slot_available);
                            slotView.setTextColor(ContextCompat.getColor(BookActivity.this, R.color.hijau)); // Warna dari colors.xml
                            slotView.setEnabled(true);
                            slotView.setOnClickListener(v -> {
                                for (int j = 0; j < gridSlotContainer.getChildCount(); j++) {
                                    View child = gridSlotContainer.getChildAt(j);
                                    if (child instanceof TextView && child.isEnabled()) {
                                        child.setBackgroundResource(R.drawable.bg_slot_available);
                                        ((TextView) child).setTextColor(ContextCompat.getColor(BookActivity.this, R.color.hijau));
                                    }
                                }
                                slotView.setBackgroundResource(R.drawable.bg_slot_selecting);
                                slotView.setTextColor(ContextCompat.getColor(BookActivity.this, R.color.biru));
                                selectedSlotId = slotId;
                                tvSelectedSlot.setText(slotId);
                            });
                            break;
                    }
                    gridSlotContainer.addView(slotView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookActivity.this, "Gagal load slots: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // handleBooking() dengan Logika Saldo Firestore
    private void handleBooking() {
        if (edtTglMsk.getText().toString().isEmpty() ||
                edtJamMsk.getText().toString().isEmpty() ||
                edtTglKlr.getText().toString().isEmpty() ||
                edtJamKlr.getText().toString().isEmpty() ||
                selectedSlotId == null ||
                sprPlate.getSelectedItem() == null) {
            Toast.makeText(this, "Lengkapi semua data sebelum booking!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalHargaTerakhir <= 0) {
            Toast.makeText(this, "Total harga tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User tidak terautentikasi.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nonaktifkan tombol untuk mencegah klik ganda
        btnBook.setEnabled(false);
        Toast.makeText(this, "Memproses pembayaran...", Toast.LENGTH_SHORT).show();

        // LANGKAH 1: Potong Saldo (Transaction)
        String uid = currentUser.getUid();

        // === PERBAIKAN: Gunakan Transaksi FIRESTORE ===
        final DocumentReference userDocRef = firestore.collection("users").document(uid);

        firestore.runTransaction(new com.google.firebase.firestore.Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull com.google.firebase.firestore.Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(userDocRef);
                Long currentSaldo = snapshot.getLong("saldo");
                if (currentSaldo == null) {
                    currentSaldo = 0L;
                }

                // Cek apakah saldo mencukupi
                if (currentSaldo < totalHargaTerakhir) {
                    // Saldo tidak cukup, batalkan transaction dengan melempar Exception
                    throw new FirebaseFirestoreException("Saldo tidak mencukupi",
                            FirebaseFirestoreException.Code.ABORTED);
                }

                // Saldo cukup, potong saldo
                long newSaldo = currentSaldo - totalHargaTerakhir;
                transaction.update(userDocRef, "saldo", newSaldo);
                return null; // Sukses
            }
        }).addOnSuccessListener(aVoid -> {
            // LANGKAH 2: Saldo berhasil dipotong, lanjutkan proses booking
            Toast.makeText(BookActivity.this, "Pembayaran berhasil!", Toast.LENGTH_SHORT).show();
            proceedWithBooking(uid);
        }).addOnFailureListener(e -> {
            // Gagal (kemungkinan besar saldo tidak cukup atau error)
            Toast.makeText(BookActivity.this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnBook.setEnabled(true); // Aktifkan kembali tombol
        });
    }

    // --- FUNGSI BARU UNTUK MELANJUTKAN BOOKING SETELAH PEMBAYARAN ---
    private void proceedWithBooking(String uid) {
        bookingId = generateBookingId(); // Tetap pakai fungsi ini

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("bookingId", bookingId);
        bookingData.put("mallId", mallId);
        bookingData.put("mallName", tvMallName.getText().toString());
        bookingData.put("mallAddress", tvMallAddress.getText().toString());
        bookingData.put("slot", selectedSlotId);
        bookingData.put("plate", sprPlate.getSelectedItem().toString());
        bookingData.put("jamMasuk", edtTglMsk.getText().toString() + " " + edtJamMsk.getText().toString());
        bookingData.put("jamKeluar", edtTglKlr.getText().toString() + " " + edtJamKlr.getText().toString());
        bookingData.put("durasiMenit", durasiMenitTerakhir);
        bookingData.put("totalHarga", totalHargaTerakhir);
        bookingData.put("status", "booked");
        bookingData.put("userId", uid);
        bookingData.put("qrScanned", false); // <-- PENTING: Set nilai awal
        bookingData.put("readonly", false); // <-- PENTING: Set nilai awal (Perbaikan: .put bukan .push)


        // ✅ 1) Update slot RTDB
        Map<String, Object> rtdbSlotData = new HashMap<>();
        rtdbSlotData.put("status", "booked");
        rtdbSlotData.put("bookingId", bookingId);
        // Ganti ke updateChildren agar lebih aman
        slotsRef.child(selectedSlotId).updateChildren(rtdbSlotData);

        // ✅ 2) Simpan booking ke RTDB
        bookingsRef.child(bookingId)
                .setValue(bookingData)
                .addOnSuccessListener(unused -> {
                    // Toast.makeText(this, "Booking berhasil!", Toast.LENGTH_SHORT).show(); // Sudah di handle di onComplete
                    toPaymentSuccess(); // lanjut ke success screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal simpan booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // TODO: Tambahkan logika untuk MENGEMBALIKAN SALDO jika langkah ini gagal (rollback)
                    btnBook.setEnabled(true); // Aktifkan lagi
                });
    }


    private void showDatePicker(EditText target) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
                    target.setText(dateFormat.format(calendar.getTime()));
                    updateTotalHarga();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker(EditText target) {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            myCalendar.set(Calendar.MINUTE, minute);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            target.setText(timeFormat.format(myCalendar.getTime()));
            updateTotalHarga();
        }, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show();
    }

    private void updateTotalHarga() {
        String tglMskStr = edtTglMsk.getText().toString();
        String jamMskStr = edtJamMsk.getText().toString();
        String tglKlrStr = edtTglKlr.getText().toString();
        String jamKlrStr = edtJamKlr.getText().toString();

        if (tglMskStr.isEmpty() || jamMskStr.isEmpty() || tglKlrStr.isEmpty() || jamKlrStr.isEmpty()) return;

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
            Date dateMasuk = format.parse(tglMskStr + " " + jamMskStr);
            Date dateKeluar = format.parse(tglKlrStr + " " + jamKlrStr);

            if (dateKeluar.before(dateMasuk)) {
                btnBook.setText("ERROR: Jam keluar < masuk");
                btnBook.setEnabled(false);
                return;
            } else btnBook.setEnabled(true);

            long durasiMillis = dateKeluar.getTime() - dateMasuk.getTime();
            long durasiJam = (long) Math.ceil(TimeUnit.MILLISECONDS.toMinutes(durasiMillis) / 60.0);
            if (durasiJam == 0) durasiJam = 1;

            durasiMenitTerakhir = durasiJam * 60;
            totalHargaTerakhir = (int) (durasiJam * hargaPerJam);
            btnBook.setText("BOOK IDR " + String.format("%,d", totalHargaTerakhir).replace(",", "."));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private String generateBookingId() {
        String prefix = "BK";
        long timestamp = System.currentTimeMillis();
        String hex = Long.toHexString(timestamp).toUpperCase();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ01230123456789";
        StringBuilder randomSuffix = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) randomSuffix.append(chars.charAt(random.nextInt(chars.length())));
        return prefix + hex + randomSuffix;
    }

    public void toPaymentSuccess() {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra("bookingId", bookingId);
        intent.putExtra("slot", tvSelectedSlot.getText().toString());
        intent.putExtra("plate", sprPlate.getSelectedItem().toString());
        intent.putExtra("jamMasuk", edtTglMsk.getText().toString() + " " + edtJamMsk.getText().toString());
        intent.putExtra("jamKeluar", edtTglKlr.getText().toString() + " " + edtJamKlr.getText().toString());
        intent.putExtra("durasiMenit", durasiMenitTerakhir);
        intent.putExtra("totalHarga", totalHargaTerakhir);
        startActivity(intent);
    }
}