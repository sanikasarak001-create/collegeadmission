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

public class RankingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack;
    private ProgressBar progressBar;
    private final ArrayList<RankItem> rankList = new ArrayList<>();

    public static class RankItem {
        public String collegeName;
        public String nirfRank;
        public String naacGrade;
        public String naacScore;
        public String category;
        public String nirfScore;

        public RankItem() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack       = findViewById(R.id.tvBack);
        progressBar  = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvBack.setOnClickListener(v -> finish());
        loadRankings();
    }

    private void loadRankings() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("rankings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        rankList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            RankItem item = ds.getValue(RankItem.class);
                            if (item != null) rankList.add(item);
                        }
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setAdapter(new RankAdapter(rankList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    static class RankAdapter extends RecyclerView.Adapter<RankAdapter.VH> {
        private final ArrayList<RankItem> list;

        RankAdapter(ArrayList<RankItem> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ranking, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            RankItem r = list.get(position);
            h.tvRankNumber.setText("#" + r.nirfRank);
            h.tvCollegeName.setText(r.collegeName);
            h.tvCategory.setText(r.category);
            h.tvNirfScore.setText("NIRF Score: " + r.nirfScore);
            h.tvNaacGrade.setText(r.naacGrade);
            h.tvNaacScore.setText("NAAC: " + r.naacScore);

            // Color NAAC grade
            int color;
            switch (r.naacGrade) {
                case "A++": color = 0xFF1B5E20; break;
                case "A+":  color = 0xFF2E7D32; break;
                case "A":   color = 0xFF388E3C; break;
                case "B++": color = 0xFFE65100; break;
                default:    color = 0xFF757575; break;
            }
            h.tvNaacGrade.setTextColor(color);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRankNumber, tvCollegeName, tvCategory,
                    tvNirfScore, tvNaacGrade, tvNaacScore;
            VH(View v) {
                super(v);
                tvRankNumber  = v.findViewById(R.id.tvRankNumber);
                tvCollegeName = v.findViewById(R.id.tvCollegeName);
                tvCategory    = v.findViewById(R.id.tvCategory);
                tvNirfScore   = v.findViewById(R.id.tvNirfScore);
                tvNaacGrade   = v.findViewById(R.id.tvNaacGrade);
                tvNaacScore   = v.findViewById(R.id.tvNaacScore);
            }
        }
    }
}