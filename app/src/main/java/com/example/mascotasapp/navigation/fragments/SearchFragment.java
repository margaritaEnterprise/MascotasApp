package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mascotasapp.R;

import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {
    RecyclerView recyclerView;
    List<Map<String, Object>> mapList;
    public SearchFragment() {
        /*        Map<String,Object> map = new HashMap<>();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.FragSearchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        return view;
    }
}
