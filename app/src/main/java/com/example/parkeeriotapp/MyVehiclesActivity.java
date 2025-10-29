package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.parkeeriotapp.VehicleAdapter;
import com.example.parkeeriotapp.model.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class MyVehiclesActivity extends AppCompatActivity {

    private ImageView btnAdd, imvLeftArrow;
    private ListView listVehicles;
    private TextView txvEmpty;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration vehicleListener;

    private List<Vehicle> vehicleList = new ArrayList<>();
    private VehicleAdapter adapter;

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

        // 🔧 Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔧 Init View
        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        btnAdd = findViewById(R.id.btnAdd);
        listVehicles = findViewById(R.id.listVehicles);
        txvEmpty = findViewById(R.id.txvEmpty);

        // 🔙 Tombol kembali
        imvLeftArrow.setOnClickListener(v -> finish());

        // ➕ Tombol tambah kendaraan
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddVehicleActivity.class)));

        // 🚘 Setup adapter
        adapter = new VehicleAdapter(this, vehicleList);
        listVehicles.setAdapter(adapter);

        // 🔥 Load kendaraan user dari Firestore
        loadVehicles();
    }

    private void loadVehicles() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        CollectionReference vehiclesRef = db.collection("users").document(uid).collection("vehicles");

        // Listener real-time
        vehicleListener = vehiclesRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Gagal memuat kendaraan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            vehicleList.clear();
            if (snapshots != null && !snapshots.isEmpty()) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Vehicle v = new Vehicle();
                    v.setPlate(doc.getString("plate"));
                    v.setBrand(doc.getString("brand"));
                    v.setModel(doc.getString("model"));
                    v.setYear(doc.getString("year"));
                    v.setColor(doc.getString("color"));
                    vehicleList.add(v);
                }
                txvEmpty.setVisibility(View.GONE);
            } else {
                txvEmpty.setVisibility(View.VISIBLE);
            }

            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vehicleListener != null) vehicleListener.remove();
    }
}