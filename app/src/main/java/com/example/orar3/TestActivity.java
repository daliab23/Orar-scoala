package com.example.orar3;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    Button updateBtn;
    EditText disciplina, data;
    DatabaseHelper dbHelper;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.test_activity);

        updateBtn = findViewById(R.id.addTestBtn);

        data = findViewById(R.id.et_test_data);
        disciplina = findViewById(R.id.et_test_disciplina);

        dbHelper = new DatabaseHelper(this);

        updateBtn.setOnClickListener(v->{
            String course = disciplina.getText().toString();    //Disciplina
            String date = data.getText().toString();    //Data testului
            String day = getDayOfWeekFromDate(date);        //calculam ziua din saptamana in functie de data

            //Verificam daca data a fost introdusa corect
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setLenient(false); //asigura ca data introdusa va fi corect validata

            try {
                boolean isDateEmpty = date.trim().isEmpty();   //verificam daca e completata data

                boolean check;

                if(isDateEmpty){
                    check = dbHelper.updateTest(course, null);
                }else{
                    //daca exista data o validam formatul
                    dateFormat.parse(date);
                    check = dbHelper.updateTest(course, date);
                }

                if(check) {
                    Toast.makeText(this, "Test adaugat", Toast.LENGTH_SHORT).show();

                    sp = getSharedPreferences("orar_pref", Context.MODE_PRIVATE);

                    boolean notifPermise = sp.getBoolean("notificari_activate", true);

                    if(notifPermise) {
                        programeazaNotificare(course, date, 7, 30);     // programam notificare pentru astazi pentru test
                        programeazaNotificareCuOZiInainte(this, course, date, 16, 0); // programam notificare cu o zi inainte
                    }
                    //Trimitem la OrarAdapter pentru a aduaga in recyle view test
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedDate", date);
                    resultIntent.putExtra("selectedDay", day);
                    resultIntent.putExtra("test", "Test");
                    setResult(RESULT_OK, resultIntent);
                    finish();

                } else {
                    Toast.makeText(this, "Eroare la adaugare", Toast.LENGTH_SHORT).show();
                }

            } catch (ParseException e) {
                // Daca ajungem aici inseamna ca data nu este valida
                Toast.makeText(this, "Data introdusa nu este valida! Format corect: dd.MM.yyyy", Toast.LENGTH_SHORT).show();
            }

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.test), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private String getDayOfWeekFromDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = sdf.parse(dateStr);

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ro"));
            return dayFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void programeazaNotificare(String subject, String dateString, int ora, int minut) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = sdf.parse(dateString);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, ora);
//            calendar.add(Calendar.SECOND, 10); // notificare în 10 secunde
            calendar.set(Calendar.MINUTE, minut);
            calendar.set(Calendar.SECOND, 0);

            //Trimiem la notificationReceiver
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title","Ai test azi!");
            intent.putExtra("subject", subject);

            int requestCode = subject.hashCode();
            intent.putExtra("requestCode",requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            //Cerem acces la serviciul de Alarma
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("NOTIF_TEST", "Eroare programare notificare: " + e.getMessage());
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public void programeazaNotificareCuOZiInainte(Context context,String subject, String dataTest, int ora, int minut) {
        try {
            // 1. Convertim stringul în dată
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date testDate = sdf.parse(dataTest);

            // 2. Punem în Calendar și scădem o zi
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(testDate);
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.HOUR_OF_DAY, ora);
            calendar.set(Calendar.MINUTE, minut);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 3. Verificăm dacă notificarea este în viitor
            if (calendar.before(Calendar.getInstance())) {
                Log.d("NOTIF", "Nu putem programa, notificarea e în trecut.");
                return;
            }

            // 4. Creăm intentul pentru notificare
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("title", "Ai test maine!");
            intent.putExtra("message", "Ai test pe " + dataTest + " 📚");
            intent.putExtra("subject",subject);

            int requestCode = subject.hashCode() + 1;
            intent.putExtra("requestCode",requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 5. Programăm notificarea
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

            Log.d("NOTIF", "Notificare programată pentru: " + calendar.getTime());

        } catch (Exception e) {
            Log.e("NOTIF", "Eroare la programare notificare", e);
        }
    }




}
