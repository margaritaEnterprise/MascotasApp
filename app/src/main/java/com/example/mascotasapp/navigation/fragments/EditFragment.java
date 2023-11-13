package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mascotasapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditFragment extends Fragment implements OnMapReadyCallback {

    ChipGroup categories; //FragEditChipCat_all
    Chip selectedChip;
    ImageView editPhoto; //FragEditPhoto
    SwitchMaterial switchState;//FragEditSwitch
    TextInputEditText description; //FragEditDescription
    Button btnSaveChanges, btnDelete; //FragEditBtnSave, FragEditBtnDelete
    MapView mapView;
    GoogleMap map;
    FirebaseFirestore db;
    Map<String, Object> post;
    Context context;
    public interface BackToProfile{
        void editSuccess();
    }
    BackToProfile backToProfile;
    public EditFragment(Map<String, Object> post, Context context) {
        this.post = post;
        this.context = context;
        this.backToProfile = (BackToProfile) context;
    }

    @SuppressLint({"MissingInflatedId", "MissingPermission"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        db = FirebaseFirestore.getInstance();
        categories = view.findViewById(R.id.FragEditChipCat_all);
        editPhoto = view.findViewById(R.id.FragEditPhoto);
        description = view.findViewById(R.id.FragEditDescription);
        switchState = view.findViewById(R.id.FragEditSwitch);
        btnSaveChanges = view.findViewById(R.id.FragEditBtnSave);
        btnDelete = view.findViewById(R.id.FragEditBtnDelete);

        btnSaveChanges.setOnClickListener(v -> editData());
        btnDelete.setOnClickListener(v -> showDialog());

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

    public void setData(){
        String cat = post.get("category").toString();
        selectedChip = categories.findViewWithTag(cat);
        selectedChip.setChecked(true);

        Uri photoUrl = Uri.parse((String) post.get("photoUrl"));
        Picasso.with(requireContext())
                .load(photoUrl)
                .resize(450, 450)
                .into(editPhoto);
        description.setText(post.get("description").toString());
        switchState.setChecked((Boolean) post.get("state"));
    }

    public void editData() {
        String desc = description.getText().toString().trim();

        if (desc.isEmpty()){
            Toast.makeText(context, R.string.input_description, Toast.LENGTH_SHORT).show();
        } else {
            String postId = post.get("id").toString();
            DocumentReference currentDocument = db.collection("posts").document(postId);
            Map<String, Object> postChanges = new HashMap<>();
            int chipId = categories.getCheckedChipId();
            postChanges.put("category", categories.findViewById(chipId).getTag().toString());
            postChanges.put("description", desc);
            postChanges.put("state", switchState.isChecked());
            postChanges.put("modified", new Timestamp(new Date()));

            currentDocument
                    .update(postChanges)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(requireContext(), R.string.edit_success, Toast.LENGTH_SHORT).show();
                        backToProfile.editSuccess();
                    })
                    .addOnFailureListener(v -> {
                        Toast.makeText(requireContext(), R.string.edit_fail, Toast.LENGTH_SHORT).show();
                    });
        }
    }
    public void deletePost() {
        String postId = post.get("id").toString();
        DocumentReference currentDocument = db.collection("posts").document(postId);
        currentDocument
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                    backToProfile.editSuccess();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), R.string.delete_fail, Toast.LENGTH_SHORT).show();
                });
    }
    public void showDialog() {
        final CharSequence[] options = {context.getString(R.string.delete), context.getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(context.getString(R.string.are_you_sure));
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(context.getString(R.string.delete))) {
                deletePost();
            } else if (options[item].equals(context.getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}