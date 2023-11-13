package com.example.mascotasapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyTarget implements Target {

    private Context context;
    private String fileName;

    public MyTarget(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        // Verificar si la memoria externa está disponible para escribir
// Verificar si la memoria externa está disponible para escribir
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Obtener el directorio de la memoria externa donde se guardarán las imágenes
            File dir = new File(Environment.getExternalStorageDirectory() + "/mascotasApp");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("sada", "Error al crear el directorio");
                    return;
                }
            }

            // Crear un nuevo archivo en el directorio
            File imageFile = new File(dir, fileName);

            try {
                FileOutputStream outputStream = new FileOutputStream(imageFile);

                // Utilizar el objeto bitmap para guardar la imagen en la memoria externa
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                // Notificar al sistema de Android sobre la nueva imagen
                MediaScannerConnection.scanFile(context, new String[]{imageFile.toString()}, null, null);

                // Puedes mostrar un mensaje o realizar cualquier acción adicional después de guardar la imagen
                Toast.makeText(context, "Imagen guardada en la memoria externa", Toast.LENGTH_SHORT).show();

                outputStream.close(); // Asegúrate de cerrar el flujo de salida después de usarlo
            } catch (IOException e) {
                Log.d("sada", e.getMessage());
            }
        }

    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        // Manejar la falla de carga de la imagen
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        // Aquí puedes mostrar un indicador de carga si lo deseas
    }
}
