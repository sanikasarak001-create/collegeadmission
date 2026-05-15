package com.example.collegeadmission;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EnhancedProfileActivity extends AppCompatActivity {

    private TextView tvBack, tvInitialsBig, tvProfileName, tvProfileEmail;
    private TextView tvCompletionPct, tvSavedCount, tvSearchCount, tvScoreVal;
    private TextView tvEditToggle;
    private ProgressBar progressCompletion;
    private LinearLayout llViewMode, llEditMode;

    private TextView tvViewName, tvViewPhone, tvViewCategory,
            tvViewState, tvViewPercentage, tvViewStream,
            tvViewTargetCourse, tvViewPrefState;

    private TextInputEditText etEditName, etEditPhone, etEditPercentage,
            etEditTargetCourse;
    private Spinner spinnerCategory, spinnerStream, spinnerState, spinnerPrefState;
    private Button btnSave;
    private ProgressBar progressSave;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private Student currentStudent;
    private boolean isEditMode = false;

    private final String[] categories   = {"General", "OBC", "SC", "ST"};
    private final String[] streams      = {"Science (PCM)", "Science (PCB)", "Commerce", "Arts", "Vocational"};
    private final String[] courses      = {"B.Tech", "MBBS", "B.Com", "BA", "B.Sc", "BBA", "BCA", "LLB", "B.Arch"};
    private final String[] indianStates = {
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar",
            "Chhattisgarh", "Goa", "Gujarat", "Haryana", "Himachal Pradesh",
            "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra",
            "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
            "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
            "Uttar Pradesh", "Uttarakhand", "West Bengal", "Delhi", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_profile);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("students");

        initViews();
        setupSpinners();
        loadProfile();

        tvBack.setOnClickListener(v -> finish());
        tvEditToggle.setOnClickListener(v -> toggleEditMode());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void initViews() {
        tvBack             = findViewById(R.id.tvBack);
        tvInitialsBig      = findViewById(R.id.tvInitialsBig);
        tvProfileName      = findViewById(R.id.tvProfileName);
        tvProfileEmail     = findViewById(R.id.tvProfileEmail);
        tvCompletionPct    = findViewById(R.id.tvCompletionPct);
        progressCompletion = findViewById(R.id.progressCompletion);
        tvSavedCount       = findViewById(R.id.tvSavedCount);
        tvSearchCount      = findViewById(R.id.tvSearchCount);
        tvScoreVal         = findViewById(R.id.tvScoreVal);
        tvEditToggle       = findViewById(R.id.tvEditToggle);

        llViewMode         = findViewById(R.id.llViewMode);
        llEditMode         = findViewById(R.id.llEditMode);

        tvViewName         = findViewById(R.id.tvViewName);
        tvViewPhone        = findViewById(R.id.tvViewPhone);
        tvViewCategory     = findViewById(R.id.tvViewCategory);
        tvViewState        = findViewById(R.id.tvViewState);
        tvViewPercentage   = findViewById(R.id.tvViewPercentage);
        tvViewStream       = findViewById(R.id.tvViewStream);
        tvViewTargetCourse = findViewById(R.id.tvViewTargetCourse);
        tvViewPrefState    = findViewById(R.id.tvViewPrefState);

        etEditName         = findViewById(R.id.etEditName);
        etEditPhone        = findViewById(R.id.etEditPhone);
        etEditPercentage   = findViewById(R.id.etEditPercentage);
        etEditTargetCourse = findViewById(R.id.etEditTargetCourse);
        spinnerCategory    = findViewById(R.id.spinnerCategory);
        spinnerStream      = findViewById(R.id.spinnerStream);
        spinnerState       = findViewById(R.id.spinnerState);
        spinnerPrefState   = findViewById(R.id.spinnerPrefState);
        btnSave            = findViewById(R.id.btnSave);
        progressSave       = findViewById(R.id.progressSave);

        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
    }

    private void setupSpinners() {
        spinnerCategory.setAdapter(makeAdapter(categories));
        spinnerStream.setAdapter(makeAdapter(streams));
        spinnerState.setAdapter(makeAdapter(indianStates));
        spinnerPrefState.setAdapter(makeAdapter(indianStates));
    }

    private ArrayAdapter<String> makeAdapter(String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        dbRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentStudent = snapshot.getValue(Student.class);
                if (currentStudent == null) {
                    currentStudent = new Student();
                    currentStudent.setEmail(user.getEmail());
                }
                populateViews();
                loadStats(user.getUid());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EnhancedProfileActivity.this,
                        "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStats(String uid) {
        // Saved count
        FirebaseDatabase.getInstance().getReference("bookmarks")
                .child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        tvSavedCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override public void onCancelled(DatabaseError e) {}
                });

        // Search/history count
        FirebaseDatabase.getInstance().getReference("history")
                .child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        tvSearchCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override public void onCancelled(DatabaseError e) {}
                });
    }

    private void populateViews() {
        if (currentStudent == null) return;

        String name = currentStudent.getName() != null ? currentStudent.getName() : "—";
        String email = currentStudent.getEmail() != null ? currentStudent.getEmail() : "—";

        tvInitialsBig.setText(currentStudent.getInitials());
        tvProfileName.setText(name);
        tvProfileEmail.setText(email);

        int pct = currentStudent.calculateCompletion();
        progressCompletion.setProgress(pct);
        tvCompletionPct.setText(pct + "% complete");

        double perc = currentStudent.getPercentage();
        tvScoreVal.setText(perc > 0 ? perc + "%" : "—");

        tvViewName.setText(name);
        tvViewPhone.setText(currentStudent.getPhone() != null
                && !currentStudent.getPhone().isEmpty()
                ? currentStudent.getPhone() : "Not added");
        tvViewCategory.setText(currentStudent.getCategory() != null
                ? currentStudent.getCategory() : "Not set");
        tvViewState.setText(currentStudent.getState() != null
                && !currentStudent.getState().isEmpty()
                ? currentStudent.getState() : "Not added");
        tvViewPercentage.setText(perc > 0 ? perc + "%" : "Not added");
        tvViewStream.setText(currentStudent.getStream() != null
                && !currentStudent.getStream().isEmpty()
                ? currentStudent.getStream() : "Not added");
        tvViewTargetCourse.setText(currentStudent.getTargetCourse() != null
                && !currentStudent.getTargetCourse().isEmpty()
                ? currentStudent.getTargetCourse() : "Not added");
        tvViewPrefState.setText(currentStudent.getPreferredState() != null
                && !currentStudent.getPreferredState().isEmpty()
                ? currentStudent.getPreferredState() : "Not added");

        // Pre-fill edit fields
        if (currentStudent.getName() != null)
            etEditName.setText(currentStudent.getName());
        if (currentStudent.getPhone() != null)
            etEditPhone.setText(currentStudent.getPhone());
        if (perc > 0)
            etEditPercentage.setText(String.valueOf(perc));
        if (currentStudent.getTargetCourse() != null)
            etEditTargetCourse.setText(currentStudent.getTargetCourse());

        setSpinnerValue(spinnerCategory, categories, currentStudent.getCategory());
        setSpinnerValue(spinnerStream, streams, currentStudent.getStream());
        setSpinnerValue(spinnerState, indianStates, currentStudent.getState());
        setSpinnerValue(spinnerPrefState, indianStates, currentStudent.getPreferredState());
    }

    private void setSpinnerValue(Spinner spinner, String[] arr, String val) {
        if (val == null) return;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(val)) { spinner.setSelection(i); return; }
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        llViewMode.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
        llEditMode.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        tvEditToggle.setText(isEditMode ? "✕ Cancel" : "✏ Edit Profile");
    }

    private void saveProfile() {
        String name    = etEditName.getText() != null
                ? etEditName.getText().toString().trim() : "";
        String phone   = etEditPhone.getText() != null
                ? etEditPhone.getText().toString().trim() : "";
        String percStr = etEditPercentage.getText() != null
                ? etEditPercentage.getText().toString().trim() : "";
        String course  = etEditTargetCourse.getText() != null
                ? etEditTargetCourse.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etEditName.setError("Name is required"); return;
        }
        if (!TextUtils.isEmpty(percStr)) {
            try {
                double p = Double.parseDouble(percStr);
                if (p < 0 || p > 100) {
                    etEditPercentage.setError("Enter 0–100"); return;
                }
            } catch (NumberFormatException e) {
                etEditPercentage.setError("Invalid number"); return;
            }
        }

        progressSave.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = dbRef.child(user.getUid());
        ref.child("name").setValue(name);
        ref.child("phone").setValue(phone);
        ref.child("category").setValue(spinnerCategory.getSelectedItem().toString());
        ref.child("stream").setValue(spinnerStream.getSelectedItem().toString());
        ref.child("state").setValue(spinnerState.getSelectedItem().toString());
        ref.child("preferredState").setValue(spinnerPrefState.getSelectedItem().toString());
        ref.child("targetCourse").setValue(course);

        if (!TextUtils.isEmpty(percStr)) {
            ref.child("percentage").setValue(Double.parseDouble(percStr));
        }

        ref.child("email").setValue(user.getEmail())
                .addOnCompleteListener(task -> {
                    progressSave.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        isEditMode = false;
                        loadProfile();
                        toggleEditMode();
                    } else {
                        Toast.makeText(this, "Save failed. Try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}