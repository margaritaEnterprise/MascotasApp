package com.example.mascotasapp.navigation.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mascotasapp.R;
import com.example.mascotasapp.utils.MyPostAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    public ProfileFragment() { }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.frag_profile_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        getMyPosts();

        return view;
    }

    public void getMyPosts(){
        CollectionReference collections = db.collection("posts");

        String userId = mAuth.getCurrentUser().getUid();
        Query query = collections.whereEqualTo("userId", userId);
        query.orderBy("date", Query.Direction.DESCENDING);

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

                            items.add(item);
                    }

                    if(!items.isEmpty()){
                        MyPostAdapter adapter = new MyPostAdapter(items, requireContext());
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(requireContext(), "No hay", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}

