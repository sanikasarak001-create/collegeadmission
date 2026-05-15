package com.example.collegeadmission;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CollegeResultAdapter
        extends RecyclerView.Adapter<CollegeResultAdapter.VH> {

    private final Context context;
    private final ArrayList<CollegeFullData> list;
    private final OnItemClick listener;
    // Track bookmark state per position
    private final boolean[] bookmarked;

    public interface OnItemClick {
        void onClick(CollegeFullData data);
    }

    public CollegeResultAdapter(Context ctx,
                                ArrayList<CollegeFullData> list,
                                OnItemClick listener) {
        this.context    = ctx;
        this.list       = list;
        this.listener   = listener;
        this.bookmarked = new boolean[200]; // enough for all colleges
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_college_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CollegeFullData d = list.get(position);
        College c = d.college;

        h.tvRank.setText(String.valueOf(position + 1));
        h.tvName.setText(c.getName());
        h.tvLocation.setText(c.getLocation() + ", " + c.getState());
        h.tvCutoff.setText("Cutoff: " + d.cutoff + "%");

        // Govt / Private badge
        boolean isGovt = "Government".equals(c.getType());
        h.tvType.setText(isGovt ? "Govt" : "Private");
        h.tvType.setBackgroundColor(isGovt
                ? Color.parseColor("#0D3321") : Color.parseColor("#2D1A00"));
        h.tvType.setTextColor(isGovt
                ? Color.parseColor("#69F0AE") : Color.parseColor("#FFB74D"));

        h.tvEligible.setText("✓ Eligible");
        h.tvEligible.setTextColor(Color.parseColor("#69F0AE"));

        // ── Save / Bookmark button ──
        checkIfBookmarked(c.getId(), position, h.tvSave);
        h.tvSave.setOnClickListener(v -> toggleBookmark(c.getId(), position, h.tvSave));

        // ── Build info tabs ──
        String[] tabs = {"Overview", "Fees", "Ranking", "Deadline"};
        final int[] activeTab = {0};
        h.llTabs.removeAllViews();

        for (int i = 0; i < tabs.length; i++) {
            TextView tab = new TextView(context);
            tab.setText(tabs[i]);
            tab.setTextSize(10f);
            tab.setPadding(dp(10), dp(5), dp(10), dp(5));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(4));
            tab.setLayoutParams(lp);
            final int idx = i;
            tab.setOnClickListener(v -> {
                activeTab[0] = idx;
                styleAllTabs(h.llTabs, idx);
                populateInfo(h, d, idx);
            });
            h.llTabs.addView(tab);
        }
        styleAllTabs(h.llTabs, 0);
        populateInfo(h, d, 0);

        // Chips
        if (d.deadline != null && !d.deadline.isEmpty()) {
            h.tvDeadlineChip.setVisibility(View.VISIBLE);
            h.tvDeadlineChip.setText("Deadline: " + d.deadline);
        } else {
            h.tvDeadlineChip.setVisibility(View.GONE);
        }
        boolean hasScholar = d.scholarship != null
                && d.scholarship.toLowerCase().startsWith("yes");
        h.tvScholarshipChip.setVisibility(hasScholar ? View.VISIBLE : View.GONE);

        h.cardView.setOnClickListener(v -> { if (listener != null) listener.onClick(d); });
    }

    // ── Check Firebase if already saved ──
    private void checkIfBookmarked(String collegeId, int position, TextView btn) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseDatabase.getInstance()
                .getReference("bookmarks")
                .child(user.getUid())
                .child(collegeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookmarked[position] = snapshot.exists();
                        updateSaveBtn(btn, bookmarked[position]);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // ── Toggle save in Firebase ──
    private void toggleBookmark(String collegeId, int position, TextView btn) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please log in to save", Toast.LENGTH_SHORT).show();
            return;
        }
        var ref = FirebaseDatabase.getInstance()
                .getReference("bookmarks")
                .child(user.getUid())
                .child(collegeId);

        if (bookmarked[position]) {
            ref.removeValue().addOnSuccessListener(unused -> {
                bookmarked[position] = false;
                updateSaveBtn(btn, false);
                Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
            });
        } else {
            ref.setValue(true).addOnSuccessListener(unused -> {
                bookmarked[position] = true;
                updateSaveBtn(btn, true);
                Toast.makeText(context, "College saved!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateSaveBtn(TextView btn, boolean saved) {
        if (saved) {
            btn.setText("❤ Saved");
            btn.setTextColor(Color.parseColor("#EF5350"));
            btn.setBackgroundColor(Color.parseColor("#1A0505"));
        } else {
            btn.setText("♡ Save");
            btn.setTextColor(Color.parseColor("#90A4AE"));
            btn.setBackgroundColor(Color.parseColor("#0F2744"));
        }
    }

    private void styleAllTabs(LinearLayout ll, int active) {
        for (int i = 0; i < ll.getChildCount(); i++) {
            TextView tv = (TextView) ll.getChildAt(i);
            if (i == active) {
                tv.setBackgroundColor(Color.parseColor("#1565C0"));
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackgroundColor(Color.TRANSPARENT);
                tv.setTextColor(Color.parseColor("#64B5F6"));
            }
        }
    }

    private void populateInfo(VH h, CollegeFullData d, int tab) {
        h.llInfoRows.removeAllViews();
        switch (tab) {
            case 0: buildOverview(h, d); break;
            case 1: buildFees(h, d);     break;
            case 2: buildRanking(h, d);  break;
            case 3: buildDeadline(h, d); break;
        }
    }

    private void buildOverview(VH h, CollegeFullData d) {
        addRow(h, "Courses",    safe(d.college.getCourses()), "#90CAF9");
        if (ok(d.examName))
            addRow(h, "Entrance",   d.examName,               "#64B5F6");
        addRow(h, "NIRF Rank",  ok(d.nirfRank)  ? "#" + d.nirfRank  : "—", "#FFB74D");
        addRow(h, "NAAC Grade", ok(d.naacGrade) ? d.naacGrade       : "—", "#69F0AE");
        addRow(h, "Type",       safe(d.college.getType()),             "#B0BEC5");
    }

    private void buildFees(VH h, CollegeFullData d) {
        addRow(h, "Annual Fee",  ok(d.annualFee) ? d.annualFee : "—",  "#69F0AE");
        addRow(h, "Total Fee",   ok(d.totalFee)  ? d.totalFee  : "—",  "#B0BEC5");
        addRow(h, "Hostel/yr",   ok(d.hostelFee) ? d.hostelFee : "—",  "#90A4AE");
        boolean yes = ok(d.scholarship) && d.scholarship.toLowerCase().startsWith("yes");
        addRow(h, "Scholarship", ok(d.scholarship) ? d.scholarship : "—",
                yes ? "#69F0AE" : "#EF5350");
    }

    private void buildRanking(VH h, CollegeFullData d) {
        addRow(h, "NIRF Rank",  ok(d.nirfRank)  ? "#" + d.nirfRank  : "—", "#FFB74D");
        addRow(h, "NIRF Score", ok(d.nirfScore) ? d.nirfScore        : "—", "#64B5F6");
        addRow(h, "NAAC Grade", ok(d.naacGrade) ? d.naacGrade        : "—", "#69F0AE");
        if (d.cutoff2022 > 0) {
            addRow(h, "Cutoff 2022", d.cutoff2022 + "%", "#78909C");
            addRow(h, "Cutoff 2023", d.cutoff2023 + "%", "#90A4AE");
            addRow(h, "Cutoff 2024", d.cutoff2024 + "%", "#90CAF9");
            boolean rising = d.cutoff2024 > d.cutoff2022;
            addRow(h, "Trend", rising ? "↑ Rising" : "↓ Stable / Falling",
                    rising ? "#EF5350" : "#69F0AE");
        }
    }

    private void buildDeadline(VH h, CollegeFullData d) {
        addRow(h, "Deadline",  ok(d.deadline) ? d.deadline : "—", "#FFB74D");
        addRow(h, "Program",   ok(d.course)   ? d.course   : "—", "#B0BEC5");
        addRow(h, "Exam Date", ok(d.examDate) ? d.examDate : "—", "#64B5F6");
        addRow(h, "Mode",      ok(d.examMode) ? d.examMode : "—", "#90A4AE");
    }

    private void addRow(VH h, String key, String val, String hex) {
        View row = LayoutInflater.from(context)
                .inflate(R.layout.item_info_row, h.llInfoRows, false);
        ((TextView) row.findViewById(R.id.tvKey)).setText(key);
        TextView tvVal = row.findViewById(R.id.tvVal);
        tvVal.setText(val);
        try { tvVal.setTextColor(Color.parseColor(hex)); }
        catch (Exception e) { tvVal.setTextColor(Color.WHITE); }
        h.llInfoRows.addView(row);
    }

    private boolean ok(String s) { return s != null && !s.isEmpty(); }
    private String  safe(String s) { return ok(s) ? s : "—"; }
    private int     dp(int v) {
        return Math.round(v * context.getResources().getDisplayMetrics().density);
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView     cardView;
        TextView     tvRank, tvName, tvLocation, tvCutoff, tvType,
                tvEligible, tvSave, tvDeadlineChip, tvScholarshipChip;
        LinearLayout llTabs, llInfoRows;

        VH(View v) {
            super(v);
            cardView          = v.findViewById(R.id.cardView);
            tvRank            = v.findViewById(R.id.tvRank);
            tvName            = v.findViewById(R.id.tvName);
            tvLocation        = v.findViewById(R.id.tvLocation);
            tvCutoff          = v.findViewById(R.id.tvCutoff);
            tvType            = v.findViewById(R.id.tvType);
            tvEligible        = v.findViewById(R.id.tvEligible);
            tvSave            = v.findViewById(R.id.tvSave);
            tvDeadlineChip    = v.findViewById(R.id.tvDeadlineChip);
            tvScholarshipChip = v.findViewById(R.id.tvScholarshipChip);
            llTabs            = v.findViewById(R.id.llTabs);
            llInfoRows        = v.findViewById(R.id.llInfoRows);
        }
    }
}
