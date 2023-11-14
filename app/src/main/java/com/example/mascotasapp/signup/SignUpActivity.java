package com.example.mascotasapp.signup;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.mascotasapp.R;
import com.example.mascotasapp.signup.fragments.RegisterAuthFragment;
import com.example.mascotasapp.signup.fragments.RegisterUserFragment;
import com.example.mascotasapp.utils.ManagerTheme;

import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements RegisterAuthFragment.AuthCreateListener {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Map<String,Object> userPrefMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userPrefMap = ManagerTheme.getUserPreference(this);
        ManagerTheme.setUserPreference(this, userPrefMap);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Intent intent = getIntent();
        boolean goToUserForm = intent.getBooleanExtra("goToUserForm", false);

        if(!goToUserForm){
            RegisterAuthFragment registerAuthFragment = new RegisterAuthFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container_reg, registerAuthFragment)
                    .commit();
        }else {
            RegisterUserFragment registerUserFragment = new RegisterUserFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_reg, registerUserFragment)
                    .commit();
        }

    }  

    @Override
    public void authCreate() {
        RegisterUserFragment registerUserFragment = new RegisterUserFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_reg, registerUserFragment)
                .commit();
    }
}