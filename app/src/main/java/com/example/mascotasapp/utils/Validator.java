package com.example.mascotasapp.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Validator {
    public String errorMsg;

    public boolean ValidateAuth(String email, String pass){
        errorMsg = "";
        return ValidateEmail(email) && ValidatePass(pass);
    }
    public boolean ValidateEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if(matcher.find()){
            return true;
        }
        errorMsg += "Ingrese un mail valido.";
        return false;
    }
    public boolean ValidatePass(String pass) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$.@$!%*?&])([A-Za-z\\d$@$!%*?&]|[^ ]){8,15}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pass);
        if(matcher.find()){
            return true;
        }
        errorMsg += "Ingrese una contraseña valida.";
        return false;
    }
}
