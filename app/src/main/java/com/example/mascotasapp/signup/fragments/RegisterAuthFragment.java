package com.example.mascotasapp.signup.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mascotasapp.R;
import com.example.mascotasapp.utils.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterAuthFragment extends Fragment {
    public interface AuthCreateListener{
        void authCreate();
    }
    AuthCreateListener authCreateListener;
    FirebaseAuth mAuth;
    Button signUpButton;
    EditText emailForm;
    EditText passwordForm;
    EditText passwordForm2;
    Validator validator = new Validator();

    public RegisterAuthFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_auth, container, false);
        mAuth = FirebaseAuth.getInstance();

        emailForm = view.findViewById(R.id.emailForm);
        passwordForm = view.findViewById(R.id.passwordForm);
        passwordForm2 = view.findViewById(R.id.passwordForm2);
        signUpButton = view.findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(v -> signUp());
        return view;
    }

    private void signUp(){
        String email = emailForm.getText().toString();
        String password = passwordForm.getText().toString();
        String password2 = passwordForm2.getText().toString();
        //validate
        if(!password.equals(password2)){
            Toast.makeText(requireContext(), R.string.pass_dont_match,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(!validator.ValidateAuth(email, password)){
            Toast.makeText(requireContext(), validator.errorMsg,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser userAuth = mAuth.getCurrentUser();
                            //
                            authCreateListener.authCreate();

                            Toast.makeText(requireActivity(), R.string.create_user,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), R.string.create_fail,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        if(context instanceof AuthCreateListener) {
            authCreateListener = (AuthCreateListener) context;
        } else {
            throw new ClassCastException();
        }
    }
}
