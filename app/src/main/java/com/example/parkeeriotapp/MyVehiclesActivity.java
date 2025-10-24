package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MyVehiclesActivity extends AppCompatActivity {

    ImageView btnAdd, imvLeftArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_vehicles);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Tombol kembali
        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());

        // Tombol tambah kendaraan
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> toAddVehicles());

        // Sementara, kita **kosongkan ListView** biar app bisa jalan
        // ListView listView = findViewById(R.id.listVehicles);
        // Tidak ada adapter, tidak ada data
    }

    public void toAddVehicles() {
        startActivity(new Intent(this, AddVehicleActivity.class));
    }
}
