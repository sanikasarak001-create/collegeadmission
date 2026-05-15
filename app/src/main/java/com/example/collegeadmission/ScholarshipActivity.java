package com.example.collegeadmission;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Locale;

public class ScholarshipActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack;
    private TextInputEditText etSearch;
    private ProgressBar progressBar;
    private final ArrayList<ScholarshipItem> allList    = new ArrayList<>();
    private final ArrayList<ScholarshipItem> filtered   = new ArrayList<>();
    private ScholarshipAdapter adapter;

    public static class ScholarshipItem {
        public String name;
        public String provider;
        public String amount;
        public String eligibility;
        public String category;
        public String deadline;
        public String applyLink;

        public ScholarshipItem() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scholarship);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack       = findViewById(R.id.tvBack);
        etSearch     = findViewById(R.id.etSearch);
        progressBar  = findViewById(R.id.progressBar);

        adapter = new ScholarshipAdapter(filtered);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterList(s.toString());
            }
        });

        tvBack.setOnClickListener(v -> finish());
        loadScholarships();
    }

    private void loadScholarships() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("scholarships")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ScholarshipItem item = ds.getValue(ScholarshipItem.class);
                            if (item != null) allList.add(item);
                        }
                        filtered.addAll(allList);
                        progressBar.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void filterList(String query) {
        filtered.clear();
        String q = query.toLowerCase(Locale.getDefault());
        for (ScholarshipItem s : allList) {
            if (s.name.toLowerCase(Locale.getDefault()).contains(q)
                    || s.category.toLowerCase(Locale.getDefault()).contains(q)
                    || s.provider.toLowerCase(Locale.getDefault()).contains(q)) {
                filtered.add(s);
            }
        }
        adapter.notifyDataSetChanged();
    }

    static class ScholarshipAdapter
            extends RecyclerView.Adapter<ScholarshipAdapter.VH> {
        private final ArrayList<ScholarshipItem> list;

        ScholarshipAdapter(ArrayList<ScholarshipItem> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_scholarship, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ScholarshipItem s = list.get(position);
            h.tvName.setText(s.name);
            h.tvProvider.setText("By: " + s.provider);
            h.tvAmount.setText(s.amount);
            h.tvEligibility.setText("✅ " + s.eligibility);
            h.tvCategory.setText(s.category);
            h.tvDeadline.setText("📅 " + s.deadline);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvProvider, tvAmount,
                    tvEligibility, tvCategory, tvDeadline;
            VH(View v) {
                super(v);
                tvName        = v.findViewById(R.id.tvName);
                tvProvider    = v.findViewById(R.id.tvProvider);
                tvAmount      = v.findViewById(R.id.tvAmount);
                tvEligibility = v.findViewById(R.id.tvEligibility);
                tvCategory    = v.findViewById(R.id.tvCategory);
                tvDeadline    = v.findViewById(R.id.tvDeadline);
            }
        }
    }
}