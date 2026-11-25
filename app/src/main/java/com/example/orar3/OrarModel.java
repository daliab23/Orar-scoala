package com.example.orar3;

public class OrarModel {
    String professor;
    String subject;
    String start_time;
    String end_time;
    String testDate;
    String day;
    int id;

    public OrarModel(int id, String professor, String subject, String start_time, String end_time, String testDate, String day) {
        this.id = id;
        this.professor = professor;
        this.subject = subject;
        this.start_time = start_time;
        this.end_time = end_time;
        this.testDate = testDate;
        this.day = day;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getSubject() {
        return subject;
    }

    public String getProfessor() {
        return professor;
    }

    public String getEnd_time() {
        return end_time;
    }
    public String getTestDate(){
        return testDate;
    }

    public String getDay() {
        return day;
    }

    public int getId() {
        return id;
    }
}

