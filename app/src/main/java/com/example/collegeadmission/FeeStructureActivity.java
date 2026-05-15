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

public class FeeStructureActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack;
    private ProgressBar progressBar;
    private final ArrayList<FeeItem> feeList = new ArrayList<>();

    public static class FeeItem {
        public String collegeName;
        public String course;
        public String annualFee;
        public String totalFee;
        public String hostelFee;
        public String scholarshipAvailable;
        public String type;

        public FeeItem() {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_structure);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack       = findViewById(R.id.tvBack);
        progressBar  = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvBack.setOnClickListener(v -> finish());
        loadFees();
    }

    private void loadFees() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("fee_structure")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        feeList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            FeeItem item = ds.getValue(FeeItem.class);
                            if (item != null) feeList.add(item);
                        }
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setAdapter(new FeeAdapter(feeList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    static class FeeAdapter extends RecyclerView.Adapter<FeeAdapter.VH> {
        private final ArrayList<FeeItem> list;

        FeeAdapter(ArrayList<FeeItem> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_fee_structure, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            FeeItem f = list.get(position);
            h.tvCollegeName.setText(f.collegeName);
            h.tvCourse.setText(f.course);
            h.tvAnnualFee.setText(f.annualFee);
            h.tvTotalFee.setText("Total: " + f.totalFee);
            h.tvHostelFee.setText("🏠 Hostel/yr: " + f.hostelFee);
            h.tvScholarship.setText("🎓 Scholarship: " + f.scholarshipAvailable);

            boolean isGovt = "Government".equals(f.type);
            h.tvType.setText(isGovt ? "Govt" : "Private");
            h.tvType.setBackgroundResource(isGovt
                    ? R.color.govt_bg : R.color.pvt_bg);
            h.tvType.setTextColor(h.tvType.getContext().getResources()
                    .getColor(isGovt ? R.color.govt_text : R.color.pvt_text, null));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvCollegeName, tvCourse, tvAnnualFee,
                    tvTotalFee, tvHostelFee, tvScholarship, tvType;
            VH(View v) {
                super(v);
                tvCollegeName = v.findViewById(R.id.tvCollegeName);
                tvCourse      = v.findViewById(R.id.tvCourse);
                tvAnnualFee   = v.findViewById(R.id.tvAnnualFee);
                tvTotalFee    = v.findViewById(R.id.tvTotalFee);
                tvHostelFee   = v.findViewById(R.id.tvHostelFee);
                tvScholarship = v.findViewById(R.id.tvScholarship);
                tvType        = v.findViewById(R.id.tvType);
            }
        }
    }
}