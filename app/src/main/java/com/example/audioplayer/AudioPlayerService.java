package com.example.audioplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class AudioPlayerService extends Service {
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "AudioPlayerChannel";
    private PendingIntent pendingIntent;
    private boolean isForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializa el reproductor de audio con un archivo en `res/raw`.
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.setLooping(false); // No repetir por defecto
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        if ("PLAY".equals(action)) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } else if ("PAUSE".equals(action)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } else if ("STOP".equals(action)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                stopSelf();
            }
        } else if ("START_FOREGROUND".equals(action)) {
            startForegroundService();
        } else if ("STOP_FOREGROUND".equals(action)) {
            stopForegroundService();
        }

        return START_STICKY;
    }
    private void startForegroundService() {
        if (!isForeground) {
            Notification notification = createNotification();
            startForeground(1, notification);
            isForeground = true;
        }
    }

    private void stopForegroundService() {
        if (isForeground) {
            stopForeground(true);
            isForeground = false;
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audio Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Reproducción de Audio")
                .setContentText("Reproduciendo audio en segundo plano")
                .setSmallIcon(R.drawable.baseline_audiotrack_24)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.baseline_pause_24, "Pausar", getActionPendingIntent("PAUSE"))
                .addAction(R.drawable.baseline_play_arrow_24, "Reproducir", getActionPendingIntent("PLAY"))
                .addAction(R.drawable.baseline_stop_24, "Detener", getActionPendingIntent("STOP"))
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Método para generar PendingIntent con acciones específicas
    private PendingIntent getActionPendingIntent(String action) {
        Intent actionIntent = new Intent(this, AudioPlayerService.class);
        actionIntent.setAction(action);
        return PendingIntent.getService(
                this,
                0,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
