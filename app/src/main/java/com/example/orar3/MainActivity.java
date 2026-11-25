package com.example.orar3;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CALENDAR = 100;
    public static final int REQUEST_CODE_EDIT_COURSE = 102;
    private static final int REQUEST_CODE_DELETE = 104;
    public static final int REQUEST_CODE_TEST = 106;

    Button addBtn;
    ImageButton calendarBtn, settingsBtn;
    RecyclerView recyclerView;
    ArrayList<OrarModel> orarList;
    OrarAdapter orarAdapter;
    TextView tvZiua, tvData, tvEmptyMessage;
    String dataCurenta, ziua;

    private String selectedDate, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        addBtn = findViewById(R.id.addBtn);
        calendarBtn = findViewById(R.id.calendarBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        tvZiua = findViewById(R.id.tv_ziua);
        tvData = findViewById(R.id.tv_data);
        tvEmptyMessage = findViewById(R.id.textView2);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inițializăm lista și adapterul
        orarList = new ArrayList<>();
        orarAdapter = new OrarAdapter(this, orarList);
        recyclerView.setAdapter(orarAdapter);

        // Setăm data curentă și ziua săptămânii
        dataCurenta = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        ziua = new SimpleDateFormat("EEEE", new Locale("ro")).format(Calendar.getInstance().getTime());

        //PENTRU A AFISA IN MAIN TEST
        selectedDay = ziua;
        selectedDate = dataCurenta;

        tvData.setText(dataCurenta);
        tvZiua.setText(ziua);


        //Ramane pe ziua in care suntem cand trecem in landscape
        if (savedInstanceState != null) {
            selectedDay = savedInstanceState.getString("savedDay");
            selectedDate = savedInstanceState.getString("savedDate");

            if (selectedDay != null && selectedDate != null) {
                tvZiua.setText(selectedDay);
                tvData.setText(selectedDate);
                loadOrar(selectedDay, selectedDate);
            }
        } else {
            // dacă nu vine dintr-un state salvat, mergem pe ziua curentă sau intent
            Intent get_intent = getIntent();
            if (get_intent.hasExtra("selectedDay") && get_intent.hasExtra("selectedDate")) {
                selectedDay = get_intent.getStringExtra("selectedDay");
                selectedDate = get_intent.getStringExtra("selectedDate");
                tvZiua.setText(selectedDay);
                tvData.setText(selectedDate);
                loadOrar(selectedDay, selectedDate);
            } else {
                //ziua curentă
                loadOrar(ziua, dataCurenta);
            }
        }

        // Buton pentru adăugarea unui curs nou
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CourseActivity.class);
            startActivity(intent);
            Log.d("ACTIVITY", "Navigating to CourseActivity");
        });

        // Buton pentru deschiderea calendarului
        calendarBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CALENDAR); //Așteptăm rezultat
        });

        //Buton pentru a deschide setarile
        settingsBtn.setOnClickListener(v->{
            Intent intent =new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_DELETE);
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel testChannel = new NotificationChannel(
                    "test_channel",
                    "Notificări Teste",
                    NotificationManager.IMPORTANCE_HIGH
            );
            testChannel.setDescription("Notificări pentru teste din orar 📚");

            NotificationChannel courseChannel = new NotificationChannel(
                    "course_channel",
                    "Notificări Cursuri",
                    NotificationManager.IMPORTANCE_HIGH
            );
            courseChannel.setDescription("Notificări cu 15 minute înainte de curs ⏰");

            if (manager != null) {
                manager.createNotificationChannel(testChannel);
                manager.createNotificationChannel(courseChannel);
            }
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Metodă pentru încărcarea orarului
    private void loadOrar(String selectedDay, String selectedDate) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getCourses(selectedDay);

        ArrayList<OrarModel> orarList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String materie = cursor.getString(cursor.getColumnIndex("subject"));
                @SuppressLint("Range") String profesor = cursor.getString(cursor.getColumnIndex("professor"));
                @SuppressLint("Range") String startTime = cursor.getString(cursor.getColumnIndex("start_time"));
                @SuppressLint("Range") String endTime = cursor.getString(cursor.getColumnIndex("end_time"));
                @SuppressLint("Range") String testDate = cursor.getString(cursor.getColumnIndex("test_date"));
                @SuppressLint("Range") String day = cursor.getString(cursor.getColumnIndex("day"));

                orarList.add(new OrarModel(id, profesor, materie, startTime, endTime, testDate, day));
            } while (cursor.moveToNext());
            cursor.close();
        }
        this.orarList.clear();
        this.orarList.addAll(orarList);
        orarAdapter.notifyDataSetChanged();

        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(this.orarList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    //Gestioneaza revenirea din alte activitati
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CALENDAR && resultCode == RESULT_OK) {
            if (data != null) {
                selectedDay = data.getStringExtra("selectedDay");  // Ziua săptămânii (ex: "luni")
                selectedDate = data.getStringExtra("selectedDate"); // Data completă (ex: "28.03.2025")

                if (selectedDay != null && selectedDate != null) {
                    tvZiua.setText(selectedDay);  // Actualizăm ziua afișată
                    tvData.setText(selectedDate); // ctualizăm data afișată

                    //Reîncărcăm orarul pentru ziua și data selectată
                    loadOrar(selectedDay, selectedDate);
                }
            }
        } else if (requestCode == REQUEST_CODE_EDIT_COURSE && resultCode == RESULT_OK) {
            // După editare, reîncărcăm orarul pentru ziua curentă
            if (selectedDate != null && selectedDay != null) {
                loadOrar(selectedDay, selectedDate);
            } else {
                loadOrar(ziua, dataCurenta); // Dacă nu există zi selectată, încărcăm ziua curentă
            }
        }else if (requestCode == REQUEST_CODE_DELETE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("deleted", false)) {
                //Reîncărcăm orarul (acum e gol)
                orarList.clear();
                orarAdapter.notifyDataSetChanged();
                loadOrar(ziua, dataCurenta);
                Toast.makeText(this, "Orarul a fost actualizat!", Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == REQUEST_CODE_TEST && resultCode == RESULT_OK){
            if(data!=null){
                selectedDate = data.getStringExtra("selectedDate");
                selectedDay = data.getStringExtra("selectedDay");

                if(selectedDate != null && selectedDay != null){
                    tvData.setText(selectedDate);
                    tvZiua.setText(selectedDay);
                    loadOrar(selectedDay, selectedDate);
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedDay != null && selectedDate != null) {
            loadOrar(selectedDay, selectedDate);
        } else {
            // fallback la data curentă
            String ziuaCurenta = new SimpleDateFormat("EEEE", new Locale("ro")).format(Calendar.getInstance().getTime());
            String dataCurenta = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
            loadOrar(ziuaCurenta, dataCurenta);
        }
    }

    //Salveaza ziua si data cand telefonul se roteste
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (selectedDay != null) {
            bundle.putString("savedDay", selectedDay);
        }

        if (selectedDate != null) {
            bundle.putString("savedDate", selectedDate);
        }
    }

    //Apelata in OrarAdapter, pentru a lua data selectata
    public String getSelectedDate() {
        return selectedDate != null ? selectedDate : ""; // Evită NullPointerException
    }
}
