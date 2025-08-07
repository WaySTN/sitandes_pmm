package com.example.sitandes.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitandes.dashboard.DashboardAdmin;
import com.example.sitandes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginAdmin extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button btnLoginAdmin;
    private TextView tvUserLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);

        editTextEmail = findViewById(R.id.etAdminEmail);
        editTextPassword = findViewById(R.id.etPasswordAdmin);
        btnLoginAdmin = findViewById(R.id.btnLoginAdmin);
        tvUserLogin = findViewById(R.id.tvUserLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang masuk...");

        btnLoginAdmin.setOnClickListener(v -> loginAdmin());

        tvUserLogin.setOnClickListener(v -> {
            startActivity(new Intent(LoginAdmin.this, LoginUser.class));
        });
    }

    private void loginAdmin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            verifyAdminRole(user);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginAdmin.this, "Autentikasi gagal", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        String errorMessage = "Email atau password salah";
                        if (task.getException() != null) {
                            String exception = task.getException().getMessage();
                            if (exception != null) {
                                if (exception.contains("no user record")) {
                                    errorMessage = "Email tidak terdaftar";
                                } else if (exception.contains("wrong-password") || exception.contains("invalid-credential")) {
                                    errorMessage = "Password salah";
                                } else if (exception.contains("too-many-requests")) {
                                    errorMessage = "Terlalu banyak percobaan login. Coba lagi nanti";
                                }
                            }
                        }
                        Toast.makeText(LoginAdmin.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyAdminRole(FirebaseUser user) {
        // Kembali menggunakan collection "admin" seperti struktur asli
        CollectionReference adminRef = db.collection("admin");

        adminRef.whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String role = document.getString("role");

                            if ("admin".equalsIgnoreCase(role)) {
                                // Simpan data admin ke SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("role", "admin");
                                editor.putString("userId", user.getUid());
                                editor.putString("userEmail", user.getEmail());
                                editor.putString("userName", document.getString("nama"));
                                editor.putString("adminDocId", document.getId()); // Simpan document ID admin
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();

                                // Admin verified, go to admin dashboard
                                Intent intent = new Intent(LoginAdmin.this, DashboardAdmin.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginAdmin.this,
                                        "Akun ini tidak memiliki akses admin", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                            }
                        } else {
                            Toast.makeText(LoginAdmin.this,
                                    "Akun admin tidak ditemukan di database", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginAdmin.this,
                                "Gagal memverifikasi akun admin: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Error tidak diketahui"),
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(LoginAdmin.this,
                            "Error koneksi database: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            String role = preferences.getString("role", "");
            boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

            if ("admin".equalsIgnoreCase(role) && isLoggedIn) {
                // Langsung ke dashboard jika sudah login sebagai admin
                Intent intent = new Intent(LoginAdmin.this, DashboardAdmin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
