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
import com.example.mascotasapp.navigation.MainActivity;
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

        String type = remoteMessage.getData().get("type");
        String title = remoteMessage.getData().get("title");
        String photo = remoteMessage.getData().get("photo");
        String message = remoteMessage.getData().get("message");
        String deviceId = remoteMessage.getData().get("deviceId");
        String username = remoteMessage.getData().get("username");
        String userPhoto = remoteMessage.getData().get("userPhoto");
        boolean state = false;
        String phone = "";
        if (type.equals("2")) {
            state = Boolean.parseBoolean(remoteMessage.getData().get("state"));
            if(state){
                phone = remoteMessage.getData().get("phone");
            }
        }
        showNotification(type, title, photo, message, deviceId, username, state, userPhoto, phone);
    }

    private void showNotification(String type, String title, String photo, String message, String deviceId, String username, boolean state,String userPhoto, String phone) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment", "1");
        intent.putExtra("type", type);
        intent.putExtra("title", title);
        intent.putExtra("photoUrl", photo);
        intent.putExtra("message", message);
        intent.putExtra("deviceId", deviceId);
        intent.putExtra("username", username);
        intent.putExtra("notifyState", state);
        intent.putExtra("userPhotoUrl", userPhoto);
        intent.putExtra("phone", phone);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);
        title = getString(R.string.notify_title);
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

        @SuppressLint("RemoteViewLayout") RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_layout_expanded);
        notificationLayoutExpanded.setTextViewText(R.id.notification_title, title);
        notificationLayoutExpanded.setTextViewText(R.id.notification_message,username + " " + message);

        if (photo != null && !photo.isEmpty()) {
            Bitmap bitmap = getBitmapFromUrl(photo);
            if (bitmap != null) {
                notificationLayoutCompact.setImageViewBitmap(R.id.notification_image, bitmap);
                notificationLayoutExpanded.setImageViewBitmap(R.id.notification_image, bitmap);
            } else {
                Log.e(TAG, "Error al cargar la imagen desde la URL");
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomContentView(notificationLayoutCompact)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Nombre del canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }


        notificationManager.notify(0, notificationBuilder.build());
    }


    private Bitmap getBitmapFromUrl(String url) {
        try {
            return Picasso.with(this).load(url).resize(50, 50).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
