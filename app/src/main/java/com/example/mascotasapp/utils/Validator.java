package com.example.mascotasapp.utils;

import com.example.mascotasapp.R;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Validator {
    public int errorMsg;

    public boolean ValidateAuth(String email, String pass){
        return ValidateEmail(email) && ValidatePass(pass);
    }
    public boolean ValidateEmail(String email) {
        String regex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if(matcher.find()){
            return true;
        }
        errorMsg = R.string.error_mail;
        return false;
    }
    public boolean ValidatePass(String pass) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$.@$!%*?&])([A-Za-z\\d$@$!%*?&]|[^ ]){8,15}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pass);
        if(matcher.find()){
            return true;
        }
        errorMsg = R.string.error_password;
        return false;
    }
}
