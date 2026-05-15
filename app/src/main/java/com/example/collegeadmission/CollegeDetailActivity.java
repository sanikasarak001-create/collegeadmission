package com.example.collegeadmission;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Toast;
import com.google.firebase.database.ValueEventListener;

public class CollegeDetailActivity extends AppCompatActivity {

    private TextView tvBack, tvCollegeName, tvLocation,
            tvEligibleBadge, tvTypeBadge;
    private TextView tvStatEligible, tvStatNirf, tvStatNaac;
    private ProgressBar progressBar;

    private LinearLayout llCutoffSection, llFeeSection, llRankSection,
            llExamSection, llTrendSection, llScholarSection;
    private TextView tvSave;
    private boolean isBookmarked = false;

    private String collegeId;
    private double percentage;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_detail);

        collegeId  = getIntent().getStringExtra("collegeId");
        percentage = getIntent().getDoubleExtra("percentage", 0);
        category   = getIntent().getStringExtra("category");
        if (category == null) category = "General";

        tvBack          = findViewById(R.id.tvBack);
        tvCollegeName   = findViewById(R.id.tvCollegeName);
        tvLocation      = findViewById(R.id.tvLocation);
        tvEligibleBadge = findViewById(R.id.tvEligibleBadge);
        tvTypeBadge     = findViewById(R.id.tvTypeBadge);
        tvStatEligible  = findViewById(R.id.tvStatEligible);
        tvStatNirf      = findViewById(R.id.tvStatNirf);
        tvStatNaac      = findViewById(R.id.tvStatNaac);
        progressBar     = findViewById(R.id.progressBar);

        llCutoffSection  = findViewById(R.id.llCutoffSection);
        llFeeSection     = findViewById(R.id.llFeeSection);
        llRankSection    = findViewById(R.id.llRankSection);
        llExamSection    = findViewById(R.id.llExamSection);
        llTrendSection   = findViewById(R.id.llTrendSection);
        llScholarSection = findViewById(R.id.llScholarSection);

        tvBack.setOnClickListener(v -> finish());

        tvSave = findViewById(R.id.tvSave);
        setupSaveButton();

        if (collegeId != null) loadAllData();
    }

    private void setupSaveButton() {
        if (collegeId == null || tvSave == null) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { tvSave.setVisibility(android.view.View.GONE); return; }
        // Check current bookmark state
        FirebaseDatabase.getInstance().getReference("bookmarks")
                .child(user.getUid()).child(collegeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snapshot) {
                        isBookmarked = snapshot.exists();
                        updateSaveUI();
                    }
                    @Override public void onCancelled(DatabaseError e) {}
                });
        tvSave.setOnClickListener(v -> toggleSave());
    }

    private void toggleSave() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        var ref = FirebaseDatabase.getInstance()
                .getReference("bookmarks").child(user.getUid()).child(collegeId);
        if (isBookmarked) {
            ref.removeValue().addOnSuccessListener(u -> {
                isBookmarked = false; updateSaveUI();
                Toast.makeText(this, "Removed from saved", Toast.LENGTH_SHORT).show();
            });
        } else {
            ref.setValue(true).addOnSuccessListener(u -> {
                isBookmarked = true; updateSaveUI();
                Toast.makeText(this, "College saved!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateSaveUI() {
        if (tvSave == null) return;
        if (isBookmarked) {
            tvSave.setText("❤ Saved");
            tvSave.setBackgroundColor(android.graphics.Color.parseColor("#1A0505"));
            tvSave.setTextColor(android.graphics.Color.parseColor("#EF5350"));
        } else {
            tvSave.setText("♡ Save College");
            tvSave.setBackgroundColor(android.graphics.Color.parseColor("#0F2744"));
            tvSave.setTextColor(android.graphics.Color.parseColor("#64B5F6"));
        }
    }

    private void loadAllData() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("colleges")
                .child(collegeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {

                        progressBar.setVisibility(View.GONE); // ✅ FIX

                        College c = snap.getValue(College.class);
                        if (c == null) return;

                        // ✅ CLEAR OLD DATA (IMPORTANT)
                        llCutoffSection.removeAllViews();
                        llFeeSection.removeAllViews();
                        llRankSection.removeAllViews();
                        llExamSection.removeAllViews();
                        llTrendSection.removeAllViews();
                        llScholarSection.removeAllViews();

                        tvCollegeName.setText(c.getName());
                        tvLocation.setText(c.getLocation() + ", " + c.getState());

                        boolean isGovt = "Government".equals(c.getType());
                        tvTypeBadge.setText(isGovt ? "Government" : "Private");
                        tvTypeBadge.setBackgroundColor(isGovt
                                ? Color.parseColor("#0D3321") : Color.parseColor("#2D1A00"));
                        tvTypeBadge.setTextColor(isGovt
                                ? Color.parseColor("#69F0AE") : Color.parseColor("#FFB74D"));

                        double cutoff = c.getCutoffForCategory(category);
                        boolean eligible = percentage >= cutoff;

                        tvEligibleBadge.setText(eligible ? "✓ Eligible" : "✗ Below cutoff");
                        tvEligibleBadge.setBackgroundColor(eligible
                                ? Color.parseColor("#0D3321") : Color.parseColor("#3B0A0A"));
                        tvEligibleBadge.setTextColor(eligible
                                ? Color.parseColor("#69F0AE") : Color.parseColor("#EF5350"));

                        tvStatEligible.setText(eligible ? "✓" : "✗");
                        tvStatEligible.setTextColor(eligible
                                ? Color.parseColor("#69F0AE") : Color.parseColor("#EF5350"));

                        populateCutoffs(c);
                        loadRanking(c.getName());
                        loadFees(c.getName());
                        loadExam(c.getName());
                        loadTrend(c.getName());
                        loadDeadline(c.getName());
                        loadScholarship();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void populateCutoffs(College c) {
        addCutoffBar(llCutoffSection, "General", c.getCutoffGeneral(), percentage);
        addCutoffBar(llCutoffSection, "OBC", c.getCutoffOBC(), percentage);
        addCutoffBar(llCutoffSection, "SC", c.getCutoffSC(), percentage);
        addCutoffBar(llCutoffSection, "ST", c.getCutoffST(), percentage);
    }

    private void addCutoffBar(LinearLayout parent, String label,
                              double cutoff, double userScore) {

        View row = getLayoutInflater()
                .inflate(R.layout.item_cutoff_bar, parent, false);

        TextView tvLabel = row.findViewById(R.id.tvBarLabel);
        TextView tvVal = row.findViewById(R.id.tvVal);
        View barFill = row.findViewById(R.id.barFill);
        TextView tvYours = row.findViewById(R.id.tvYours);

        tvLabel.setText(label + " " + cutoff + "%");

        boolean eligible = userScore >= cutoff;

        tvVal.setText(eligible ? "✓" : "✗");
        tvVal.setTextColor(eligible
                ? Color.parseColor("#69F0AE") : Color.parseColor("#EF5350"));

        // ✅ FIXED BAR WIDTH
        int fillPct = (int) Math.min(100, cutoff);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = fillPct / 100f;
        barFill.setLayoutParams(lp);

        barFill.setBackgroundColor(eligible
                ? Color.parseColor("#1565C0") : Color.parseColor("#3B0A0A"));

        if (eligible) {
            tvYours.setVisibility(View.VISIBLE);
            tvYours.setText("Your: " + userScore + "%");
        } else {
            tvYours.setVisibility(View.GONE);
        }

        parent.addView(row);
    }

    private void loadRanking(String name) {
        FirebaseDatabase.getInstance().getReference("rankings")
                .addListenerForSingleValueEvent(new SimpleListener(snapshot -> {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String cn = ds.child("collegeName").getValue(String.class);
                        if (cn != null && cn.equalsIgnoreCase(name)) {
                            addDetailRow(llRankSection, "NIRF Rank",
                                    "#" + str(ds, "nirfRank"), "#FFB74D");
                            addDetailRow(llRankSection, "NAAC Grade",
                                    str(ds, "naacGrade"), "#69F0AE");
                            break;
                        }
                    }
                }));
    }

    private void loadFees(String name) {
        FirebaseDatabase.getInstance().getReference("fee_structure")
                .addListenerForSingleValueEvent(new SimpleListener(snapshot -> {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String cn = ds.child("collegeName").getValue(String.class);
                        if (cn != null && cn.equalsIgnoreCase(name)) {
                            addDetailRow(llFeeSection, "Annual Fee",
                                    "₹" + str(ds, "annualFee"), "#69F0AE");
                            break;
                        }
                    }
                }));
    }

    private void loadExam(String name) {}
    private void loadTrend(String name) {}
    private void loadDeadline(String name) {}

    private void loadScholarship() {
        addDetailRow(llScholarSection, "Scholarship", "Available", "#69F0AE");
    }

    private void addDetailRow(LinearLayout parent, String label, String val, String color) {
        View row = getLayoutInflater().inflate(R.layout.item_info_row, parent, false);
        TextView tvLabel = row.findViewById(R.id.tvKey);
        TextView tvVal = row.findViewById(R.id.tvVal);

        tvLabel.setText(label);
        tvVal.setText(val);
        tvVal.setTextColor(Color.parseColor(color));

        parent.addView(row);
    }

    private String str(DataSnapshot ds, String key) {
        Object v = ds.child(key).getValue();
        return v == null ? "—" : String.valueOf(v);
    }

    // ✅ Helper Listener (clean code)
    private static class SimpleListener implements ValueEventListener {
        private final OnData cb;
        SimpleListener(OnData cb) { this.cb = cb; }

        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
            cb.run(snapshot);
        }
        @Override public void onCancelled(@NonNull DatabaseError error) {}

        interface OnData { void run(DataSnapshot snapshot); }
    }
}