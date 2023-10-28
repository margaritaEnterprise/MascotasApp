package com.example.mascotasapp.navigation.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.mascotasapp.R;
import com.google.android.material.chip.Chip;

public class SettingFragment extends Fragment {
    ImageView image;
    Chip language;
    public SettingFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        // Asocio las variables con la UI
        image = view.findViewById(R.id.frag_setting_image);
        image.bringToFront();
        language = view.findViewById(R.id.frag_setting_language);

        return view;
    }
}