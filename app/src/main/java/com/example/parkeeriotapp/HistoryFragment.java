package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.parkeeriotapp.model.Booking;
import com.example.parkeeriotapp.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class HistoryFragment extends Fragment {

    private ListView listView;
    private Realm realm;
    private UserSessionManager session;

    public HistoryFragment() {
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        session = new UserSessionManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        listView = view.findViewById(R.id.listHistory);

        loadHistoryFromRealm();

        return view;
    }

    private void loadHistoryFromRealm() {
        String email = session.getEmail();
        RealmResults<Booking> results = realm.where(Booking.class)
                .equalTo("userEmail", email)
                .equalTo("expired", true) // hanya booking yang sudah expired
                .sort("jamMasuk", io.realm.Sort.DESCENDING)
                .findAll();


        List<Booking> bookingList = new ArrayList<>(results); // convert ke list biasa

        HistoryAdapter adapter = new HistoryAdapter(requireContext(), bookingList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Booking selected = bookingList.get(position);

            Intent intent = new Intent(requireContext(), BookDetailsActivity.class);
            intent.putExtra("readonly", true);
            intent.putExtra("bookingId", selected.getBookingId());
            intent.putExtra("mallName", selected.getMallName());
            intent.putExtra("mallAddress", selected.getMallAddress());
            intent.putExtra("slot", selected.getSlot());
            intent.putExtra("plate", selected.getPlate());
            intent.putExtra("jamMasuk", selected.getJamMasuk());
            intent.putExtra("jamKeluar", selected.getJamKeluar());
            intent.putExtra("durasiMenit", selected.getDurasiMenit());
            intent.putExtra("totalHarga", selected.getTotalHarga());

            startActivity(intent);
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
