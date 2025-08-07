package com.example.sitandes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sitandes.auth.LoginUser;
import com.example.sitandes.dashboard.DashboardAdmin;
import com.example.sitandes.dashboard.DashboardUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen); // Make sure you have this layout file

        new Handler().postDelayed(() -> {
            checkUserAuth();
        }, SPLASH_DURATION);
    }

    private void checkUserAuth() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            db.collection("admin")
                    .document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            String role = task.getResult().getString("role");
                            if ("admin".equals(role)) {
                                goToDashboard(true);
                                return;
                            }
                        }

                        db.collection("users")
                                .document(mAuth.getCurrentUser().getUid())
                                .get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().exists()) {
                                        goToDashboard(false);
                                    } else {
                                        mAuth.signOut();
                                        goToLogin();
                                    }
                                });
                    });
        } else {
            goToLogin();
        }
    }

    private void goToDashboard(boolean isAdmin) {
        Intent intent = isAdmin ?
                new Intent(this, DashboardAdmin.class) :
                new Intent(this, DashboardUser.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginUser.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}