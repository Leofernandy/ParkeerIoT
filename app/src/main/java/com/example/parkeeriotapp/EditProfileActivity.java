package com.example.parkeeriotapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullname, edtPhone, edtEmail;
    private Button btnSaveProfile;
    private ImageView imvLeftArrow, imageProfile, imvEditPhoto;
    private View flPhotoContainer;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    private String selectedBase64Image = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // === Init Firebase ===
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // === Inisialisasi View ===
        edtFullname = findViewById(R.id.edtFullname);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imageProfile = findViewById(R.id.imageProfile);
        imvEditPhoto = findViewById(R.id.imvEditPhoto);
        flPhotoContainer = findViewById(R.id.flPhotoContainer);

        // Inisialisasi Photo Picker (Pilih Gambar dari Galeri)
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processSelectedImage(uri);
                    }
                }
        );

        // Listener untuk Membuka Galeri
        View.OnClickListener pickPhotoListener = v -> imagePickerLauncher.launch("image/*");
        if (flPhotoContainer != null) {
            flPhotoContainer.setOnClickListener(pickPhotoListener);
        } else {
            imageProfile.setOnClickListener(pickPhotoListener);
            imvEditPhoto.setOnClickListener(pickPhotoListener);
        }

        // Tombol kembali
        imvLeftArrow.setOnClickListener(v -> finish());

        // Cek autentikasi user
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ambil UID user aktif
        String uid = auth.getCurrentUser().getUid();
        userRef = db.collection("users").document(uid);

        // Tampilkan data user
        loadProfile();

        // Tombol Simpan
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void processSelectedImage(Uri uri) {
        try {
            // 1. Baca EXIF Orientation agar foto tidak terputar 90 derajat
            int rotationDegrees = 0;
            try {
                InputStream exifStream = getContentResolver().openInputStream(uri);
                if (exifStream != null) {
                    android.media.ExifInterface exif = new android.media.ExifInterface(exifStream);
                    int orientation = exif.getAttributeInt(
                            android.media.ExifInterface.TAG_ORIENTATION,
                            android.media.ExifInterface.ORIENTATION_NORMAL
                    );
                    if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_90) {
                        rotationDegrees = 90;
                    } else if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_180) {
                        rotationDegrees = 180;
                    } else if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_270) {
                        rotationDegrees = 270;
                    }
                    exifStream.close();
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            // 2. Decode Stream ke Bitmap
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            if (originalBitmap == null) return;

            // 3. Putar Bitmap ke orientasi tegak yang benar jika perlu
            Bitmap rotatedBitmap = originalBitmap;
            if (rotationDegrees != 0) {
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.postRotate(rotationDegrees);
                rotatedBitmap = Bitmap.createBitmap(
                        originalBitmap,
                        0, 0,
                        originalBitmap.getWidth(), originalBitmap.getHeight(),
                        matrix,
                        true
                );
            }

            // 4. Center Crop ke Persegi 1:1 (Square)
            int minEdge = Math.min(rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
            int xOffset = (rotatedBitmap.getWidth() - minEdge) / 2;
            int yOffset = (rotatedBitmap.getHeight() - minEdge) / 2;
            Bitmap squareBitmap = Bitmap.createBitmap(rotatedBitmap, xOffset, yOffset, minEdge, minEdge);

            // 5. Resize ke 250x250 px (Ukuran optimal)
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(squareBitmap, 250, 250, true);

            // 6. Potong Menjadi Lingkaran Sempurna (Circular Crop)
            Bitmap circularBitmap = Bitmap.createBitmap(250, 250, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(circularBitmap);
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
            paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(125f, 125f, 125f, paint);
            paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint);

            // 7. Kompres ke PNG agar transparansi lingkaran terjaga
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // 8. Encode ke Base64
            selectedBase64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // 9. Tampilkan langsung di ImageView
            imageProfile.setImageBitmap(circularBitmap);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfile() {
        userRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        edtFullname.setText(snapshot.getString("fullname"));
                        edtPhone.setText(snapshot.getString("phone"));
                        edtEmail.setText(snapshot.getString("email"));

                        // Load foto profil jika ada
                        String photoBase64 = snapshot.getString("photoBase64");
                        if (photoBase64 != null && !photoBase64.isEmpty()) {
                            try {
                                byte[] decodedBytes = Base64.decode(photoBase64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                if (bitmap != null) {
                                    imageProfile.setImageBitmap(bitmap);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Email tidak dapat diubah
                        edtEmail.setEnabled(false);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                );
    }

    private void saveProfile() {
        String newFullname = edtFullname.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();

        if (newFullname.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Nama dan nomor HP wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPhone.length() < 8) {
            Toast.makeText(this, "Nomor HP terlalu pendek", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullname", newFullname);
        updates.put("phone", newPhone);

        // Jika user memilih foto baru, sertakan photoBase64
        if (selectedBase64Image != null) {
            updates.put("photoBase64", selectedBase64Image);
        }

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setText("SAVE CHANGES");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("SAVE CHANGES");
                });
    }
}