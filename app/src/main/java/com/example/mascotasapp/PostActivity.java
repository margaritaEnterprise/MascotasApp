package com.example.mascotasapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mascotasapp.navigation.MainActivity;
import com.example.mascotasapp.utils.ImageHandler;
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
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    ChipGroup categories;
    ImageHandler imageHandler; //
    TextInputLayout textInputLayout;
    TextInputEditText locationEditText;
    GeoPoint geoPoint = new GeoPoint(-34.77564,-58.26793); //UNAJ
    String uriPhoto;
    TextInputEditText descriptionEditText;
    ImageView postPhoto;
    Button btnPhoto;
    Button savePost;
    boolean imageChange;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        //Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        //post
        postPhoto = findViewById(R.id.actPostPhoto);
        btnPhoto = findViewById(R.id.actPostBtnPhoto);
        savePost = findViewById(R.id.actPostBtnPost);
        textInputLayout = findViewById(R.id.actPostInputLocation);
        locationEditText = findViewById(R.id.actPostEditLocation);
        descriptionEditText = findViewById(R.id.actPostTextDescription);
        categories = findViewById(R.id.actPostChipCat_all);
        imageHandler = new ImageHandler(PostActivity.this, postPhoto ,requestCameraPermissionLauncher, pickImageLauncher, captureImageLauncher, cropImageLauncher);

        locationEditText.setKeyListener(null);
        locationEditText.setOnClickListener(v -> showLocationSourceDialog());

        btnPhoto.setOnClickListener(v -> imageHandler.showImageSourceDialog());
        savePost.setOnClickListener(v -> createPost(mAuth.getCurrentUser()));
    }
    private void createPost(FirebaseUser userAuth){
/*        if(!validate()){
            Toast.makeText(requireActivity(), R.string.create_fail, Toast.LENGTH_SHORT).show();
            return;
        }*/
        try {
            postPhoto.setDrawingCacheEnabled(true);
            postPhoto.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) postPhoto.getDrawable()).getBitmap();
            uploadImageToFirebaseStorage(userAuth, bitmap);

        } catch (ClassCastException e) {
            Toast.makeText(PostActivity.this, "ClassCastEx!!!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(PostActivity.this, "Exception e!!!", Toast.LENGTH_SHORT).show();
        }
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
                Toast.makeText(PostActivity.this, "Ocurrió un error al subir la foto.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        uriPhoto = uri.toString();
                        imageChange = false;
                        savePost(uriPhoto);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(PostActivity.this, "Ocurrió un error al obtener la URL de descarga.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public void savePost(String uriPhoto) {
        Chip selectedChip = findViewById(categories.getCheckedChipId());
        Map<String,Object> map = new HashMap<>();
        map.put("category", selectedChip.getTag().toString());
        map.put("photoUrl", uriPhoto);
        map.put("description", descriptionEditText);
        map.put("location", geoPoint);
        map.put("state", true);
        map.put("date", new Timestamp(new Date()));
        map.put("userId", mAuth.getCurrentUser().getUid());
        db.collection("posts")
                .add(map)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PostActivity.this, "Save Post :)", Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(PostActivity.this, MainActivity.class);
                    //startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PostActivity.this, "El Post ha fallado :/", Toast.LENGTH_SHORT).show()
                );
    }

    //Permisos
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    imageHandler.openCamera();
                } else {
                    Toast.makeText(PostActivity.this, "El permiso de la cámara es necesario para tomar fotos.", Toast.LENGTH_SHORT).show();
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

    public void showLocationSourceDialog() {
        final CharSequence[] options = {"Elegir una ubicacion", "Elegir la ubicacion actual", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Selecciona una opción");
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Elegir una ubicacion")) {
                    Toast.makeText(PostActivity.this, "Elegir una ubicacion", Toast.LENGTH_SHORT).show();
                } else if (options[item].equals("Elegir la ubicacion actual")) {
                    locationEditText.setText("Mi ubicacion");
                    Toast.makeText(PostActivity.this, "Elegir la ubicacion actual", Toast.LENGTH_SHORT).show();
                } else if (options[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }




}
