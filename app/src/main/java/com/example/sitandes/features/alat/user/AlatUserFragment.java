package com.example.sitandes.features.alat.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitandes.R;
import com.example.sitandes.models.AlatPertanian;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AlatUserFragment extends Fragment implements AlatUserAdapter.OnAlatClickListener {

    private RecyclerView rvAlatUser;
    private AlatUserAdapter alatAdapter;
    private List<AlatPertanian> alatList;
    private List<AlatPertanian> filteredAlatList;

    private EditText etSearchAlat;
    private ChipGroup chipGroupFilter;
    private Chip chipSemua, chipTersedia, chipDipinjam;
    private LinearLayout layoutEmptyState;

    private FirebaseFirestore db;
    private String currentFilter = "semua";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alat_user, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearchAndFilter();
        loadAlatData();

        return view;
    }

    private void initViews(View view) {
        rvAlatUser = view.findViewById(R.id.rv_alat_user);
        etSearchAlat = view.findViewById(R.id.et_search_alat);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        chipSemua = view.findViewById(R.id.chip_semua);
        chipTersedia = view.findViewById(R.id.chip_tersedia);
        chipDipinjam = view.findViewById(R.id.chip_dipinjam);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        db = FirebaseFirestore.getInstance();
        alatList = new ArrayList<>();
        filteredAlatList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        alatAdapter = new AlatUserAdapter(filteredAlatList, this);
        rvAlatUser.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlatUser.setAdapter(alatAdapter);
    }

    private void setupSearchAndFilter() {
        // Setup search functionality
        etSearchAlat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAlat(s.toString(), currentFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup filter chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipSemua.setChecked(true);
                currentFilter = "semua";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_semua) {
                    currentFilter = "semua";
                } else if (checkedId == R.id.chip_tersedia) {
                    currentFilter = "tersedia";
                } else if (checkedId == R.id.chip_dipinjam) {
                    currentFilter = "dipinjam";
                }
            }
            filterAlat(etSearchAlat.getText().toString(), currentFilter);
        });
    }

    private void loadAlatData() {
        db.collection("alat_pertanian")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        alatList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            AlatPertanian alat = doc.toObject(AlatPertanian.class);
                            alat.setId(doc.getId());
                            alatList.add(alat);
                        }
                        filterAlat(etSearchAlat.getText().toString(), currentFilter);
                    }
                });
    }

    private void filterAlat(String searchText, String filter) {
        filteredAlatList.clear();

        for (AlatPertanian alat : alatList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    alat.getNama().toLowerCase().contains(searchText.toLowerCase()) ||
                    alat.getDeskripsi().toLowerCase().contains(searchText.toLowerCase()) ||
                    alat.getKelompokTani().toLowerCase().contains(searchText.toLowerCase());

            boolean matchesFilter = true;
            switch (filter) {
                case "tersedia":
                    // Sekarang mengecek status yang sebenarnya, bukan hanya stok
                    matchesFilter = isAlatTersedia(alat);
                    break;
                case "dipinjam":
                    // Mengecek status yang menunjukkan alat tidak tersedia
                    matchesFilter = !isAlatTersedia(alat);
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

        alatAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    /**
     * Mengecek apakah alat benar-benar tersedia berdasarkan status dan stok
     * Logika ini harus sama dengan yang ada di DetailAlatFragment
     */
    private boolean isAlatTersedia(AlatPertanian alat) {
        String status = alat.getStatus();

        if (status == null) {
            // Jika status null, fallback ke logika stok lama
            return alat.getStok() > 0;
        }

        switch (status) {
            case "available":
                // Available dan ada stok = tersedia
                return alat.getStok() > 0;
            case "booked":
            case "rented":
            case "maintenance":
                // Status ini menunjukkan alat tidak tersedia
                return false;
            default:
                // Status tidak dikenal, fallback ke logika stok
                return alat.getStok() > 0;
        }
    }

    private void updateEmptyState() {
        if (filteredAlatList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvAlatUser.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvAlatUser.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetailClick(AlatPertanian alat) {
        // Navigate to detail fragment
        DetailAlatFragment detailFragment = DetailAlatFragment.newInstance(alat.getId());

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.UserFragment, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onPinjamClick(AlatPertanian alat) {
        // Menggunakan logika yang sama dengan filter untuk mengecek ketersediaan
        if (isAlatTersedia(alat)) {
            // Navigate to detail fragment for borrowing process
            onDetailClick(alat);
        } else {
            // Pesan yang lebih spesifik berdasarkan status
            String message = getUnavailableMessage(alat);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Memberikan pesan yang sesuai berdasarkan status alat
     */
    private String getUnavailableMessage(AlatPertanian alat) {
        String status = alat.getStatus();

        if (status == null) {
            return "Alat sedang tidak tersedia";
        }

        switch (status) {
            case "available":
                if (alat.getStok() == 0) {
                    return "Stok alat sedang habis";
                } else {
                    return "Alat sedang tidak tersedia";
                }
            case "booked":
                return "Alat sedang dibooking";
            case "rented":
                return "Alat sedang dipinjam";
            case "maintenance":
                return "Alat sedang dalam perbaikan";
            default:
                return "Alat sedang tidak tersedia";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment resumes
        if (alatAdapter != null) {
            loadAlatData();
        }
    }
}