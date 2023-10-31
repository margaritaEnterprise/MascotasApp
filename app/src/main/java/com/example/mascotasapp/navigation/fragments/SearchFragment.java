package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mascotasapp.R;
import com.example.mascotasapp.utils.PostAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchFragment extends Fragment {
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    List<Map<String, Object>> mapList;
    public SearchFragment() {
        /*
        Map<String,Object> map = new HashMap<>();
        map.put("category", selectedChip.getTag().toString());
        map.put("photoUrl", uriPhoto);
        map.put("description", descriptionEditText.getText().toString());
        map.put("location", geoPoint);
        map.put("state", true);
        map.put("date", new Timestamp(new Date()));
        map.put("userId", mAuth.getCurrentUser().getUid());
        db.collection("posts")
        */
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.FragSearchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        firebaseGetPublis();
        return view;
    }

    public void firebaseGetPublis(){
        CollectionReference collections = db.collection("posts");
        String userId = mAuth.getCurrentUser().getUid();
        Set<String> userIds = new HashSet<>();
        collections.whereNotEqualTo("userId", userId)
                .orderBy("userId", Query.Direction.ASCENDING)
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

                        userIds.add((String) document.get("userId"));
                        items.add(item);
                    }
                    mapList = items;
                    List<String> idsList = new ArrayList<>(userIds);
                    searchUsers(idsList);
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(requireContext(), "xd", Toast.LENGTH_SHORT).show();
                });
    }

    public  void searchUsers(List<String> ids){
        CollectionReference collections = db.collection("users");

        collections.whereIn("id", ids)
                .orderBy("id", Query.Direction.ASCENDING)
                .get().
                addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("photoUrl",document.getString("photoUrl"));
                        user.put("id",document.getString("id"));
                        user.put("username",document.getString("username"));
                        users.add(user);
                    }
                    for (Map<String, Object> map : mapList) {
                        for (Map<String, Object> user : users){
                            if(map.get("userId").equals(user.get("id"))){
                                map.put("userPhotoUrl", user.get("photoUrl"));
                                map.put("username",user.get("username"));
                            }
                        }
                    }
                    finishGet();
                })
                .addOnFailureListener(e -> {
                    // Maneja errores aquí, si es necesario.
                });

    }

    public void finishGet(){
        PostAdapter postAdapter = new PostAdapter(mapList, requireContext());
        recyclerView.setAdapter(postAdapter);
    }
}
