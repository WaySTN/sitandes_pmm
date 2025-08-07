package com.example.sitandes.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitandes.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterUser extends AppCompatActivity {

    private TextInputEditText etFullName, etUserEmail, etUserPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etUserEmail = findViewById(R.id.etUserEmail);
        etUserPassword = findViewById(R.id.etUserPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Mendaftarkan akun...");
        progressDialog.setCancelable(false);

        // Register button click listener
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etUserEmail.getText().toString().trim();
            String password = etUserPassword.getText().toString().trim();

            if (TextUtils.isEmpty(fullName)) {
                etFullName.setError("Nama lengkap harus diisi");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                etUserEmail.setError("Email harus diisi");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etUserPassword.setError("Kata sandi harus diisi");
                return;
            }

            if (password.length() < 6) {
                etUserPassword.setError("Kata sandi minimal 6 karakter");
                return;
            }

            registerUser(fullName, email, password);
        });

        // Login text click listener
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterUser.this, LoginUser.class));
            finish();
        });
    }

    private void registerUser(String fullName, String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success, send verification email
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            // Save additional user data to Firestore
                                            saveUserDataToFirestore(user.getUid(), fullName, email);
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterUser.this,
                                                    "Gagal mengirim email verifikasi: " + emailTask.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUser.this,
                                "Pendaftaran gagal: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String fullName, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("nama", fullName);
        user.put("email", email);
        user.put("role", "user"); // Default role for normal users
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterUser.this,
                            "Pendaftaran berhasil! Silakan cek email untuk verifikasi",
                            Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterUser.this, LoginUser.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterUser.this,
                            "Gagal menyimpan data user: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
