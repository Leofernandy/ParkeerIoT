package com.example.parkeeriotapp;

import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkeeriotapp.utils.UserSessionManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    ImageView imvEditProfile;
    LinearLayout llyMyVehicles, llyLogout , llyCS , llyHowUse , llyLanguage , llySetPin;

    TextView txvFullname, txvEmail, txvPhone;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imvEditProfile = view.findViewById(R.id.imvEditProfile);
        llyMyVehicles = view.findViewById(R.id.llyMyVehicles);
        llyCS = view.findViewById(R.id.llyCS);
        llySetPin = view.findViewById(R.id.llySetPin);
        llyLanguage = view.findViewById(R.id.llyLanguage);
        llyHowUse = view.findViewById(R.id.llyHowUse);
        llyLogout = view.findViewById(R.id.llyLogout);
        txvFullname = view.findViewById(R.id.txvFullname);
        txvEmail = view.findViewById(R.id.txvEmail);
        txvPhone = view.findViewById(R.id.txvPhone);



        loadProfileData();
        llyLogout.setOnClickListener(v -> logout());
        View.OnClickListener toastListener = v -> Toast.makeText(getContext(), "Fitur ini sedang dalam proses pengembangan!", Toast.LENGTH_SHORT).show();

        llySetPin.setOnClickListener(toastListener);
        llyLanguage.setOnClickListener(toastListener);
        llyHowUse.setOnClickListener(toastListener);
        llyCS.setOnClickListener(toastListener);

        UserSessionManager session = new UserSessionManager(requireContext());
        if (session.isLoggedIn()) {
            txvFullname.setText(session.getFullname());
            txvEmail.setText(session.getEmail());
            txvPhone.setText(session.getPhone());
        }

        imvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEditProfile();
            }
        });

        llyMyVehicles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toMyVehicles();
            }
        });



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(requireContext(), R.color.white) // warna default kamu
            );
        }
    }
    public void toEditProfile(){
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), edit_profile.class);
            startActivity(intent);
        }
    }

    public void toMyVehicles(){
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MyVehiclesActivity.class);
            startActivity(intent);
        }
    }

    private void loadProfileData() {
        UserSessionManager session = new UserSessionManager(requireContext());
        if (session.isLoggedIn()) {
            txvFullname.setText(session.getFullname());
            txvEmail.setText(session.getEmail());
            txvPhone.setText(session.getPhone());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void logout() {
        UserSessionManager session = new UserSessionManager(requireContext());
        session.clearSession();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finishAffinity();
    }



}