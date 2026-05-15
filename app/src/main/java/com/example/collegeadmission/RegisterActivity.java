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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPass;
    private Spinner spinnerCategory;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private final String[] categories = {"General", "OBC", "SC", "ST"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("students");

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPass = findViewById(R.id.etConfirmPass);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPass = etConfirmPass.getText() != null ? etConfirmPass.getText().toString().trim() : "";
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name))        { etName.setError("Name required"); return; }
        if (TextUtils.isEmpty(email))       { etEmail.setError("Email required"); return; }
        if (TextUtils.isEmpty(phone))       { etPhone.setError("Phone required"); return; }
        if (TextUtils.isEmpty(password))    { etPassword.setError("Password required"); return; }
        if (password.length() < 6)          { etPassword.setError("Min 6 characters"); return; }
        if (!password.equals(confirmPass))  { etConfirmPass.setError("Passwords don't match"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) return;
                        String uid = firebaseUser.getUid();
                        Student student = new Student(uid, name, email, phone, category, 0.0);
                        dbRef.child(uid).setValue(student)
                                .addOnCompleteListener(dbTask -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, "Registration successful!",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, DashboardActivity.class));
                                        finish();
                                    } else {
                                        btnRegister.setEnabled(true);
                                        if (dbTask.getException() != null) {
                                            Toast.makeText(this,
                                                    "DB Error: " + dbTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        if (task.getException() != null) {
                            Toast.makeText(this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}