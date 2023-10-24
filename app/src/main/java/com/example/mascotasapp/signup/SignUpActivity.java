package com.example.mascotasapp.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.signup.fragments.RegisterAuthFragment;
import com.example.mascotasapp.signup.fragments.RegisterUserFragment;
import com.example.mascotasapp.utils.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity implements RegisterAuthFragment.AuthCreateListener {
    FragmentManager fragmentManager = getSupportFragmentManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        RegisterAuthFragment registerAuthFragment = new RegisterAuthFragment();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container_reg, registerAuthFragment)
                .commit();
    }  

    @Override
    public void authCreate() {
        RegisterUserFragment registerUserFragment = new RegisterUserFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_reg, registerUserFragment)
                .commit();
    }
}