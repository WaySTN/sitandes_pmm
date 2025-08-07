package com.example.sitandes.features.profil.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sitandes.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfilFragment extends Fragment {

    private TextInputEditText etNama, etEmail, etPasswordLama, etPasswordBaru;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profil, container, false);

        // Initialize views
        etNama = view.findViewById(R.id.etNama);
        etEmail = view.findViewById(R.id.etEmail);
        etPasswordLama = view.findViewById(R.id.etPasswordLama);
        etPasswordBaru = view.findViewById(R.id.etPasswordBaru);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> navigateBack());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return view;
        }

        // Load current user data
        loadCurrentUserData();

        // Set save button click listener
        view.findViewById(R.id.btnSimpan).setOnClickListener(v -> updateProfile());

        return view;
    }
    private void navigateBack() {
        // Use the FragmentManager to pop the back stack
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            // If no fragments in back stack, just finish the activity
            requireActivity().onBackPressed();
        }
    }

    private void loadCurrentUserData() {
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etNama.setText(documentSnapshot.getString("nama"));
                        etEmail.setText(documentSnapshot.getString("email"));
                    } else {
                        // Fallback to Firebase Auth data if Firestore doesn't have user doc
                        etEmail.setText(currentUser.getEmail());
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            etNama.setText(displayName);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memuat data profil", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfile() {
        String newName = Objects.requireNonNull(etNama.getText()).toString().trim();
        String newEmail = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String oldPassword = Objects.requireNonNull(etPasswordLama.getText()).toString();
        String newPassword = Objects.requireNonNull(etPasswordBaru.getText()).toString();

        // Validate inputs
        if (newName.isEmpty()) {
            etNama.setError("Nama tidak boleh kosong");
            etNama.requestFocus();
            return;
        }

        if (newEmail.isEmpty()) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return;
        }

        // Check if password fields are filled properly
        if (!oldPassword.isEmpty() && newPassword.isEmpty()) {
            etPasswordBaru.setError("Password baru harus diisi");
            etPasswordBaru.requestFocus();
            return;
        }

        if (oldPassword.isEmpty() && !newPassword.isEmpty()) {
            etPasswordLama.setError("Password lama harus diisi");
            etPasswordLama.requestFocus();
            return;
        }

        // Update Firestore first
        updateFirestoreProfile(newName, newEmail);

        // Then handle email/password updates if changed
        if (!newEmail.equals(currentUser.getEmail())) {
            updateEmail(newEmail, oldPassword);
        }

        if (!oldPassword.isEmpty() && !newPassword.isEmpty()) {
            updatePassword(oldPassword, newPassword);
        }
    }

    private void updateFirestoreProfile(String name, String email) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("email", email);

        db.collection("users").document(currentUser.getUid())
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal memperbarui profil: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateEmail(String newEmail, String currentPassword) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        currentUser.updateEmail(newEmail)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Email berhasil diperbarui", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Gagal memperbarui email: " +
                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Autentikasi gagal: " +
                                authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePassword(String oldPassword, String newPassword) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), oldPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(getContext(),
                                                "Password berhasil diperbarui",
                                                Toast.LENGTH_SHORT).show();
                                        // Clear password fields after successful update
                                        etPasswordLama.setText("");
                                        etPasswordBaru.setText("");
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Gagal memperbarui password: " +
                                                        updateTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(),
                                "Password lama salah: " +
                                        authTask.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}