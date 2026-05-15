package com.example.collegeadmission;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ChecklistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack, tvProgress;
    private ProgressBar progressBar, progressDone;
    private DatabaseReference userChecklistRef;
    private final ArrayList<ChecklistItem> itemList = new ArrayList<>();
    private ChecklistAdapter adapter;

    public static class ChecklistItem {
        public String id;
        public String task;
        public String description;
        public boolean done;

        public ChecklistItem() {}
        public ChecklistItem(String id, String task, String description) {
            this.id = id; this.task = task;
            this.description = description; this.done = false;
        }
    }

    // Default checklist steps every student needs
    private static final ChecklistItem[] DEFAULT_ITEMS = {
            new ChecklistItem("c1",  "Register on NSP Portal",       "Go to scholarships.gov.in and create account"),
            new ChecklistItem("c2",  "Fill CUET / Entrance Form",    "Apply on cuet.samarth.ac.in before deadline"),
            new ChecklistItem("c3",  "Gather Class 10 Marksheet",    "Original + 3 attested photocopies"),
            new ChecklistItem("c4",  "Gather Class 12 Marksheet",    "Original + 3 attested photocopies"),
            new ChecklistItem("c5",  "Get Category Certificate",     "SC/ST/OBC – get from Tehsil office"),
            new ChecklistItem("c6",  "Get Income Certificate",       "For scholarship and fee waiver"),
            new ChecklistItem("c7",  "Prepare Passport Photo",       "6 recent passport size colour photos"),
            new ChecklistItem("c8",  "Get Aadhar Card copy",         "Self-attested Aadhar + PAN copy"),
            new ChecklistItem("c9",  "Apply for Domicile Certificate","Required for state quota seats"),
            new ChecklistItem("c10", "Shortlist 5 Colleges",         "Based on your prediction results"),
            new ChecklistItem("c11", "Check College Fee Structure",  "Plan finances and loan if needed"),
            new ChecklistItem("c12", "Apply for Education Loan",     "If needed — SBI, Canara, HDFC offer them"),
            new ChecklistItem("c13", "Fill College Application Forms","Apply on official college website"),
            new ChecklistItem("c14", "Pay Application Fees",         "Save all payment receipts"),
            new ChecklistItem("c15", "Attend Counselling / Interview","Carry all original documents"),
            new ChecklistItem("c16", "Confirm Admission & Pay Fees", "Pay seat booking fee to confirm seat"),
            new ChecklistItem("c17", "Apply for Hostel",             "If required, apply immediately after admission"),
            new ChecklistItem("c18", "Apply for Scholarship",        "Apply on NSP / college portal"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        recyclerView  = findViewById(R.id.recyclerView);
        tvBack        = findViewById(R.id.tvBack);
        tvProgress    = findViewById(R.id.tvProgress);
        progressBar   = findViewById(R.id.progressBar);
        progressDone  = findViewById(R.id.progressDone);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        userChecklistRef = FirebaseDatabase.getInstance()
                .getReference("checklists").child(user.getUid());

        adapter = new ChecklistAdapter(itemList, (item, isDone) -> {
            item.done = isDone;
            userChecklistRef.child(item.id).child("done").setValue(isDone);
            updateProgress();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        tvBack.setOnClickListener(v -> finish());
        loadChecklist();
    }

    private void loadChecklist() {
        progressBar.setVisibility(View.VISIBLE);
        userChecklistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (ChecklistItem def : DEFAULT_ITEMS) {
                    DataSnapshot ds = snapshot.child(def.id);
                    boolean done = Boolean.TRUE.equals(ds.child("done").getValue(Boolean.class));
                    def.done = done;
                    itemList.add(def);
                    // Init in Firebase if not exists
                    if (!ds.exists()) userChecklistRef.child(def.id).setValue(def);
                }
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                updateProgress();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateProgress() {
        int done = 0;
        for (ChecklistItem i : itemList) if (i.done) done++;
        int total = itemList.size();
        int pct = total > 0 ? (done * 100) / total : 0;
        tvProgress.setText(done + " / " + total + " tasks completed (" + pct + "%)");
        progressDone.setProgress(pct);
    }

    interface OnCheckChanged {
        void onChanged(ChecklistItem item, boolean isDone);
    }

    static class ChecklistAdapter
            extends RecyclerView.Adapter<ChecklistAdapter.VH> {
        private final ArrayList<ChecklistItem> list;
        private final OnCheckChanged listener;

        ChecklistAdapter(ArrayList<ChecklistItem> list, OnCheckChanged l) {
            this.list = list; this.listener = l;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_checklist, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ChecklistItem item = list.get(position);
            h.checkBox.setText(item.task);
            h.tvDesc.setText(item.description);
            h.checkBox.setOnCheckedChangeListener(null);
            h.checkBox.setChecked(item.done);
            h.checkBox.setOnCheckedChangeListener(
                    (btn, checked) -> listener.onChanged(item, checked));
            h.itemView.setAlpha(item.done ? 0.6f : 1.0f);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            TextView tvDesc;
            VH(View v) {
                super(v);
                checkBox = v.findViewById(R.id.checkBox);
                tvDesc   = v.findViewById(R.id.tvDesc);
            }
        }
    }
}