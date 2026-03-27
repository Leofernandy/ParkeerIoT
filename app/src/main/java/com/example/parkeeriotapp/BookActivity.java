package com.example.parkeeriotapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BookActivity extends AppCompatActivity {

    Button btnBook;
    String mallId;
    private FirebaseFirestore firestore;
    private DatabaseReference slotsRef;
    private DatabaseReference bookingsRef;
    private FirebaseAuth auth;
    private RequestQueue requestQueue;

    private long durasiMenitTerakhir = 0;
    private int totalHargaTerakhir = 0;
    private int hargaPerJam = 10000;
    private String bookingId;
    private String selectedSlotId = null;

    // --- VARIABEL WALLET ---
    private long currentSaldo = 0;
    private Spinner sprPayment;
    private List<String> paymentOptions = new ArrayList<>();
    private ArrayAdapter<String> paymentAdapter;

    GridLayout gridSlotContainer;
    TextView tvSelectedSlot, tvMallName, tvMallAddress, tvMallPrice;
    Spinner sprPlate;
    ImageView imvLeftArrow;
    EditText edtTglMsk, edtTglKlr, edtJamMsk, edtJamKlr;
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        mallId = getIntent().getStringExtra("mallId");
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        DatabaseReference rtdbRoot = FirebaseDatabase.getInstance().getReference();
        slotsRef = rtdbRoot.child("slots").child(mallId);
        bookingsRef = rtdbRoot.child("bookings");

        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());

        gridSlotContainer = findViewById(R.id.gridSlotContainer);
        tvSelectedSlot = findViewById(R.id.tvSelectedSlot);
        sprPlate = findViewById(R.id.sprPlate);
        sprPayment = findViewById(R.id.sprPayment); // Inisialisasi Spinner Payment

        tvMallName = findViewById(R.id.tvMallName);
        tvMallAddress = findViewById(R.id.tvMallAddress);
        tvMallPrice = findViewById(R.id.tvMallPrice);
        edtTglMsk = findViewById(R.id.edtTglMsk);
        edtTglKlr = findViewById(R.id.edtTglKlr);
        edtJamMsk = findViewById(R.id.edtJamMsk);
        edtJamKlr = findViewById(R.id.edtJamKlr);
        btnBook = findViewById(R.id.btnBook);

        edtTglMsk.setOnClickListener(v -> showDatePicker(edtTglMsk));
        edtTglKlr.setOnClickListener(v -> showDatePicker(edtTglKlr));
        edtJamMsk.setOnClickListener(v -> showTimePicker(edtJamMsk));
        edtJamKlr.setOnClickListener(v -> showTimePicker(edtJamKlr));

        loadMallInfo();
        loadUserData(); // Load kendaraan & saldo wallet
        loadSlotsFromRTDB();

        btnBook.setOnClickListener(v -> handleBooking());

        TextView txvTerms = findViewById(R.id.txvTerms);
        String fullText = "By paying, you agree to Parkeer’s Terms & Conditions";
        if(txvTerms != null) {
            SpannableString spannable = new SpannableString(fullText);
            int start = fullText.indexOf("Parkeer’s Terms & Conditions");
            if(start >= 0) {
                int end = start + "Parkeer’s Terms & Conditions".length();
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#001F54")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                txvTerms.setText(spannable);
            }
        }
    }

    private void handleBooking() {
        if (edtTglMsk.getText().toString().isEmpty() ||
                edtJamMsk.getText().toString().isEmpty() ||
                edtTglKlr.getText().toString().isEmpty() ||
                edtJamKlr.getText().toString().isEmpty() ||
                selectedSlotId == null ||
                sprPlate.getSelectedItem() == null ||
                sprPayment.getSelectedItem() == null) {
            Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalHargaTerakhir <= 0) {
            Toast.makeText(this, "Total harga tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        btnBook.setEnabled(false);
        bookingId = generateBookingId();

        // CEK PILIHAN DROPDOWN (Index 0 = Wallet, Index 1 = Xendit)
        if (sprPayment.getSelectedItemPosition() == 0) {
            // PAKAI WALLET
            if (currentSaldo < totalHargaTerakhir) {
                Toast.makeText(this, "Saldo Wallet tidak cukup!", Toast.LENGTH_SHORT).show();
                btnBook.setEnabled(true);
            } else {
                Toast.makeText(this, "Memproses Wallet...", Toast.LENGTH_SHORT).show();
                payWithWallet(currentUser.getUid());
            }
        } else {
            // PAKAI XENDIT
            Toast.makeText(this, "Menyiapkan Xendit...", Toast.LENGTH_SHORT).show();
            saveToFirebaseBeforeXendit(currentUser.getUid());
        }
    }

    private void payWithWallet(String uid) {
        DocumentReference userDocRef = firestore.collection("users").document(uid);

        firestore.runTransaction(new com.google.firebase.firestore.Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull com.google.firebase.firestore.Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(userDocRef);
                Long saldoDB = snapshot.getLong("saldo");
                if (saldoDB == null) saldoDB = 0L;

                if (saldoDB < totalHargaTerakhir) {
                    throw new FirebaseFirestoreException("Saldo kurang", FirebaseFirestoreException.Code.ABORTED);
                }

                long newSaldo = saldoDB - totalHargaTerakhir;
                transaction.update(userDocRef, "saldo", newSaldo);
                return null;
            }
        }).addOnSuccessListener(aVoid -> {
            // =======================================================
            // 1. TAMBAHAN BARU: CATAT KE HISTORY WALLET SEBAGAI PAYMENT
            // =======================================================
            String paymentId = "PY-" + bookingId + "-" + System.currentTimeMillis();

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("userId", uid);
            paymentData.put("amount", totalHargaTerakhir);
            paymentData.put("status", "SUCCESS");
            paymentData.put("type", "PAYMENT");
            paymentData.put("title", "Pay Booking Slot " + selectedSlotId);
            paymentData.put("timestamp", System.currentTimeMillis());

            DatabaseReference rtdbRoot = FirebaseDatabase.getInstance().getReference();
            rtdbRoot.child("topups").child(paymentId).setValue(paymentData)
                    .addOnSuccessListener(aVoid2 -> {

                        // =======================================================
                        // 2. LANJUTKAN PROSES BOOKING SEPERTI BIASA
                        // =======================================================
                        saveBookingData(uid, "booked", "WALLET");
                        Intent intent = new Intent(BookActivity.this, PaymentSuccessActivity.class);
                        putSuccessIntentData(intent);
                        startActivity(intent);
                        finish();

                    });
            // =======================================================

        }).addOnFailureListener(e -> {
            btnBook.setEnabled(true);
            Toast.makeText(this, "Gagal potong saldo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveToFirebaseBeforeXendit(String uid) {
        saveBookingData(uid, "pending", "UNPAID");

        String url = "https://createinvoice-d2jn6f3etq-uc.a.run.app";
        JSONObject params = new JSONObject();
        try {
            params.put("order_id", bookingId);
            params.put("amount", totalHargaTerakhir);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, params,
                res -> {
                    try {
                        String invoiceUrl = res.getString("invoice_url");
                        Intent intent = new Intent(BookActivity.this, PaymentSuccessActivity.class);
                        putSuccessIntentData(intent);
                        intent.putExtra("invoiceUrl", invoiceUrl);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                err -> {
                    btnBook.setEnabled(true);
                    Toast.makeText(this, "Xendit Error", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(req);
    }

    private void saveBookingData(String uid, String statusBooking, String statusPayment) {
        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", bookingId);
        data.put("mallId", mallId);
        data.put("mallName", tvMallName.getText().toString());
        data.put("mallAddress", tvMallAddress.getText().toString());
        data.put("slot", selectedSlotId);
        data.put("plate", sprPlate.getSelectedItem().toString());
        data.put("jamMasuk", edtTglMsk.getText().toString() + " " + edtJamMsk.getText().toString());
        data.put("jamKeluar", edtTglKlr.getText().toString() + " " + edtJamKlr.getText().toString());
        data.put("totalHarga", totalHargaTerakhir);
        data.put("durasiMenit", durasiMenitTerakhir);
        data.put("status", statusBooking);
        data.put("payment_status", statusPayment);
        data.put("userId", uid);
        data.put("qrScanned", false);

        bookingsRef.child(bookingId).setValue(data).addOnSuccessListener(unused -> {
            Map<String, Object> slotUpdate = new HashMap<>();
            slotUpdate.put("status", "booked");
            slotUpdate.put("bookingId", bookingId);
            slotsRef.child(selectedSlotId).updateChildren(slotUpdate);
        });
    }

    private void putSuccessIntentData(Intent intent) {
        intent.putExtra("bookingId", bookingId);
        intent.putExtra("mallName", tvMallName.getText().toString());
        intent.putExtra("mallAddress", tvMallAddress.getText().toString());
        intent.putExtra("slot", selectedSlotId);
        intent.putExtra("plate", sprPlate.getSelectedItem().toString());
        intent.putExtra("jamMasuk", edtTglMsk.getText().toString() + " " + edtJamMsk.getText().toString());
        intent.putExtra("jamKeluar", edtTglKlr.getText().toString() + " " + edtJamKlr.getText().toString());
        intent.putExtra("totalHarga", totalHargaTerakhir);
        intent.putExtra("durasiMenit", durasiMenitTerakhir);
    }

    private void loadUserData() {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return;

        // 1. Load Saldo & Isi Dropdown Payment
        firestore.collection("users").document(u.getUid()).get().addOnSuccessListener(doc -> {
            paymentOptions.clear();
            if (doc.exists() && doc.contains("saldo")) {
                currentSaldo = doc.getLong("saldo");
            }
            String fmtSaldo = String.format("%,d", currentSaldo).replace(",", ".");
            paymentOptions.add("My Wallet (Rp " + fmtSaldo + ")");
            paymentOptions.add("Online Payment (Xendit)");

            paymentAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, paymentOptions);
            paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sprPayment.setAdapter(paymentAdapter);
        });

        // 2. Load Kendaraan
        firestore.collection("users").document(u.getUid()).collection("vehicles").get().addOnSuccessListener(query -> {
            List<String> p = new ArrayList<>();
            for (DocumentSnapshot doc : query.getDocuments()) {
                if (doc.getString("plate") != null) p.add(doc.getString("plate"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, p);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sprPlate.setAdapter(adapter);
        });
    }

    private void loadMallInfo() {
        firestore.collection("malls").document(mallId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvMallName.setText(doc.getString("name"));
                tvMallAddress.setText(doc.getString("address"));
                if (doc.getLong("pricePerHour") != null) {
                    hargaPerJam = doc.getLong("pricePerHour").intValue();
                    tvMallPrice.setText("Rp " + hargaPerJam + " / jam");
                }
            }
        });
    }

    private void loadSlotsFromRTDB() {
        slotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gridSlotContainer.removeAllViews();
                for (DataSnapshot slotSnap : snapshot.getChildren()) {
                    String slotId = slotSnap.getKey();
                    String status = slotSnap.child("status").getValue(String.class);
                    if (status == null) status = "available";

                    TextView slotView = new TextView(BookActivity.this);
                    slotView.setTextSize(16);
                    slotView.setGravity(Gravity.CENTER);
                    slotView.setWidth(dpToPx(70));
                    slotView.setHeight(dpToPx(70));

                    // ---> INI YANG BIKIN RAPI: Tambahan Margin biar kotak gak nempel <---
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    int margin = dpToPx(8);
                    params.setMargins(margin, margin, margin, margin);
                    slotView.setLayoutParams(params);

                    if ("booked".equals(status)) {
                        slotView.setText("BOOKED");
                        slotView.setBackgroundResource(R.drawable.bg_slot_booked);
                        slotView.setTextColor(ContextCompat.getColor(BookActivity.this, R.color.kuning));
                        slotView.setEnabled(false);
                    } else if ("occupied".equals(status)) {
                        slotView.setText("OCCUPIED");
                        slotView.setBackgroundResource(R.drawable.bg_slot_occupied);
                        slotView.setTextColor(ContextCompat.getColor(BookActivity.this, R.color.merah));
                        slotView.setEnabled(false);
                    } else {
                        slotView.setText(slotId);
                        slotView.setBackgroundResource(R.drawable.bg_slot_available);
                        slotView.setTextColor(ContextCompat.getColor(BookActivity.this, R.color.hijau));

                        // ---> INI EFEK KLIKNYA BIAR CANTIK <---
                        slotView.setOnClickListener(v -> {
                            // Reset semua slot yang available jadi hijau lagi
                            for (int j = 0; j < gridSlotContainer.getChildCount(); j++) {
                                View child = gridSlotContainer.getChildAt(j);
                                if (child instanceof TextView && child.isEnabled()) {
                                    child.setBackgroundResource(R.drawable.bg_slot_available);
                                    ((TextView) child).setTextColor(ContextCompat.getColor(BookActivity.this, R.color.hijau));
                                }
                            }
                            // Slot yang diklik berubah jadi biru
                            slotView.setBackgroundResource(R.drawable.bg_slot_selecting);
                            slotView.setTextColor(ContextCompat.getColor(BookActivity.this, R.color.biru));
                            selectedSlotId = slotId;
                            tvSelectedSlot.setText(slotId);
                        });
                    }
                    gridSlotContainer.addView(slotView);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showDatePicker(EditText t) {
        DatePickerDialog dialog = new DatePickerDialog(this, (v, y, m, d) -> {
            myCalendar.set(y, m, d);
            t.setText(new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(myCalendar.getTime()));
            updateTotalHarga();
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        // ---> INI KUNCINYA: Mencegah pilihan tanggal di masa lalu <---
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());

        dialog.show();
    }

    private void showTimePicker(EditText t) {
        new TimePickerDialog(this, (v, h, m) -> {
            myCalendar.set(Calendar.HOUR_OF_DAY, h); myCalendar.set(Calendar.MINUTE, m);
            t.setText(new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(myCalendar.getTime()));
            updateTotalHarga();
        }, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show();
    }

    private void updateTotalHarga() {
        try {
            SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
            Date d1 = f.parse(edtTglMsk.getText() + " " + edtJamMsk.getText());
            Date d2 = f.parse(edtTglKlr.getText() + " " + edtJamKlr.getText());
            long diff = d2.getTime() - d1.getTime();
            long hours = (long) Math.ceil(TimeUnit.MILLISECONDS.toMinutes(diff) / 60.0);
            if (hours <= 0) hours = 1;
            durasiMenitTerakhir = hours * 60;
            totalHargaTerakhir = (int) (hours * hargaPerJam);
            btnBook.setText("BOOK IDR " + totalHargaTerakhir);
        } catch (Exception e) {}
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }
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
}