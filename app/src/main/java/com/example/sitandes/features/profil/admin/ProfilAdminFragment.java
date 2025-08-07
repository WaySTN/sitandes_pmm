package com.example.sitandes.features.profil.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.sitandes.R;
import com.example.sitandes.auth.LoginUser;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

public class ProfilAdminFragment extends Fragment {

    private ImageView ivProfile;
    private TextView tvName, tvEmail, tvRole;
    private LinearLayout layoutPengaturan;
    private MaterialButton btnLogout;
    private FirebaseAuth mAuth;

    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profil_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupFirebaseAuth(); // Tambahkan inisialisasi Firebase
        setupData();
        setupClickListeners();
    }

    private void initViews(View view) {
        ivProfile = view.findViewById(R.id.ivProfile);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRole = view.findViewById(R.id.tvRole);
        layoutPengaturan = view.findViewById(R.id.layoutPengaturan);
        btnLogout = view.findViewById(R.id.btnLogout);

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
    }

    // Tambahkan method untuk inisialisasi Firebase Auth
    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupData() {
        String nama = sharedPreferences.getString("nama", "Admin Ganteng");
        String email = sharedPreferences.getString("email", "wahyuadmin@gmail.com");
        String role = sharedPreferences.getString("role", "admin");

        tvName.setText(nama);
        tvEmail.setText(email);

        if ("admin".equals(role)) {
            tvRole.setText("Administrator");
            tvRole.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        layoutPengaturan.setOnClickListener(v -> navigateToPengaturanFragment());
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void navigateToPengaturanFragment() {
        try {
            PengaturanAdminFragment pengaturanFragment = new PengaturanAdminFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.AdminFragment, pengaturanFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Tidak dapat membuka pengaturan", Toast.LENGTH_SHORT).show();
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
            // 1. Sign out dari Firebase Auth (jika ada)
            if (mAuth != null) {
                mAuth.signOut();
            }

            // 2. Bersihkan SharedPreferences
            clearUserSession();

            // 3. Navigate ke LoginUser activity
            Intent intent = new Intent(getActivity(), LoginUser.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 4. Finish current activity
            if (getActivity() != null) {
                getActivity().finish();
            }

            Toast.makeText(getContext(), "Berhasil logout", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Terjadi kesalahan saat logout: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // Method untuk membersihkan session user
    private void clearUserSession() {
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Hapus semua data
            // Atau hapus data spesifik:
            // editor.remove("nama");
            // editor.remove("email");
            // editor.remove("role");
            // editor.remove("isLoggedIn");
            editor.apply();
        }
    }

    public void refreshData() {
        setupData();
    }
}