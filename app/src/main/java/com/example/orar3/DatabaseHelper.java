package com.example.orar3;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "orar.db";
    private static final int DATABASE_VERSION = 2;

    //Numele coloanelor si a tabelului
    private static final String TABLE_NAME = "course";
    private static final String COL_ID = "id";
    private static final String COL_DAY = "day";
    private static final String COL_START_TIME = "start_time";
    private static final String COL_END_TIME = "end_time";
    private static final String COL_SUBJECT = "subject";
    private static final String COL_PROFESSOR = "professor";
    private static final String COL_TEST_DATE = "test_date";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DAY + " TEXT, " +
                COL_START_TIME + " TEXT, "+
                COL_END_TIME +" TEXT, " +
                COL_SUBJECT + " TEXT, " +
                COL_PROFESSOR + " TEXT, " +
                COL_TEST_DATE + " TEXT DEFAULT NULL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    //Inserarea unui curs
    public boolean insert(String subject, String prof, String start_time, String end_time, String day){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_SUBJECT, subject);
        values.put(COL_PROFESSOR, prof);
        values.put(COL_START_TIME, start_time);
        values.put(COL_END_TIME, end_time);
        values.put(COL_DAY, day);

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getCourses(String day){
        SQLiteDatabase db = this.getReadableDatabase();

        // Obținem data curentă în format "dd.MM.yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        // Selecționăm toate materiile pentru ziua curentă, ordonate cronologic (inclusiv format 12h cu AM/PM)
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_DAY + " = ? ORDER BY " +
                        "CASE " +
                        "WHEN " + COL_START_TIME + " LIKE '%PM%' AND CAST(" + COL_START_TIME + " AS INTEGER) < 12 THEN CAST(" + COL_START_TIME + " AS INTEGER) + 12 " +
                        "WHEN " + COL_START_TIME + " LIKE '%AM%' AND CAST(" + COL_START_TIME + " AS INTEGER) = 12 THEN 0 " +
                        "ELSE CAST(" + COL_START_TIME + " AS INTEGER) " +
                        "END ASC, " +
                        "CASE " +
                        "WHEN INSTR(" + COL_START_TIME + ", ':') > 0 THEN CAST(SUBSTR(" + COL_START_TIME + ", INSTR(" + COL_START_TIME + ", ':') + 1, 2) AS INTEGER) " +
                        "ELSE 0 " +
                        "END ASC",
                new String[]{day}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String subject = cursor.getString(cursor.getColumnIndex("subject"));
                @SuppressLint("Range") String testDate = cursor.getString(cursor.getColumnIndex("test_date"));

                // Verificăm dacă testul este azi
                boolean hasTestToday ;
                if (testDate != null) {
                    if (testDate.equals(todayDate)) {
                        hasTestToday = true;
                    } else {
                        hasTestToday = false;
                    }
                } else {
                    hasTestToday = false;
                }

                Log.d("DB_FETCH", "Materie: " + subject + " | Test: " + testDate + " | Azi: " + (hasTestToday ? "DA" : "NU"));
            } while (cursor.moveToNext());
        } else {
            Log.d("DB_FETCH", "Nu există cursuri pentru ziua: " + day);
        }

        return cursor;
    }

    public boolean updateTest(String subject, String newTestDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("test_date", newTestDate);  //Actualizăm data testului

        // Log pentru debug
        Log.d("DB_UPDATE", "Actualizare Test: Materie=" + subject + " | Data Test=" + newTestDate);

        // Actualizăm testul pentru disciplina respectivă
        int result = db.update(TABLE_NAME, values, "subject = ?", new String[]{subject});

        db.close();
        return result > 0;  //Returnează true dacă s-a modificat ceva
    }

    public boolean updateCourse(int id, String subject, String professor, String startTime, String endTime, String day, String testDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("subject", subject);
        values.put("professor", professor);
        values.put("start_time", startTime);
        values.put("end_time", endTime);
        values.put("day", day);
        values.put("test_date", testDate);

        int result = db.update("course", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();

        return result > 0; // Returnează `true` dacă actualizarea a fost realizată cu succes
    }

    public void deleteAllCourses(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

    public boolean deleteCourse(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
        db.close();
        return result>0;
    }

}