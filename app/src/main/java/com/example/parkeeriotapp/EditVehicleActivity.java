package com.example.parkeeriotapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditVehicleActivity extends AppCompatActivity {

    private EditText edtPlate;
    private Spinner sprBrand, sprModel, sprYear, sprColor;
    private Button btnSave;
    private TextView txvDelete;
    private ImageView imvLeftArrow;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private DocumentReference vehicleRef;
    private String uid, plateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_vehicle);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔧 Init Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔧 Init View
        edtPlate = findViewById(R.id.edtPlate);
        sprBrand = findViewById(R.id.sprBrand);
        sprModel = findViewById(R.id.sprModel);
        sprYear = findViewById(R.id.sprYear);
        sprColor = findViewById(R.id.sprColor);
        btnSave = findViewById(R.id.btnSaveVehicle);
        txvDelete = findViewById(R.id.txvDeleteVehicle);
        imvLeftArrow = findViewById(R.id.imvLeftArrow);

        // 🔙 Back button
        imvLeftArrow.setOnClickListener(v -> finish());

        // Spinner
        sprBrand.setAdapter(createHintedAdapter(R.array.brand_array));
        sprModel.setAdapter(createHintedAdapter(R.array.model_array));
        sprYear.setAdapter(createHintedAdapter(R.array.year_array));
        sprColor.setAdapter(createHintedAdapter(R.array.color_array));

        // 🔹 Ambil plate dari Intent (dikirim dari VehicleAdapter)
        String plate = getIntent().getStringExtra("plate");
        if (plate == null || plate.trim().isEmpty()) {
            Toast.makeText(this, "Data kendaraan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        plateId = plate.replaceAll("\\s+", "").toUpperCase();
        uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "Silakan login ulang", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        vehicleRef = db.collection("users").document(uid)
                .collection("vehicles").document(plateId);

        edtPlate.setText(plate);
        edtPlate.setEnabled(false);

        loadVehicle();

        btnSave.setOnClickListener(v -> saveVehicle());
        txvDelete.setOnClickListener(v -> deleteVehicle());
    }

    // ==================== 🔹 LOAD DATA 🔹 ====================
    private void loadVehicle() {
        vehicleRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        setSpinnerSelection(sprBrand, snapshot.getString("brand"));
                        setSpinnerSelection(sprModel, snapshot.getString("model"));
                        setSpinnerSelection(sprYear, snapshot.getString("year"));
                        setSpinnerSelection(sprColor, snapshot.getString("color"));
                    } else {
                        Toast.makeText(this, "Data kendaraan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ==================== 🔹 SAVE (UPDATE) 🔹 ====================
    private void saveVehicle() {
        int brandPos = sprBrand.getSelectedItemPosition();
        int modelPos = sprModel.getSelectedItemPosition();
        int yearPos = sprYear.getSelectedItemPosition();
        int colorPos = sprColor.getSelectedItemPosition();

        if (brandPos == 0 || modelPos == 0 || yearPos == 0 || colorPos == 0) {
            Toast.makeText(this, "Lengkapi semua data kendaraan", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("brand", sprBrand.getSelectedItem().toString());
        updates.put("model", sprModel.getSelectedItem().toString());
        updates.put("year", sprYear.getSelectedItem().toString());
        updates.put("color", sprColor.getSelectedItem().toString());

        vehicleRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Kendaraan berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("SAVE");
                });
    }

    // ==================== 🔹 DELETE 🔹 ====================
    private void deleteVehicle() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Hapus Kendaraan")
                .setMessage("Apakah Anda yakin ingin menghapus kendaraan ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    vehicleRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Kendaraan berhasil dihapus", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Gagal hapus: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ==================== 🔹 UTIL 🔹 ====================
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equalsIgnoreCase(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private ArrayAdapter<CharSequence> createHintedAdapter(int arrayResId) {
        return new ArrayAdapter<CharSequence>(this, R.layout.spinner_item, getResources().getTextArray(arrayResId)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, ViewGroup parent) {
                android.view.View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
    }
}