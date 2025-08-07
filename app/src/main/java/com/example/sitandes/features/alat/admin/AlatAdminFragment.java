package com.example.sitandes.features.alat.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitandes.R;
import com.example.sitandes.models.AlatPertanian;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlatAdminFragment extends Fragment implements AlatAdminAdapter.OnAlatActionListener {

    private RecyclerView rvAlatAdmin;
    private TextInputEditText searchAlatAdmin;
    private MaterialButton btnTambahAlat, btnTambahPertama;
    private View layoutEmptyAlatAdmin;
    private Chip chipSemua, chipTersedia, chipHabis;

    private AlatAdminAdapter adapter;
    private List<AlatPertanian> alatList;
    private List<AlatPertanian> filteredAlatList;

    private FirebaseFirestore db;
    private String currentFilter = "semua";
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alat_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initFirestore();
        setupRecyclerView();
        setupSearchListener();
        setupFilterChips();
        setupClickListeners();

        loadAlatData();
    }

    private void initViews(View view) {
        rvAlatAdmin = view.findViewById(R.id.rvAlatAdmin);
        searchAlatAdmin = view.findViewById(R.id.searchAlatAdmin);
        btnTambahAlat = view.findViewById(R.id.btnTambahAlat);
        btnTambahPertama = view.findViewById(R.id.btnTambahPertama);
        layoutEmptyAlatAdmin = view.findViewById(R.id.layout_empty_alat_admin);

        chipSemua = view.findViewById(R.id.chipSemua);
        chipTersedia = view.findViewById(R.id.chipTersedia);
        chipHabis = view.findViewById(R.id.chipHabis);
    }

    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        alatList = new ArrayList<>();
        filteredAlatList = new ArrayList<>();
        adapter = new AlatAdminAdapter(filteredAlatList, this);

        rvAlatAdmin.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlatAdmin.setAdapter(adapter);
    }

    private void setupSearchListener() {
        searchAlatAdmin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                filterData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        chipSemua.setOnClickListener(v -> {
            if (chipSemua.isChecked()) {
                uncheckOtherChips(chipSemua);
                currentFilter = "semua";
                filterData();
            }
        });

        chipTersedia.setOnClickListener(v -> {
            if (chipTersedia.isChecked()) {
                uncheckOtherChips(chipTersedia);
                currentFilter = "available"; // Updated filter key
                filterData();
            }
        });

        chipHabis.setOnClickListener(v -> {
            if (chipHabis.isChecked()) {
                uncheckOtherChips(chipHabis);
                currentFilter = "rented"; // Filter untuk yang dipinjam
                filterData();
            }
        });
    }

    private void uncheckOtherChips(Chip selectedChip) {
        if (selectedChip != chipSemua) chipSemua.setChecked(false);
        if (selectedChip != chipTersedia) chipTersedia.setChecked(false);
        if (selectedChip != chipHabis) chipHabis.setChecked(false);
    }

    private void setupClickListeners() {
        btnTambahAlat.setOnClickListener(v -> navigateToTambahAlat());
        btnTambahPertama.setOnClickListener(v -> navigateToTambahAlat());
    }

    private void navigateToTambahAlat() {
        TambahEditAlatFragment fragment = TambahEditAlatFragment.newInstance(null);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.AdminFragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadAlatData() {
        db.collection("alat_pertanian")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        alatList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            AlatPertanian alat = doc.toObject(AlatPertanian.class);
                            alat.setId(doc.getId());
                            alatList.add(alat);
                        }
                        filterData();
                    }
                });
    }

    private void filterData() {
        filteredAlatList.clear();

        for (AlatPertanian alat : alatList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    alat.getNama().toLowerCase().contains(currentSearchQuery) ||
                    alat.getKelompokTani().toLowerCase().contains(currentSearchQuery);

            boolean matchesFilter = true;
            switch (currentFilter) {
                case "available":
                    matchesFilter = "available".equals(alat.getStatus());
                    break;
                case "rented":
                    matchesFilter = "rented".equals(alat.getStatus());
                    break;
                case "maintenance":
                    matchesFilter = "maintenance".equals(alat.getStatus());
                    break;
                case "semua":
                default:
                    matchesFilter = true;
                    break;
            }

            if (matchesSearch && matchesFilter) {
                filteredAlatList.add(alat);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredAlatList.isEmpty()) {
            layoutEmptyAlatAdmin.setVisibility(View.VISIBLE);
            rvAlatAdmin.setVisibility(View.GONE);
        } else {
            layoutEmptyAlatAdmin.setVisibility(View.GONE);
            rvAlatAdmin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditAlat(AlatPertanian alat) {
        TambahEditAlatFragment fragment = TambahEditAlatFragment.newInstance(alat);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.AdminFragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeleteAlat(AlatPertanian alat) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Hapus Alat")
                .setMessage("Apakah Anda yakin ingin menghapus " + alat.getNama() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    deleteAlatFromFirestore(alat);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onTogglePinjam(AlatPertanian alat) {
        if (alat.isRented()) {
            // Kembalikan alat
            showReturnDialog(alat);
        } else {
            // Pinjamkan alat
            showLendDialog(alat);
        }
    }

    @Override
    public void onToggleMaintenance(AlatPertanian alat) {
        if ("maintenance".equals(alat.getStatus())) {
            // Selesai maintenance
            updateAlatStatus(alat.getId(), "available", null, null, null, null);
        } else {
            // Set maintenance
            updateAlatStatus(alat.getId(), "maintenance", null, null, null, null);
        }
    }

    private void showLendDialog(AlatPertanian alat) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pinjam_alat, null);

        TextInputEditText etNamaPeminjam = dialogView.findViewById(R.id.et_nama_peminjam);
        TextInputEditText etHpPeminjam = dialogView.findViewById(R.id.et_hp_peminjam);
        TextInputEditText etTanggalKembali = dialogView.findViewById(R.id.et_tanggal_kembali);

        new AlertDialog.Builder(requireContext())
                .setTitle("Pinjamkan " + alat.getNama())
                .setView(dialogView)
                .setPositiveButton("Pinjamkan", (dialog, which) -> {
                    String nama = etNamaPeminjam.getText().toString().trim();
                    String hp = etHpPeminjam.getText().toString().trim();
                    String tanggalKembali = etTanggalKembali.getText().toString().trim();

                    if (nama.isEmpty() || hp.isEmpty()) {
                        Toast.makeText(getContext(), "Nama dan HP harus diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update status ke rented
                    updateAlatStatus(alat.getId(), "rented", nama, hp, new Date(), tanggalKembali);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showReturnDialog(AlatPertanian alat) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Kembalikan Alat")
                .setMessage("Apakah " + alat.getBorrowerName() + " sudah mengembalikan " + alat.getNama() + "?")
                .setPositiveButton("Ya, Sudah Dikembalikan", (dialog, which) -> {
                    // Update status ke available
                    updateAlatStatus(alat.getId(), "available", null, null, null, null);
                })
                .setNegativeButton("Belum", null)
                .show();
    }

    private void updateAlatStatus(String alatId, String status, String borrowerName,
                                  String borrowerPhone, Date borrowedAt, String returnDate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        if ("rented".equals(status)) {
            updates.put("currentBorrower", "manual_" + System.currentTimeMillis());
            updates.put("borrowerName", borrowerName);
            updates.put("borrowerPhone", borrowerPhone);
            updates.put("borrowedAt", borrowedAt);
            updates.put("expectedReturnDate", returnDate);
        } else {
            updates.put("currentBorrower", null);
            updates.put("borrowerName", null);
            updates.put("borrowerPhone", null);
            updates.put("borrowedAt", null);
            updates.put("expectedReturnDate", null);
        }

        db.collection("alat_pertanian")
                .document(alatId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    String message = "rented".equals(status) ?
                            "Alat berhasil dipinjamkan" : "Status alat berhasil diupdate";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteAlatFromFirestore(AlatPertanian alat) {
        db.collection("alat_pertanian")
                .document(alat.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Alat berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal menghapus alat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}