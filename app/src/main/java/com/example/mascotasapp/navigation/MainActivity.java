package com.example.mascotasapp.navigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.NotifyActivity;
import com.example.mascotasapp.PostActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.navigation.fragments.DetailFragment;
import com.example.mascotasapp.navigation.fragments.EditFragment;
import com.example.mascotasapp.navigation.fragments.NotifyFragment;
import com.example.mascotasapp.navigation.fragments.ProfileFragment;
import com.example.mascotasapp.navigation.fragments.SearchFragment;
import com.example.mascotasapp.navigation.fragments.SettingFragment;
import com.example.mascotasapp.navigation.fragments.ToolbarFragment;
import com.example.mascotasapp.signup.SignUpActivity;
import com.example.mascotasapp.utils.ManagerTheme;
import com.example.mascotasapp.utils.MyPostAdapter;
import com.example.mascotasapp.utils.PostAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PostAdapter.PostClickListener, MyPostAdapter.PostClickListener, DetailFragment.ButtonEdit, EditFragment.BackToProfile {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; //viewDetailMyPost
    BottomNavigationView bottomNavigationView;
    Map<String, Object> dataUser;
    Map<String, Object> userPrefMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userPrefMap = ManagerTheme.getUserPreference(this);
        ManagerTheme.setUserPreference(this, userPrefMap);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registrarDispositivo();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item  -> {
            switch (Objects.requireNonNull(item.getTitle()).toString()) {
                case "Search":
                    replaceFragment(new SearchFragment(this));
                    break;
                case "Notify":
                    replaceFragment(new NotifyFragment());
                    break;
                case "Add":
                    Intent intent = new Intent(MainActivity.this, PostActivity.class);
                    startActivity(intent);
                    break;
                case "Setting":
                    replaceFragment(new SettingFragment(dataUser, MainActivity.this));
                    break;
                case "Profile":
                    replaceFragment(new ProfileFragment(dataUser));
                    break;
            }
            return true;
        });
        if (getIntent().getExtras() != null) {
            String fragment = getIntent().getStringExtra("fragment");
            if(fragment.equals("1")){
                String username = getIntent().getStringExtra("username");
                String photoUrl = getIntent().getStringExtra("photoUrl");
                String type = getIntent().getStringExtra("type");
                String message = getIntent().getStringExtra("message");
                String deviceId = getIntent().getStringExtra("deviceId");
                String userPhotoUrl = getIntent().getStringExtra("userPhotoUrl");
                String phone = getIntent().getStringExtra("phone");
                boolean state = !phone.isEmpty();
                Map<String, Object> notify= new HashMap<>();
                notify.put("username", username);
                notify.put("photoUrl", photoUrl);
                notify.put("type", type);
                notify.put("message", message);
                notify.put("deviceId", deviceId);
                notify.put("userPhotoUrl", userPhotoUrl);
                notify.put("phone", phone);
                notify.put("state", state);
                replaceFragment(new NotifyFragment(notify));
                return;
            }
        }
        replaceFragment(new SearchFragment(this));
    }
    private  void ponerToolbar() {
        Uri uri = Uri.parse(dataUser.get("photoUrl").toString());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_toolbar, new ToolbarFragment(uri))
                .commit();
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

    private void getUserData(FirebaseUser user){
        DocumentReference docRef = db.collection("users").document(user.getUid());

        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        dataUser = documentSnapshot.getData();
                        ponerToolbar();

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
        replaceFragment(new ProfileFragment(dataUser));//get
    }

    public void registrarDispositivo(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()){
                        Log.w("Messaging", "Error al obtener el token");
                        return;
                    }
                    String token = task.getResult();
                    String tokenGuardado = getSharedPreferences( "SP_FILE", 0)
                            .getString("DEVICEID", null);
                    if(token != null){
                        if(!token.equals(tokenGuardado)){
                            guardarDeviceId(token);
                        }
                    }
                });
    }

    public void guardarDeviceId(String token){
        SharedPreferences sharedPreference = getApplicationContext()
                .getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DocumentReference currentDocument = db.collection("users").document(userId);
        Map<String, Object> userChanges = new HashMap<>();
        userChanges.put("deviceId", token);

        currentDocument
                .update(userChanges)
                .addOnSuccessListener(v -> {
                    SharedPreferences.Editor editPref = sharedPreference.edit();
                    editPref.putString("DEVICEID",token);
                    editPref.apply();
                })
                .addOnFailureListener(v -> Log.w("Messaging", "No se guardo deviceId"));
    }

}