package com.example.collegeadmission;

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

public class ProfileActivity extends AppCompatActivity {

    private TextView tvBack, tvEmail;
    private TextInputEditText etName, etPhone;
    private Spinner spinnerCategory;
    private Button btnSave;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private final String[] categories = {"General", "OBC", "SC", "ST"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("students");

        tvBack          = findViewById(R.id.tvBack);
        tvEmail         = findViewById(R.id.tvEmail);
        etName          = findViewById(R.id.etName);
        etPhone         = findViewById(R.id.etPhone);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave         = findViewById(R.id.btnSave);
        progressBar     = findViewById(R.id.progressBar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        loadProfile();
        tvBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        tvEmail.setText(user.getEmail());
        dbRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Student s = snapshot.getValue(Student.class);
                if (s != null) {
                    if (s.getName() != null)  etName.setText(s.getName());
                    if (s.getPhone() != null) etPhone.setText(s.getPhone());
                    if (s.getCategory() != null) {
                        for (int i = 0; i < categories.length; i++) {
                            if (categories[i].equals(s.getCategory())) {
                                spinnerCategory.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Handle silently
            }
        });
    }

    private void saveProfile() {
        String name     = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone    = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name))  { etName.setError("Name required"); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Phone required"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        DatabaseReference userRef = dbRef.child(user.getUid());
        userRef.child("name").setValue(name);
        userRef.child("phone").setValue(phone);
        userRef.child("category").setValue(category)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}