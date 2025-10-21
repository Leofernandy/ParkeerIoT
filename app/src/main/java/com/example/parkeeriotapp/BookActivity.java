package com.example.parkeeriotapp;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.realm.Realm;
import io.realm.RealmResults;

import com.example.parkeeriotapp.model.Booking;
import com.example.parkeeriotapp.model.Mall;
import com.example.parkeeriotapp.model.SlotParkir;
import com.example.parkeeriotapp.model.Vehicle;
import com.example.parkeeriotapp.utils.DateTimeUtil;
import com.example.parkeeriotapp.utils.UserSessionManager;

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
    Realm realm;
    int mallId;
    private Mall selectedMall;
    private UserSessionManager session;
    private Mall mall;
    private long durasiMenitTerakhir = 0;
    private int totalHargaTerakhir = 0;
    private String bookingId;
    GridLayout gridSlotContainer;
    String selectedSlotId = null;
    TextView tvSelectedSlot;
    Spinner sprPlate;
    ImageView imvLeftArrow;
    final Calendar myCalendar = Calendar.getInstance();
    EditText edtTglMsk , edtTglKlr , edtJamMsk , edtJamKlr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        mallId = getIntent().getIntExtra("mallId", -1);
        session = new UserSessionManager(this);
        mall = realm.where(Mall.class).equalTo("id", mallId).findFirst();

        RealmResults<SlotParkir> slotList = realm.where(SlotParkir.class)
                .equalTo("mallId", mallId)
                .findAll();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book);

        imvLeftArrow= findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());

        selectedMall = realm.where(Mall.class).equalTo("id", mallId).findFirst();
        if (selectedMall != null) {
            ImageView imvBgMall = findViewById(R.id.imvBgMall);
            TextView txMallName = findViewById(R.id.txMallName);
            TextView tvMallAddress = findViewById(R.id.tvMallAddress);
            TextView tvMallPrice = findViewById(R.id.tvMallPrice);
            TextView tvMallDistance = findViewById(R.id.tvMallDistance);

            imvBgMall.setImageResource(selectedMall.getImageResId());
            txMallName.setText(selectedMall.getName());
            tvMallAddress.setText(selectedMall.getAddress());
            tvMallDistance.setText(selectedMall.getDistance());
            tvMallPrice.setText("IDR " + selectedMall.getPricePerHour() + "/HR");
        } else {
            System.out.println("Mall dengan ID " + mallId + " tidak ditemukan.");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        gridSlotContainer = findViewById(R.id.gridSlotContainer);
        tvSelectedSlot = findViewById(R.id.tvSelectedSlot);

        loadParkingSlots(mallId);

        edtTglMsk = findViewById(R.id.edtTglMsk);
        edtTglKlr = findViewById(R.id.edtTglKlr);

        edtTglMsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(BookActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR,year);
                        myCalendar.set(Calendar.MONTH,month);
                        myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                        String myFormat = "dd-MMM-yyyy";
                        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.ENGLISH);
                        edtTglMsk.setText(dateFormat.format(myCalendar.getTime()));
                        updateTotalHarga();
                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        edtTglKlr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(BookActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR,year);
                        myCalendar.set(Calendar.MONTH,month);
                        myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                        String myFormat = "dd-MMM-yyyy";
                        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.ENGLISH);
                        edtTglKlr.setText(dateFormat.format(myCalendar.getTime()));
                        updateTotalHarga();
                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        edtJamMsk = findViewById(R.id.edtJamMsk);
        edtJamKlr = findViewById(R.id.edtJamKlr);
        edtTglMsk.addTextChangedListener(new SimpleTextWatcher(() -> updateTotalHarga()));
        edtJamMsk.addTextChangedListener(new SimpleTextWatcher(() -> updateTotalHarga()));
        edtTglKlr.addTextChangedListener(new SimpleTextWatcher(() -> updateTotalHarga()));
        edtJamKlr.addTextChangedListener(new SimpleTextWatcher(() -> updateTotalHarga()));


        edtJamMsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(BookActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                myCalendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                edtJamMsk.setText(timeFormat.format(myCalendar.getTime()));
                                updateTotalHarga();
                            }
                        },
                        myCalendar.get(Calendar.HOUR_OF_DAY),
                        myCalendar.get(Calendar.MINUTE),
                        true
                ).show();

            }
        });

        edtJamKlr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(BookActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                myCalendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                                edtJamKlr.setText(timeFormat.format(myCalendar.getTime()));
                                updateTotalHarga();
                            }
                        },
                        myCalendar.get(Calendar.HOUR_OF_DAY),
                        myCalendar.get(Calendar.MINUTE),
                        true
                ).show();

            }
        });

        btnBook = findViewById(R.id.btnBook);

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = session.getEmail();
                SharedPreferences prefs = getSharedPreferences("wallet_" + email, MODE_PRIVATE);
                int saldoSekarang = prefs.getInt("balance", 0);

                if (saldoSekarang < totalHargaTerakhir) {
                    Toast.makeText(BookActivity.this, "Saldo tidak cukup untuk melakukan booking.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (
                        edtTglMsk.getText().toString().isEmpty() ||
                                edtJamMsk.getText().toString().isEmpty() ||
                                edtTglKlr.getText().toString().isEmpty() ||
                                edtJamKlr.getText().toString().isEmpty() ||
                                selectedSlotId == null ||
                                sprPlate.getSelectedItem() == null
                ) {
                    Toast.makeText(BookActivity.this, "Lengkapi semua data sebelum booking!", Toast.LENGTH_SHORT).show();
                    return;
                }



                realm.executeTransaction(r -> {
                    // Buat objek Booking
                    bookingId = generateBookingId();
                    Booking booking = r.createObject(Booking.class, bookingId);
                    booking.setUserEmail(email);
                    booking.setMallName(mall.getName());
                    booking.setMallAddress(mall.getAddress());
                    booking.setSlot(tvSelectedSlot.getText().toString());
                    booking.setSlotId(selectedSlotId);
                    booking.setPlate(sprPlate.getSelectedItem().toString());
                    booking.setJamMasuk(edtTglMsk.getText().toString() + " " + edtJamMsk.getText().toString());
                    booking.setJamKeluar(edtTglKlr.getText().toString() + " " + edtJamKlr.getText().toString());
                    booking.setDurasiMenit(durasiMenitTerakhir);
                    booking.setTotalHarga(totalHargaTerakhir);
                    booking.setMetodePembayaran("Wallet");
                    booking.setExpired(false);

                    SlotParkir slot = r.where(SlotParkir.class)
                            .equalTo("slotId", selectedSlotId)
                            .findFirst();

                    if (slot != null) {
                        slot.setBooked(true);
                    }
                });

                int saldoBaru = saldoSekarang - totalHargaTerakhir;
                prefs.edit().putInt("balance", saldoBaru).apply();
                session.setSaldo(saldoBaru);

                toPaymentSuccess();
            }
        });

        TextView txvTerms = findViewById(R.id.txvTerms);
        String fullText = "By paying, you agree to Parkeer’s Terms & Conditions";
        SpannableString spannable = new SpannableString(fullText);


        int start = fullText.indexOf("Parkeer’s Terms & Conditions");
        int end = start + "Parkeer’s Terms & Conditions".length();
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#001F54")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        txvTerms.setText(spannable);


        sprPlate = findViewById(R.id.sprPlate);

        RealmResults<Vehicle> vehicleList = realm.where(Vehicle.class).findAll();
        List<String> plateNumbers = new ArrayList<>();
        for (Vehicle v : vehicleList) {
            plateNumbers.add(v.getPlate());
        }

        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, plateNumbers);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprPlate.setAdapter(vehicleAdapter);


        String email = session.getEmail();

        vehicleList = realm.where(Vehicle.class)
                .equalTo("ownerEmail", email)
                .findAll();

        List<String> plates = new java.util.ArrayList<>();
        for (Vehicle v : vehicleList) {
            plates.add(v.getPlate());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                plates
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprPlate.setAdapter(adapter);

    }

    private void updateTotalHarga() {
        String tglMskStr = edtTglMsk.getText().toString();
        String jamMskStr = edtJamMsk.getText().toString();
        String tglKlrStr = edtTglKlr.getText().toString();
        String jamKlrStr = edtJamKlr.getText().toString();

        if (tglMskStr.isEmpty() || jamMskStr.isEmpty() || tglKlrStr.isEmpty() || jamKlrStr.isEmpty()) {
            return;
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);

            Date dateMasuk = format.parse(tglMskStr + " " + jamMskStr);
            Date dateKeluar = format.parse(tglKlrStr + " " + jamKlrStr);

            if (dateKeluar.before(dateMasuk)) {
                btnBook.setText("ERROR: Jam keluar < masuk");
                btnBook.setEnabled(false); // Disable tombol
                return;
            } else {
                btnBook.setEnabled(true);
            }

            long durasiMillis = dateKeluar.getTime() - dateMasuk.getTime();
            long durasiMenit = TimeUnit.MILLISECONDS.toMinutes(durasiMillis);
            long durasiJam = (long) Math.ceil(durasiMenit / 60.0); // bulatkan ke atas
            if (durasiJam == 0) durasiJam = 1; // minimal 1 jam

            Mall selectedMall = realm.where(Mall.class).equalTo("id", mallId).findFirst();
            if (selectedMall != null) {
                int hargaPerJam = selectedMall.getPricePerHour();
                durasiMenitTerakhir = durasiJam * 60;
                totalHargaTerakhir = (int) (durasiJam * hargaPerJam);

                btnBook.setText("BOOK IDR " + String.format("%,d", totalHargaTerakhir).replace(",", "."));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

        private void loadParkingSlots(int mallId) {
        // Cek booking yang expired dan bebaskan slotnya
            realm.executeTransaction(r -> {
                List<String> expiredSlotIds = new ArrayList<>();

                RealmResults<Booking> bookings = r.where(Booking.class)
                        .equalTo("mallName", mall.getName())
                        .equalTo("expired", false)
                        .findAll();

                // 1: Tandai booking yang sudah expired
                for (Booking b : bookings) {
                    if (DateTimeUtil.isExpired(b.getJamKeluar())) {
                        b.setExpired(true);
                        expiredSlotIds.add(b.getSlotId());
                        System.out.println("📛 Booking expired & slot akan dibebaskan: " + b.getBookingId());
                    }
                }

                // 2: Bebaskan slot yang tidak dipakai booking aktif lain
                for (String slotId : expiredSlotIds) {
                    long aktifLain = r.where(Booking.class)
                            .equalTo("slotId", slotId)
                            .equalTo("expired", false)
                            .count();

                    if (aktifLain == 0) {
                        SlotParkir slot = r.where(SlotParkir.class)
                                .equalTo("slotId", slotId)
                                .findFirst();
                        if (slot != null) {
                            slot.setBooked(false);
                            System.out.println("✅ Slot dibebaskan: " + slot.getSlotName());
                        }
                    } else {
                        System.out.println("⛔ Slot " + slotId + " masih dipakai booking aktif lain.");
                    }
                }
            });

            List<SlotParkir> slotList = realm.copyFromRealm(
                    realm.where(SlotParkir.class)
                            .equalTo("mallId", mallId)
                            .findAll()
            );

        for (SlotParkir slot : slotList) {
            System.out.println("Slot tersedia: " + slot.getSlotName() + ", Booked? " + slot.isBooked());
        }

        gridSlotContainer.removeAllViews();

        for (SlotParkir slot : slotList) {
            TextView slotView = new TextView(this);
            String slotName = slot.getSlotName() != null ? slot.getSlotName() : "N/A";
            slotView.setText(slot.isBooked() ? "BOOKED" : slotName);
            slotView.setTextSize(18);
            slotView.setGravity(Gravity.CENTER);
            slotView.setPadding(8, 8, 8, 8);
            slotView.setWidth(dpToPx(80));
            slotView.setHeight(dpToPx(80));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(20, 20, 20, 20);
            slotView.setLayoutParams(params);

            if (slot.isBooked()) {
                slotView.setBackgroundResource(R.drawable.bg_slot_booked);
                slotView.setTextColor(Color.parseColor("#D32F2F"));
                slotView.setEnabled(false);
            } else {
                slotView.setBackgroundResource(R.drawable.bg_slot_available);
                slotView.setTextColor(Color.parseColor("#00C853"));
                slotView.setEnabled(true);

                slotView.setOnClickListener(v -> {
                    for (int i = 0; i < gridSlotContainer.getChildCount(); i++) {
                        View child = gridSlotContainer.getChildAt(i);
                        if (child instanceof TextView && child.isEnabled()) {
                            child.setBackgroundResource(R.drawable.bg_slot_available);
                            ((TextView) child).setTextColor(Color.parseColor("#00C853"));
                        }
                    }

                    slotView.setBackgroundResource(R.drawable.bg_slot_booked);
                    slotView.setTextColor(Color.parseColor("#D32F2F"));
                    selectedSlotId = slot.getSlotId();
                    tvSelectedSlot.setText(slot.getSlotName());
                });
            }

            gridSlotContainer.addView(slotView);
        }

    }

    private class SimpleTextWatcher implements android.text.TextWatcher {
        private Runnable onChange;

        public SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChange.run();
        }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

    public void toPaymentSuccess(){
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra("bookingId", bookingId);
        intent.putExtra("mallName", mall.getName());
        intent.putExtra("mallAddress", mall.getAddress());
        intent.putExtra("slot", tvSelectedSlot.getText().toString());
        intent.putExtra("plate", sprPlate.getSelectedItem().toString());
        intent.putExtra("jamMasuk", edtTglMsk.getText().toString() + " " + edtJamMsk.getText().toString());
        intent.putExtra("jamKeluar", edtTglKlr.getText().toString() + " " + edtJamKlr.getText().toString());
        intent.putExtra("durasiMenit", durasiMenitTerakhir);
        intent.putExtra("totalHarga", totalHargaTerakhir);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadParkingSlots(mallId);
    }




}