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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookActivity extends AppCompatActivity {
    Button btnBook;
    int mallId;
    // private UserSessionManager session; // 🔸 Dinonaktifkan sementara
    private long durasiMenitTerakhir = 0;
    private int totalHargaTerakhir = 0;
    private String bookingId;
    GridLayout gridSlotContainer;
    String selectedSlotId = null;
    TextView tvSelectedSlot;
    Spinner sprPlate;
    ImageView imvLeftArrow;
    final Calendar myCalendar = Calendar.getInstance();
    EditText edtTglMsk, edtTglKlr, edtJamMsk, edtJamKlr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mallId = getIntent().getIntExtra("mallId", -1);
        // session = new UserSessionManager(this); // 🔸 Commented out

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book);

        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gridSlotContainer = findViewById(R.id.gridSlotContainer);
        tvSelectedSlot = findViewById(R.id.tvSelectedSlot);

        // Dummy load slot parkir
        loadParkingSlots(mallId);

        edtTglMsk = findViewById(R.id.edtTglMsk);
        edtTglKlr = findViewById(R.id.edtTglKlr);
        edtJamMsk = findViewById(R.id.edtJamMsk);
        edtJamKlr = findViewById(R.id.edtJamKlr);

        edtTglMsk.setOnClickListener(v -> showDatePicker(edtTglMsk));
        edtTglKlr.setOnClickListener(v -> showDatePicker(edtTglKlr));
        edtJamMsk.setOnClickListener(v -> showTimePicker(edtJamMsk));
        edtJamKlr.setOnClickListener(v -> showTimePicker(edtJamKlr));

        sprPlate = findViewById(R.id.sprPlate);

        // Dummy data kendaraan
        List<String> plates = new ArrayList<>();
        plates.add("B1234ABC");
        plates.add("D5678XYZ");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, plates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprPlate.setAdapter(adapter);

        btnBook = findViewById(R.id.btnBook);
        btnBook.setOnClickListener(v -> {
            // 🔸 Skip saldo check
            if (edtTglMsk.getText().toString().isEmpty() ||
                    edtJamMsk.getText().toString().isEmpty() ||
                    edtTglKlr.getText().toString().isEmpty() ||
                    edtJamKlr.getText().toString().isEmpty() ||
                    selectedSlotId == null ||
                    sprPlate.getSelectedItem() == null) {
                Toast.makeText(BookActivity.this, "Lengkapi semua data sebelum booking!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ===== Simulasi penyimpanan booking =====
            bookingId = generateBookingId();
            Toast.makeText(this, "Booking berhasil (dummy mode). ID: " + bookingId, Toast.LENGTH_SHORT).show();

            // 🔸 Saldo tidak dikurangi
            toPaymentSuccess();
        });

        TextView txvTerms = findViewById(R.id.txvTerms);
        String fullText = "By paying, you agree to Parkeer’s Terms & Conditions";
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf("Parkeer’s Terms & Conditions");
        int end = start + "Parkeer’s Terms & Conditions".length();
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#001F54")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txvTerms.setText(spannable);
    }

    private void showDatePicker(EditText target) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
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
            } else {
                btnBook.setEnabled(true);
            }

            long durasiMillis = dateKeluar.getTime() - dateMasuk.getTime();
            long durasiMenit = TimeUnit.MILLISECONDS.toMinutes(durasiMillis);
            long durasiJam = (long) Math.ceil(durasiMenit / 60.0);
            if (durasiJam == 0) durasiJam = 1;
            int hargaPerJam = 10000;
            durasiMenitTerakhir = durasiJam * 60;
            totalHargaTerakhir = (int) (durasiJam * hargaPerJam);
            btnBook.setText("BOOK IDR " + String.format("%,d", totalHargaTerakhir).replace(",", "."));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void loadParkingSlots(int mallId) {
        gridSlotContainer.removeAllViews();
        for (int i = 1; i <= 10; i++) {
            String slotId = "S" + i;
            String slotName = "Slot " + i;
            boolean booked = false;

            TextView slotView = new TextView(this);
            slotView.setText(booked ? "BOOKED" : slotName);
            slotView.setTextSize(18);
            slotView.setGravity(Gravity.CENTER);
            slotView.setPadding(8, 8, 8, 8);
            slotView.setWidth(dpToPx(80));
            slotView.setHeight(dpToPx(80));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(20, 20, 20, 20);
            slotView.setLayoutParams(params);

            if (booked) {
                slotView.setBackgroundResource(R.drawable.bg_slot_booked);
                slotView.setTextColor(Color.parseColor("#D32F2F"));
                slotView.setEnabled(false);
            } else {
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
                    slotView.setTextColor(Color.parseColor("#D32F2F"));
                    selectedSlotId = slotId;
                    tvSelectedSlot.setText(slotName);
                });
            }
            gridSlotContainer.addView(slotView);
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
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 4; i++) {
            randomSuffix.append(chars.charAt(random.nextInt(chars.length())));
        }
        return prefix + hex + randomSuffix.toString();
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