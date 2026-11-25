package com.example.orar3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    Button deleteBtn;
    Switch notficari;
    DatabaseHelper dbHelper;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.settings_activity);

        deleteBtn = findViewById(R.id.deleteBtn);
        notficari = findViewById(R.id.notificariSwitch);

        sp = getSharedPreferences("orar_pref", Context.MODE_PRIVATE);
        //citim daca notificarile sunt active sau nu
        boolean notificariActivate = sp.getBoolean("notificari_activate", true);

        //Setam satrea switch-ul in functie de SharedPreference
        notficari.setChecked(notificariActivate);

        deleteBtn.setOnClickListener(v->{
            //Afisam o casuta de dialog pentru a confirma stergerea intregului orar
            new AlertDialog.Builder(this)
                    .setTitle("Confirmare Ștergere")
                    .setMessage("Ești sigur că vrei să ștergi tot orarul? Această acțiune nu poate fi anulată.")
                    .setPositiveButton("Da", (dialog, which) -> {
                        // Dacă utilizatorul confirmă, ștergem orarul
                        dbHelper = new DatabaseHelper(this);
                        dbHelper.deleteAllCourses();
                        Toast.makeText(this, "Orarul a fost șters!", Toast.LENGTH_SHORT).show();

                        //Trimite un rezultat înapoi la MainActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("deleted", true);
                        setResult(RESULT_OK, resultIntent);

                        finish(); // Închide activitatea
                    })
                    .setNegativeButton("Nu", (dialog, which) -> {
                        //Dacă utilizatorul anulează, închidem dialogul
                        dialog.dismiss();
                    })
                    .show();


        });

        notficari.setOnCheckedChangeListener((buttonView, isChecked)->{
            //Scriem in SharedPreferences daca notificartile sunt sau nu activate
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("notificari_activate", isChecked);
            editor.apply();

            if(isChecked){
                Toast.makeText(this,"Notifcarile sunt activate!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Notifcarile sunt dezactivate!", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}
