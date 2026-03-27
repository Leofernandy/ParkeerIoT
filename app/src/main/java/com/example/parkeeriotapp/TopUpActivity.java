package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class TopUpActivity extends AppCompatActivity {

    private EditText edtAmount;
    private Button btnContinue;
    private RequestQueue requestQueue;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);

        edtAmount = findViewById(R.id.edtAmount);
        btnContinue = findViewById(R.id.btnContinueTopUp);
        requestQueue = Volley.newRequestQueue(this);
        auth = FirebaseAuth.getInstance();

        // Tombol Cepat
        findViewById(R.id.btn10k).setOnClickListener(v -> edtAmount.setText("10000"));
        findViewById(R.id.btn20k).setOnClickListener(v -> edtAmount.setText("20000"));
        findViewById(R.id.btn50k).setOnClickListener(v -> edtAmount.setText("50000"));

        btnContinue.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            if (amountStr.isEmpty() || Integer.parseInt(amountStr) < 10000) {
                Toast.makeText(this, "Minimal Top Up Rp 10.000", Toast.LENGTH_SHORT).show();
                return;
            }
            generateTopUpInvoice(Integer.parseInt(amountStr));
        });
    }

    private void generateTopUpInvoice(int amount) {
        btnContinue.setEnabled(false);
        String uid = auth.getCurrentUser().getUid();
        // FORMAT ID: TU-UID-TIMESTAMP (Penting buat Cloud Functions!)
        String topUpId = "TU-" + uid + "-" + System.currentTimeMillis();

        String url = "https://createinvoice-d2jn6f3etq-uc.a.run.app";
        JSONObject params = new JSONObject();
        try {
            params.put("order_id", topUpId);
            params.put("amount", amount);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, params,
                res -> {
                    try {
                        String invoiceUrl = res.getString("invoice_url");

                        // Buka WebView Payment yang sudah kamu punya sebelumnya
                        Intent intent = new Intent(TopUpActivity.this, PaymentWebActivity.class);
                        intent.putExtra("invoiceUrl", invoiceUrl);
                        intent.putExtra("bookingId", topUpId); // Pakai key yang sama agar WebView bisa pantau status
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                err -> {
                    btnContinue.setEnabled(true);
                    Toast.makeText(this, "Gagal membuat invoice", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(req);
    }
}