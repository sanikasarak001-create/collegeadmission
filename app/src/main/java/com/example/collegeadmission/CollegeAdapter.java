package com.example.collegeadmission;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.collegeadmission.College;
import com.example.collegeadmission.R;

import java.util.ArrayList;

public class CollegeAdapter extends RecyclerView.Adapter<CollegeAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<College> list;
    private final OnCollegeClickListener listener;

    public interface OnCollegeClickListener {
        void onClick(College college);
    }

    public CollegeAdapter(Context context, ArrayList<College> list,
                          OnCollegeClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_college, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        College college = list.get(position);
        holder.tvName.setText(college.getName());
        holder.tvLocation.setText("📍 " + college.getLocation()
                + ", " + college.getState());
        holder.tvType.setText(college.getType());
        holder.tvCutoff.setText("Cutoff: " + college.getCutoffGeneral() + "%");
        holder.tvCourses.setText(college.getCourses());

        // Color-code Govt vs Private
        boolean isGovt = "Government".equals(college.getType());
        holder.tvType.setBackgroundResource(
                isGovt ? R.color.govt_bg : R.color.pvt_bg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.tvType.setTextColor(context.getResources().getColor(
                    isGovt ? R.color.govt_text : R.color.pvt_text, null));
        }

        holder.card.setOnClickListener(v -> listener.onClick(college));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvName, tvLocation, tvType, tvCutoff, tvCourses;

        ViewHolder(View v) {
            super(v);
            card = v.findViewById(R.id.card);
            tvName = v.findViewById(R.id.tvName);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvType = v.findViewById(R.id.tvType);
            tvCutoff = v.findViewById(R.id.tvCutoff);
            tvCourses = v.findViewById(R.id.tvCourses);
        }
    }
}