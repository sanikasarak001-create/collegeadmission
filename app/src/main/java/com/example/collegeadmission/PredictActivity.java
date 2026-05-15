package com.example.collegeadmission;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PredictActivity extends AppCompatActivity {

    private TextInputEditText etPercentage;
    private Button btnPredict;
    private ProgressBar progressBar;
    private TextView tvBack;
    private Spinner spinnerCategory, spinnerCourse, spinnerStream;

    private DatabaseReference dbColleges, dbStudents;
    private FirebaseAuth mAuth;

    private final String[] categories = {"General", "OBC", "SC", "ST"};

    private final String[] courses = {
            "Any Course",
            "B.Tech / B.E",
            "MBBS",
            "BBA",
            "BCA",
            "B.Sc",
            "BA",
            "B.Com",
            "LLB",
            "B.Arch",
            "MBA",
            "MCA"
    };

    private final String[] streams = {
            "Any Stream",
            "Science (PCM)",
            "Science (PCB)",
            "Commerce",
            "Arts / Humanities",
            "Vocational"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        mAuth      = FirebaseAuth.getInstance();
        dbColleges = FirebaseDatabase.getInstance().getReference("colleges");
        dbStudents = FirebaseDatabase.getInstance().getReference("students");

        etPercentage    = findViewById(R.id.etPercentage);
        btnPredict      = findViewById(R.id.btnPredict);
        progressBar     = findViewById(R.id.progressBar);
        tvBack          = findViewById(R.id.tvBack);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCourse   = findViewById(R.id.spinnerCourse);
        spinnerStream   = findViewById(R.id.spinnerStream);

        spinnerCategory.setAdapter(makeAdapter(categories));
        spinnerCourse.setAdapter(makeAdapter(courses));
        spinnerStream.setAdapter(makeAdapter(streams));

        loadStudentData();
        btnPredict.setOnClickListener(v -> predictColleges());
        tvBack.setOnClickListener(v -> finish());
    }

    private ArrayAdapter<String> makeAdapter(String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void loadStudentData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        dbStudents.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Student s = snapshot.getValue(Student.class);
                if (s == null) return;

                if (s.getPercentage() > 0)
                    etPercentage.setText(String.valueOf(s.getPercentage()));

                String cat = s.getCategory();
                if (cat != null) {
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equals(cat)) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                }

                String stream = s.getStream();
                if (stream != null) {
                    for (int i = 0; i < streams.length; i++) {
                        if (streams[i].equals(stream)) {
                            spinnerStream.setSelection(i);
                            break;
                        }
                    }
                }

                String course = s.getTargetCourse();
                if (course != null) {
                    for (int i = 0; i < courses.length; i++) {
                        if (courses[i].equals(course)) {
                            spinnerCourse.setSelection(i);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void predictColleges() {
        String percStr = etPercentage.getText() != null
                ? etPercentage.getText().toString().trim() : "";

        if (TextUtils.isEmpty(percStr)) {
            etPercentage.setError("Enter percentage");
            return;
        }

        double percentage;
        try {
            percentage = Double.parseDouble(percStr);
        } catch (NumberFormatException e) {
            etPercentage.setError("Invalid number");
            return;
        }

        if (percentage < 0 || percentage > 100) {
            etPercentage.setError("Enter value between 0–100");
            return;
        }

        String category     = spinnerCategory.getSelectedItem().toString();
        String targetCourse = spinnerCourse.getSelectedItem().toString();
        String stream       = spinnerStream.getSelectedItem().toString();

        progressBar.setVisibility(View.VISIBLE);
        btnPredict.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference uRef = dbStudents.child(user.getUid());
            uRef.child("percentage").setValue(percentage);
            uRef.child("category").setValue(category);
            uRef.child("targetCourse").setValue(
                    targetCourse.equals("Any Course") ? "" : targetCourse);
            uRef.child("stream").setValue(
                    stream.equals("Any Stream") ? "" : stream);
        }

        final double finalPercentage = percentage;

        dbColleges.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                btnPredict.setEnabled(true);

                // Save to history
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a", java.util.Locale.getDefault());
                String date = sdf.format(new java.util.Date());

                FirebaseUser histUser = mAuth.getCurrentUser();
                if (histUser != null) {
                    HistoryActivity.PredictionRecord record =
                            new HistoryActivity.PredictionRecord(
                                    date, finalPercentage, category,
                                    (int) snapshot.getChildrenCount());
                    FirebaseDatabase.getInstance()
                            .getReference("history")
                            .child(histUser.getUid())
                            .push()
                            .setValue(record);
                }

                Intent intent = new Intent(PredictActivity.this, ResultActivity.class);
                intent.putExtra("percentage",   finalPercentage);
                intent.putExtra("category",     category);
                intent.putExtra("targetCourse", targetCourse);
                intent.putExtra("stream",       stream);
                startActivity(intent);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                btnPredict.setEnabled(true);
                Toast.makeText(PredictActivity.this,
                        "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
