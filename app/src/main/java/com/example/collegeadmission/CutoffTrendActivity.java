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

public class CutoffTrendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack;
    private ProgressBar progressBar;
    private final ArrayList<TrendItem> trendList = new ArrayList<>();

    public static class TrendItem {
        public String collegeName;
        public String category;
        public double cutoff2022;
        public double cutoff2023;
        public double cutoff2024;

        public TrendItem() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cutoff_trend);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack       = findViewById(R.id.tvBack);
        progressBar  = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvBack.setOnClickListener(v -> finish());
        loadTrends();
    }

    private void loadTrends() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("cutoff_trends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        trendList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            TrendItem item = ds.getValue(TrendItem.class);
                            if (item != null) trendList.add(item);
                        }
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setAdapter(new TrendAdapter(trendList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    static class TrendAdapter extends RecyclerView.Adapter<TrendAdapter.VH> {
        private final ArrayList<TrendItem> list;

        TrendAdapter(ArrayList<TrendItem> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cutoff_trend, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            TrendItem t = list.get(position);
            h.tvCollegeName.setText(t.collegeName);
            h.tvCategory.setText("Category: " + t.category);

            h.tvYear1.setText("2022\n" + t.cutoff2022 + "%");
            h.tvYear2.setText("2023\n" + t.cutoff2023 + "%");
            h.tvYear3.setText("2024\n" + t.cutoff2024 + "%");

            // Bar heights proportional to cutoff (max 100dp)
            int max = 100;
            setBarHeight(h.bar1, (int)(t.cutoff2022 * max / 100));
            setBarHeight(h.bar2, (int)(t.cutoff2023 * max / 100));
            setBarHeight(h.bar3, (int)(t.cutoff2024 * max / 100));

            // Trend arrow
            if (t.cutoff2024 > t.cutoff2022) {
                h.tvTrend.setText("📈 Rising Cutoff");
                h.tvTrend.setTextColor(0xFFC62828);
            } else if (t.cutoff2024 < t.cutoff2022) {
                h.tvTrend.setText("📉 Falling Cutoff");
                h.tvTrend.setTextColor(0xFF2E7D32);
            } else {
                h.tvTrend.setText("➡️ Stable Cutoff");
                h.tvTrend.setTextColor(0xFF1565C0);
            }
        }

        private void setBarHeight(View bar, int heightDp) {
            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            float density = bar.getContext().getResources()
                    .getDisplayMetrics().density;
            lp.height = Math.max((int)(heightDp * density), (int)(8 * density));
            bar.setLayoutParams(lp);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvCollegeName, tvCategory, tvYear1, tvYear2,
                    tvYear3, tvTrend;
            View bar1, bar2, bar3;
            VH(View v) {
                super(v);
                tvCollegeName = v.findViewById(R.id.tvCollegeName);
                tvCategory    = v.findViewById(R.id.tvCategory);
                tvYear1       = v.findViewById(R.id.tvYear1);
                tvYear2       = v.findViewById(R.id.tvYear2);
                tvYear3       = v.findViewById(R.id.tvYear3);
                tvTrend       = v.findViewById(R.id.tvTrend);
                bar1          = v.findViewById(R.id.bar1);
                bar2          = v.findViewById(R.id.bar2);
                bar3          = v.findViewById(R.id.bar3);
            }
        }
    }
}