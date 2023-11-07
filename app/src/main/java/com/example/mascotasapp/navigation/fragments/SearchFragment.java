package com.example.mascotasapp.navigation.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mascotasapp.R;
import com.example.mascotasapp.utils.PostAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {
    RecyclerView recyclerView;
    TextView noResults;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    List<Map<String, Object>> mapList;
    SearchView search;
    ChipGroup categories;
    SeekBar range;
    boolean seekBarChange = false;
    Switch state;
    Button btnSearch;
    GeoPoint geo;
    public SearchFragment() {
    }
    public SearchFragment(GeoPoint geo) {
        this.geo = geo;
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
        noResults = view.findViewById(R.id.FragSearchNoResults);
        firebaseGetPublis();

        search = view.findViewById(R.id.FragSearchSearchView);
        categories = view.findViewById(R.id.FragSearchChipGroup);
        range = view.findViewById(R.id.FragSearchSeekBarRange);
        state = view.findViewById(R.id.FragSearchSwitchState);
        state.setChecked(true);
        btnSearch = view.findViewById(R.id.FragSearchButtonSearch);
        TextView rangeText = view.findViewById(R.id.FragSearchEditTextRange);

        btnSearch.setOnClickListener(v -> firebaseSearchPublisFilters(view));

        range.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Actualiza el texto del TextView con el valor del SeekBar
                rangeText.setText("Range: " + progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarChange  = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    public void firebaseGetPublis(){
        CollectionReference collections = db.collection("posts");
        String userId = mAuth.getCurrentUser().getUid();
        Set<String> userIds = new HashSet<>();
        Query query = collections.whereNotEqualTo("userId", userId);
        query.orderBy("userId", Query.Direction.ASCENDING);

        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> items = new ArrayList<>();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        Map<String, Object> item = new HashMap<>();
                        if((boolean)document.get("state")){
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
                    }
                    mapList = items;
                    if(!items.isEmpty()){
                        List<String> idsList = new ArrayList<>(userIds);
                        searchUsers(idsList);
                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void firebaseSearchPublisFilters(View view){
        CharSequence text = search.getQuery();
        ArrayList<String> filterCategories = categories.getCheckedChipIds()
                .stream()
                .map(chipId -> {
                    Chip chip = view.findViewById(chipId);
                    Object tag = chip.getTag();
                    return (tag != null) ? tag.toString() : null;
                })
                .filter(tagValue -> tagValue != null)
                .collect(Collectors.toCollection(ArrayList::new));
        boolean statePubli = this.state.isChecked();
        int range = this.range.getProgress();

        CollectionReference collections = db.collection("posts");
        String userId = mAuth.getCurrentUser().getUid();
        Set<String> userIds = new HashSet<>();
        Query query = collections.whereNotEqualTo("userId", userId);
        query.orderBy("userId", Query.Direction.ASCENDING);

        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> items = new ArrayList<>();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        Map<String, Object> item = new HashMap<>();
                        if((boolean)document.get("state") == statePubli){
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
                    }

                    if(!filterCategories.isEmpty()){
                        items = items.stream()
                                .filter(item -> filterCategories.contains((String) item.get("category")))
                                .collect(Collectors.toList());
                    }

                    if(!text.equals("")){
                        items = items.stream()
                                .filter(item -> (item.get("description").toString().contains(text)))
                                .collect(Collectors.toList());
                    }

                    mapList = items;
                    if(!items.isEmpty()){
                        recyclerView.setVisibility(View.VISIBLE);
                        noResults.setVisibility(View.GONE);
                        List<String> idsList = new ArrayList<>(userIds);
                        searchUsers(idsList);
                    }else {
                        recyclerView.setVisibility(View.GONE);
                        noResults.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
