package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mascotasapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class ToolbarFragment extends Fragment {
    Uri uriUserPhoto;
    ImageView userPhoto;//frag_setting_user_image

    public ToolbarFragment(Uri uriUserPhoto) {
        this.uriUserPhoto = uriUserPhoto;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toolbar, container, false);

        userPhoto = view.findViewById(R.id.FragToolbarPhoto);
        setDataUser();
        return view;
    }
    public void setDataUser(){
        Picasso.with(requireContext())
                .load(uriUserPhoto)
                .resize(200, 200)
                .into(userPhoto);
    }
}