package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailFragment extends Fragment implements OnMapReadyCallback {
    FirebaseAuth mAuth;
    ImageView userPhoto, postPhoto;
    TextView username, description;
    Chip category;
    MapView mMapView;
    Context context;
    Activity activity;
    GoogleMap map;
    ImageButton edit, notify;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    Map<String, Object> post;
    public interface ButtonEdit{
        void btnClickEdit(Map<String, Object> item);
    }
    ButtonEdit buttonEditOnClic;
    public DetailFragment(Map<String, Object> post, Context context, Activity activity) {
        this.post = post;
        this.context = context;
        this.activity = activity;
        this.buttonEditOnClic = (ButtonEdit) context;
    }

    @SuppressLint({"MissingInflatedId", "MissingPermission"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        mAuth = FirebaseAuth.getInstance();

        userPhoto = view.findViewById(R.id.FragPostImageUser);
        postPhoto = view.findViewById(R.id.FragPostImagePost);
        username = view.findViewById(R.id.FragPostUsername);
        description = view.findViewById(R.id.FragPostTextDesc);
        category = view.findViewById(R.id.FragPostChipCategory);
        edit = view.findViewById(R.id.FragDetailBtnEdit);
        notify = view.findViewById(R.id.FragDetailBtnNotify);


        String postUserId = post.get("userId").toString();
        if(mAuth.getCurrentUser().getUid().equals(postUserId)) {
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener(v -> buttonEditOnClic.btnClickEdit(this.post));
        }else {
            notify.setVisibility(View.VISIBLE);
            notify.setOnClickListener(v ->sendNotification() );
        }

        mMapView = view.findViewById(R.id.FragPostMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

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


    public void sendNotification() {
        try {
            String token = post.get("deviceId").toString();
            String message = "oaasdasdasdl";
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=AAAA05seiD0:APA91bH38cHnWgsv8dFJmNm5dizN8ey9_4BoaNWUehehdf-A2WRAF9hVtkxF6ojs6DdwO7gj27xAzW3nJH1G-2sdxCmDyZtmSf39EptgL64Fa6PKDBkzVsS76OPTrCS0svZxdjI9W2hS");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Construir el cuerpo del mensaje
            String body = "{\"to\":\"" + token + "\",\"data\":{\"message\":\"" + message + "\"}}";

            // Enviar el mensaje
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.flush();
            os.close();

            // Obtener la respuesta
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}