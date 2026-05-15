package com.example.collegeadmission;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack, tvResultCount, tvNoResults;
    private TextView tvTotalCount, tvGovtCount, tvPvtCount;
    private ProgressBar progressBar;

    private final ArrayList<CollegeFullData> resultList = new ArrayList<>();
    private CollegeResultAdapter adapter;

    private double percentage;
    private String category;
    private String targetCourse;
    private String stream;

    private final Map<String, DataSnapshot> rankingMap     = new HashMap<>();
    private final Map<String, DataSnapshot> feeMap         = new HashMap<>();
    private final Map<String, DataSnapshot> deadlineMap    = new HashMap<>();
    private final Map<String, DataSnapshot> examMap        = new HashMap<>();
    private final Map<String, DataSnapshot> trendMap       = new HashMap<>();
    private final Map<String, DataSnapshot> scholarshipMap = new HashMap<>();

    private int loadedCount = 0;
    private static final int TOTAL_NODES = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        percentage   = getIntent().getDoubleExtra("percentage", 0);
        category     = getIntent().getStringExtra("category");
        targetCourse = getIntent().getStringExtra("targetCourse");
        stream       = getIntent().getStringExtra("stream");

        if (category     == null) category     = "General";
        if (targetCourse == null) targetCourse = "Any Course";
        if (stream       == null) stream       = "Any Stream";

        recyclerView  = findViewById(R.id.recyclerView);
        tvBack        = findViewById(R.id.tvBack);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvNoResults   = findViewById(R.id.tvNoResults);
        progressBar   = findViewById(R.id.progressBar);
        tvTotalCount  = findViewById(R.id.tvTotalCount);
        tvGovtCount   = findViewById(R.id.tvGovtCount);
        tvPvtCount    = findViewById(R.id.tvPvtCount);

        adapter = new CollegeResultAdapter(this, resultList, data -> {
            Intent intent = new Intent(this, CollegeDetailActivity.class);
            intent.putExtra("collegeId",    data.college.getId());
            intent.putExtra("percentage",   percentage);
            intent.putExtra("category",     category);
            intent.putExtra("targetCourse", targetCourse);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        tvBack.setOnClickListener(v -> finish());

        String cl = targetCourse.equals("Any Course") ? "" : " | " + targetCourse;
        tvResultCount.setText("Searching for " + percentage + "% (" + category + ")" + cl);

        loadAllDataThenFilter();
    }

    private void loadAllDataThenFilter() {
        progressBar.setVisibility(View.VISIBLE);
        loadedCount = 0;
        loadNode("rankings",       rankingMap);
        loadNode("fee_structure",  feeMap);
        loadNode("deadlines",      deadlineMap);
        loadNode("entrance_exams", examMap);
        loadNode("cutoff_trends",  trendMap);
        loadNode("scholarships",   scholarshipMap);
    }

    private void loadNode(String node, Map<String, DataSnapshot> map) {
        FirebaseDatabase.getInstance().getReference(node)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.getKey() != null) map.put(ds.getKey(), ds);
                        }
                        loadedCount++;
                        if (loadedCount >= TOTAL_NODES) filterColleges();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadedCount++;
                        if (loadedCount >= TOTAL_NODES) filterColleges();
                    }
                });
    }

    // ════════════════════════════════════════════════════════
    // FIXED: smart course matching against real Firebase data
    // Firebase stores: "Engineering, Science" / "Arts, Science, Commerce, Law"
    // Spinner has:     "BCA" / "B.Tech / B.E" / "BA" etc.
    // We map spinner → keywords that appear in those course strings.
    // ════════════════════════════════════════════════════════
    private boolean courseMatches(String collegeCoursesRaw, String target) {
        if (target == null || target.equals("Any Course")) return true;
        if (collegeCoursesRaw == null) return false;

        String offered = collegeCoursesRaw.toLowerCase();
        String t       = target.toLowerCase();

        // B.Tech / B.E  → Engineering or Technology
        if (t.contains("b.tech") || t.contains("b.e")) {
            return offered.contains("engineering") || offered.contains("technology");
        }
        // MBBS → Medicine
        if (t.contains("mbbs")) {
            return offered.contains("medicine") || offered.contains("mbbs");
        }
        // BBA → Management
        if (t.contains("bba")) {
            return offered.contains("management") || offered.contains("bba");
        }
        // BCA → Management OR Engineering (many multi-course universities offer BCA)
        // IMPORTANT: Firebase has "Engineering, Management, Law, Media" not "BCA"
        // So we open this up broadly: any university with Management or Engineering
        if (t.contains("bca")) {
            return offered.contains("management")
                    || offered.contains("engineering")
                    || offered.contains("technology")
                    || offered.contains("science")
                    || offered.contains("bca")
                    || offered.contains("computer");
        }
        // B.Sc → Science
        if (t.contains("b.sc")) {
            return offered.contains("science");
        }
        // BA → Arts
        if (t.contains("ba")) {
            return offered.contains("arts");
        }
        // B.Com → Commerce
        if (t.contains("b.com")) {
            return offered.contains("commerce");
        }
        // LLB → Law
        if (t.contains("llb")) {
            return offered.contains("law");
        }
        // B.Arch → Engineering or Architecture
        if (t.contains("b.arch")) {
            return offered.contains("arch") || offered.contains("engineering");
        }
        // MBA → Management
        if (t.contains("mba")) {
            return offered.contains("management") || offered.contains("mba");
        }
        // MCA → Engineering / Science / Management / Technology
        if (t.contains("mca")) {
            return offered.contains("engineering")
                    || offered.contains("science")
                    || offered.contains("management")
                    || offered.contains("technology")
                    || offered.contains("mca")
                    || offered.contains("computer");
        }
        // Fallback — always show
        return true;
    }

    private void filterColleges() {
        FirebaseDatabase.getInstance().getReference("colleges")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        resultList.clear();
                        int govt = 0, pvt = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            College college = ds.getValue(College.class);
                            if (college == null) continue;

                            // 1️⃣ Cutoff check
                            double cutoff = college.getCutoffForCategory(category);
                            if (percentage < cutoff) continue;

                            // 2️⃣ Course check — uses the smart matcher above
                            if (!courseMatches(college.getCourses(), targetCourse)) continue;

                            CollegeFullData data = new CollegeFullData();
                            data.college  = college;
                            data.cutoff   = cutoff;
                            data.matchPct = (int) Math.min(100,
                                    Math.round((percentage / cutoff) * 100));

                            for (DataSnapshot r : rankingMap.values()) {
                                String cn = r.child("collegeName").getValue(String.class);
                                if (cn != null && cn.equalsIgnoreCase(college.getName())) {
                                    data.nirfRank  = r.child("nirfRank").getValue(String.class);
                                    data.naacGrade = r.child("naacGrade").getValue(String.class);
                                    data.nirfScore = r.child("nirfScore").getValue(String.class);
                                    break;
                                }
                            }
                            for (DataSnapshot f : feeMap.values()) {
                                String cn = f.child("collegeName").getValue(String.class);
                                if (cn != null && cn.equalsIgnoreCase(college.getName())) {
                                    data.annualFee   = f.child("annualFee").getValue(String.class);
                                    data.totalFee    = f.child("totalFee").getValue(String.class);
                                    data.hostelFee   = f.child("hostelFee").getValue(String.class);
                                    data.scholarship = f.child("scholarshipAvailable").getValue(String.class);
                                    break;
                                }
                            }
                            for (DataSnapshot d : deadlineMap.values()) {
                                String cn = d.child("collegeName").getValue(String.class);
                                if (cn != null && cn.equalsIgnoreCase(college.getName())) {
                                    data.deadline = d.child("deadline").getValue(String.class);
                                    data.course   = d.child("course").getValue(String.class);
                                    break;
                                }
                            }
                            for (DataSnapshot e : examMap.values()) {
                                String cn = e.child("collegeName").getValue(String.class);
                                if (cn != null && cn.equalsIgnoreCase(college.getName())) {
                                    data.examName    = e.child("examName").getValue(String.class);
                                    data.examDate    = e.child("examDate").getValue(String.class);
                                    data.examMode    = e.child("mode").getValue(String.class);
                                    data.eligibility = e.child("eligibility").getValue(String.class);
                                    break;
                                }
                            }
                            for (DataSnapshot t : trendMap.values()) {
                                String cn = t.child("collegeName").getValue(String.class);
                                if (cn != null && cn.equalsIgnoreCase(college.getName())) {
                                    Double c22 = t.child("cutoff2022").getValue(Double.class);
                                    Double c23 = t.child("cutoff2023").getValue(Double.class);
                                    Double c24 = t.child("cutoff2024").getValue(Double.class);
                                    data.cutoff2022 = c22 != null ? c22 : 0;
                                    data.cutoff2023 = c23 != null ? c23 : 0;
                                    data.cutoff2024 = c24 != null ? c24 : 0;
                                    break;
                                }
                            }

                            resultList.add(data);
                            if ("Government".equals(college.getType())) govt++;
                            else pvt++;
                        }

                        final int g = govt, p = pvt;
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                            int total = resultList.size();
                            tvTotalCount.setText(String.valueOf(total));
                            tvGovtCount.setText(String.valueOf(g));
                            tvPvtCount.setText(String.valueOf(p));
                            String cl2 = targetCourse.equals("Any Course") ? "" : " | " + targetCourse;
                            tvResultCount.setText(total + " colleges for " + percentage + "% (" + category + ")" + cl2);
                            if (resultList.isEmpty()) {
                                tvNoResults.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    }
                });
    }
}
