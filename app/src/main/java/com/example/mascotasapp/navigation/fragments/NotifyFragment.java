package com.example.mascotasapp.navigation.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mascotasapp.LoginActivity;
import com.example.mascotasapp.NotifyActivity;
import com.example.mascotasapp.R;
import com.example.mascotasapp.navigation.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

public class NotifyFragment extends Fragment {

    Map<String, Object> notify;
    ImageView photo, userPhoto;
    TextView messageText;
    ConstraintLayout form;
    RadioGroup buttons;
    RadioButton btnAccept, btnDecline;
    TextInputLayout  phoneLayout;
    TextInputEditText phone;
    Button send;
    boolean isAccept;
    public NotifyFragment(Map<String, Object> notify) {
        this.notify = notify;
    }
    public NotifyFragment() { }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notify, container, false);
        photo = view.findViewById(R.id.ActNotifyPhoto);
        messageText = view.findViewById(R.id.ActNotifyMessage);
        form = view.findViewById(R.id.ActNotifyForm);
        buttons = view.findViewById(R.id.ActNotifyRadioGroup);
        btnAccept = view.findViewById(R.id.ActNotifyRadioBtnAccept);
        btnDecline = view.findViewById(R.id.ActNotifyRadioBtnDecline);
        phone = view.findViewById(R.id.ActNotifyEditPhone);
        send = view.findViewById(R.id.ActNotifyRadioBtnSend);
        userPhoto = view.findViewById(R.id.ActNotifyUserPhoto);
        phoneLayout = view.findViewById(R.id.ActNotifyInputLayPhone);

        if(notify == null){
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
        }else {

            Uri photoUri = Uri.parse((String) notify.get("photoUrl"));
            Picasso.with(requireContext())
                    .load(photoUri)
                    .resize(100, 100)
                    .into(photo);

            Uri userPhotoUri = Uri.parse((String) notify.get("userPhotoUrl"));
            Picasso.with(requireContext())
                    .load(userPhotoUri)
                    .resize(100,100)
                    .into(userPhoto);

            if(notify.get("type").toString().equals("1")){
                messageText.setText( notify.get("username").toString() + " " + getString(R.string.notify_message_type1));
                buttons.setVisibility(View.VISIBLE);
                btnAccept.setOnClickListener( v -> clickAccept() );
                btnDecline.setOnClickListener( v-> clickDecline() );
            }else {
                if(Boolean.parseBoolean(notify.get("state").toString())){
                    messageText.setText( notify.get("username").toString() + " " + getString(R.string.accept_notification_type2));
                    phoneLayout.setVisibility(View.VISIBLE);
                    phone.setVisibility(View.VISIBLE);
                    phone.setText(notify.get("phone").toString());
                }else {
                    messageText.setText( notify.get("username").toString() + " " + getString(R.string.decline_notification_type2));
                }
            }

        }

        return view;
    }

    public void clickAccept(){
        buttons.setVisibility(View.GONE);
        phoneLayout.setVisibility(View.VISIBLE);
        phone.setVisibility(View.VISIBLE);
        send.setVisibility(View.VISIBLE);
        send.setOnClickListener( v -> notifyAccept() );
    }

    public void clickDecline(){
        buttons.setVisibility(View.GONE);
        notifyDecline();
    }

    public void notifyAccept(){
        isAccept = true;
        String text = phone.getText().toString();
        if(!text.isEmpty()){
            getUser();
        }else {
            Toast.makeText(requireContext(), R.string.input_a_valid_phone, Toast.LENGTH_SHORT).show();
        }
    }

    public void notifyDecline(){
        isAccept = false;
        getUser();
    }

    private void sendNotification(String myUsername, String myToken, String userPhoto){
        RequestQueue myrequest= Volley.newRequestQueue(requireContext());
        JSONObject json = new JSONObject();

        try {
            json.put("to",notify.get("deviceId"));
            JSONObject notificacion = new JSONObject();
            notificacion.put("type", "2");
            notificacion.put("title", R.string.notify_title);
            notificacion.put("photo",notify.get("photoUrl"));
            notificacion.put("username", myUsername);
            notificacion.put("userPhoto", userPhoto);
            if(isAccept){
                notificacion.put("state" , "true");
                notificacion.put("message", R.string.accept_notification_type2);
                notificacion.put("phone", phone.getText());
            }else {
                notificacion.put("state" , "false");
                notificacion.put("message",  R.string.decline_notification_type2);
            }
            notificacion.put("deviceId", myToken);

            json.put("data",notificacion);
            String URL = "https://fcm.googleapis.com/fcm/send";
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
            Intent intent = new Intent(requireContext(), MainActivity.class);
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
                    sendNotification( doc.get("username").toString(), doc.get("deviceId").toString(), doc.get("photoUrl").toString() );
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}