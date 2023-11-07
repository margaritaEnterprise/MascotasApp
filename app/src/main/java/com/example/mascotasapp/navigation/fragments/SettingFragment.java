package com.example.mascotasapp.navigation.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.signup.SignUpActivity;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

public class SettingFragment extends Fragment {
    FirebaseAuth mAuth;
    ImageView image;
    Chip language;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button saveButton;
    Button signOutButton;

    private final String PREF_NAME = "userPref";
    private final int PRIVATE = 0;
    private final String KEY = "usuario";
    public SettingFragment() {}

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        //Auth
        mAuth = FirebaseAuth.getInstance();
        //Editor de shared preference
        sharedPreferences = getContext().getSharedPreferences(PREF_NAME, PRIVATE);
        editor =  sharedPreferences.edit();

        // Asocio las variables con la UI
        //image = view.findViewById(R.id.frag_setting_image);
        //image.bringToFront();
        //language = view.findViewById(R.id.frag_setting_language);

        saveButton = view.findViewById(R.id.frag_setting_save);

        /*signOutButton = view.findViewById(R.id.frag_setting_signOut);
        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
        });*/

        return view;
    }
    public void setPreferences() {
        editor.putString(KEY, "prueba");
        editor.putBoolean("UnBool", true);
        editor.commit();
    }
    public void getPreferences() {
        if(sharedPreferences.contains("UnBool")) {
            Toast.makeText(requireContext(), "contiene", Toast.LENGTH_SHORT).show();
        }
    }

}