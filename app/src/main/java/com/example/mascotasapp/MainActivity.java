package com.example.mascotasapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mascotasapp.signup.SignUpActivity;
import com.example.mascotasapp.signup.fragments.RegisterAuthFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Button signOutButton;
    TextView emailText, idText, usernameText, birthDateText;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppLanguage(this, "en");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailText = findViewById(R.id.text_email);
        idText = findViewById(R.id.text_id);
        usernameText = findViewById(R.id.text_username);
        birthDateText = findViewById(R.id.text_birthdate);
        signOutButton = findViewById(R.id.signOutButton);

        //navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item  -> {
            //Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show();

            switch (item.getTitle().toString()) {
                case "Search":
                    Toast.makeText(this, "buscar", Toast.LENGTH_SHORT).show();
                    break;
                case "Notify":
                    Toast.makeText(this, "notificar", Toast.LENGTH_SHORT).show();
                    break;
                case "Add":
                    Toast.makeText(this, "agregar", Toast.LENGTH_SHORT).show();
                    break;
                case "Setting":
                    Toast.makeText(this, "configurar", Toast.LENGTH_SHORT).show();
                    break;
                case "Profile":
                    Toast.makeText(this, "perfilar", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
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
        emailText.setText(user.getEmail());
        idText.setText(user.getUid());
        usernameText.setText((String)data.get("username"));
        Timestamp timestamp = (Timestamp) data.get("birthdate");
        Date birthdate = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String birthdateString = sdf.format(birthdate);
        birthDateText.setText(birthdateString);
    }
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