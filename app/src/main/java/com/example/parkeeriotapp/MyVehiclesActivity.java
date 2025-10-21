package com.example.parkeeriotapp;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.parkeeriotapp.model.Vehicle;
import com.example.parkeeriotapp.utils.UserSessionManager;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

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
        imvLeftArrow= findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());


        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAddVehicles();
            }
        });
        loadVehicleList();

        ListView listView = findViewById(R.id.listVehicles);

        Realm realm = Realm.getDefaultInstance();

        UserSessionManager session = new UserSessionManager(this);
        String email = session.getEmail();

        realm.executeTransaction(r -> {
            realm.where(Vehicle.class)
                    .isNull("ownerEmail")
                    .or()
                    .equalTo("ownerEmail", "")
                    .findAll()
                    .deleteAllFromRealm();
        });

        RealmResults<Vehicle> vehicles = realm.where(Vehicle.class)
                .equalTo("ownerEmail", email)
                .findAll();


        List<Vehicle> filteredList = realm.copyFromRealm(vehicles);

        com.example.parkeeriotapp.adapter.VehicleAdapter adapter = new com.example.parkeeriotapp.adapter.VehicleAdapter(this, filteredList);
        listView.setAdapter(adapter);


    }

        public void toAddVehicles(){
        Intent intent = new Intent(this, AddVehicleActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicleList();
    }

    private void loadVehicleList() {
        Realm realm = Realm.getDefaultInstance();

        UserSessionManager session = new UserSessionManager(this);
        String email = session.getEmail();

        RealmResults<Vehicle> vehicles = realm.where(Vehicle.class)
                .equalTo("ownerEmail", email)
                .findAll();

        List<Vehicle> filteredList = realm.copyFromRealm(vehicles);

        com.example.parkeeriotapp.adapter.VehicleAdapter adapter = new com.example.parkeeriotapp.adapter.VehicleAdapter(this, filteredList);
        ListView listView = findViewById(R.id.listVehicles);
        listView.setAdapter(adapter);

    }
}