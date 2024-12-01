package com.example.audioplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
     private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, R.raw.music);

        findViewById(R.id.btnPlay).setOnClickListener(v -> controlAudio("PLAY"));
        findViewById(R.id.btnPause).setOnClickListener(v -> controlAudio("PAUSE"));
        findViewById(R.id.btnStop).setOnClickListener(v -> controlAudio("STOP"));

    }

    private void controlAudio(String action) {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(action);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Detiene el servicio en primer plano al regresar a la app
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction("STOP_FOREGROUND");
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Inicia el servicio en primer plano al salir de la app
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction("START_FOREGROUND");
        startService(intent);
    }

    private void startForegroundService() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        startService(intent);
    }

    private void stopForegroundService() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        stopService(intent);
    }
}