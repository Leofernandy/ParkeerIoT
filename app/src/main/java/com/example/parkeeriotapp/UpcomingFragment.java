package com.example.parkeeriotapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.parkeeriotapp.model.Booking;
import com.example.parkeeriotapp.model.SlotParkir;
import com.example.parkeeriotapp.utils.DateTimeUtil;
import com.example.parkeeriotapp.utils.UserSessionManager;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UpcomingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpcomingFragment extends Fragment {
    private ListView listView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UpcomingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpcomingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpcomingFragment newInstance(String param1, String param2) {
        UpcomingFragment fragment = new UpcomingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);
        listView = view.findViewById(R.id.listUpcoming);

        Realm realm = Realm.getDefaultInstance();
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String currentUserEmail = sessionManager.getEmail();

        // Ambil semua booking user
        RealmResults<Booking> allBookings = realm.where(Booking.class)
                .equalTo("userEmail", currentUserEmail)
                .findAll();

        // Update expired status jika sudah lewat jamKeluar
        realm.executeTransaction(r -> {
            for (Booking b : allBookings) {
                if (!b.isExpired()) {
                    String keluar = b.getJamKeluar(); // format "yyyy-MM-dd HH:mm"
                    if (DateTimeUtil.isExpired(keluar)) {
                        b.setExpired(true); // update jadi expired

                        SlotParkir slot = r.where(SlotParkir.class)
                                .equalTo("slotId", b.getSlotId())
                                .findFirst();
                        if (slot != null) {
                            slot.setBooked(false);
                        }
                    }
                }
            }
        });

        RealmResults<Booking> validBookings = realm.where(Booking.class)
                .equalTo("userEmail", currentUserEmail)
                .equalTo("expired", false)
                .findAll();

        List<Booking> bookingList = realm.copyFromRealm(validBookings);
        UpcomingAdapter adapter = new UpcomingAdapter(requireContext(), bookingList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUpcoming();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Realm.getDefaultInstance().close();
    }

    private void loadUpcoming() {
        Realm realm = Realm.getDefaultInstance();
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String currentUserEmail = sessionManager.getEmail();

        RealmResults<Booking> allBookings = realm.where(Booking.class)
                .equalTo("userEmail", currentUserEmail)
                .findAll();

        realm.executeTransaction(r -> {
            for (Booking b : allBookings) {
                if (!b.isExpired()) {
                    String keluar = b.getJamKeluar();
                    if (DateTimeUtil.isExpired(keluar)) {
                        b.setExpired(true);

                        SlotParkir slot = r.where(SlotParkir.class)
                                .equalTo("slotId", b.getSlotId())
                                .findFirst();
                        if (slot != null) {
                            slot.setBooked(false);
                        }

                    }


                }
            }
        });

        RealmResults<Booking> validBookings = realm.where(Booking.class)
                .equalTo("userEmail", currentUserEmail)
                .equalTo("expired", false)
                .findAll();

        List<Booking> bookingList = realm.copyFromRealm(validBookings);
        UpcomingAdapter adapter = new UpcomingAdapter(requireContext(), bookingList);
        listView.setAdapter(adapter);

        realm.close();
    }


}