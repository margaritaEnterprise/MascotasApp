package com.example.mascotasapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mascotasapp.utils.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button signInButton;
    Button signUpButton;
    LinearLayout linearLayout;
    EditText emailForm;
    EditText passwordForm;
    Validator validator = new Validator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppLanguage(this, "es");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        linearLayout = findViewById(R.id.loginForm);
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
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signUp(){
        String email = emailForm.getText().toString();
        String password = passwordForm.getText().toString();
        //validate
        if(!validator.ValidateAuth(email, password)){
            Toast.makeText(LoginActivity.this, validator.errorMsg,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Create user failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }
    public static void setAppLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        context.getResources().updateConfiguration(config, resources.getDisplayMetrics());
    }
    private void updateUI(FirebaseUser user) {

        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            Bundle b = new Bundle();
            b.putString("mail", user.getEmail());
            b.putString("id", user.getUid());
            intent.putExtras(b);
            startActivity(intent);

        }
    }
}
