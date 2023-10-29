package com.example.mascotasapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mascotasapp.utils.ImageHandler;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.theartofdev.edmodo.cropper.CropImage;

public class PostActivity extends AppCompatActivity {

    ImageHandler imageHandler;
    TextInputLayout textInputLayout;
    TextInputEditText locationEditText;
    ImageView postPhoto;
    Button btnPhoto;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postPhoto = findViewById(R.id.actPostPhoto);
        btnPhoto = findViewById(R.id.actPostBtnPhoto);

        imageHandler = new ImageHandler(PostActivity.this, postPhoto ,requestCameraPermissionLauncher, pickImageLauncher, captureImageLauncher, cropImageLauncher);


        textInputLayout = findViewById(R.id.actPostInputLocation);
        locationEditText = findViewById(R.id.actPostEditLocation);

        locationEditText.setKeyListener(null);
        locationEditText.setOnClickListener(v -> showLocationSourceDialog());

        btnPhoto.setOnClickListener(v -> imageHandler.showImageSourceDialog());
    }

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    imageHandler.openCamera();
                } else {
                    Toast.makeText(PostActivity.this, "El permiso de la cámara es necesario para tomar fotos.", Toast.LENGTH_SHORT).show();
                }
            });
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
    private final ActivityResultLauncher<Intent> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri captureImageUri = imageHandler.getImageUri();
                    imageHandler.startCropActivity(captureImageUri);

                }
            });
    private final ActivityResultLauncher<Intent> cropImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri croppedImageUri = CropImage.getActivityResult(result.getData()).getUri();
                    postPhoto.setImageURI(croppedImageUri);
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
