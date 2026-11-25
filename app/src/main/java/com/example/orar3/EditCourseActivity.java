package com.example.orar3;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditCourseActivity extends AppCompatActivity {
    EditText etSubject, etProfessor, etTestDate;
    TextView tvStartTime, tvEndTime;
    Spinner spinner_select_day;
    String selected_day;
    Button btnSave, btnDelete;
    DatabaseHelper dbHelper;
    int courseId;

    SharedPreferences sp;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_course_activity);

        etSubject = findViewById(R.id.etSubject);
        etProfessor = findViewById(R.id.etProfessor);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        spinner_select_day = findViewById(R.id.spinner_select_day);
        etTestDate = findViewById(R.id.etTestDate);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.stergeCursBtn);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.zilele_saptamanii,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner_select_day.setAdapter(adapter);

        dbHelper = new DatabaseHelper(this);

        // Preluăm datele din Intent
        Intent intent = getIntent();
        courseId = intent.getIntExtra("id", -1);

        // Setăm datele
        etSubject.setText(intent.getStringExtra("subject"));
        etProfessor.setText(intent.getStringExtra("professor"));
        tvStartTime.setText(intent.getStringExtra("startTime"));
        tvEndTime.setText(intent.getStringExtra("endTime"));
        etTestDate.setText(intent.getStringExtra("testDate"));

        String dayIntent = intent.getStringExtra("day");
        if (dayIntent != null) {
            selected_day = dayIntent.toLowerCase();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(dayIntent)) {
                    spinner_select_day.setSelection(i);
                    break;
                }
            }
        }

        // TimePickerDialog pentru ora de start
        tvStartTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int oraCurenta = calendar.get(Calendar.HOUR_OF_DAY);
            int minutCurent = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, (view, oraAleasa, minutAles) -> {
                int displayHour = oraAleasa % 12 == 0 ? 12 : oraAleasa % 12;
                String amPm = oraAleasa >= 12 ? "PM" : "AM";
                tvStartTime.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, minutAles, amPm));
            }, oraCurenta, minutCurent, false);

            timePicker.show();
        });

        // TimePickerDialog pentru ora de sfârșit
        tvEndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int oraCurenta = calendar.get(Calendar.HOUR_OF_DAY);
            int minutCurent = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, (view, oraAleasa, minutAles) -> {
                int displayHour = oraAleasa % 12 == 0 ? 12 : oraAleasa % 12;
                String amPm = oraAleasa >= 12 ? "PM" : "AM";
                tvEndTime.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, minutAles, amPm));
            }, oraCurenta, minutCurent, false);

            timePicker.show();
        });

        spinner_select_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selected_day = adapterView.getItemAtPosition(position).toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Salvăm modificările în baza de date
        btnSave.setOnClickListener(v -> {
            String newSubject = etSubject.getText().toString();
            String newProfessor = etProfessor.getText().toString();
            String newStartTime = tvStartTime.getText().toString();
            String newEndTime = tvEndTime.getText().toString();
            String newDay = selected_day;
            String newTestDate = etTestDate.getText().toString();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setLenient(false);

            boolean isDateEmpty = newTestDate == null || newTestDate.trim().isEmpty();

            try {
                boolean updated;

                if (isDateEmpty) {
                    updated = dbHelper.updateCourse(courseId, newSubject, newProfessor, newStartTime, newEndTime, newDay, null);
                } else {
                    dateFormat.parse(newTestDate);
                    updated = dbHelper.updateCourse(courseId, newSubject, newProfessor, newStartTime, newEndTime, newDay, newTestDate);
                }

                sp = getSharedPreferences("orar_pref", Context.MODE_PRIVATE);
                boolean notifPermise = sp.getBoolean("notificari_activate", true);

                if (updated) {
                    CourseNotificationHelper.programeazaNotificareCurs(this, newSubject, newProfessor, newStartTime, newDay);
                    Toast.makeText(EditCourseActivity.this, "Curs actualizat!", Toast.LENGTH_SHORT).show();
                    TestActivity test = new TestActivity();

                    // Setăm notificările
                    if (notifPermise && !isDateEmpty) {
                        test.programeazaNotificareCuOZiInainte(this, newSubject, newTestDate, 16, 0);
                        test.programeazaNotificare(newSubject, newTestDate, 7, 30);
                    }

                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish(); // Revenim la ecranul principal
                } else {
                    Toast.makeText(EditCourseActivity.this, "Eroare la actualizare!", Toast.LENGTH_SHORT).show();
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Data introdusă nu este validă! Format corect: dd.MM.yyyy", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Șterge curs")
                    .setMessage("Ești sigur că vrei să ștergi cursul din orar? Această acțiune nu poate fi anulată.")
                    .setPositiveButton("Da", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteCourse(courseId);
                        if (deleted) {
                            CourseNotificationHelper.anuleazaNotificareCurs(this, etSubject.getText().toString(), tvStartTime.getText().toString(), selected_day);
                            Toast.makeText(this, "Curs șters cu succes!", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(EditCourseActivity.this, "Eroare la ștergere!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Nu", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
