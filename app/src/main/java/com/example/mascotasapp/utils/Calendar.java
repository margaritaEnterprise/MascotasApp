package com.example.mascotasapp.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.EditText;

public class Calendar {

    Context context;

    public Calendar(Context context){
        this.context = context;
    }

    public void getDate(EditText dateEdit) {
        // Obtiene el texto actual del EditText
        String currentDate = dateEdit.getText().toString();

        // Si el EditText está vacío, establece la fecha actual
        if (currentDate.isEmpty()) {
            android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance();
            int year = calendar.get(android.icu.util.Calendar.YEAR);
            int month = calendar.get(android.icu.util.Calendar.MONTH);
            int day = calendar.get(android.icu.util.Calendar.DAY_OF_MONTH);
            currentDate = day + "-" + (month + 1) + "-" + year;
        }

        // Divide la fecha actual en día, mes y año
        String[] dateParts = currentDate.split("-");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Resta 1 al mes porque en Calendar, enero es 0
        int year = Integer.parseInt(dateParts[2]);

        // Crea un DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context, // Contexto
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
}
