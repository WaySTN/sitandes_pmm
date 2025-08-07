package com.example.sitandes.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitandes.dashboard.DashboardAdmin;
import com.example.sitandes.dashboard.DashboardUser;
import com.example.sitandes.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginUser extends AppCompatActivity {

    private TextInputEditText etUserEmail, etUserPassword;
    private Button btnLoginUser;
    private TextView tvRegister, tvAdminLogin, tvForgotPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        // Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI references
        etUserEmail = findViewById(R.id.etUserEmail);
        etUserPassword = findViewById(R.id.etUserPassword);
        btnLoginUser = findViewById(R.id.btnLoginUser);
        tvRegister = findViewById(R.id.tvRegister);
        tvAdminLogin = findViewById(R.id.tvAdminLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Sedang memproses...");
        progressDialog.setCancelable(false);

        // Login button
        btnLoginUser.setOnClickListener(v -> {
            String email = etUserEmail.getText().toString().trim();
            String password = etUserPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etUserEmail.setError("Email tidak boleh kosong");
                return;
            }

            if (password.isEmpty()) {
                etUserPassword.setError("Kata sandi tidak boleh kosong");
                return;
            }

            loginUser(email, password);
        });

        // Lupas pw
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginUser.this, ForgotPassword.class));
        });

        // Register
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginUser.this, RegisterUser.class));
        });

        // Admin login
        tvAdminLogin.setOnClickListener(v -> {
            startActivity(new Intent(LoginUser.this, LoginAdmin.class));
        });
    }

    private void loginUser(String email, String password) {
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Cek role user di Firestore
                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String role = documentSnapshot.getString("role");
                                            saveUserSession(role);

                                            if ("user".equalsIgnoreCase(role)) {
                                                startActivity(new Intent(LoginUser.this, DashboardUser.class));
                                            } else {
                                                Toast.makeText(LoginUser.this,
                                                        "Akun ini bukan role user. Gunakan halaman login admin.",
                                                        Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                            }

                                            finish();
                                        } else {
                                            Toast.makeText(LoginUser.this,
                                                    "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
                                            mAuth.signOut();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(LoginUser.this,
                                                "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(LoginUser.this,
                                "Login gagal: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserSession(String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("role", role);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
            String role = sharedPreferences.getString("role", "");

            if ("user".equalsIgnoreCase(role)) {
                startActivity(new Intent(LoginUser.this, DashboardUser.class));
                finish();
            } else if ("admin".equalsIgnoreCase(role)) {
                startActivity(new Intent(LoginUser.this, DashboardAdmin.class));
                finish();
            } else {
                // Role tidak valid, logout
                mAuth.signOut();
            }
        }
    }
}

