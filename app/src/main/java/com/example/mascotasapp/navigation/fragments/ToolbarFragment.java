package com.example.mascotasapp.navigation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mascotasapp.R;

import java.util.Map;

public class ToolbarFragment extends Fragment {
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