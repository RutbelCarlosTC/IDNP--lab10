package com.example.audioplayer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        // Detiene el servicio en segundo plano al regresar a la app
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction("STOP_FOREGROUND");
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Inicia el servicio en segundo plano al salir de la app
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction("START_FOREGROUND");
        startService(intent);
    }
}