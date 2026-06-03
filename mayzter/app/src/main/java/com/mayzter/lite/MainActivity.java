package com.mayzter.lite;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private EditText etMinMxnKm, etMinMxnH;
    private TextView tvStatusNotif, tvStatusOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("mayzter", MODE_PRIVATE);
        etMinMxnKm = findViewById(R.id.etMinMxnKm);
        etMinMxnH  = findViewById(R.id.etMinMxnH);
        tvStatusNotif  = findViewById(R.id.tvStatusNotif);
        tvStatusOverlay = findViewById(R.id.tvStatusOverlay);

        // Cargar valores guardados
        etMinMxnKm.setText(String.valueOf(prefs.getFloat("min_mxn_km", 8.2f)));
        etMinMxnH.setText(String.valueOf(prefs.getFloat("min_mxn_h", 150f)));

        findViewById(R.id.btnSave).setOnClickListener(v -> saveSettings());
        findViewById(R.id.btnPermitNotif).setOnClickListener(v -> openNotificationSettings());
        findViewById(R.id.btnPermitOverlay).setOnClickListener(v -> openOverlaySettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusIndicators();
    }

    private void saveSettings() {
        try {
            float minKm = Float.parseFloat(etMinMxnKm.getText().toString());
            float minH  = Float.parseFloat(etMinMxnH.getText().toString());
            prefs.edit()
                .putFloat("min_mxn_km", minKm)
                .putFloat("min_mxn_h", minH)
                .apply();
            Toast.makeText(this, "✅ Configuración guardada", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresa números válidos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatusIndicators() {
        boolean notifOk = isNotificationListenerEnabled();
        boolean overlayOk = Settings.canDrawOverlays(this);

        tvStatusNotif.setText(notifOk ? "✅ Notificaciones: Activado" : "❌ Notificaciones: Desactivado");
        tvStatusNotif.setTextColor(notifOk ? 0xFF4CAF50 : 0xFFF44336);

        tvStatusOverlay.setText(overlayOk ? "✅ Overlay: Activado" : "❌ Overlay: Desactivado");
        tvStatusOverlay.setTextColor(overlayOk ? 0xFF4CAF50 : 0xFFF44336);
    }

    private boolean isNotificationListenerEnabled() {
        String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        return flat != null && flat.contains(getPackageName());
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }

    private void openOverlaySettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
}
