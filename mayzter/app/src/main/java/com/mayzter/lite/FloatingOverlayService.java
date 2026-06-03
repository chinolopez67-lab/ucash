package com.mayzter.lite;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Locale;

public class FloatingOverlayService extends Service implements TextToSpeech.OnInitListener {

    private static final String CHANNEL_ID = "mayzter_overlay";
    private WindowManager windowManager;
    private View floatingView;
    private TextToSpeech tts;
    private String pendingSpeech;
    private boolean ttsReady = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        tts = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String displayText = intent.getStringExtra("displayText");
        String speechText  = intent.getStringExtra("speechText");
        String bgColor     = intent.getStringExtra("bgColor");

        showOverlay(displayText, bgColor);

        if (ttsReady) {
            speak(speechText);
        } else {
            pendingSpeech = speechText;
        }

        return START_NOT_STICKY;
    }

    private void showOverlay(String htmlText, String bgColor) {
        // Remover overlay anterior si existe
        if (floatingView != null) {
            try { windowManager.removeView(floatingView); } catch (Exception ignored) {}
            floatingView = null;
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_view, null);
        TextView tv = floatingView.findViewById(R.id.tvOverlay);
        tv.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT));
        floatingView.setBackgroundColor(Color.parseColor(bgColor));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 20;
        params.y = 80;

        windowManager.addView(floatingView, params);

        // Auto-ocultar después de 7 segundos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (floatingView != null) {
                try { windowManager.removeView(floatingView); } catch (Exception ignored) {}
                floatingView = null;
            }
        }, 7000);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(new Locale("es", "MX"));
            tts.setSpeechRate(1.4f);
            ttsReady = true;
            if (pendingSpeech != null) {
                speak(pendingSpeech);
                pendingSpeech = null;
            }
        }
    }

    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mayzter_" + System.currentTimeMillis());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (floatingView != null) {
            try { windowManager.removeView(floatingView); } catch (Exception ignored) {}
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        NotificationChannel ch = new NotificationChannel(
            CHANNEL_ID, "MAYZTER LITE", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
    }

    private Notification buildNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("MAYZTER LITE activo")
            .setContentText("Analizando viajes de Uber y DiDi")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build();
    }
}
