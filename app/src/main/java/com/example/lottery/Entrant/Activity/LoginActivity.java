package com.example.lottery.Entrant.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lottery.MainActivity;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import android.graphics.Paint;

public class LoginActivity extends AppCompatActivity {

    private TextView tabLogin, tabSignup, tvForgotPassword;
    private TextView labelName;
    private EditText etName, etEmail, etPassword;
    private MaterialButton btnPrimary, btnGuest;

    private boolean isLoginMode = true;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        mAuth.signOut(); // temporary - remove after testing


        if (mAuth.getCurrentUser() != null) {
            navigateToMain(mAuth.getCurrentUser().isAnonymous());
            return;
        }

        tabLogin         = findViewById(R.id.tabLogin);
        tabSignup        = findViewById(R.id.tabSignup);
        labelName        = findViewById(R.id.labelName);
        etName           = findViewById(R.id.etName);
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnPrimary       = findViewById(R.id.btnPrimary);
        btnGuest         = findViewById(R.id.btnGuest);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvForgotPassword.setPaintFlags(tvForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        tabLogin.setOnClickListener(v -> switchMode(true));
        tabSignup.setOnClickListener(v -> switchMode(false));

        btnPrimary.setOnClickListener(v -> {
            if (isLoginMode) handleLogin();
            else handleSignup();
        });

        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        btnGuest.setOnClickListener(v -> handleDeviceLogin());

        switchMode(true);
    }

    private void switchMode(boolean loginMode) {
        isLoginMode = loginMode;

        if (loginMode) {
            tabLogin.setBackgroundResource(R.drawable.bg_tab_selected);
            tabSignup.setBackgroundResource(android.R.color.transparent);
            labelName.setVisibility(View.GONE);
            etName.setVisibility(View.GONE);
            btnPrimary.setText("Sign In");
            tvForgotPassword.setVisibility(View.VISIBLE);
        } else {
            tabSignup.setBackgroundResource(R.drawable.bg_tab_selected);
            tabLogin.setBackgroundResource(android.R.color.transparent);
            labelName.setVisibility(View.VISIBLE);
            etName.setVisibility(View.VISIBLE);
            btnPrimary.setText("Sign-up");
            tvForgotPassword.setVisibility(View.GONE);
        }

        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
    }

    // ─── Email/Password Login ───────────────────────────────────────────────

    private void handleLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPrimary.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    navigateToMain(false);
                })
                .addOnFailureListener(e -> {
                    btnPrimary.setEnabled(true);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ─── Email/Password Sign-up ─────────────────────────────────────────────

    private void handleSignup() {
        String name     = etName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPrimary.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, name, email, "entrant", false);
                })
                .addOnFailureListener(e -> {
                    btnPrimary.setEnabled(true);
                    Toast.makeText(this, "Sign-up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ─── Device / Guest Login ───────────────────────────────────────────────

    private void handleDeviceLogin() {
        btnGuest.setEnabled(false);

        mAuth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    DocumentReference userRef = db.collection("users").document(uid);
                    userRef.get().addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()) {
                            // First time on this device - create guest profile
                            saveUserToFirestore(uid, "Guest", null, "guest", true);
                        } else {
                            // Returning device user
                            navigateToMain(true);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    btnGuest.setEnabled(true);
                    Toast.makeText(this, "Device login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ─── Save User Profile to Firestore ────────────────────────────────────

    private void saveUserToFirestore(String uid, String name, String email,
                                     String role, boolean isGuest) {
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("uid", uid);
        userProfile.put("name", name);
        userProfile.put("role", role);
        userProfile.put("isGuest", isGuest);
        userProfile.put("deviceId", deviceId);

        if (email != null) {
            userProfile.put("email", email);
        }

        db.collection("users").document(uid)
                .set(userProfile)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            isGuest ? "Continuing as guest" : "Account created!",
                            Toast.LENGTH_SHORT).show();
                    navigateToMain(isGuest);
                })
                .addOnFailureListener(e -> {
                    btnPrimary.setEnabled(true);
                    btnGuest.setEnabled(true);
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // ─── Forgot Password ────────────────────────────────────────────────────

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email above first", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Reset email sent to " + email, Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // ─── Navigation ─────────────────────────────────────────────────────────

    private void navigateToMain(boolean isGuest) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isGuest", isGuest);
        startActivity(intent);
        finish();
    }
}