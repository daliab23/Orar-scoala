package com.example.orar3;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Time;
import java.util.Calendar;

public class CourseActivity extends AppCompatActivity {
    Button homeBtn;
    Button saveBtn;
    EditText subject, prof;
    TextView tv_start_time, tv_end_time;
    DatabaseHelper dbHelper;

    Spinner spinner_select_day;
    String selected_day;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.course_activity);

        homeBtn = findViewById(R.id.tvOrar);
        saveBtn = findViewById(R.id.saveBtn);

        subject = findViewById(R.id.et_nume_disciplina);
        prof = findViewById(R.id.et_nume_prof);

        tv_start_time = findViewById(R.id.tv_start_time);
        tv_end_time = findViewById(R.id.tv_end_time);

        spinner_select_day = findViewById(R.id.spinner_select_day);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.zilele_saptamanii,
                R.layout.spinner_item
        );


        adapter.setDropDownViewResource(R.layout.spinner_item);


        spinner_select_day.setAdapter(adapter);

        dbHelper = new DatabaseHelper(this);


        homeBtn.setOnClickListener(v->{
            Intent intent = new Intent(CourseActivity.this, MainActivity.class);
            startActivity(intent);
        });

        //Inseram in baza de date
        saveBtn.setOnClickListener(v->{
            String course = subject.getText().toString();
            String profesor = prof.getText().toString();

            String startTime = tv_start_time.getText().toString();
            String endTime = tv_end_time.getText().toString();
            String ziua = selected_day;

            //Verificam daca casutele sunt corect completate
            if(course.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || ziua.isEmpty()){
                Toast.makeText(this,"Nu ați completat câmpurile obligatorii!",Toast.LENGTH_SHORT).show();
                return;
            }
            //Inseram in baza de date
            boolean check = dbHelper.insert(course, profesor, startTime, endTime, ziua);

            if (check){
                Toast.makeText(this,"Curs adaugat in orar", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Eroare la adaugare in orar", Toast.LENGTH_SHORT).show();
            }

        });

        // deschidem ora cu AM/PM
        tv_start_time.setOnClickListener(v->{
            Calendar calendar = Calendar.getInstance();
            int oraCurenta = calendar.get(Calendar.HOUR_OF_DAY);
            int minutCurent = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, (view, oraAleasa, minutAles) -> {
                int displayHour = oraAleasa % 12 == 0 ? 12 : oraAleasa % 12;
                String amPm = oraAleasa >= 12 ? "PM" : "AM";
                tv_start_time.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, minutAles, amPm));
            }, oraCurenta, minutCurent, false);

            timePicker.show();
        });

        tv_end_time.setOnClickListener(v->{
            Calendar calendar = Calendar.getInstance();
            int oraCurenta = calendar.get(Calendar.HOUR_OF_DAY);
            int minutCurent = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, (view, oraAleasa, minutAles) -> {
                int displayHour = oraAleasa % 12 == 0 ? 12 : oraAleasa % 12;
                String amPm = oraAleasa >= 12 ? "PM" : "AM";
                tv_end_time.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, minutAles, amPm));
            }, oraCurenta, minutCurent, false);

            timePicker.show();
        });

        spinner_select_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selected_day = adapterView.getItemAtPosition(position).toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.course), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

}