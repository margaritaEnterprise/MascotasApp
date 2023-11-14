package com.example.mascotasapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.mascotasapp.navigation.MainActivity;
import com.example.mascotasapp.utils.ImageHandler;
import com.example.mascotasapp.utils.ManagerTheme;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {
    //Firebase
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    //Location
    private LocationManager locationManager = null;
    private MyLocationListener locationListener = null;
    private Boolean flagGPS = false;
    GeoPoint geoPoint;// = new GeoPoint(-34.77564,-58.26793); //UNAJ
    TextInputEditText locationEditText;
    TextInputLayout inputLocation;
    //Photo
    ImageHandler imageHandler;
    boolean imageChange;
    String uriPhoto;
    ImageView postPhoto;
    Button btnPhoto;

    ChipGroup categories;
    TextInputEditText descriptionEditText;

    Button savePost;
    Map<String, Object> userPrefMap;
    String errorMessagge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userPrefMap = ManagerTheme.getUserPreference(this);
        ManagerTheme.setUserPreference(this, userPrefMap);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        //Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        //Location
        inputLocation = findViewById(R.id.actPostInputLocation);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationEditText = findViewById(R.id.actPostEditLocation);
        locationEditText.setKeyListener(null);
        locationEditText.setOnClickListener(v -> showLocationSourceDialog());

        //Photo
        btnPhoto = findViewById(R.id.actPostBtnPhoto);
        btnPhoto.setOnClickListener(v -> imageHandler.showImageSourceDialog());
        postPhoto = findViewById(R.id.actPostPhoto);
        postPhoto.setOnClickListener(v -> imageHandler.showImageSourceDialog());
        imageHandler = new ImageHandler(PostActivity.this, postPhoto ,requestCameraPermissionLauncher, pickImageLauncher, captureImageLauncher, cropImageLauncher);

        descriptionEditText = findViewById(R.id.actPostTextDescription);
        categories = findViewById(R.id.actPostChipCat_all);

        //SavePost
        savePost = findViewById(R.id.actPostBtnPost);
        savePost.setOnClickListener(v -> createPost(mAuth.getCurrentUser()));
    }

    private void createPost(FirebaseUser userAuth){
        if(!validatePost()){
            Toast.makeText(PostActivity.this, errorMessagge, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            postPhoto.setDrawingCacheEnabled(true);
            postPhoto.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) postPhoto.getDrawable()).getBitmap();
            uploadImageToFirebaseStorage(userAuth, bitmap);

        } catch (ClassCastException e) {
            Toast.makeText(PostActivity.this, R.string.upload_photo, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(PostActivity.this, R.string.upload_photo, Toast.LENGTH_SHORT).show();
        }
    }
    private boolean validatePost() {
        String desc = descriptionEditText.getText().toString().trim();
        if (desc.isEmpty()){
            errorMessagge = getString(R.string.input_a_valid_description);
            return false;
        }
        if (locationEditText.getText().toString().isEmpty() || geoPoint == null){
            errorMessagge = getString(R.string.input_a_valid_location);
            return false;
        }
        Chip selectedChip = findViewById(categories.getCheckedChipId());
        if(selectedChip == null){
            errorMessagge = getString(R.string.input_a_valid_category);
            return false;
        }
        return true;
    }
    private void uploadImageToFirebaseStorage(FirebaseUser userAuth, Bitmap bitmap)  {
        if(!imageChange){
            savePost(uriPhoto);
            return;
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + UUID.randomUUID().toString() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                String errorMessage = exception.getMessage();
                Toast.makeText(PostActivity.this, R.string.an_error_photo, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                uriPhoto = uri.toString();
                imageChange = false;
                savePost(uriPhoto);
            }
        }).addOnFailureListener(exception -> Toast.makeText(PostActivity.this, R.string.an_error_photoUrl, Toast.LENGTH_SHORT).show()));
    }
    public void savePost(String uriPhoto) {
        Chip selectedChip = findViewById(categories.getCheckedChipId());
        Map<String,Object> map = new HashMap<>();
        map.put("category", selectedChip.getTag().toString());
        map.put("photoUrl", uriPhoto);
        map.put("description", descriptionEditText.getText().toString());
        map.put("location", geoPoint);
        map.put("state", true);
        map.put("date", new Timestamp(new Date()));
        map.put("modified", new Timestamp(new Date()));

        map.put("userId", mAuth.getCurrentUser().getUid());
        db.collection("posts")
                .add(map)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PostActivity.this, R.string.save_post_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PostActivity.this, MainActivity.class);
                    
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostActivity.this, R.string.save_post_fail, Toast.LENGTH_SHORT).show();
                });
    }

    //Permisos
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    imageHandler.openCamera();
                } else {
                    Toast.makeText(PostActivity.this, R.string.permission_photo, Toast.LENGTH_SHORT).show();
                }
            });
    //Galeria
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        if (selectedImageUri != null) {
                            imageHandler.startCropActivity(selectedImageUri);
                        }
                    }
                }
            });
    //Capture
    private final ActivityResultLauncher<Intent> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri captureImageUri = imageHandler.getImageUri();
                    imageHandler.startCropActivity(captureImageUri);
                }
            });
    //Crop
    private final ActivityResultLauncher<Intent> cropImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri croppedImageUri = CropImage.getActivityResult(result.getData()).getUri();
                    postPhoto.setImageURI(croppedImageUri);
                    uriPhoto = croppedImageUri.toString();
                    imageChange = true;
                }
            });

    private final ActivityResultLauncher<Intent> mapActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String selectedLocation = data.getStringExtra("selectedLocationName");
                        Double selectedLocationLat = data.getDoubleExtra("selectedLocationLat", 0);
                        Double selectedLocationLong = data.getDoubleExtra("selectedLocationLong", 0);
                        geoPoint = new GeoPoint(selectedLocationLat, selectedLocationLong);
                        locationEditText.setText(selectedLocation);
                        inputLocation.setEndIconDrawable(R.drawable.ic_location);
                    }
                }
            }
    );

    public void showLocationSourceDialog() {
        final CharSequence[] options = {getString(R.string.choose_ubication), getString(R.string.choose_actual_ubication), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle(getString(R.string.select_an_option));
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(getString(R.string.choose_ubication))) {
                geoPoint = null;
                Intent mapIntent = new Intent(PostActivity.this, MapActivity.class);
                mapActivityLauncher.launch(mapIntent);
            }
            else if (options[item].equals(getString(R.string.choose_actual_ubication))) {
                flagGPS = displayGpsStatus();
                if (flagGPS) {
                    // Instancio el listener
                    locationListener = new MyLocationListener();
                    if (ActivityCompat.checkSelfPermission(PostActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PostActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PostActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        return;
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
                    locationEditText.setText(getString(R.string.my_location));
                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.gps_disable), Toast.LENGTH_SHORT).show();
                }
            }
            else if (options[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    //Obtener coordenadas
    private Boolean displayGpsStatus() {
        if (locationManager != null) {
            boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return gpsStatus;
        } else {
            return false;
        }
    }

    public class MyLocationListener implements LocationListener {
        private double longitude, latitude;
        @Override
        public void onLocationChanged(Location loc) {
            this.longitude = loc.getLongitude();
            this.latitude = loc.getLatitude();
            geoPoint = new GeoPoint(this.latitude,this.longitude);
            inputLocation.setEndIconDrawable(R.drawable.ic_my_location);
        }
        @Override
        public void onProviderDisabled(@NonNull String provider) {}
        @Override
        public void onProviderEnabled(@NonNull String provider) {}
    }
}
