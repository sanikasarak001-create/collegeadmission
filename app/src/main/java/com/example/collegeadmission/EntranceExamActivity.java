package com.example.collegeadmission;

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
import java.util.ArrayList;

public class EntranceExamActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack;
    private ProgressBar progressBar;
    private final ArrayList<ExamItem> examList = new ArrayList<>();

    public static class ExamItem {
        public String examName;
        public String collegeName;
        public String examDate;
        public String eligibility;
        public String syllabus;
        public String mode;
        public String website;

        public ExamItem() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance_exam);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack       = findViewById(R.id.tvBack);
        progressBar  = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvBack.setOnClickListener(v -> finish());
        loadExams();
    }

    private void loadExams() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("entrance_exams")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        examList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ExamItem item = ds.getValue(ExamItem.class);
                            if (item != null) examList.add(item);
                        }
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setAdapter(new ExamAdapter(examList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    static class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.VH> {
        private final ArrayList<ExamItem> list;
        private boolean expandedState[] = {};

        ExamAdapter(ArrayList<ExamItem> list) {
            this.list = list;
            this.expandedState = new boolean[list.size()];
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_entrance_exam, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ExamItem e = list.get(position);
            h.tvExamName.setText(e.examName);
            h.tvCollege.setText(e.collegeName);
            h.tvMode.setText("📱 Mode: " + e.mode);
            h.tvDate.setText("📅 " + e.examDate);
            h.tvEligibility.setText("✅ Eligibility: " + e.eligibility);
            h.tvSyllabus.setText("📖 Syllabus: " + e.syllabus);
            h.tvWebsite.setText("🔗 " + e.website);

            boolean expanded = expandedState[position];
            h.llDetails.setVisibility(expanded ? View.VISIBLE : View.GONE);
            h.tvToggle.setText(expanded ? "▲ Hide Details" : "▼ View Details");

            h.tvToggle.setOnClickListener(v -> {
                expandedState[position] = !expandedState[position];
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvExamName, tvCollege, tvMode, tvDate,
                    tvEligibility, tvSyllabus, tvWebsite, tvToggle;
            View llDetails;
            VH(View v) {
                super(v);
                tvExamName    = v.findViewById(R.id.tvExamName);
                tvCollege     = v.findViewById(R.id.tvCollege);
                tvMode        = v.findViewById(R.id.tvMode);
                tvDate        = v.findViewById(R.id.tvDate);
                tvEligibility = v.findViewById(R.id.tvEligibility);
                tvSyllabus    = v.findViewById(R.id.tvSyllabus);
                tvWebsite     = v.findViewById(R.id.tvWebsite);
                tvToggle      = v.findViewById(R.id.tvToggle);
                llDetails     = v.findViewById(R.id.llDetails);
            }
        }
    }
}