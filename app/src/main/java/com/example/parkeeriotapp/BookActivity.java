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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BookActivity extends AppCompatActivity {

    Button btnBook;
    String mallId;
    private FirebaseFirestore firestore;
    private DatabaseReference slotsRef;
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
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        slotsRef = FirebaseDatabase.getInstance().getReference("slots").child(mallId);

        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());

        gridSlotContainer = findViewById(R.id.gridSlotContainer);
        tvSelectedSlot = findViewById(R.id.tvSelectedSlot);
        sprPlate = findViewById(R.id.sprPlate);

        // ✅ Tambahan TextView info mall
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

        loadMallInfo(); // 🔥 ambil data mall
        loadUserVehicles();
        loadSlotsFromRTDB();

        btnBook.setOnClickListener(v -> handleBooking());

        TextView txvTerms = findViewById(R.id.txvTerms);
        String fullText = "By paying, you agree to Parkeer’s Terms & Conditions";
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf("Parkeer’s Terms & Conditions");
        int end = start + "Parkeer’s Terms & Conditions".length();
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#001F54")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txvTerms.setText(spannable);
    }

    // ✅ Load data mall dari Firestore
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

    private void loadUserVehicles() {
        String uid = auth.getCurrentUser().getUid();
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

    private void loadSlotsFromRTDB() {
        gridSlotContainer.removeAllViews();
        slotsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                gridSlotContainer.removeAllViews();
                for (DataSnapshot slotSnap : snapshot.getChildren()) {
                    String slotId = slotSnap.getKey();
                    String status = slotSnap.getValue(String.class);

                    TextView slotView = new TextView(BookActivity.this);
                    slotView.setText(slotId);
                    slotView.setTextSize(18);
                    slotView.setGravity(Gravity.CENTER);
                    slotView.setPadding(8, 8, 8, 8);
                    slotView.setWidth(dpToPx(80));
                    slotView.setHeight(dpToPx(80));

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.setMargins(20, 20, 20, 20);
                    slotView.setLayoutParams(params);

                    switch (status) {
                        case "booked":
                            slotView.setBackgroundResource(R.drawable.bg_slot_booked);
                            slotView.setTextColor(Color.YELLOW);
                            slotView.setEnabled(false);
                            break;
                        case "occupied":
                            slotView.setBackgroundResource(R.drawable.bg_slot_booked);
                            slotView.setTextColor(Color.RED);
                            slotView.setEnabled(false);
                            break;
                        default:
                            slotView.setBackgroundResource(R.drawable.bg_slot_available);
                            slotView.setTextColor(Color.parseColor("#00C853"));
                            slotView.setEnabled(true);
                            slotView.setOnClickListener(v -> {
                                for (int j = 0; j < gridSlotContainer.getChildCount(); j++) {
                                    View child = gridSlotContainer.getChildAt(j);
                                    if (child instanceof TextView && child.isEnabled()) {
                                        child.setBackgroundResource(R.drawable.bg_slot_available);
                                        ((TextView) child).setTextColor(Color.parseColor("#00C853"));
                                    }
                                }
                                slotView.setBackgroundResource(R.drawable.bg_slot_booked);
                                slotView.setTextColor(Color.YELLOW);
                                selectedSlotId = slotId;
                                tvSelectedSlot.setText(slotId);
                            });
                            break;
                    }
                    gridSlotContainer.addView(slotView);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BookActivity.this, "Gagal load slots: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleBooking() {
        if (edtTglMsk.getText().toString().isEmpty() ||
                edtJamMsk.getText().toString().isEmpty() ||
                edtTglKlr.getText().toString().isEmpty() ||
                edtJamKlr.getText().toString().isEmpty() ||
                selectedSlotId == null ||
                sprPlate.getSelectedItem() == null) {
            Toast.makeText(BookActivity.this, "Lengkapi semua data sebelum booking!", Toast.LENGTH_SHORT).show();
            return;
        }

        bookingId = generateBookingId();

        slotsRef.child(selectedSlotId).setValue("booked")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking berhasil!", Toast.LENGTH_SHORT).show();
                    toPaymentSuccess();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal update slot: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDatePicker(EditText target) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            myCalendar.set(year, month, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            target.setText(dateFormat.format(myCalendar.getTime()));
            updateTotalHarga();
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
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
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
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