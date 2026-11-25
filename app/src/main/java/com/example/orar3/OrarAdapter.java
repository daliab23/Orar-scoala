package com.example.orar3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OrarAdapter extends RecyclerView.Adapter<OrarAdapter.ViewHolder>{

    private ArrayList<OrarModel> orarModels;
    Context context;

    public OrarAdapter(Context context, ArrayList<OrarModel> orarModels){
        this.context = context;
        this.orarModels = orarModels;

    }

    @NonNull
    @Override
    public OrarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);

        return new OrarAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrarAdapter.ViewHolder holder, int position) {
        //assigning values to the views we created in the recycler_view_row layout file
        //based on the position of the recycler view

        holder.tvMaterie.setText(orarModels.get(position).getSubject());
        holder.tvTimp.setText(orarModels.get(position).getStart_time() + " - " + orarModels.get(position).getEnd_time());
        holder.tvProfesor.setText("Prof. " + orarModels.get(position).getProfessor());



        holder.itemView.setOnLongClickListener(view -> {
            //Deschide activitatea de editare
            Intent intent = new Intent(context, EditCourseActivity.class);
            intent.putExtra("id", orarModels.get(position).getId()); // Trimitem ID-ul cursului
            intent.putExtra("subject", orarModels.get(position).getSubject());
            intent.putExtra("professor", orarModels.get(position).getProfessor());
            intent.putExtra("startTime", orarModels.get(position).getStart_time());
            intent.putExtra("endTime", orarModels.get(position).getEnd_time());
            intent.putExtra("day", orarModels.get(position).getDay());
            intent.putExtra("testDate", orarModels.get(position).getTestDate());

            // Folosim startActivityForResult pentru a putea trimite update-ul în MainActivity
            ((Activity) context).startActivityForResult(intent, MainActivity.REQUEST_CODE_EDIT_COURSE);
            return true;
        });
        // Afișează testul doar dacă există
        if (holder.tvTest != null) {
            String testDate = orarModels.get(position).getTestDate();
            String selectedDate = ((MainActivity) context).getSelectedDate();

            if (testDate != null && !testDate.isEmpty()) {
                if (testDate.equals(selectedDate)) {
                    holder.tvTest.setText("Test");
                    holder.tvTest.setVisibility(View.VISIBLE);
                } else {
                    holder.tvTest.setVisibility(View.GONE);
                }
            } else {
                holder.tvTest.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return orarModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaterie, tvProfesor, tvTimp, tvTest;

        public ViewHolder(View itemView) {
            super(itemView);

            tvMaterie = itemView.findViewById(R.id.tv_materie);
            tvProfesor = itemView.findViewById(R.id.tv_prof);
            tvTimp = itemView.findViewById(R.id.tv_ora);
            tvTest = itemView.findViewById(R.id.tvTest);
        }
    }
}