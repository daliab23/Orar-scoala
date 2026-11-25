package com.example.orar3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {
    Button homeBtn, testBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.calendar_activity);

        homeBtn = findViewById(R.id.orarBtn);
        testBtn = findViewById(R.id.testBtn);

        homeBtn.setOnClickListener(v->{
            Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
            startActivity(intent);
        });

        testBtn.setOnClickListener(v->{
            Intent intent = new Intent(CalendarActivity.this, TestActivity.class);
            startActivityForResult(intent, MainActivity.REQUEST_CODE_TEST);

        });

        CalendarView calendarView = findViewById(R.id.calendarView);

        // Când selectăm o zi
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Convertim data într-un format utilizabil (ex: "Luni", "Marti")
                String selectedDay = getDayOfWeek(dayOfMonth, month, year);
                String selectedDate = String.format(Locale.getDefault(), "%02d.%02d.%d", dayOfMonth, month + 1, year); // Format: "28.03.2025"

                Log.d("CALENDAR_SELECT", "Zi selectată: " + selectedDay + "| Data: " + selectedDate);

                // Trimite ziua selectată către MainActivity
                Intent intent = new Intent();
                intent.putExtra("selectedDay", selectedDay);
                intent.putExtra("selectedDate", selectedDate);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calendar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    // Funcție pentru a obține numele zilei săptămânii
    private String getDayOfWeek(int day, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        Log.d("CALENDAR_DAY_OF_WEEK","Day of week: " + calendar.getTime());

        // Formatăm ziua în limba română
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", new Locale("ro"));

        return sdf.format(calendar.getTime());
    }
}
