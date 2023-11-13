package com.example.mascotasapp;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    private Button btnAccept;
    private Marker selectedMarker;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.ActMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnAccept = findViewById(R.id.ActMapButton);
        btnAccept.setOnClickListener(view -> {
            if (selectedMarker != null) {
                LatLng selectedLatLng = selectedMarker.getPosition();
                String locationName = getLocationName(selectedLatLng.latitude, selectedLatLng.longitude);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedLocationName", locationName);
                resultIntent.putExtra("selectedLocationLat", selectedLatLng.latitude);
                resultIntent.putExtra("selectedLocationLong", selectedLatLng.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(MapActivity.this, R.string.select_location, Toast.LENGTH_SHORT).show();
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = location -> {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng myLocation = new LatLng(latitude, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.0f));
        };
    }

    private String getLocationName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String locationName = address.getFeatureName() + ", " + address.getLocality();
                return locationName;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getString(R.string.unknown_location);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(latLng -> {
            if (selectedMarker != null) {
                selectedMarker.remove();
            }
            selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
