package com.example.audioplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class AudioPlayerService extends Service {
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "AudioPlayerChannel";
    private PendingIntent pendingIntent;
    private boolean isForeground = false;
    private Handler handler = new Handler();
    private Runnable updateProgressRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializa el reproductor de audio con un archivo en `res/raw`
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.setLooping(false); // No repetir por defecto
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if ("PLAY".equals(action)) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                startForegroundService();
                startProgressUpdate();
            }
        } else if ("PAUSE".equals(action)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                stopProgressUpdate();
            }
        } else if ("STOP".equals(action)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                stopSelf();
                stopProgressUpdate();
            }
        } else if ("START_FOREGROUND".equals(action)) {
            startForegroundService();
        } else if ("STOP_FOREGROUND".equals(action)) {
            stopForegroundService();
        } else if ("SEEK".equals(action)) {
            int seekPosition = intent.getIntExtra("seek_position", 0);
            mediaPlayer.seekTo(seekPosition * mediaPlayer.getDuration() / 100); // Calcular la posición proporcional
        }

        updateNotification();
        return START_STICKY;
    }

    private void startForegroundService() {
        if (!isForeground) {
            Log.d("AudioPlayerService", "Iniciando servicio en primer plano");
            Notification notification = createNotification();
            startForeground(1, notification);
            isForeground = true;
        }
    }

    private void stopForegroundService() {
        if (isForeground) {
            Log.d("AudioPlayerService", "Deteniendo servicio en primer plano");
            stopForeground(true);
            isForeground = false;
        }
    }

    private Notification createNotification() {
        Log.d("AudioPlayerService", "Creando notificación");
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

    private void startProgressUpdate() {
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    // Progreso del audio como porcentaje
                    int progress = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();

                    // Calcular tiempo transcurrido (passTime)
                    int currentPosition = mediaPlayer.getCurrentPosition(); // En milisegundos
                    String passTime = formatTime(currentPosition); // Convertir a formato "mm:ss"

                    // Calcular duración total (dueTime)
                    int totalDuration = mediaPlayer.getDuration(); // En milisegundos
                    String dueTime = formatTime(totalDuration); // Convertir a formato "mm:ss"

                    // Enviar los tiempos al MainActivity
                    sendProgressToActivity(progress, passTime, dueTime);

                    // Actualizar cada segundo
                    handler.postDelayed(this, 250);
                }
            }
        };
        handler.post(updateProgressRunnable);
    }

    private String formatTime(int timeInMillis) {
        int minutes = (timeInMillis / 1000) / 60;
        int seconds = (timeInMillis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void sendProgressToActivity(int progress, String passTime, String dueTime) {
        Intent intent = new Intent("AUDIO_PROGRESS");
        intent.putExtra("progress", progress);
        intent.putExtra("passTime", passTime);
        intent.putExtra("dueTime", dueTime);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void stopProgressUpdate() {
        if (updateProgressRunnable != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    private void sendProgressToActivity(int progress) {
        Intent intent = new Intent("AUDIO_PROGRESS");
        intent.putExtra("progress", progress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopProgressUpdate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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

    private void updateNotification() {
        // Aquí se puede actualizar la notificación para mostrar el progreso de la canción.
        int progress = (mediaPlayer.isPlaying()) ? mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration() : 0;

        Notification notification = createNotification();
        startForeground(1, notification); // Actualiza la notificación
    }
}
