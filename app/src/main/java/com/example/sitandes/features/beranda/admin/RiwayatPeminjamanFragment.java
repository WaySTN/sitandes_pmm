package com.example.sitandes.features.beranda.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitandes.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RiwayatPeminjamanFragment extends Fragment {

    private RecyclerView rvRiwayatPeminjaman;
    private TextView tvTotalPeminjaman;
    private TextInputEditText etSearch;
    private Spinner spinnerStatus;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;

    private RiwayatPeminjamanAdapter adapter;
    private List<RiwayatPeminjamanModel> riwayatList;
    private List<RiwayatPeminjamanModel> filteredList;
    private FirebaseFirestore db;

    private String[] statusArray = {"Semua Status", "rented", "returned", "overdue", "cancelled"};
    private String selectedStatus = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_riwayat_peminjaman, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupFirestore();
        setupRecyclerView();
        setupSearch();
        setupSpinner();
        loadRiwayatPeminjaman();
    }

    private void initViews(View view) {
        rvRiwayatPeminjaman = view.findViewById(R.id.rv_riwayat_peminjaman);
        tvTotalPeminjaman = view.findViewById(R.id.tv_total_peminjaman);
        etSearch = view.findViewById(R.id.et_search);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        riwayatList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new RiwayatPeminjamanAdapter(getContext(), filteredList);
        rvRiwayatPeminjaman.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRiwayatPeminjaman.setAdapter(adapter);

        // Add item click listener if needed
        adapter.setOnItemClickListener(new RiwayatPeminjamanAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RiwayatPeminjamanModel riwayat) {
                // Handle item click - show detail or perform action
                showDetailDialog(riwayat);
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, statusArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(spinnerAdapter);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedStatus = "";
                } else {
                    selectedStatus = statusArray[position];
                }
                filterData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadRiwayatPeminjaman() {
        showLoading(true);

        db.collection("alat_pertanian")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    riwayatList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            RiwayatPeminjamanModel riwayat = new RiwayatPeminjamanModel();

                            // Set data from Firebase document
                            riwayat.setId(document.getId());
                            riwayat.setAlamatKelompok(document.getString("alamatKelompok"));
                            riwayat.setBorrowerName(document.getString("borrowerName"));
                            riwayat.setBorrowerPhone(document.getString("borrowerPhone"));
                            riwayat.setNamaKetua(document.getString("namaKetua"));
                            riwayat.setKelompokTani(document.getString("kelompokTani"));
                            riwayat.setNama(document.getString("nama"));
                            riwayat.setStatus(document.getString("status"));
                            riwayat.setHarga(document.getLong("harga"));
                            riwayat.setStok(document.getLong("stok"));

                            // Convert timestamps to dates
                            if (document.getTimestamp("createdAt") != null) {
                                riwayat.setCreatedAt(document.getTimestamp("createdAt").toDate());
                            }
                            if (document.getTimestamp("borrowedAt") != null) {
                                riwayat.setBorrowedAt(document.getTimestamp("borrowedAt").toDate());
                            }
                            if (document.getTimestamp("bookingExpiry") != null) {
                                riwayat.setBookingExpiry(document.getTimestamp("bookingExpiry").toDate());
                            }

                            riwayatList.add(riwayat);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    filterData();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    // Handle error - show toast or dialog
                });
    }

    private void filterData() {
        filteredList.clear();
        String searchText = etSearch.getText().toString().toLowerCase().trim();

        for (RiwayatPeminjamanModel riwayat : riwayatList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    (riwayat.getBorrowerName() != null &&
                            riwayat.getBorrowerName().toLowerCase().contains(searchText));

            boolean matchesStatus = selectedStatus.isEmpty() ||
                    (riwayat.getStatus() != null &&
                            riwayat.getStatus().equals(selectedStatus));

            if (matchesSearch && matchesStatus) {
                filteredList.add(riwayat);
            }
        }

        adapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        tvTotalPeminjaman.setText("Total: " + filteredList.size() + " peminjaman");

        if (filteredList.isEmpty()) {
            rvRiwayatPeminjaman.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvRiwayatPeminjaman.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            rvRiwayatPeminjaman.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showDetailDialog(RiwayatPeminjamanModel riwayat) {
        // Implement detail dialog here
        // You can create a new dialog fragment or activity to show detailed information
    }

    // Method untuk refresh data
    public void refreshData() {
        loadRiwayatPeminjaman();
    }

    // Inner class untuk model data
    public static class RiwayatPeminjamanModel {
        private String id;
        private String alamatKelompok;
        private String borrowerName;
        private String borrowerPhone;
        private String namaKetua;
        private String kelompokTani;
        private String nama;
        private String status;
        private Long harga;
        private Long stok;
        private Date createdAt;
        private Date borrowedAt;
        private Date bookingExpiry;

        // Constructors
        public RiwayatPeminjamanModel() {}

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getAlamatKelompok() { return alamatKelompok; }
        public void setAlamatKelompok(String alamatKelompok) { this.alamatKelompok = alamatKelompok; }

        public String getBorrowerName() { return borrowerName; }
        public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

        public String getBorrowerPhone() { return borrowerPhone; }
        public void setBorrowerPhone(String borrowerPhone) { this.borrowerPhone = borrowerPhone; }

        public String getNamaKetua() { return namaKetua; }
        public void setNamaKetua(String namaKetua) { this.namaKetua = namaKetua; }

        public String getKelompokTani() { return kelompokTani; }
        public void setKelompokTani(String kelompokTani) { this.kelompokTani = kelompokTani; }

        public String getNama() { return nama; }
        public void setNama(String nama) { this.nama = nama; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getHarga() { return harga; }
        public void setHarga(Long harga) { this.harga = harga; }

        public Long getStok() { return stok; }
        public void setStok(Long stok) { this.stok = stok; }

        public Date getCreatedAt() { return createdAt; }
        public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

        public Date getBorrowedAt() { return borrowedAt; }
        public void setBorrowedAt(Date borrowedAt) { this.borrowedAt = borrowedAt; }

        public Date getBookingExpiry() { return bookingExpiry; }
        public void setBookingExpiry(Date bookingExpiry) { this.bookingExpiry = bookingExpiry; }

        // Helper methods untuk format tanggal
        public String getFormattedCreatedDate() {
            if (createdAt != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
                return sdf.format(createdAt);
            }
            return "";
        }

        public String getFormattedBorrowedDate() {
            if (borrowedAt != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
                return sdf.format(borrowedAt);
            }
            return "";
        }

        public String getFormattedExpiryDate() {
            if (bookingExpiry != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
                return sdf.format(bookingExpiry);
            }
            return "";
        }

        public String getFormattedHarga() {
            if (harga != null) {
                return String.format(Locale.getDefault(), "Rp %,d", harga);
            }
            return "Rp 0";
        }
    }
}