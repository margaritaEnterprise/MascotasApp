package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.PostActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.navigation.MainActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class SettingFragment extends Fragment {
    Uri uriUserPhoto;
    ImageView userPhoto;//frag_setting_user_image
    TextView username;//frag_setting_username, frag_setting_lan_chipgroup
    ChipGroup languages, themes; //frag_setting_theme_chipgroup, frag_setting_pass1
    Button save, logout;//frag_setting_save, frag_setting_logout
    Chip selectedLanguage, selectedTheme;

    FirebaseAuth mAuth;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editPref;
    Context activity;
    Map<String, Object> dataUser; //foto name
    Map<String, Object> userPrefMap; //las seteadas

    public SettingFragment(Map<String, Object> dataUser, SharedPreferences sharedPreference, Context activity) {
        this.dataUser = dataUser;
        this.activity = (Context) activity;
        this.sharedPreference = sharedPreference;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        mAuth = FirebaseAuth.getInstance();
        editPref =  sharedPreference.edit();
        userPrefMap = (Map<String, Object>) sharedPreference.getAll();

        userPhoto = view.findViewById(R.id.frag_setting_user_image);
        username = view.findViewById(R.id.frag_setting_username);
        languages = view.findViewById(R.id.frag_setting_lan_chipgroup);
        themes = view.findViewById(R.id.frag_setting_theme_chipgroup);
        save = view.findViewById(R.id.frag_setting_save);
        logout = view.findViewById(R.id.frag_setting_logout);

        logout.setOnClickListener(v -> logOut());
        save.setOnClickListener(v -> savePreferences());

        setData();
        return view;
    }
    public void savePreferences() {
        int chipLang = languages.getCheckedChipId();
        int chipThem = themes.getCheckedChipId();
        String setLang = languages.findViewById(chipLang).getTag().toString();
        String setTheme = themes.findViewById(chipThem).getTag().toString();
        if (setTheme != userPrefMap.get("theme") || setLang != userPrefMap.get("language")) {
            editPref.putString("theme", setTheme); //light, dark
            editPref.putString("language", setLang); //en, es, ch
            editPref.commit();
        }
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
    }

    public void setData(){
        //del user
        username.setText(dataUser.get("username").toString());
        uriUserPhoto = Uri.parse(dataUser.get("photoUrl").toString());
        Picasso.with(requireContext())
                .load(uriUserPhoto)
                .resize(200, 200)
                .into(userPhoto);
        //de las pref
        selectedLanguage = languages.findViewWithTag(userPrefMap.get("language").toString());
        selectedLanguage.setChecked(true);
        selectedTheme = themes.findViewWithTag(userPrefMap.get("theme").toString());
        selectedTheme.setChecked(true);
    }

    public void logOut(){
        mAuth.signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
    }
}