package com.example.orar3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

public class CourseNotificationHelper {

    public static void programeazaNotificareCurs(Context context, String subject, String profesor, String startTime, String day) {
        SharedPreferences sp = context.getSharedPreferences("orar_pref", Context.MODE_PRIVATE);
        boolean notifPermise = sp.getBoolean("notificari_activate", true);
        if (!notifPermise) {
            return;
        }

        if (subject == null || startTime == null || day == null || startTime.trim().isEmpty()) {
            return;
        }

        try {
            int hour = 0;
            int minute = 0;
            String timeUpper = startTime.toUpperCase(Locale.ROOT).trim();

            boolean isPM = timeUpper.contains("PM");
            boolean isAM = timeUpper.contains("AM");
            String cleanTime = timeUpper.replace("AM", "").replace("PM", "").trim();

            String[] parts = cleanTime.split(":");
            if (parts.length >= 2) {
                hour = Integer.parseInt(parts[0].trim());
                minute = Integer.parseInt(parts[1].trim());

                if (isPM && hour < 12) {
                    hour += 12;
                } else if (isAM && hour == 12) {
                    hour = 0;
                }
            }

            int targetDayOfWeek = getDayOfWeekInt(day);
            if (targetDayOfWeek == -1) return;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Scădem 15 minute
            calendar.add(Calendar.MINUTE, -15);

            // Setăm ziua din săptămână
            calendar.set(Calendar.DAY_OF_WEEK, targetDayOfWeek);

            // Dacă momentul a trecut deja în această săptămână, programăm pentru săptămâna viitoare
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("channelId", "course_channel");
            intent.putExtra("title", "🔔 Cursul începe în curând!");
            String profText = (profesor != null && !profesor.trim().isEmpty()) ? " (Prof. " + profesor + ")" : "";
            intent.putExtra("message", "În 15 minute începe " + subject + profText + " 📚");
            intent.putExtra("subject", subject);

            int requestCode = ("course_" + subject + "_" + day + "_" + startTime).hashCode();
            intent.putExtra("requestCode", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                );
                Log.d("NOTIF_CURS", "Notificare curs programată pentru " + subject + " la: " + calendar.getTime());
            }

        } catch (Exception e) {
            Log.e("NOTIF_CURS", "Eroare la programare notificare curs: " + e.getMessage(), e);
        }
    }

    public static void anuleazaNotificareCurs(Context context, String subject, String startTime, String day) {
        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            int requestCode = ("course_" + subject + "_" + day + "_" + startTime).hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
                pendingIntent.cancel();
            }
        } catch (Exception e) {
            Log.e("NOTIF_CURS", "Eroare la anulare notificare: " + e.getMessage());
        }
    }

    private static int getDayOfWeekInt(String day) {
        if (day == null) return -1;
        String lowerDay = day.toLowerCase(Locale.ROOT).trim();
        if (lowerDay.contains("lun")) return Calendar.MONDAY;
        if (lowerDay.contains("mar")) return Calendar.TUESDAY;
        if (lowerDay.contains("mie") || lowerDay.contains("mer")) return Calendar.WEDNESDAY;
        if (lowerDay.contains("joi")) return Calendar.THURSDAY;
        if (lowerDay.contains("vin")) return Calendar.FRIDAY;
        if (lowerDay.contains("sâm") || lowerDay.contains("sam")) return Calendar.SATURDAY;
        if (lowerDay.contains("dum")) return Calendar.SUNDAY;
        return -1;
    }
}
