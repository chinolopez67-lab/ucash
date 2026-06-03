package com.mayzter.lite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TripNotificationListener extends NotificationListenerService {

    private static final String TAG = "MayzterLite";

    // Paquetes de Uber y DiDi conductor
    private static final String PKG_DIDI = "com.didiglobal.driver";
    private static final String PKG_UBER = "com.ubercab.driver";
    private static final String PKG_UBER2 = "com.ubercab.eats"; // por si acaso

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();

        if (!pkg.equals(PKG_DIDI) && !pkg.equals(PKG_UBER) && !pkg.equals(PKG_UBER2)) {
            return;
        }

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        String title = extras.getString("android.title", "");
        String text  = extras.getString("android.text", "");
        String bigText = extras.getString("android.bigText", "");

        String fullText = title + " " + text + " " + bigText;
        Log.d(TAG, "Notif de " + pkg + ": " + fullText);

        // Intentar parsear datos del viaje
        TripData trip = parseTripData(fullText, pkg);

        if (trip != null && trip.isValid()) {
            Log.d(TAG, "Viaje detectado: " + trip);
            analyzeAndShow(trip, pkg);
        }
    }

    private TripData parseTripData(String text, String pkg) {
        TripData trip = new TripData();

        // ---- Precio MXN ----
        // Patrones: "MXN$259.76", "MXN 259.76", "$259", "259.76"
        Pattern pricePattern = Pattern.compile("(?:MXN\\$?|\\$)\\s*(\\d+(?:\\.\\d{1,2})?)");
        Matcher m = pricePattern.matcher(text);
        if (m.find()) {
            trip.priceMXN = Double.parseDouble(m.group(1));
        }

        // ---- Kilómetros al pasajero ----
        // Patrones: "7.1km", "7.1 km", "1-3 min" (DiDi a veces usa minutos)
        Pattern kmPasPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*km", Pattern.CASE_INSENSITIVE);
        Matcher km = kmPasPattern.matcher(text);
        if (km.find()) trip.kmPasajero = Double.parseDouble(km.group(1));
        if (km.find()) trip.kmViaje    = Double.parseDouble(km.group(1));

        // ---- Minutos al pasajero ----
        // Patrones: "10min", "10 min", "A 1-3 min"
        Pattern minPasPattern = Pattern.compile("(\\d+)(?:-\\d+)?\\s*min", Pattern.CASE_INSENSITIVE);
        Matcher minM = minPasPattern.matcher(text);
        if (minM.find()) trip.minPasajero = Integer.parseInt(minM.group(1));
        if (minM.find()) trip.minViaje    = Integer.parseInt(minM.group(1));

        trip.appName = pkg.contains("didi") ? "DiDi" : "Uber";
        return trip;
    }

    private void analyzeAndShow(TripData trip, String pkg) {
        SharedPreferences prefs = getSharedPreferences("mayzter", MODE_PRIVATE);
        float minMxnKm = prefs.getFloat("min_mxn_km", 8.2f);
        float minMxnH  = prefs.getFloat("min_mxn_h", 150f);

        double kmTotal  = trip.kmPasajero + trip.kmViaje;
        double minTotal = trip.minPasajero + trip.minViaje;

        double mxnKm = (kmTotal > 0) ? trip.priceMXN / kmTotal : 0;
        double mxnH  = (minTotal > 0) ? (trip.priceMXN / minTotal) * 60.0 : 0;
        double mxnMin = (minTotal > 0) ? trip.priceMXN / minTotal : 0;

        boolean accept = (mxnKm >= minMxnKm) && (mxnH >= minMxnH);

        String decision = accept ? "ACEPTA" : "RECHAZA";
        String color    = accept ? "#2E7D32" : "#B71C1C"; // verde / rojo

        String displayText = String.format(
            "<b>MAYZTER LITE</b><br>" +
            "%.1f km &nbsp; $%.1f/km<br>" +
            "%d min &nbsp; $%.0f/h<br>" +
            "<b>%s</b>",
            kmTotal, mxnKm, (int) minTotal, mxnH, decision
        );

        String speechText = String.format(
            "%s, %.0f pesos por hora, %.1f kilómetros, %d minutos",
            decision.toLowerCase(), mxnH, kmTotal, (int) minTotal
        );

        // Lanzar overlay flotante
        Intent intent = new Intent(this, FloatingOverlayService.class);
        intent.putExtra("displayText", displayText);
        intent.putExtra("speechText", speechText);
        intent.putExtra("bgColor", color);
        intent.putExtra("accept", accept);
        startForegroundService(intent);
    }

    // ---- Modelo de datos ----
    static class TripData {
        double priceMXN  = 0;
        double kmPasajero = 0;
        double kmViaje   = 0;
        int    minPasajero = 0;
        int    minViaje  = 0;
        String appName   = "";

        boolean isValid() {
            return priceMXN > 0;
        }

        @Override
        public String toString() {
            return appName + " $" + priceMXN + " | km_pas=" + kmPasajero +
                   " km_viaje=" + kmViaje + " min_pas=" + minPasajero + " min_viaje=" + minViaje;
        }
    }
}
