package com.example.mascotasapp.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.mascotasapp.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHandler {
    private Context context;
    private ImageView userPhoto;
    private Uri imageUri;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> captureImageLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;

    public ImageHandler(Context context, ImageView userPhoto,
                        ActivityResultLauncher<String> requestCameraPermissionLauncher,
                        ActivityResultLauncher<Intent> pickImageLauncher,
                        ActivityResultLauncher<Intent> captureImageLauncher,
                        ActivityResultLauncher<Intent> cropImageLauncher) {
        this.context = context;
        this.userPhoto = userPhoto;
        this.requestCameraPermissionLauncher = requestCameraPermissionLauncher;
        this.pickImageLauncher = pickImageLauncher;
        this.captureImageLauncher = captureImageLauncher;
        this.cropImageLauncher = cropImageLauncher;
    }

    public void showImageSourceDialog() {
        final CharSequence[] options = {context.getString(R.string.take_photo), context.getString(R.string.choose_photo), context.getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.select_an_option));
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(context.getString(R.string.take_photo))) {
                openCamera();
            } else if (options[item].equals(context.getString(R.string.choose_photo))) {
                openGallery();
            } else if (options[item].equals(context.getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    public void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(galleryIntent);
    }
    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                return;
            }
            this.imageUri = FileProvider.getUriForFile(context, "com.tu.paquete.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.imageUri);
            captureImageLauncher.launch(takePictureIntent);
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return  File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    public void startCropActivity(Uri imageUri) {
        cropImageLauncher.launch(
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle(context.getText(R.string.crop))
                        .setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .getIntent(context)
        );
    }
    public Uri getImageUri() {
        return this.imageUri;
    }
}
