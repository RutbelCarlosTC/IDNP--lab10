package com.example.audioplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {

    SeekBar seekBar;
    TextView passTimeTextView;
    TextView dueTimeTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        passTimeTextView = findViewById(R.id.passTime);
        dueTimeTextView = findViewById(R.id.dueTime);
        findViewById(R.id.btnPlay).setOnClickListener(v -> controlAudio("PLAY"));
        findViewById(R.id.btnPause).setOnClickListener(v -> controlAudio("PAUSE"));
        findViewById(R.id.btnStop).setOnClickListener(v -> {
            // Detener la reproducción de audio
            controlAudio("STOP");
            passTimeTextView.setText("00:00");
            // Establecer el SeekBar a 0
            seekBar.setProgress(0);
        });


        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(100);  // Asumimos que el máximo es 100

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Si el usuario mueve el SeekBar, cambia la posición del audio
                    Intent intent = new Intent(MainActivity.this, AudioPlayerService.class);
                    intent.setAction("SEEK");
                    intent.putExtra("seek_position", progress);
                    startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Registrar el receiver para escuchar el progreso de la música
        LocalBroadcastManager.getInstance(this).registerReceiver(progressReceiver,
                new android.content.IntentFilter("AUDIO_PROGRESS"));
    }

    private void controlAudio(String action) {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(action);
        startService(intent);
    }

    // BroadcastReceiver para recibir el progreso del audio
    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress", 0);
            String passTime = intent.getStringExtra("passTime");
            String dueTime = intent.getStringExtra("dueTime");

            // Actualizar SeekBar
            seekBar.setProgress(progress);
            // Actualizar los TextViews de tiempo

            passTimeTextView.setText(passTime);  // Tiempo transcurrido
            dueTimeTextView.setText(dueTime);    // Duración total
        }
    };

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(progressReceiver);
    }
}
