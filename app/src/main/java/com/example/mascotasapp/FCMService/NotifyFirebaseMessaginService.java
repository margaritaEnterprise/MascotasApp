package com.example.mascotasapp.FCMService;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotifyFirebaseMessaginService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessageService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message){
        super.onMessageReceived(message);
    }
}
