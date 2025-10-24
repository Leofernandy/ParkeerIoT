package com.example.parkeeriotapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;

public class EditVehicleActivity extends AppCompatActivity {

    EditText edtPlate;
    Spinner sprBrand, sprModel, sprYear, sprColor;
    Button btnSave;
    TextView txvDelete;
    // Realm sudah dihapus
    // Vehicle currentVehicle;

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

        edtPlate = findViewById(R.id.edtPlate);
        sprBrand = findViewById(R.id.sprBrand);
        sprModel = findViewById(R.id.sprModel);
        sprYear = findViewById(R.id.sprYear);
        sprColor = findViewById(R.id.sprColor);
        btnSave = findViewById(R.id.btnSaveVehicle);
        txvDelete = findViewById(R.id.txvDeleteVehicle);
        ImageView imvLeftArrow = findViewById(R.id.imvLeftArrow);

        imvLeftArrow.setOnClickListener(v -> finish());

        sprBrand.setAdapter(createHintedAdapter(R.array.brand_array));
        sprModel.setAdapter(createHintedAdapter(R.array.model_array));
        sprYear.setAdapter(createHintedAdapter(R.array.year_array));
        sprColor.setAdapter(createHintedAdapter(R.array.color_array));

        // Populate field sementara
        edtPlate.setText("AB123CD");
        edtPlate.setEnabled(false);
        sprBrand.setSelection(1);
        sprModel.setSelection(1);
        sprYear.setSelection(1);
        sprColor.setSelection(1);

        btnSave.setOnClickListener(v -> {
            // sementara hanya menampilkan Toast
            Toast.makeText(this, "Kendaraan berhasil diperbarui", Toast.LENGTH_SHORT).show();
        });

        txvDelete.setOnClickListener(v -> {
            Toast.makeText(this, "Kendaraan dihapus", Toast.LENGTH_SHORT).show();
        });
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
