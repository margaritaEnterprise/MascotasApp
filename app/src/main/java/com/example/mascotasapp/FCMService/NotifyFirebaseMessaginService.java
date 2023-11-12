package com.example.mascotasapp.FCMService;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.mascotasapp.R;
import com.example.mascotasapp.navigation.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class NotifyFirebaseMessaginService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessageService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage){
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Puedes acceder a los datos del mensaje usando remoteMessage.getData()
            String message = remoteMessage.getData().get("message");

            // Muestra la notificación o realiza otras acciones según tus necesidades
            showNotification(message);

    }
    private void showNotification(String message) {
        // Crear un Intent para la actividad principal de la aplicación
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Construir la notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle("Título de la notificación")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Obtener el servicio de notificación
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Mostrar la notificación
        notificationManager.notify(0, notificationBuilder.build());
    }

}
