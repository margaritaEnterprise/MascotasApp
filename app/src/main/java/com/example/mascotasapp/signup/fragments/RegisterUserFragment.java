package com.example.mascotasapp.signup.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.mascotasapp.navigation.MainActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.utils.Calendar;
import com.example.mascotasapp.utils.ImageHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterUserFragment extends Fragment {

    Calendar calendar;
    ImageHandler imageHandler;
    ImageView userPhoto;
    ImageView addPhoto;
    boolean imageChange;
    EditText username;
    EditText dateEdit;
    Date date;
    Button finishBtn;
    Uri imageUri;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    public RegisterUserFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_user, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        calendar = new Calendar(requireContext());
        userPhoto = view.findViewById(R.id.FragRegUserPhoto);
        addPhoto = view.findViewById(R.id.FragRegUserAddPhoto);
        username = view.findViewById(R.id.regNameInput);
        dateEdit = view.findViewById(R.id.regBirthdayInput);
        finishBtn = view.findViewById(R.id.regUserButton);
        imageHandler = new ImageHandler(requireContext(),
                    userPhoto,
                    requestCameraPermissionLauncher,
                    pickImageLauncher,
                    captureImageLauncher,
                    cropImageLauncher);

        addPhoto.setOnClickListener(v -> imageHandler.showImageSourceDialog());
        userPhoto.setOnClickListener(v -> imageHandler.showImageSourceDialog());
        dateEdit.setOnClickListener(v -> calendar.getDate(dateEdit));
        dateEdit.setKeyListener(null);
        finishBtn.setOnClickListener(v -> createUser(mAuth.getCurrentUser()));

        return view;
    }

    private void goToMainActivity(){
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        startActivity(intent);
    }
    private void createUser(FirebaseUser userAuth){

        if(!validate()){
            Toast.makeText(requireActivity(), R.string.create_fail, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            userPhoto.setDrawingCacheEnabled(true);
            userPhoto.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) userPhoto.getDrawable()).getBitmap();
            uploadImageToFirebaseStorage(userAuth, bitmap);
        } catch (ClassCastException e) {
            Toast.makeText(requireContext(), "Sube una foto!!!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Sube una foto!!!", Toast.LENGTH_SHORT).show();
        }


    }
    private void uploadImageToFirebaseStorage(FirebaseUser userAuth,Bitmap bitmap)  {

        if(!imageChange){
            saveUserToFirestore(userAuth,imageUri.toString());
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
                Toast.makeText(requireContext(), "Ocurrió un error al subir la foto.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUri = uri;
                        imageChange = false;
                        saveUserToFirestore(userAuth, imageUri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(requireContext(), "Ocurrió un error al obtener la URL de descarga.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void saveUserToFirestore(FirebaseUser userAuth,String imageUrl) {
        String newUsername = username.getText().toString();
        db.collection("users")
                .whereEqualTo("username", newUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                String uid = userAuth.getUid();
                                DocumentReference userDocRef = db.collection("users").document(uid);
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("id", uid);
                                userData.put("username", newUsername);
                                userData.put("birthdate", new Timestamp(date));
                                userData.put("photoUrl", imageUrl);
                                userDocRef.set(userData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                goToMainActivity();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(requireActivity(), R.string.create_fail, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(requireActivity(), R.string.username_already_exists, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireActivity(), R.string.create_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private  boolean validate(){
        String newUsername = username.getText().toString();

        boolean validUsername = !newUsername.isEmpty();
        boolean validDate = true;

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        date = null; // Inicializa date como nulo

        String dateStr = dateEdit.getText().toString();
        if (!dateStr.isEmpty()) {
            try {
                date = format.parse(dateStr);
            } catch (ParseException e) {
               validDate = false;
            }
        } else {
            validDate = false;
        }

        return validUsername && validDate;
    }



    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    imageHandler.openCamera();
                } else {
                    Toast.makeText(requireContext(), "El permiso de la cámara es necesario para tomar fotos.", Toast.LENGTH_SHORT).show();
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
                    userPhoto.setImageURI(croppedImageUri);
                    imageUri = croppedImageUri;
                    imageChange = true;
                }
            });
}