package com.example.parkeeriotapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddVehicleActivity extends AppCompatActivity {

    Spinner sprBrand, sprModel, sprYear, sprColor;
    ImageView imvLeftArrow;
    private Button btnAddVehicle;
    private EditText edtPlate;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private static final char[] ILLEGAL_DOC_CHARS = {'/', '#', '[', ']', '?'};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_vehicle);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());

        sprBrand = findViewById(R.id.sprBrand);
        sprColor = findViewById(R.id.sprColor);
        sprModel = findViewById(R.id.sprModel);
        sprYear = findViewById(R.id.sprYear);
        edtPlate = findViewById(R.id.edtPlate);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);

        sprBrand.setAdapter(createHintedAdapter(R.array.brand_array));
        sprModel.setAdapter(createHintedAdapter(R.array.model_array));
        sprYear.setAdapter(createHintedAdapter(R.array.year_array));
        sprColor.setAdapter(createHintedAdapter(R.array.color_array));

        btnAddVehicle.setOnClickListener(v -> onAddVehicleClicked());
    }

    private void onAddVehicleClicked() {
        String plateOriginal = edtPlate.getText().toString().trim();
        String plateId = plateOriginal.replaceAll("\\s+", "").toUpperCase(); // 🔹 versi tanpa spasi

        int brandPos = sprBrand.getSelectedItemPosition();
        int modelPos = sprModel.getSelectedItemPosition();
        int yearPos = sprYear.getSelectedItemPosition();
        int colorPos = sprColor.getSelectedItemPosition();

        if (plateOriginal.isEmpty() || brandPos == 0 || modelPos == 0 || yearPos == 0 || colorPos == 0) {
            Toast.makeText(this, "Lengkapi semua data kendaraan", Toast.LENGTH_SHORT).show();
            return;
        }

        if (containsIllegalDocChar(plateId)) {
            Toast.makeText(this, "Nomor plat mengandung karakter tidak valid: / # [ ] ?", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddVehicle.setEnabled(false);
        btnAddVehicle.setText("Processing...");

        String uid = user.getUid();
        String brand = sprBrand.getSelectedItem().toString();
        String model = sprModel.getSelectedItem().toString();
        String year = sprYear.getSelectedItem().toString();
        String color = sprColor.getSelectedItem().toString();

        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("plateId", plateId); // 🔹 untuk identifikasi unik
        vehicleData.put("plate", plateOriginal); // 🔹 untuk ditampilkan di UI
        vehicleData.put("brand", brand);
        vehicleData.put("model", model);
        vehicleData.put("year", year);
        vehicleData.put("color", color);
        vehicleData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .collection("vehicles")
                .document(plateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "Plat " + plateOriginal + " sudah terdaftar.", Toast.LENGTH_LONG).show();
                        btnAddVehicle.setEnabled(true);
                        btnAddVehicle.setText("ADD VEHICLE");
                    } else {
                        db.collection("users")
                                .document(uid)
                                .collection("vehicles")
                                .document(plateId)
                                .set(vehicleData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Kendaraan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal tambah kendaraan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    btnAddVehicle.setEnabled(true);
                                    btnAddVehicle.setText("ADD VEHICLE");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memeriksa plat: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnAddVehicle.setEnabled(true);
                    btnAddVehicle.setText("ADD VEHICLE");
                });
    }

    private boolean containsIllegalDocChar(String s) {
        for (char c : ILLEGAL_DOC_CHARS) {
            if (s.indexOf(c) >= 0) return true;
        }
        return false;
    }

    private ArrayAdapter<CharSequence> createHintedAdapter(int arrayResId) {
        return new ArrayAdapter<CharSequence>(this, R.layout.spinner_item, getResources().getTextArray(arrayResId)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
    }
}