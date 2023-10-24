package com.example.mascotasapp.signup.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterUserFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    EditText username;
    EditText dateEdit;
    Button finishBtn;

    public RegisterUserFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_user, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        username = view.findViewById(R.id.regNameInput);
        dateEdit = view.findViewById(R.id.regBirthdayInput);
        finishBtn = view.findViewById(R.id.regUserButton);

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
            currentDate = day + "/" + (month + 1) + "/" + year;
        }

        // Divide la fecha actual en día, mes y año
        String[] dateParts = currentDate.split("/");
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
                        String selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
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
                                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                                Date date;
                                try {

                                    date = format.parse(dateEdit.getText().toString());
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                userData.put("birthdate", new Timestamp(date));

                                userDocRef.set(userData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Usuario agregado exitosamente
                                                goToMainActivity();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //userAuth.delete();
                                                Toast.makeText(requireActivity(), R.string.create_fail, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                //userAuth.delete();
                                Toast.makeText(requireActivity(), R.string.username_already_exists, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireActivity(), R.string.create_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}