package com.example.sitandes.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sitandes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgotPassword extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";

    private TextInputEditText etResetEmail;
    private TextInputLayout tilResetEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lupa_password);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        etResetEmail = findViewById(R.id.etResetEmail);
        tilResetEmail = etResetEmail != null ?
                (TextInputLayout) etResetEmail.getParent().getParent() : null;
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setClickListeners() {
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Kembali ke activity sebelumnya (biasanya LoginActivity)
            }
        });
    }

    private void resetPassword() {
        String email = etResetEmail.getText().toString().trim();

        // Clear previous errors
        if (tilResetEmail != null) {
            tilResetEmail.setError(null);
        }

        // Validate input
        if (!validateInput(email)) {
            return;
        }

        // Show loading state
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Mengirim...");

        // Check if email exists in Firestore users collection
        checkEmailExists(email);
    }

    private boolean validateInput(String email) {
        if (TextUtils.isEmpty(email)) {
            if (tilResetEmail != null) {
                tilResetEmail.setError("Email tidak boleh kosong");
            }
            etResetEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilResetEmail != null) {
                tilResetEmail.setError("Format email tidak valid");
            }
            etResetEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void checkEmailExists(String email) {
        Log.d(TAG, "Checking if email exists: " + email);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d(TAG, "Firestore query successful. Documents found: " + queryDocumentSnapshots.size());

                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Email exists in Firestore, proceed with password reset
                            Log.d(TAG, "Email found in database, sending reset email");
                            sendPasswordResetEmail(email);
                        } else {
                            // Email not found in database
                            Log.d(TAG, "Email not found in database");
                            resetButtonState();
                            if (tilResetEmail != null) {
                                tilResetEmail.setError("Email tidak terdaftar dalam sistem");
                            }
                            Toast.makeText(ForgotPassword.this,
                                    "Email tidak ditemukan. Pastikan email sudah terdaftar.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error checking email in Firestore", e);
                        resetButtonState();

                        // Try to send reset email directly if Firestore check fails
                        Log.d(TAG, "Firestore check failed, trying direct password reset");
                        sendPasswordResetEmail(email);
                    }
                });
    }

    private void sendPasswordResetEmail(String email) {
        Log.d(TAG, "Sending password reset email to: " + email);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        resetButtonState();

                        if (task.isSuccessful()) {
                            // Success
                            Log.d(TAG, "Password reset email sent successfully");
                            Toast.makeText(ForgotPassword.this,
                                    "Link reset password telah dikirim ke email Anda. Silakan periksa inbox atau folder spam.",
                                    Toast.LENGTH_LONG).show();

                            // Clear the email field
                            etResetEmail.setText("");

                        } else {
                            // Failed to send reset email
                            Log.e(TAG, "Failed to send password reset email", task.getException());

                            String errorMessage = "Gagal mengirim email reset password.";

                            // Handle specific Firebase Auth errors
                            if (task.getException() != null) {
                                String exception = task.getException().getMessage();
                                Log.e(TAG, "Firebase Auth Error: " + exception);

                                if (exception != null) {
                                    if (exception.contains("user-not-found")) {
                                        errorMessage = "Email tidak terdaftar di sistem.";
                                    } else if (exception.contains("invalid-email")) {
                                        errorMessage = "Format email tidak valid.";
                                    } else if (exception.contains("too-many-requests")) {
                                        errorMessage = "Terlalu banyak permintaan. Silakan coba lagi nanti.";
                                    } else {
                                        errorMessage = "Terjadi kesalahan: " + exception;
                                    }
                                }
                            }

                            Toast.makeText(ForgotPassword.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void resetButtonState() {
        btnResetPassword.setEnabled(true);
        btnResetPassword.setText("Kirim Link Reset");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
