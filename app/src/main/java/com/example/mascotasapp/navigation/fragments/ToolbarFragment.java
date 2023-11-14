package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.navigation.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class ToolbarFragment extends Fragment {
    Uri uriUserPhoto;
    ImageView userPhoto;//frag_setting_user_image
    Boolean withArrow;
    Activity context;
    CardView back;
    Toolbar toolbar;

    public ToolbarFragment(Uri uriUserPhoto, Boolean withArrow, Activity context) {
        this.uriUserPhoto = uriUserPhoto;
        this.withArrow = withArrow;
        this.context = context;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toolbar, container, false);
        toolbar = view.findViewById(R.id.idtoolbar);
        back = view.findViewById(R.id.FragToolbarArrow);
        userPhoto = view.findViewById(R.id.FragToolbarPhoto);
        setDataUser();

        if (withArrow) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
            ActionBar actionBar =  ((AppCompatActivity)getActivity()).getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(e -> {
                ((AppCompatActivity)getActivity()).onBackPressed();
            });
        }
        return view;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setDataUser(){
        Picasso.with(requireContext())
                .load(uriUserPhoto)
                .resize(200, 200)
                .into(userPhoto);
    }
}