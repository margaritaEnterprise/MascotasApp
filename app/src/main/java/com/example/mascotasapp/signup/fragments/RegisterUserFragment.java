package com.example.mascotasapp.signup.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.example.mascotasapp.MainActivity;
import com.example.mascotasapp.R;
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
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class RegisterUserFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ImageView userPhoto;
    ImageView addPhoto;
    boolean imageChange;
    EditText username;
    EditText dateEdit;
    Date date;
    Button finishBtn;
    Uri imageUri;

    public RegisterUserFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_user, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        userPhoto = view.findViewById(R.id.FragRegUserPhoto);
        addPhoto = view.findViewById(R.id.FragRegUserAddPhoto);
        username = view.findViewById(R.id.regNameInput);
        dateEdit = view.findViewById(R.id.regBirthdayInput);
        finishBtn = view.findViewById(R.id.regUserButton);

        addPhoto.setOnClickListener(v -> showImageSourceDialog());
        userPhoto.setOnClickListener(v -> showImageSourceDialog());
        dateEdit.setOnClickListener(v -> getDate());
        dateEdit.setKeyListener(null);
        finishBtn.setOnClickListener(v -> createUser(currentUser));

        return view;
    }

    private void getDate() {
        cerrarTeclado(requireActivity());
        // Obtiene el texto actual del EditText
        String currentDate = dateEdit.getText().toString();

        // Si el EditText está vacío, establece la fecha actual
        if (currentDate.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            currentDate = day + "-" + (month + 1) + "-" + year;
        }

        // Divide la fecha actual en día, mes y año
        String[] dateParts = currentDate.split("-");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Resta 1 al mes porque en Calendar, enero es 0
        int year = Integer.parseInt(dateParts[2]);

        // Crea un DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(), // Contexto
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        // Maneja la fecha seleccionada y la muestra en el EditText
                        String selectedDate = selectedDayOfMonth + "-" + (selectedMonth + 1) + "-" + selectedYear;
                        dateEdit.setText(selectedDate);
                    }
                },
                year, month, day
        );

        // Muestra el diálogo
        datePickerDialog.show();
    }

    public static void cerrarTeclado(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
    private void showImageSourceDialog() {
        final CharSequence[] options = {"Tomar Foto", "Elegir de la Galería", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona una opción");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Tomar Foto")) {
                    openCamera();
                } else if (options[item].equals("Elegir de la Galería")) {
                    openGallery();
                } else if (options[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(galleryIntent);
    }
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                return;
            }
            imageUri = FileProvider.getUriForFile(requireContext(), "com.tu.paquete.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            captureImageLauncher.launch(takePictureIntent);
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;

        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return  File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
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
                           startCropActivity(selectedImageUri);
                        }
                    }
                }
            });
    private final ActivityResultLauncher<Intent> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && imageUri != null) {
                    startCropActivity(imageUri);
                }
            });

    private final ActivityResultLauncher<Intent> cropImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri croppedImageUri = CropImage.getActivityResult(result.getData()).getUri();
                    userPhoto.setImageURI(croppedImageUri);
                    imageChange = true;
                }
            });

    private void startCropActivity(Uri imageUri) {
        cropImageLauncher.launch(
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle("Recortar")
                        .setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .getIntent(requireContext())
        );
    }


}