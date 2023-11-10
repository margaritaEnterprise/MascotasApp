package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.mascotasapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class EditFragment extends Fragment implements OnMapReadyCallback {

    ChipGroup categories; //FragEditChipCat_all
    ImageView editPhoto; //FragEditPhoto
    SwitchMaterial switchState;//FragEditSwitch
    TextInputEditText description; //FragEditDescription
    TextInputEditText locationEditText; //FragEditEditLocation
    Button btnSaveChanges, btnDelete; //FragEditBtnSave, FragEditBtnDelete
    MapView mapView;

    GoogleMap map;

    FirebaseAuth mAuth;
    ImageView userPhoto;
    TextView username;//, description;
    Chip category;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    Map<String, Object> post;
    Context context;
    Activity activity;
    public EditFragment(Map<String, Object> post, Context context, Activity activity) {
        this.post = post;
        this.context = context;
        this.activity = activity;
    }

    @SuppressLint({"MissingInflatedId", "MissingPermission"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        categories = view.findViewById(R.id.FragEditChipCat_all);
        editPhoto = view.findViewById(R.id.FragEditPhoto);
        description = view.findViewById(R.id.FragEditDescription);
        switchState = view.findViewById(R.id.FragEditSwitch);
        btnSaveChanges = view.findViewById(R.id.FragEditBtnSave);
        btnDelete = view.findViewById(R.id.FragEditBtnDelete);

        mAuth = FirebaseAuth.getInstance();

        mapView = view.findViewById(R.id.FragEditMap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        setData();

        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        GeoPoint geo = (GeoPoint) this.post.get("location");
        LatLng latLng = new LatLng(geo.getLatitude(), geo.getLongitude());
        map.addMarker(new MarkerOptions().position(latLng));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
        provider(map);
    }

    public void provider(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                googleMap.setMyLocationEnabled(true);
            }
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationClickListener(v -> Toast.makeText(requireContext(), "RR", Toast.LENGTH_SHORT).show());
    }

    public void setData(){
        String cat = post.get("category").toString();
        Chip sel = categories.findViewWithTag(cat);
        sel.setChecked(true);

        Uri photoUrl = Uri.parse((String) post.get("photoUrl"));
        Picasso.with(requireContext())
                .load(photoUrl)
                .resize(450, 450)
                .into(editPhoto);
        description.setText(post.get("description").toString());
        switchState.setChecked((Boolean) post.get("state"));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}