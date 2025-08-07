package com.example.sitandes.features.beranda.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.sitandes.R;
import com.example.sitandes.databinding.FragmentBerandaAdminBinding;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BerandaAdminFragment extends Fragment {

    private static final String TAG = "BerandaAdminFragment";
    private FragmentBerandaAdminBinding binding;
    private FirebaseFirestore firestore;

    // UI Components
    private TextView tvTotalTools;
    private TextView tvTodayLoans;
    private TextView tvCurrentDate;
    private MaterialButton btnRiwayatPeminjaman;
    private MaterialButton btnDataPenghasilan;
    private MaterialButton btnAddTool;
    private MaterialButton btnManageGroups;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBerandaAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        setupCurrentDate();
        loadStatistics();
        setupClickListeners();
    }

    private void initViews() {
        tvTotalTools = binding.tvTotalTools;
        tvTodayLoans = binding.tvTodayLoans;
        tvCurrentDate = binding.tvCurrentDate;
        btnRiwayatPeminjaman = binding.btnRiwayatPeminjaman;
        btnDataPenghasilan = binding.btnDataPenghasilan;
        btnAddTool = binding.btnAddTool;
        btnManageGroups = binding.btnManageGroups;
    }

    private void setupCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy",
                new Locale("id", "ID"));
        String currentDate = dateFormat.format(new Date());
        tvCurrentDate.setText(currentDate);
    }

    private void loadStatistics() {
        loadTotalTools();
        loadTodayLoans();
    }

    private void loadTotalTools() {
        firestore.collection("alat_pertanian")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalTools = queryDocumentSnapshots.size();
                    tvTotalTools.setText(String.valueOf(totalTools));
                    Log.d(TAG, "Total alat pertanian: " + totalTools);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading total tools", e);
                    tvTotalTools.setText("0");
                });
    }

    private void loadTodayLoans() {
        // Count alat_pertanian with status "rented"
        firestore.collection("alat_pertanian")
                .whereEqualTo("status", "rented")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int rentedTools = queryDocumentSnapshots.size();
                    tvTodayLoans.setText(String.valueOf(rentedTools));
                    Log.d(TAG, "Total alat yang sedang dipinjam (rented): " + rentedTools);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading rented tools", e);
                    tvTodayLoans.setText("0");
                });
    }

    private void setupClickListeners() {
        // Riwayat Peminjaman Button
        btnRiwayatPeminjaman.setOnClickListener(v -> {
            navigateToFragment(new RiwayatPeminjamanFragment());
        });

        // Data Penghasilan Button
        btnDataPenghasilan.setOnClickListener(v -> {
            navigateToFragment(new PenghasilanFragment());
        });

        // Tambah Alat Button
        btnAddTool.setOnClickListener(v -> {
            navigateToFragment(new com.example.sitandes.features.alat.admin.TambahEditAlatFragment());
        });

        // Kelola Kelompok Button (optional implementation)
        btnManageGroups.setOnClickListener(v -> {
            // TODO: Implement group management navigation
            Log.d(TAG, "Kelola Kelompok clicked - not implemented yet");
        });
    }

    private void navigateToFragment(Fragment fragment) {
        if (getParentFragment() != null) {
            // Navigate within AdminFragment container
            getParentFragment().getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.AdminFragment, fragment)
                    .addToBackStack(null)
                    .commit();
        } else if (getActivity() != null) {
            // Fallback to activity fragment manager
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.AdminFragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh statistics when returning to this fragment
        loadStatistics();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}