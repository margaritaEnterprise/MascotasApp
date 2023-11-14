package com.example.mascotasapp.navigation.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
    private LocationManager locationManager;
    private LocationListener locationListener;
    RecyclerView recyclerView;
    TextView noResults;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    List<Map<String, Object>> mapList;
    SearchView search;
    CardView filters;
    ChipGroup categories;
    SeekBar range;
    boolean seekBarChange = false;
    Switch state;
    Button btnSearch, btnFilter;
    boolean visibleFilter;
    GeoPoint geo;
    Context context;
    Boolean first;
    ProgressBar loader;

    public SearchFragment(Context context) {
        this.context = context;
    }

    public SearchFragment(GeoPoint geo) {
        this.geo = geo;
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
        first = true;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.FragSearchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        noResults = view.findViewById(R.id.FragSearchNoResults);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (first) {
                    if(location.getLatitude() > 0 || Math.floor(location.getLongitude()) == -122){
                        geo = new GeoPoint(-34.77590260925053, -58.26947642872307);
                    }else {
                        geo = new GeoPoint(location.getLatitude(), location.getLongitude());
                    }

                    firebaseGetPublis();
                    first = false;
                }
            }
        };
        checkPermissionGeo();

        search = view.findViewById(R.id.FragSearchSearchView);
        categories = view.findViewById(R.id.FragSearchChipGroup);
        range = view.findViewById(R.id.FragSearchSeekBarRange);
        state = view.findViewById(R.id.FragSearchSwitchState);
        state.setChecked(true);
        btnSearch = view.findViewById(R.id.FragSearchButtonSearch);
        btnFilter = view.findViewById(R.id.FragSearchButtonFilter);
        visibleFilter = false;
        TextView rangeText = view.findViewById(R.id.FragSearchEditTextRange);
        rangeText.setText(getString(R.string.range) + ": 10km");

        loader = view.findViewById(R.id.FragSearchProgressBar);
        loader.setVisibility(View.VISIBLE);
        btnSearch.setOnClickListener(v -> firebaseSearchPublisFilters(view));

        filters = view.findViewById(R.id.frag_search_card_filter);

        btnFilter.setOnClickListener (v -> {
            if (!visibleFilter) {
                filters.setVisibility(View.VISIBLE);
                visibleFilter = true;
            } else {
                filters.setVisibility(View.GONE);
                visibleFilter = false;
            }
        });

        range.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String range = getString(R.string.range)  + " ";
                rangeText.setText(range + progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarChange = true;
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

        double latitude = geo.getLatitude() ;
        double longitude = geo.getLongitude();
        double radiusInKilometers = 10;  // Radio de 10 km por defecto
        double latitudeMin = latitude - (180.0 * radiusInKilometers) / (40075.0);
        double latitudeMax = latitude + (180.0 * radiusInKilometers) / (40075.0);
        double longitudeMin = longitude - (180.0 * radiusInKilometers) / (40075.0) / Math.cos(Math.toRadians(latitude));
        double longitudeMax = longitude + (180.0 * radiusInKilometers) / (40075.0) / Math.cos(Math.toRadians(latitude));


        Query query = collections.whereGreaterThanOrEqualTo("location", new GeoPoint(latitudeMin, longitudeMin))
                .whereLessThanOrEqualTo("location", new GeoPoint(latitudeMax, longitudeMax));

        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> items = new ArrayList<>();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        Map<String, Object> item = new HashMap<>();
                        String documentUserId = (String)document.get("userId");
                        if((boolean)document.get("state") && !documentUserId.equalsIgnoreCase(userId)){
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
                        recyclerView.setVisibility(View.VISIBLE);
                        noResults.setVisibility(View.GONE);
                        List<String> idsList = new ArrayList<>(userIds);
                        searchUsers(idsList);
                    }else {
                        loader.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        noResults.setVisibility(View.VISIBLE);

                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        user.put("deviceId", document.getString("deviceId"));
                        users.add(user);
                    }
                    for (Map<String, Object> map : mapList) {
                        for (Map<String, Object> user : users){
                            if(map.get("userId").equals(user.get("id"))){
                                map.put("userPhotoUrl", user.get("photoUrl"));
                                map.put("username",user.get("username"));
                                map.put("deviceId", user.get("deviceId"));
                            }
                        }
                    }
                    finishGet();
                })
                .addOnFailureListener(e -> {
                    Log.w("Messaging", "No se encontro el usuario");
                });

    }

    public void finishGet(){
        recyclerView.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        PostAdapter postAdapter = new PostAdapter(mapList, context);
        recyclerView.setAdapter(postAdapter);
    }

    public void firebaseSearchPublisFilters(View view){
        recyclerView.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        noResults.setVisibility(View.GONE);
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

        double latitude = geo.getLatitude() ;
        double longitude = geo.getLongitude();
        double radiusInKilometers = range;  // Radio de 10 km por defecto
        double latitudeMin = latitude - (180.0 * radiusInKilometers) / (40075.0);
        double latitudeMax = latitude + (180.0 * radiusInKilometers) / (40075.0);
        double longitudeMin = longitude - (180.0 * radiusInKilometers) / (40075.0) / Math.cos(Math.toRadians(latitude));
        double longitudeMax = longitude + (180.0 * radiusInKilometers) / (40075.0) / Math.cos(Math.toRadians(latitude));

        Query query = collections.whereGreaterThanOrEqualTo("location", new GeoPoint(latitudeMin, longitudeMin))
                .whereLessThanOrEqualTo("location", new GeoPoint(latitudeMax, longitudeMax));

        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> items = new ArrayList<>();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        Map<String, Object> item = new HashMap<>();
                        String documentUserId = (String) document.get("userId");
                        if((boolean)document.get("state") == statePubli && !documentUserId.equals(userId)){
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
                        loader.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        noResults.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void checkPermissionGeo(){
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);

    }
}
