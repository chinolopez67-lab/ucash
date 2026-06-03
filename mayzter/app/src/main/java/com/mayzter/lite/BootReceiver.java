package com.mayzter.lite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // El NotificationListenerService se inicia automáticamente si el permiso está dado
            // Solo necesitamos asegurarnos que nada bloquea el servicio
        }
    }
}
