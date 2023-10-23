package com.example.mascotasapp.signup.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.mascotasapp.R;

public class RegisterUserFragment extends Fragment {
    EditText date;
    EditText username;

    public RegisterUserFragment() { }
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_user, container, false);
        date = view.findViewById(R.id.regBirthdayInput);
        username = view.findViewById(R.id.regNameInput);
        date.setOnClickListener(v -> getDate());
        // Inflate the layout for this fragment
        return view;
    }

    private void getDate() {
        Log.d("getDate", "Entro a la funcion");
        int dia, mes, anio;
        final Calendar calendar = Calendar.getInstance();
        dia = calendar.get(Calendar.DAY_OF_MONTH);
        mes = calendar.get(Calendar.MONTH);
        anio = calendar.get(Calendar.YEAR);

        DatePickerDialog picker = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener()  {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                date.setText(dayOfMonth + "-" + month+1 + "-" + year);
            }
        }, dia, mes, anio);
        picker.show();
    }
}