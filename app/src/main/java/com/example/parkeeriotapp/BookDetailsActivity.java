package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.parkeeriotapp.model.Booking;
import com.example.parkeeriotapp.model.SlotParkir;
import com.example.parkeeriotapp.utils.UserSessionManager;

import io.realm.Realm;

public class BookDetailsActivity extends AppCompatActivity {
    ImageView imvLeftArrow;

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
        imvLeftArrow.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        String bookingId = intent.getStringExtra("bookingId");
        String mallName = intent.getStringExtra("mallName");
        String mallAddress = intent.getStringExtra("mallAddress");
        String slot = intent.getStringExtra("slot");
        String plate = intent.getStringExtra("plate");
        String jamMasuk = intent.getStringExtra("jamMasuk");
        String jamKeluar = intent.getStringExtra("jamKeluar");
        int totalHarga = intent.getIntExtra("totalHarga", 0);
        long durasiMenit = intent.getLongExtra("durasiMenit", 0);
        boolean readonly = intent.getBooleanExtra("readonly", false);
        boolean qrScanned = intent.getBooleanExtra("qrScanned", false);

        long hours = durasiMenit / 60;
        long minutes = durasiMenit % 60;
        String durasiFormatted = String.format("%02d hours %02d minutes", hours, minutes);

        ((TextView) findViewById(R.id.textViewBookingId)).setText(bookingId);
        ((TextView) findViewById(R.id.textViewMallName)).setText(mallName);
        ((TextView) findViewById(R.id.textViewSlot)).setText(slot);
        ((TextView) findViewById(R.id.textViewPlate)).setText(plate);
        ((TextView) findViewById(R.id.textViewMasuk)).setText(jamMasuk);
        ((TextView) findViewById(R.id.textViewKeluar)).setText(jamKeluar);
        ((TextView) findViewById(R.id.textViewDurasi)).setText(durasiFormatted);
        ((TextView) findViewById(R.id.textViewHarga)).setText("IDR " + String.format("%,d", totalHarga).replace(",", "."));

        TextView btnCancel = findViewById(R.id.btnCancel);
        ImageView qrCode = findViewById(R.id.qrCode);
        TextView btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);

        btnDownloadReceipt.setOnClickListener(v -> {
            Toast.makeText(this, "Receipt has been downloaded", Toast.LENGTH_SHORT).show();

            Intent navIntent = new Intent(this, MainActivity.class);
            navIntent.putExtra("navigateTo", "activity"); // flag untuk tab ActivityFragment
            navIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(navIntent);
            finish(); // tutup halaman saat ini
        });

        boolean isScanned = getSharedPreferences("qr_status", MODE_PRIVATE)
                .getBoolean("scanned_" + bookingId, false);

        if (readonly || qrScanned || isScanned) {
            btnCancel.setEnabled(false);
            btnCancel.setAlpha(0.4f);
        } else {
            btnCancel.setEnabled(true);
        }


        qrCode.setOnClickListener(v -> { // ⬅️ Tambahkan ini
            btnCancel.setEnabled(false);
            btnCancel.setAlpha(0.4f);

            // Simpan flag kalau QR telah discan untuk booking ini
            getSharedPreferences("qr_status", MODE_PRIVATE)
                    .edit()
                    .putBoolean("scanned_" + bookingId, true)
                    .apply();
        });

        btnCancel.setOnClickListener(v -> {
            if (qrScanned) return;

            Realm realm = Realm.getDefaultInstance();
            UserSessionManager session = new UserSessionManager(this);
            String userEmail = session.getEmail();

            realm.executeTransaction(r -> {
                Booking booking = r.where(Booking.class).equalTo("bookingId", bookingId).findFirst();
                if (booking != null) {
                    // Kembalikan slot jadi available
                    SlotParkir slotModel = r.where(SlotParkir.class)
                            .equalTo("slotId", booking.getSlotId())
                            .findFirst();
                    if (slotModel != null) {
                        slotModel.setBooked(false);
                    }

                    // Kembalikan saldo ke wallet
                    int oldBalance = getSharedPreferences("wallet_" + userEmail, MODE_PRIVATE)
                            .getInt("balance", 0);
                    int newBalance = oldBalance + booking.getTotalHarga();
                    getSharedPreferences("wallet_" + userEmail, MODE_PRIVATE)
                            .edit()
                            .putInt("balance", newBalance)
                            .apply();

                    session.setSaldo(newBalance); // update di session juga

                    // Hapus booking
                    booking.deleteFromRealm();
                    getSharedPreferences("qr_status", MODE_PRIVATE)
                            .edit()
                            .remove("scanned_" + bookingId)
                            .apply();
                    Toast.makeText(this, "Booking dibatalkan & saldo dikembalikan.", Toast.LENGTH_SHORT).show();
                }
            });

            realm.close();
            finish();
        });
    }

}
