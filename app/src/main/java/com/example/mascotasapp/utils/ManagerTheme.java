package com.example.mascotasapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.example.mascotasapp.R;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ManagerTheme {
    public static Map<String,Object> getUserPreference(Activity activity){
        SharedPreferences sharedPreference = activity
                .getApplicationContext()
                .getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        if (!sharedPreference.contains("language") && !sharedPreference.contains("theme")) {
            // default
            SharedPreferences.Editor editPref = sharedPreference.edit();
            editPref.putString("language", "en"); //en, es, ch
            editPref.putString("theme", "dark"); //light, dark
            editPref.apply();
        }
        return (Map<String,Object>) sharedPreference.getAll();
    }
    public static void setUserPreference(Activity activity, Map<String,Object> map){
        String lang = Objects.requireNonNull(map.get("language")).toString();
        String theme = Objects.requireNonNull(map.get("theme")).toString();
        setAppLanguage(activity, lang);
        setAppTheme(activity, theme);
    }
    protected static void setAppLanguage(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = activity.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        activity.getResources().updateConfiguration(config, resources.getDisplayMetrics());
    }
    protected static void setAppTheme(Activity activity, String themeCode) {
        if (themeCode.equals("light")) {
            activity.setTheme(R.style.AppTheme_Light);
        } else if (themeCode.equals("dark")) {
            activity.setTheme(R.style.AppTheme_Dark);
        }
   }

}