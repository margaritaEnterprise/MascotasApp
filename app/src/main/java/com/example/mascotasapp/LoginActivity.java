package com.example.mascotasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mascotasapp.navigation.MainActivity;
import com.example.mascotasapp.signup.SignUpActivity;
import com.example.mascotasapp.utils.ManagerTheme;
import com.example.mascotasapp.utils.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    Button signInButton;
    Button signUpButton;
    LinearLayout linearLayout;
    EditText emailForm;
    EditText passwordForm;
    Validator validator = new Validator();
    Map<String,Object> userPrefMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userPrefMap = ManagerTheme.getUserPreference(this);
        ManagerTheme.setUserPreference(this, userPrefMap);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //linearLayout = findViewById(R.id.loginForm);
        signInButton = findViewById(R.id.signInButton);

        signUpButton = findViewById(R.id.signUpButton);
        emailForm = findViewById(R.id.emailForm);
        passwordForm = findViewById(R.id.passwordForm);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn() {

        String email = emailForm.getText().toString();
        String password = passwordForm.getText().toString();

        //validate
        if(!validator.ValidateAuth(email, password)){
            Toast.makeText(LoginActivity.this, validator.errorMsg,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.auth_fail,
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signUp(){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
