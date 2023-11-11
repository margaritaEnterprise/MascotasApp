package com.example.mascotasapp.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.PostActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.navigation.fragments.DetailFragment;
import com.example.mascotasapp.navigation.fragments.EditFragment;
import com.example.mascotasapp.navigation.fragments.NotifyFragment;
import com.example.mascotasapp.navigation.fragments.ProfileFragment;
import com.example.mascotasapp.navigation.fragments.SearchFragment;
import com.example.mascotasapp.navigation.fragments.SettingFragment;
import com.example.mascotasapp.signup.SignUpActivity;
import com.example.mascotasapp.utils.MyPostAdapter;
import com.example.mascotasapp.utils.PostAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PostAdapter.PostClickListener, MyPostAdapter.PostClickListener, DetailFragment.ButtonEdit, EditFragment.BackToProfile {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; //viewDetailMyPost
    SharedPreferences sharedPreference;
    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;
    Map<String, Object> dataUser;
    Map<String, Object> userPrefMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        defaultPreferences();
        //setAppLanguage(this, "en");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        frameLayout = findViewById(R.id.frame_layout);

        replaceFragment(new SearchFragment(this));

        bottomNavigationView.setOnItemSelectedListener(item  -> {

            switch (item.getTitle().toString()) {
                case "Search":
                    Toast.makeText(this, "buscar", Toast.LENGTH_SHORT).show();
                    replaceFragment(new SearchFragment(this));
                    break;
                case "Notify":
                    Toast.makeText(this, "notificar", Toast.LENGTH_SHORT).show();
                    replaceFragment(new NotifyFragment());
                    break;
                case "Add":
                    Toast.makeText(this, "agregar", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, PostActivity.class);
                    startActivity(intent);
                    break;
                case "Setting":
                    Toast.makeText(this, "configurar", Toast.LENGTH_SHORT).show();
                    replaceFragment(new SettingFragment(dataUser, sharedPreference, userPrefMap, this));
                    break;
                case "Profile":
                    Toast.makeText(this, "perfilar", Toast.LENGTH_SHORT).show();
                    replaceFragment(new ProfileFragment());//get
                    break;
            }
            return true;
        });
    }

    private  void replaceFragment(Fragment fragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .commit();
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user == null) {
           goToLoginActivity();
        }else{
            getUserData(user);
        }
    }

    private void loadDataUI(Map<String, Object> data, FirebaseUser user){
    }
    public static void setAppLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        context.getResources().updateConfiguration(config, resources.getDisplayMetrics());
    }
    public void setAppTheme(String themeCode) {
        if (themeCode.equals("light")) {
            this.setTheme(R.style.AppTheme_Light);
        } else if (themeCode.equals("dark")) {
            this.setTheme(R.style.AppTheme_Dark);
        }
    }
    public void defaultPreferences(){
        sharedPreference = getApplicationContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        if (sharedPreference.contains("language") & sharedPreference.contains("theme")) {
            userPrefMap = (Map<String, Object>) sharedPreference.getAll();
            setAppLanguage(this, userPrefMap.get("language").toString());
            setAppTheme(userPrefMap.get("theme").toString());
            Toast.makeText(this, "Recupero preferencias", Toast.LENGTH_SHORT).show();
        } else {
            // preferencias por defecto
            Toast.makeText(this, "Creo preferencias", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editPref = sharedPreference.edit();
            editPref.putString("language", "en"); //en, es, ch
            editPref.putString("theme", "dark"); //light, dark
            editPref.apply();
            userPrefMap = (Map<String, Object>) sharedPreference.getAll();
        }
    }

    private void getUserData(FirebaseUser user){
        DocumentReference docRef = db.collection("users").document(user.getUid());

        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        dataUser = documentSnapshot.getData();
                        loadDataUI(dataUser, user);
                    } else {
                        goToSignUpActivity();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, R.string.create_fail,
                        Toast.LENGTH_SHORT).show());
    }

    private void goToSignUpActivity(){
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        intent.putExtra("goToUserForm", true);
        startActivity(intent);
    }

    private void goToLoginActivity(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void postClick(Map<String, Object> item) {
        replaceFragment(new DetailFragment(item, this, this));
    }

    //Metodo del my post adapter: open photo
    @Override
    public void viewDetailMyPost(Map<String, Object> item) {
        replaceFragment(new DetailFragment(item, this, this));
    }
    //abrir
    @Override
    public void btnClickEdit(Map<String, Object> item) {
        replaceFragment(new EditFragment(item, this));
    }

    @Override
    public void editSuccess() {
        replaceFragment(new ProfileFragment());//get
    }
}