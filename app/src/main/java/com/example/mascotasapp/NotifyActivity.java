package com.example.mascotasapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mascotasapp.navigation.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


public class NotifyActivity extends AppCompatActivity {

    String username, photoUrl, type, message, deviceId;
    ImageView photo;
    TextView messageText;
    ConstraintLayout form;
    RadioGroup buttons;
    RadioButton btnAccept, btnDecline;
    EditText phone;
    Button send;
    boolean isAccept;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        photo = findViewById(R.id.ActNotifyPhoto);
        messageText = findViewById(R.id.ActNotifyMessage);
        form = findViewById(R.id.ActNotifyForm);
        buttons = findViewById(R.id.ActNotifyRadioGroup);
        btnAccept = findViewById(R.id.ActNotifyRadioBtnAccept);
        btnDecline = findViewById(R.id.ActNotifyRadioBtnDecline);
        phone = findViewById(R.id.ActNotifyEditPhone);
        send = findViewById(R.id.ActNotifyRadioBtnSend);

        if (getIntent().getExtras() != null) {
             username = getIntent().getStringExtra("username");
             photoUrl = getIntent().getStringExtra("photoUrl");
             type = getIntent().getStringExtra("type");
             message = getIntent().getStringExtra("message");
             deviceId = getIntent().getStringExtra("deviceId");

             Uri photoUri = Uri.parse(photoUrl);
             Picasso.with(this)
                    .load(photoUri)
                    .resize(100, 100)
                    .into(photo);

             messageText.setText(username + " " + message);

             btnAccept.setOnClickListener( v -> clickAccept() );
             btnDecline.setOnClickListener( v-> clickDecline() );
        }
        else {
            Intent intent = new Intent(NotifyActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void clickAccept(){
        buttons.setVisibility(View.GONE);
        form.setVisibility(View.VISIBLE);
        send.setOnClickListener( v -> notifyAccept() );
    }

    public void clickDecline(){
        buttons.setVisibility(View.GONE);
        notifyDecline();
    }

    public void notifyAccept(){
        isAccept = true;
        getUser();
    }

    public void notifyDecline(){
        isAccept = false;
        getUser();
    }

    private void sendNotification(String myUsername, String myToken){
        RequestQueue myrequest= Volley.newRequestQueue(this);
        JSONObject json = new JSONObject();

        try {
            json.put("to",deviceId);
            JSONObject notificacion = new JSONObject();
            notificacion.put("type", "2");
            notificacion.put("title", "Nueva notificación");
            notificacion.put("photo",photoUrl);
            notificacion.put("username", myUsername);
            if(isAccept){
                notificacion.put("state" , "true");
                notificacion.put("message", "acepto la comunicacion");
                notificacion.put("phone", phone.getText());
            }else {
                notificacion.put("state" , "false");
                notificacion.put("message",  "rechazo la comunicacion");
            }
            notificacion.put("deviceId", myToken);

            json.put("data",notificacion);
            String URL="https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,URL,json,null,null){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String>header=new HashMap<>();
                    header.put("Content-Type","application/json");
                    header.put("Authorization","key=AAAA05seiD0:APA91bH38cHnWgsv8dFJmNm5dizN8ey9_4BoaNWUehehdf-A2WRAF9hVtkxF6ojs6DdwO7gj27xAzW3nJH1G-2sdxCmDyZtmSf39EptgL64Fa6PKDBkzVsS76OPTrCS0svZxdjI9W2hS");
                    return header;
                }
            };
            myrequest.add(request);
            Intent intent = new Intent(NotifyActivity.this, MainActivity.class);
            startActivity(intent);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void getUser(){
        CollectionReference collections = FirebaseFirestore.getInstance().collection("users");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = collections.whereEqualTo("id", userId);
        query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    sendNotification( doc.get("username").toString(), doc.get("deviceId").toString() );
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}