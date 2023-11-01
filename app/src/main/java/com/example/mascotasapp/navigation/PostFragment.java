package com.example.mascotasapp.navigation;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mascotasapp.R;
import com.google.android.gms.maps.MapView;
import com.google.android.material.chip.Chip;
import com.squareup.picasso.Picasso;
import java.util.Map;

public class PostFragment extends Fragment {
    ImageView userPhoto, postPhoto;
    TextView username, description;
    Chip category;
    MapView map;

    Map<String, Object> post;
    public PostFragment(Map<String, Object> post) {
        this.post = post;
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_post, container, false);
        userPhoto = view.findViewById(R.id.FragPostImageUser);
        postPhoto = view.findViewById(R.id.FragPostImagePost);
        username = view.findViewById(R.id.FragPostUsername);
        description = view.findViewById(R.id.FragPostTextDesc);
        category  = view.findViewById(R.id.FragPostChipCategory);
        map = view.findViewById(R.id.FragPostMap);

        setData();

        return view;
    }

    public void setData(){

        username.setText((CharSequence) post.get("username"));
        description.setText((CharSequence) post.get("description"));
        category.setText((CharSequence)post.get("category"));

        Uri userPhotoUrl = Uri.parse((String) post.get("userPhotoUrl"));
        Picasso.with(requireContext())
                .load(userPhotoUrl)
                .resize(30, 30)
                .into(userPhoto);

        Uri photoUrl = Uri.parse((String) post.get("photoUrl"));
        Picasso.with(requireContext())
                .load(photoUrl)
                .resize(450, 450)
                .into(postPhoto);

    }
}