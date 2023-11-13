package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mascotasapp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class ToolbarFragment extends Fragment {
    Uri uriUserPhoto;
    ImageView userPhoto;//frag_setting_user_image
    Map<String, Object> dataUser; //foto name

    public ToolbarFragment(Map<String, Object> dataUser) {
        this.dataUser = dataUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toolbar, container, false);
        Toast.makeText(requireContext(), "tool", Toast.LENGTH_SHORT).show();
        return view;
    }
}