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

import com.example.parkeeriotapp.utils.UserSessionManager;

public class AddVehicleActivity extends AppCompatActivity {

    Spinner sprBrand, sprModel , sprYear, sprColor;
    ImageView imvLeftArrow;

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

        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(v -> finish());

        sprBrand = findViewById(R.id.sprBrand);
        sprColor = findViewById(R.id.sprColor);
        sprModel = findViewById(R.id.sprModel);
        sprYear = findViewById(R.id.sprYear);

        // Spinner dengan hint
        sprBrand.setAdapter(createHintedAdapter(R.array.brand_array));
        sprModel.setAdapter(createHintedAdapter(R.array.model_array));
        sprYear.setAdapter(createHintedAdapter(R.array.year_array));
        sprColor.setAdapter(createHintedAdapter(R.array.color_array));

        Button btnAddVehicle = findViewById(R.id.btnAddVehicle);
        btnAddVehicle.setOnClickListener(v -> {
            String plate = ((EditText)findViewById(R.id.edtPlate)).getText().toString().trim();
            int brandPos = sprBrand.getSelectedItemPosition();
            int modelPos = sprModel.getSelectedItemPosition();
            int yearPos = sprYear.getSelectedItemPosition();
            int colorPos = sprColor.getSelectedItemPosition();

            if (plate.isEmpty() || brandPos == 0 || modelPos == 0 || yearPos == 0 || colorPos == 0) {
                Toast.makeText(this, "Lengkapi semua data kendaraan", Toast.LENGTH_SHORT).show();
                return;
            }

            String brand = sprBrand.getSelectedItem().toString();
            String model = sprModel.getSelectedItem().toString();
            String year = sprYear.getSelectedItem().toString();
            String color = sprColor.getSelectedItem().toString();

            UserSessionManager session = new UserSessionManager(this);
            String email = session.getEmail();

            // ===== Placeholder untuk Realm =====
            // Di sini sebelumnya ada kode pengecekan & penyimpanan ke Realm
            // Kamu bisa ganti nanti dengan penyimpanan ke database lain atau Firebase
            Toast.makeText(this, "Kendaraan berhasil ditambahkan (Realm dihapus)", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private ArrayAdapter<CharSequence> createHintedAdapter(int arrayResId) {
        return new ArrayAdapter<CharSequence>(this, R.layout.spinner_item, getResources().getTextArray(arrayResId)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // index 0 tidak bisa dipilih
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY); // warna hint
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
    }
}
