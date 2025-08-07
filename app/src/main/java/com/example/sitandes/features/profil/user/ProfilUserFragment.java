package com.example.sitandes.features.profil.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.sitandes.R;
import com.example.sitandes.auth.LoginUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilUserFragment extends Fragment {

    private TextView tvName, tvEmail;
    private LinearLayout layoutFAQ;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil_user, container, false);

        // Initialize views
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        layoutFAQ = view.findViewById(R.id.layoutFAQ);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load user data
        loadUserData();

        // Set click listeners
        view.findViewById(R.id.layoutDataDiri).setOnClickListener(v -> navigateToEditProfile());
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
        layoutFAQ.setOnClickListener(v -> navigateToFAQ());

        return view;
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Coba ambil data dari Firestore
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("nama");
                                String email = document.getString("email");

                                tvName.setText(name != null ? name : "Nama Pengguna");
                                tvEmail.setText(email != null ? email : currentUser.getEmail());
                            } else {
                                // Dokumen tidak ada, gunakan data dari FirebaseAuth
                                setUserDataFromAuth(currentUser);
                            }
                        } else {
                            // Jika gagal mengakses Firestore, gunakan data dari FirebaseAuth
                            // Tidak menampilkan toast error, langsung gunakan data yang tersedia
                            setUserDataFromAuth(currentUser);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Jika ada error (termasuk PERMISSION_DENIED), gunakan data dari FirebaseAuth
                        setUserDataFromAuth(currentUser);
                    });
        } else {
            // User tidak login, redirect ke login
            startActivity(new Intent(getActivity(), LoginUser.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void setUserDataFromAuth(FirebaseUser currentUser) {
        tvName.setText(currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "Nama Pengguna");
        tvEmail.setText(currentUser.getEmail() != null ?
                currentUser.getEmail() : "Email tidak tersedia");
    }

    private void navigateToEditProfile() {
        EditProfilFragment editProfilFragment = new EditProfilFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.UserFragment, editProfilFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToFAQ() {
        FaqFragment faqFragment = new FaqFragment();
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.UserFragment, faqFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> logoutUser())
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void logoutUser() {
        try {
            mAuth.signOut();

            // Tampilkan toast logout berhasil
            Toast.makeText(getContext(), "Logout berhasil", Toast.LENGTH_SHORT).show();

            // Redirect ke login
            Intent intent = new Intent(getActivity(), LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Terjadi kesalahan saat logout", Toast.LENGTH_SHORT).show();
        }
    }
}