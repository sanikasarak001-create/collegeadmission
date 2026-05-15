package com.example.collegeadmission;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DeadlineActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack;
    private ProgressBar progressBar;
    private final ArrayList<DeadlineItem> deadlineList = new ArrayList<>();

    public static class DeadlineItem {
        public String collegeName;
        public String deadline;
        public String course;
        public String link;

        public DeadlineItem() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deadline);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack       = findViewById(R.id.tvBack);
        progressBar  = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvBack.setOnClickListener(v -> finish());
        loadDeadlines();
    }

    private void loadDeadlines() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference("deadlines")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        deadlineList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            DeadlineItem item = ds.getValue(DeadlineItem.class);
                            if (item != null) deadlineList.add(item);
                        }
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setAdapter(new DeadlineAdapter(deadlineList));
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    static class DeadlineAdapter
            extends RecyclerView.Adapter<DeadlineAdapter.VH> {

        private final ArrayList<DeadlineItem> list;
        private final SimpleDateFormat sdf =
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        DeadlineAdapter(ArrayList<DeadlineItem> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_deadline, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            DeadlineItem item = list.get(position);
            holder.tvCollegeName.setText(item.collegeName);
            holder.tvCourse.setText(item.course);
            holder.tvDeadline.setText("📅 Deadline: " + item.deadline);

            // Color code by urgency
            try {
                Date deadlineDate = sdf.parse(item.deadline);
                Date today = new Date();
                if (deadlineDate != null) {
                    long diffMs = deadlineDate.getTime() - today.getTime();
                    long diffDays = diffMs / (1000 * 60 * 60 * 24);

                    if (diffDays < 0) {
                        holder.tvStatus.setText("❌ Closed");
                        holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
                    } else if (diffDays <= 7) {
                        holder.tvStatus.setText("🔴 " + diffDays + " days left");
                        holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
                    } else if (diffDays <= 30) {
                        holder.tvStatus.setText("🟡 " + diffDays + " days left");
                        holder.tvStatus.setTextColor(Color.parseColor("#E65100"));
                    } else {
                        holder.tvStatus.setText("🟢 " + diffDays + " days left");
                        holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                    }
                }
            } catch (ParseException e) {
                holder.tvStatus.setText("Date TBD");
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvCollegeName, tvCourse, tvDeadline, tvStatus;
            VH(View v) {
                super(v);
                tvCollegeName = v.findViewById(R.id.tvCollegeName);
                tvCourse      = v.findViewById(R.id.tvCourse);
                tvDeadline    = v.findViewById(R.id.tvDeadline);
                tvStatus      = v.findViewById(R.id.tvStatus);
            }
        }
    }
}