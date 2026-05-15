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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack, tvEmpty;
    private ProgressBar progressBar;
    private final ArrayList<PredictionRecord> historyList = new ArrayList<>();

    // Simple data class for a history record
    public static class PredictionRecord {
        public String date;
        public double percentage;
        public String category;
        public int matchCount;

        public PredictionRecord() {}

        public PredictionRecord(String date, double percentage,
                                String category, int matchCount) {
            this.date = date;
            this.percentage = percentage;
            this.category = category;
            this.matchCount = matchCount;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack       = findViewById(R.id.tvBack);
        tvEmpty      = findViewById(R.id.tvEmpty);
        progressBar  = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvBack.setOnClickListener(v -> finish());
        loadHistory();
    }

    private void loadHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance()
                .getReference("history")
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PredictionRecord record = ds.getValue(PredictionRecord.class);
                            if (record != null) historyList.add(record);
                        }
                        // Show newest first
                        Collections.reverse(historyList);
                        progressBar.setVisibility(View.GONE);

                        if (historyList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setAdapter(new HistoryAdapter(historyList));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    // Inner Adapter
    static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        private final ArrayList<PredictionRecord> list;

        HistoryAdapter(ArrayList<PredictionRecord> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            PredictionRecord r = list.get(position);
            holder.tvPercentage.setText(r.percentage + "%");
            holder.tvCategory.setText("Category: " + r.category);
            holder.tvMatchCount.setText(r.matchCount + " colleges matched");
            holder.tvDate.setText(r.date);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvPercentage, tvCategory, tvMatchCount, tvDate;
            VH(View v) {
                super(v);
                tvPercentage  = v.findViewById(R.id.tvPercentage);
                tvCategory    = v.findViewById(R.id.tvCategory);
                tvMatchCount  = v.findViewById(R.id.tvMatchCount);
                tvDate        = v.findViewById(R.id.tvDate);
            }
        }
    }
}