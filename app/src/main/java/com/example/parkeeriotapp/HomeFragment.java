package com.example.parkeeriotapp;
import com.example.parkeeriotapp.utils.UserSessionManager;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.parkeeriotapp.model.Mall;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap gMap;
    ImageView imvToWallet;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        LatLng jakarta = new LatLng(-6.200000, 106.816666);
        gMap.addMarker(new MarkerOptions().position(jakarta).title("Jakarta"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jakarta, 12));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imvToWallet = view.findViewById(R.id.imvToWallet);
        imvToWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toWallet();
            }
        });

        TextView tvName = view.findViewById(R.id.textViewNama);
        TextView tvSaldo = view.findViewById(R.id.textViewSaldo);

        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String fullname = sessionManager.getFullname();

        if (fullname != null) {
            tvName.setText("Hi " + fullname);
        } else {
            tvName.setText("Hi Guest");
        }

        int saldo = sessionManager.getSaldo();
        String formattedSaldo = String.format("IDR %,d", saldo).replace(',', '.');
        tvSaldo.setText(formattedSaldo);

        SupportMapFragment mapFragment = new SupportMapFragment();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        ListView listView = view.findViewById(R.id.listViewMall);
        List<Mall> mallList = new ArrayList<>();

        mallList.add(new Mall(1, "Delipark Mall", "Jl. Putri Hijau No. 1, Kota Medan", "8.3 km", R.drawable.delipark, 8000 , 3.594806874103257, 98.67443958253722));
        mallList.add(new Mall(2, "Sun Plaza", "Jl. KH. Zainul Arifin No.7, Kota Medan", "10 km", R.drawable.sunplaza, 8000, 3.5835585221252932, 98.6719027052982));
        mallList.add(new Mall(3, "Centre Point", "Jl. Jawa No.8, Kota Medan", "9.9 km", R.drawable.centrepoint, 6000, 3.591730551642377, 98.68065295370103));
        mallList.add(new Mall(4, "Lippo Plaza", "Jl. Imam Bonjol No.6, Kota Medan", "9.3 km", R.drawable.lippoplaza, 5000, 3.5866011059040623, 98.67327799417457));
        mallList.add(new Mall(5, "Cambridge City", "Jl. S. Parman No.217, Kota Medan", "10 km", R.drawable.cambridge, 5000, 3.584928806179916, 98.66710095370091));
        mallList.add(new Mall(6, "Thamrin Plaza", "Jl. M.H Thamrin R No.75, Kota Medan", "9.1 km", R.drawable.thamrin, 4000, 3.5868645371450993, 98.69240612671868));

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(r -> {
            for (Mall mall : mallList) {
                Mall existingMall = r.where(Mall.class).equalTo("id", mall.getId()).findFirst();
                if (existingMall == null) {
                    r.insert(mall);
                }
            }

            for (Mall mall : mallList) {
                int mallId = mall.getId();
                boolean slotAlreadyExist = !r.where(com.example.parkeeriotapp.model.SlotParkir.class)
                        .equalTo("mallId", mallId)
                        .findAll()
                        .isEmpty();

                if (!slotAlreadyExist) {
                    for (int i = 1; i <= 8; i++) {
                        String slotName = "A-0" + i;
                        String slotId = slotName + "_" + mallId;

                        com.example.parkeeriotapp.model.SlotParkir slot = new com.example.parkeeriotapp.model.SlotParkir();
                        slot.setSlotId(slotId);
                        slot.setSlotName(slotName);
                        slot.setMallId(mallId);
                        slot.setBooked(false);
                        r.insert(slot);
                    }
                }
            }
        });
        realm.close();


        MallAdapter adapter = new MallAdapter(requireContext(), mallList);
        listView.setAdapter(adapter);

        EditText searchBar = view.findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());

                List<Mall> filteredList = adapter.getFilteredMallList();
                if (filteredList.size() == 1 && gMap != null) {
                    Mall mall = filteredList.get(0);
                    LatLng location = new LatLng(mall.getLatitude(), mall.getLongitude());
                    gMap.clear();
                    gMap.addMarker(new MarkerOptions().position(location).title(mall.getName()));
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                } else if (filteredList.isEmpty() && gMap != null) {
                    LatLng defaultLocation = new LatLng(-6.200000, 106.816666);
                    gMap.clear();
                    gMap.addMarker(new MarkerOptions().position(defaultLocation).title("Jakarta"));
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(requireContext(), R.color.navy)
            );
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        TextView tvSaldo = getView().findViewById(R.id.textViewSaldo);
        int saldo = new UserSessionManager(requireContext()).getSaldo();
        String formattedSaldo = String.format("IDR %,d", saldo).replace(',', '.');
        tvSaldo.setText(formattedSaldo);
    }

    public void toWallet(){
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new WalletFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}