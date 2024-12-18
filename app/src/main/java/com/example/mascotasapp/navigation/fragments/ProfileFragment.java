package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mascotasapp.R;
import com.example.mascotasapp.utils.MyPostAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileFragment extends Fragment {
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView username, email, noResults;
    ImageView photoUser;
    Uri photoUserUri;
    Map<String,Object> dataUser;
    Context context;

    List<Map<String,Object>> posts;

    public ProfileFragment(Map<String,Object> dataUser, Context context) {
        this.dataUser = dataUser;
        this.context = context;
    }
    ProgressBar loader;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.frag_profile_recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(context ,3));

        username = view.findViewById(R.id.frag_profile_username);
        email = view.findViewById(R.id.FragProfileEmail);
        photoUser = view.findViewById(R.id.frag_profile_image);
        noResults = view.findViewById(R.id.FragProfileText);
        loader = view.findViewById(R.id.frag_profile_progressbar);

        getUser();
        String userId = mAuth.getCurrentUser().getUid();
        getMyPosts(userId);
        return view;
    }

    public void getUser() {
        username.setText(dataUser.get("username").toString());
        photoUserUri = Uri.parse(dataUser.get("photoUrl").toString());
        Picasso.with(context)
                .load(photoUserUri)
                .resize(50, 50)
                .into(photoUser);
        String mail = mAuth.getCurrentUser().getEmail();
        email.setText(mail);

    }
    public void getMyPosts(String userId){
        String name = dataUser.get("username").toString();
        String uri = dataUser.get("photoUrl").toString();

        CollectionReference collections = db.collection("posts");
        Query query = collections.whereEqualTo("userId", userId);
        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> items = new ArrayList<>();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        Map<String, Object> item = new HashMap<>();

                            item.put("id", document.getId());
                            item.put("category", document.get("category"));
                            item.put("photoUrl", document.get("photoUrl"));
                            item.put("description", document.get("description"));
                            item.put("location", document.get("location"));
                            item.put("state", document.get("state"));
                            item.put("date", document.get("date"));
                            item.put("userId", document.get("userId"));
                            item.put("userPhotoUrl", uri);
                            item.put("username", name);

                            items.add(item);
                    }
                    loader.setVisibility(View.GONE);

                    this.posts = items;

                    loadMyPosts();

                })
                .addOnFailureListener(e ->{
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void loadMyPosts(){
        if(!this.posts.isEmpty()){
            recyclerView.setVisibility(View.VISIBLE);
            MyPostAdapter adapter = new MyPostAdapter(this.posts, context);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            noResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}

