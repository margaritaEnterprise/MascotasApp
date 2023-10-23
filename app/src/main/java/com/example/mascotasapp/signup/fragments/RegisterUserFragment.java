package com.example.mascotasapp.signup.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.mascotasapp.R;

public class RegisterUserFragment extends Fragment {
    EditText date;
    EditText username;

    public RegisterUserFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_user, container, false);
        date = view.findViewById(R.id.regBirthdayInput);
        username = view.findViewById(R.id.regNameInput);
        date.setOnClickListener(v -> getDate());
        date.setKeyListener(null);
        // Inflate the layout for this fragment
        return view;
    }

    private void getDate() {
        cerrarTeclado(requireActivity());
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

    public static void cerrarTeclado(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}