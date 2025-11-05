package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private ListView listView;

    public HistoryFragment() { }

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        listView = view.findViewById(R.id.listHistory);

        loadDummyHistory();

        return view;
    }

    private void loadDummyHistory() {
        // Data dummy
        List<String> historyList = new ArrayList<>();
        historyList.add("Mall Central - B 1234 XYZ - 23 Oct 2025");
        historyList.add("Mall City - B 5678 ABC - 22 Oct 2025");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                historyList
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(requireContext(), "Klik item " + position, Toast.LENGTH_SHORT).show();

            // Contoh intent, bisa diganti sesuai kebutuhan
            Intent intent = new Intent(requireContext(), ScanQR.class);
            startActivity(intent);
        });
    }
}
