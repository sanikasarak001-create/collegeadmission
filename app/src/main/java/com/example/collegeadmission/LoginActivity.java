package com.example.collegeadmission;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPass;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPass = findViewById(R.id.tvForgotPass);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPass.setOnClickListener(v -> {
            String email = etEmail.getText() != null
                    ? etEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter email first");
                return;
            }
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show();
                } else if (task.getException() != null) {
                    Toast.makeText(this, task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void loginUser() {
        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) { etEmail.setError("Email required"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password required"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    } else if (task.getException() != null) {
                        Toast.makeText(this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}