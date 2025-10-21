package com.example.parkeeriotapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkeeriotapp.utils.UserSessionManager;

public class WalletFragment extends Fragment {

    private TextView txvSaldo, txvPhone;
    private ImageView imvTopup;

    SharedPreferences prefs;

    public WalletFragment() {
    }

    public static WalletFragment newInstance(String param1, String param2) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
            );
        }

        txvSaldo = view.findViewById(R.id.txvSaldo);
        txvPhone = view.findViewById(R.id.txvPhone);
        imvTopup = view.findViewById(R.id.imvTopup);

        UserSessionManager session = new UserSessionManager(requireContext());
        String email = session.getEmail();
        prefs = requireContext().getSharedPreferences("wallet_" + email, Context.MODE_PRIVATE);

        String phone = session.getPhone();
        if (phone != null && phone.length() >= 10) {
            String masked = phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
            txvPhone.setText(masked);
        } else {
            txvPhone.setText(phone != null ? phone : "-");
        }

        updateBalance();

        imvTopup.setOnClickListener(v1 -> {
            int currentBalance = prefs.getInt("balance", 0);
            int newBalance = currentBalance + 10000;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("balance", newBalance);
            editor.apply();

            session.setSaldo(newBalance);

            updateBalance();
            Toast.makeText(requireContext(), "Top-up Rp10.000 berhasil", Toast.LENGTH_SHORT).show();
        });
    }


    private void updateBalance() {
        UserSessionManager session = new UserSessionManager(requireContext());
        int balance = session.getSaldo();
        txvSaldo.setText("IDR " + String.format("%,d", balance).replace(',', '.'));
    }
}
