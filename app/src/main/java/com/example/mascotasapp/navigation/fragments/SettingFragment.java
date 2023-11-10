package com.example.mascotasapp.navigation.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.signup.SignUpActivity;
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
    Chip selectedLang;

    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;
    Map<String, Object> dataUser;

    private final String PREF_NAME = "userPref";
    private final int PRIVATE = 0;
    private final String KEY = "usuario";
    public SettingFragment(Map<String, Object> dataUser, Context context) {
        this.dataUser = dataUser;
        this.context = context;
    }

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
        userPhoto = view.findViewById(R.id.frag_setting_user_image);
        username = view.findViewById(R.id.frag_setting_username);
        languages = view.findViewById(R.id.frag_setting_lan_chipgroup);
        themes = view.findViewById(R.id.frag_setting_theme_chipgroup);
        pass1 = view.findViewById(R.id.frag_setting_pass1);
        pass2 = view.findViewById(R.id.frag_setting_pass2);
        save = view.findViewById(R.id.frag_setting_save);
        logout = view.findViewById(R.id.frag_setting_logout);


        logout.setOnClickListener(v -> logOut());

        setData();
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
    public void logOut(){
        mAuth.signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
    }
    public void setData(){
        username.setText(dataUser.get("username").toString());
        uriUserPhoto = Uri.parse(dataUser.get("photoUrl").toString());
        Picasso.with(requireContext())
                .load(uriUserPhoto)
                .resize(200, 200)
                .into(userPhoto);
        String cat = "eng";
        selectedLang = languages.findViewWithTag(cat);
        selectedLang.setChecked(true);
    }
}