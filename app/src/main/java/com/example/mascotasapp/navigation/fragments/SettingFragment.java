package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.R;
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
    EditText pass1, pass2;//frag_setting_pass2
    Button save, logout;//frag_setting_save, frag_setting_logout
    Chip selectedLanguage, selectedTheme;

    FirebaseAuth mAuth;
    SharedPreferences userPreferences;
    SharedPreferences.Editor editor;
    Context context;
    Map<String, Object> dataUser;
    Map<String, Object> userPrefMap;

    public SettingFragment(Map<String, Object> dataUser, SharedPreferences userPreferences, Map<String, Object> userPrefMap, Context context) {
        this.dataUser = dataUser;
        this.context = context;
        this.userPreferences = userPreferences;
        this.userPrefMap = userPrefMap;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        mAuth = FirebaseAuth.getInstance();
        editor =  userPreferences.edit();

        userPhoto = view.findViewById(R.id.frag_setting_user_image);
        username = view.findViewById(R.id.frag_setting_username);
        languages = view.findViewById(R.id.frag_setting_lan_chipgroup);
        themes = view.findViewById(R.id.frag_setting_theme_chipgroup);
        pass1 = view.findViewById(R.id.frag_setting_pass1);
        pass2 = view.findViewById(R.id.frag_setting_pass2);
        save = view.findViewById(R.id.frag_setting_save);
        logout = view.findViewById(R.id.frag_setting_logout);

        logout.setOnClickListener(v -> logOut());
        save.setOnClickListener(v -> savePreferences());

        setData(); //getPreferences --> setData()
        return view;
    }
    public void savePreferences() {
        //Agarro selectedLanguaje y selectedTheme
        //Valido las contrasenas
        //Guardo en sharedPreference

        //editor.putString(KEY, "prueba");
        //editor.putBoolean("UnBool", true);
        //editor.commit();
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