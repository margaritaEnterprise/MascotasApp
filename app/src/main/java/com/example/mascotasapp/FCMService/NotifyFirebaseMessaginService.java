package com.example.mascotasapp.FCMService;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.mascotasapp.NotifyActivity;
import com.example.mascotasapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.io.IOException;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class NotifyFirebaseMessaginService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessageService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage){
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        // Acceder a los datos del mensaje usando remoteMessage.getData()
        String type = remoteMessage.getData().get("type");
        String title = remoteMessage.getData().get("title");
        String photo = remoteMessage.getData().get("photo");
        String message = remoteMessage.getData().get("message");
        String deviceId = remoteMessage.getData().get("deviceId");
        String username = remoteMessage.getData().get("username");
        boolean state = false;
        if (type.equals("2")) {
            state = Boolean.parseBoolean(remoteMessage.getData().get("state"));
        }
        // Muestra la notificación o realiza otras acciones según tus necesidades
        showNotification(type, title, photo, message, deviceId, username, state);
    }

    private void showNotification(String type, String title, String photo, String message, String deviceId, String username, boolean state) {
        // Crear un Intent para la actividad principal de la aplicación
        Intent intent = new Intent(this, NotifyActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("title", title);
        intent.putExtra("photoUrl", photo);
        intent.putExtra("message", message);
        intent.putExtra("deviceId", deviceId);
        intent.putExtra("username", username);
        intent.putExtra("state", state);
        // Asegúrate de que la bandera PendingIntent.FLAG_UPDATE_CURRENT se establezca para actualizar los extras si la notificación se ha creado previamente
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);


        // Crear un RemoteViews para la vista compacta
        @SuppressLint("RemoteViewLayout") RemoteViews notificationLayoutCompact = new RemoteViews(getPackageName(), R.layout.notification_layout_compact);
        notificationLayoutCompact.setTextViewText(R.id.notification_title, title);
        if (type.equals("1")) {
            message = getString(R.string.notify_message_type1);
        } else if (type.equals("2") && state) {
            message = getString(R.string.accept_notification_type2);
        } else {
            message = getString(R.string.decline_notification_type2);
        }
        notificationLayoutCompact.setTextViewText(R.id.notification_message, username + " " + message);

        // Crear un RemoteViews para la vista expandida
        @SuppressLint("RemoteViewLayout") RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_layout_expanded);
        notificationLayoutExpanded.setTextViewText(R.id.notification_title, title);
        notificationLayoutExpanded.setTextViewText(R.id.notification_message,username + " " + message);

        // Cargar la imagen usando Picasso en ambas vistas
        if (photo != null && !photo.isEmpty()) {
            Bitmap bitmap = getBitmapFromUrl(photo);
            if (bitmap != null) {
                notificationLayoutCompact.setImageViewBitmap(R.id.notification_image, bitmap);
                notificationLayoutExpanded.setImageViewBitmap(R.id.notification_image, bitmap);
            } else {
                Log.e(TAG, "Error al cargar la imagen desde la URL");
            }
        }

        // Construir la notificación con las vistas compacta y expandida
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_notify)
                .setCustomContentView(notificationLayoutCompact)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Obtener el servicio de notificación
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Comprobar si la versión de Android es mayor o igual a Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Crear un canal de notificación para versiones de Android 8.0 y superiores
            NotificationChannel channel = new NotificationChannel("channel_id", "Nombre del canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Mostrar la notificación
        notificationManager.notify(0, notificationBuilder.build());
    }


// Resto del código...


    private Bitmap getBitmapFromUrl(String url) {
        try {
            return Picasso.with(this).load(url).resize(50, 50).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
