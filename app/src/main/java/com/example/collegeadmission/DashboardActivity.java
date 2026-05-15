package com.example.collegeadmission;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUserName, tvWelcome, tvInitials, tvCompletionPct;
    private CardView cardPredict, cardBrowse, cardBookmark, cardDeadline;
    private CardView cardExam, cardTrend, cardFee, cardScholarship;
    private CardView cardRanking, cardChecklist, cardAi, cardNearMe;
    private CardView cardAvatarBtn;
    private ProgressBar progressCompletion;
    private ImageView ivLogout;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("students");

        tvUserName       = findViewById(R.id.tvUserName);
        tvWelcome        = findViewById(R.id.tvWelcome);
        tvInitials       = findViewById(R.id.tvInitials);
        tvCompletionPct  = findViewById(R.id.tvCompletionPct);
        progressCompletion = findViewById(R.id.progressCompletion);
        cardAvatarBtn    = findViewById(R.id.cardAvatarBtn);
        ivLogout         = findViewById(R.id.ivLogout);

        cardPredict      = findViewById(R.id.cardPredict);
        cardBrowse       = findViewById(R.id.cardBrowse);
        cardBookmark     = findViewById(R.id.cardBookmark);
        cardDeadline     = findViewById(R.id.cardDeadline);
        cardExam         = findViewById(R.id.cardExam);
        cardTrend        = findViewById(R.id.cardTrend);
        cardFee          = findViewById(R.id.cardFee);
        cardScholarship  = findViewById(R.id.cardScholarship);
        cardRanking      = findViewById(R.id.cardRanking);
        cardChecklist    = findViewById(R.id.cardChecklist);
        cardAi           = findViewById(R.id.cardAi);
        cardNearMe       = findViewById(R.id.cardNearMe);

        loadStudentData();

        cardAvatarBtn.setOnClickListener(v ->
                startActivity(new Intent(this, EnhancedProfileActivity.class)));
        ivLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        cardPredict.setOnClickListener(v     -> startActivity(new Intent(this, PredictActivity.class)));
        cardBrowse.setOnClickListener(v      -> startActivity(new Intent(this, BrowseActivity.class)));
        cardBookmark.setOnClickListener(v    -> startActivity(new Intent(this, BookmarkActivity.class)));
        cardDeadline.setOnClickListener(v    -> startActivity(new Intent(this, DeadlineActivity.class)));
        cardExam.setOnClickListener(v        -> startActivity(new Intent(this, EntranceExamActivity.class)));
        cardTrend.setOnClickListener(v       -> startActivity(new Intent(this, CutoffTrendActivity.class)));
        cardFee.setOnClickListener(v         -> startActivity(new Intent(this, FeeStructureActivity.class)));
        cardScholarship.setOnClickListener(v -> startActivity(new Intent(this, ScholarshipActivity.class)));
        cardRanking.setOnClickListener(v     -> startActivity(new Intent(this, RankingActivity.class)));
        cardChecklist.setOnClickListener(v   -> startActivity(new Intent(this, ChecklistActivity.class)));
        cardAi.setOnClickListener(v          -> startActivity(new Intent(this, AiCounsellorActivity.class)));
        cardNearMe.setOnClickListener(v      -> startActivity(new Intent(this, NearMeActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudentData();
    }

    private void loadStudentData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        dbRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Student student = snapshot.getValue(Student.class);
                if (student != null) {
                    tvUserName.setText(student.getName() != null
                            ? student.getName() : "Student");
                    tvInitials.setText(student.getInitials());

                    int pct = student.calculateCompletion();
                    progressCompletion.setProgress(pct);
                    tvCompletionPct.setText("Profile " + pct + "% complete");
                } else {
                    tvInitials.setText("?");
                    tvCompletionPct.setText("Complete your profile");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}