package com.example.mascotasapp.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.PostActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.navigation.fragments.NotifyFragment;
import com.example.mascotasapp.navigation.fragments.ProfileFragment;
import com.example.mascotasapp.navigation.fragments.SearchFragment;
import com.example.mascotasapp.navigation.fragments.SettingFragment;
import com.example.mascotasapp.signup.SignUpActivity;
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

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppLanguage(this, "en");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        frameLayout = findViewById(R.id.frame_layout);

        replaceFragment(new SearchFragment());

        bottomNavigationView.setOnItemSelectedListener(item  -> {

            switch (item.getTitle().toString()) {
                case "Search":
                    Toast.makeText(this, "buscar", Toast.LENGTH_SHORT).show();
                    replaceFragment(new SearchFragment());
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
                    replaceFragment(new SettingFragment());
                    break;
                case "Profile":
                    Toast.makeText(this, "perfilar", Toast.LENGTH_SHORT).show();
                    replaceFragment(new ProfileFragment());
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
    private void signOut() {
        mAuth.signOut();
        goToLoginActivity();
    }
    private void updateUI(FirebaseUser user) {
        if (user == null) {
           goToLoginActivity();
        }else{
            getUserData(user);
        }
    }

    private void loadDataUI(Map<String, Object> data, FirebaseUser user){
        /*
        emailText.setText(user.getEmail());
        idText.setText(user.getUid());
        usernameText.setText((String)data.get("username"));
        Timestamp timestamp = (Timestamp) data.get("birthdate");
        Date birthdate = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String birthdateString = sdf.format(birthdate);
        birthDateText.setText(birthdateString);
    */}
    public static void setAppLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        context.getResources().updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void getUserData(FirebaseUser user){
        DocumentReference docRef = db.collection("users").document(user.getUid());

        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> data = documentSnapshot.getData();
                            loadDataUI(data, user);
                        } else {
                            goToSignUpActivity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, R.string.create_fail,
                                Toast.LENGTH_SHORT).show();
                    }
                });
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

}