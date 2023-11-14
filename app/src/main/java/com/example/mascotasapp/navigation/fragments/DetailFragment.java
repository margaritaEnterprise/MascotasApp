package com.example.mascotasapp.navigation.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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
import com.example.mascotasapp.PostActivity;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.logging.type.HttpRequest;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DetailFragment extends Fragment implements OnMapReadyCallback {
    FirebaseAuth mAuth;
    ImageView userPhoto, postPhoto;
    TextView username, description, notifyText, editText;
    Chip category, state;
    MapView mMapView;
    Context context;
    Activity activity;
    GoogleMap map;
    ImageButton edit, notify;
    Map<String, Object> post;
    String photoUrl;
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
        editText = view.findViewById(R.id.FragDetailTextViewEdit);
        notifyText = view.findViewById(R.id.FragDetailTextViewNotify);
        state = view.findViewById(R.id.FragPostChipState);

        String postUserId = post.get("userId").toString();
        if(mAuth.getCurrentUser().getUid().equals(postUserId)) {
            editText.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener(v -> buttonEditOnClic.btnClickEdit(this.post));
        }else {
            notifyText.setVisibility(View.VISIBLE);
            notify.setVisibility(View.VISIBLE);
            notify.setOnClickListener(v -> getUser());
        }

        mMapView = view.findViewById(R.id.FragPostMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        setData();

        String url  = (String) post.get("photoUrl");
        postPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                downloadAndSaveImage();
                return true;
            }
        });


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
        String value = (String) post.get("category");
        String text = "";
        switch (value){
            case "adoption":
                text = context.getString(R.string.adoption);
                break;
            case "lost":
                text = context.getString(R.string.lost);
                break;
            case "found":
                text = context.getString(R.string.found);
                break;
            case "couple":
                text = context.getString(R.string.couple);
                break;
        }
        category.setText(text);

        Uri userPhotoUrl = Uri.parse((String) post.get("userPhotoUrl"));
        Picasso.with(requireContext())
                .load(userPhotoUrl)
                .resize(30, 30)
                .into(userPhoto);

        photoUrl = post.get("photoUrl").toString();
        Uri photoUrl = Uri.parse((String) post.get("photoUrl"));
        Picasso.with(requireContext())
                .load(photoUrl)
                .resize(450, 450)
                .into(postPhoto);

        if((boolean) post.get("state")){
            state.setText(R.string.state_post_active);
        }else {
            state.setText(R.string.state_post_closed);
        }
    }

    private void sendNotification(String username,String myToken, String userPhoto){
        RequestQueue myrequest= Volley.newRequestQueue(context);
        JSONObject json = new JSONObject();

        try {
            json.put("to",post.get("deviceId"));
            JSONObject notificacion=new JSONObject();
            notificacion.put("type", "1");
            notificacion.put("title", getString(R.string.notify_title));
            notificacion.put("photo",post.get("photoUrl"));
            notificacion.put("username", username);
            notificacion.put("message", R.string.notify_message_type1);
            notificacion.put("deviceId", myToken);
            notificacion.put("userPhoto", userPhoto);

            json.put("data",notificacion);
            String URL="https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,URL,json,null,null){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String>header=new HashMap<>();
                    header.put("Content-Type","application/json");
                     header.put("Authorization","key=AAAA05seiD0:APA91bH38cHnWgsv8dFJmNm5dizN8ey9_4BoaNWUehehdf-A2WRAF9hVtkxF6ojs6DdwO7gj27xAzW3nJH1G-2sdxCmDyZtmSf39EptgL64Fa6PKDBkzVsS76OPTrCS0svZxdjI9W2hS");
                    return header;
                }
            };
            myrequest.add(request);


        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void getUser(){
        CollectionReference collections = FirebaseFirestore.getInstance().collection("users");
        String userId = mAuth.getCurrentUser().getUid();
        Query query = collections.whereEqualTo("id", userId);
        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    sendNotification( doc.get("username").toString(), doc.get("deviceId").toString(), doc.get("photoUrl").toString() );
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void downloadAndSaveImage() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) postPhoto.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // En Android 10 y superiores, utiliza el Scoped Storage
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, String.format("%d.jpg", System.currentTimeMillis()));
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            if (imageUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(imageUri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    showDownloadNotification(getString(R.string.image_downloaded), getString(R.string.image_succes_downloaded));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showDownloadNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Comprobación de la versión de Android para la creación de canales
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("download_channel", "Download Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Crear un Intent para abrir la galería
        Intent galleryIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"));
        PendingIntent pendingGalleryIntent = PendingIntent.getActivity(context, 0, galleryIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "download_channel")
                .setSmallIcon(R.drawable.baseline_file_download_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingGalleryIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
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