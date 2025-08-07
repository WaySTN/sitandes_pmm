package com.example.sitandes.features.alat.admin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.sitandes.R;
import com.example.sitandes.models.AlatPertanian;
import com.example.sitandes.models.KelompokTani;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TambahEditAlatFragment extends Fragment {

    private static final String ARG_ALAT = "alat";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;
    private static final int REQUEST_PERMISSIONS = 1003;
    private static final String TAG = "TambahEditAlatFragment";

    // Views
    private TextView tvHeaderTitle;
    private ImageView ivBackButton, ivPreviewFoto;
    private MaterialCardView cardUploadFoto;
    private View layoutUploadPlaceholder;
    private TextInputEditText etNamaAlat, etDeskripsiAlat, etHargaAlat, etStokAlat, etNamaKetua, etNoWhatsapp;
    private AutoCompleteTextView actKelompokTani;
    private MaterialButton btnTambahKelompok, btnBatal, btnSimpan;
    private CircularProgressIndicator progressIndicator;

    // Data
    private AlatPertanian currentAlat;
    private boolean isEditMode = false;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private String uploadedImageUrl;
    private List<KelompokTani> kelompokTaniList;
    private ArrayAdapter<String> kelompokAdapter;
    private boolean isUploading = false;

    // Firebase
    private FirebaseFirestore db;

    public static TambahEditAlatFragment newInstance(AlatPertanian alat) {
        TambahEditAlatFragment fragment = new TambahEditAlatFragment();
        Bundle args = new Bundle();
        if (alat != null) {
            args.putSerializable(ARG_ALAT, alat);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Cloudinary
        initCloudinary();

        if (getArguments() != null) {
            currentAlat = (AlatPertanian) getArguments().getSerializable(ARG_ALAT);
            isEditMode = (currentAlat != null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tambah_edit_alat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initFirestore();
        setupHeader();
        setupStokField();
        setupClickListeners();

        // TAMBAHAN: Debug click issues
        debugClickIssues();

        initializeKelompokTaniData();

        if (isEditMode) {
            populateFieldsForEdit();
        }
    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dxuglqp0l");
            config.put("api_key", "118832189971179");
            config.put("api_secret", "jPDvNhuuPMVVfsNVpOc03MyGwwk");

            MediaManager.init(requireContext(), config);
            Log.d(TAG, "Cloudinary initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Cloudinary", e);
        }
    }

    private void initViews(View view) {
        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        ivBackButton = view.findViewById(R.id.iv_back_button);
        cardUploadFoto = view.findViewById(R.id.card_upload_foto);
        layoutUploadPlaceholder = view.findViewById(R.id.layout_upload_placeholder);
        ivPreviewFoto = view.findViewById(R.id.iv_preview_foto);

        etNamaAlat = view.findViewById(R.id.et_nama_alat);
        etDeskripsiAlat = view.findViewById(R.id.et_deskripsi_alat);
        etHargaAlat = view.findViewById(R.id.et_harga_alat);
        etStokAlat = view.findViewById(R.id.et_stok_alat);
        etNamaKetua = view.findViewById(R.id.et_nama_ketua);
        etNoWhatsapp = view.findViewById(R.id.et_no_whatsapp);
        actKelompokTani = view.findViewById(R.id.act_kelompok_tani);

        btnTambahKelompok = view.findViewById(R.id.btn_tambah_kelompok);
        btnBatal = view.findViewById(R.id.btn_batal);
        btnSimpan = view.findViewById(R.id.btn_simpan);

        // Add progress indicator programmatically

        ViewGroup parentLayout = (ViewGroup) view.findViewById(R.id.AdminFragment); // sesuaikan dengan ID container utama di XML
        if (parentLayout != null) {
            progressIndicator = new CircularProgressIndicator(requireContext());
            progressIndicator.setVisibility(View.GONE);
            parentLayout.addView(progressIndicator);
        }
    }

    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupStokField() {
        // Set default value ke "1"
        etStokAlat.setText("1");

        // Disable input - user tidak bisa mengedit
        etStokAlat.setEnabled(false);
        etStokAlat.setFocusable(false);
        etStokAlat.setClickable(false);

        // Optional: Ubah appearance untuk menandakan field disabled
        etStokAlat.setAlpha(0.6f); // Buat sedikit transparan
    }

    private void setupHeader() {
        if (isEditMode) {
            tvHeaderTitle.setText("Edit Alat");
            btnSimpan.setText("Update Alat");
        } else {
            tvHeaderTitle.setText("Tambah Alat Baru");
            btnSimpan.setText("Simpan Alat");
        }
    }

    private void setupClickListeners() {
        ivBackButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // PERBAIKAN UTAMA: Tambahkan multiple click listener untuk memastikan click terdeteksi
        cardUploadFoto.setOnClickListener(v -> handleCardClick());

        // Tambahan: Set click listener juga untuk child views sebagai backup
        layoutUploadPlaceholder.setOnClickListener(v -> handleCardClick());
        ivPreviewFoto.setOnClickListener(v -> handleCardClick());

        btnTambahKelompok.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Feature tambah kelompok akan segera hadir", Toast.LENGTH_SHORT).show();
        });

        btnBatal.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        btnSimpan.setOnClickListener(v -> {
            if (validateInput()) {
                saveAlat();
            }
        });

        actKelompokTani.setOnItemClickListener((parent, view, position, id) -> {
            String selectedKelompok = (String) parent.getItemAtPosition(position);
            updateContactInfo(selectedKelompok);
        });
    }
    // Method baru untuk handle click
    private void handleCardClick() {
        Log.d(TAG, "Card clicked!");

        if (getContext() == null) {
            Log.e(TAG, "Context is null");
            return;
        }

        if (!isAdded()) {
            Log.e(TAG, "Fragment not attached");
            return;
        }

        showImagePickerDialog();
    }


    private void initializeKelompokTaniData() {
        kelompokTaniList = new ArrayList<>();

        // Add the predefined kelompok tani data
        kelompokTaniList.add(new KelompokTani("kt1", "Kel Tani Mangga", "Pak Suyitno", "81238830542", "Dusun Kopencungking"));
        kelompokTaniList.add(new KelompokTani("kt2", "Kel Tani Podo Subur", "Pak Asmuni", "82337486986", "Dusun Kopencungking"));
        kelompokTaniList.add(new KelompokTani("kt3", "Kel Tani Podo Makmur", "Pak Umar Said", "85331852334", "Dusun Kopencungking"));
        kelompokTaniList.add(new KelompokTani("kt4", "Kel Tani Muara", "H Isdianto", "85258301456", "Dusun Krajan"));
        kelompokTaniList.add(new KelompokTani("kt5", "Kel Tani Panggang Makmur", "Pak Sumandi", "85258079835", "Dusun Panggang"));
        kelompokTaniList.add(new KelompokTani("kt6", "Kel Tani Sido Muncul", "Pak Barno", "82344968802", "Dusun Rejopuro"));

        // Create adapter for dropdown
        List<String> kelompokNames = new ArrayList<>();
        for (KelompokTani kelompok : kelompokTaniList) {
            kelompokNames.add(kelompok.getNama());
        }

        kelompokAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                kelompokNames
        );
        actKelompokTani.setAdapter(kelompokAdapter);

        // Also try to load from Firestore if available
        loadKelompokTaniFromFirestore();
    }

    private void loadKelompokTaniFromFirestore() {
        db.collection("kelompok_tani")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> firestoreKelompokNames = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        KelompokTani kelompok = doc.toObject(KelompokTani.class);
                        kelompok.setId(doc.getId());

                        // Check if this kelompok already exists in our predefined list
                        boolean exists = false;
                        for (KelompokTani existing : kelompokTaniList) {
                            if (existing.getNama().equals(kelompok.getNama())) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            kelompokTaniList.add(kelompok);
                            firestoreKelompokNames.add(kelompok.getNama());
                        }
                    }

                    // Update adapter if new data found
                    if (!firestoreKelompokNames.isEmpty()) {
                        List<String> allKelompokNames = new ArrayList<>();
                        for (KelompokTani kelompok : kelompokTaniList) {
                            allKelompokNames.add(kelompok.getNama());
                        }

                        kelompokAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                allKelompokNames
                        );
                        actKelompokTani.setAdapter(kelompokAdapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to load kelompok tani from Firestore, using predefined data", e);
                });
    }

    private void updateContactInfo(String kelompokName) {
        for (KelompokTani kelompok : kelompokTaniList) {
            if (kelompok.getNama().equals(kelompokName)) {
                etNamaKetua.setText(kelompok.getKetua());
                // Remove +62 prefix if it exists and set the phone number
                String phoneNumber = kelompok.getNoTelp();
                if (phoneNumber.startsWith("+62")) {
                    phoneNumber = phoneNumber.substring(3);
                }
                etNoWhatsapp.setText(phoneNumber);
                break;
            }
        }
    }

    // Perbaikan method populateFieldsForEdit
    private void populateFieldsForEdit() {
        if (currentAlat != null) {
            etNamaAlat.setText(currentAlat.getNama());
            etDeskripsiAlat.setText(currentAlat.getDeskripsi());
            etHargaAlat.setText(String.valueOf((int) currentAlat.getHarga()));
            etStokAlat.setText("1");
            actKelompokTani.setText(currentAlat.getKelompokTani(), false);
            etNamaKetua.setText(currentAlat.getNamaKetua());

            String phoneNumber = currentAlat.getNoWhatsapp();
            if (phoneNumber.startsWith("+62")) {
                phoneNumber = phoneNumber.substring(3);
            }
            etNoWhatsapp.setText(phoneNumber);

            uploadedImageUrl = currentAlat.getFotoUrl();

            if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
                layoutUploadPlaceholder.setVisibility(View.GONE);
                ivPreviewFoto.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(uploadedImageUrl)
                        .centerCrop()
                        .into(ivPreviewFoto);

                // PENTING: Set click listener untuk preview image
                ivPreviewFoto.setOnClickListener(v -> handleCardClick());
            }
        }
    }


    private void showImagePickerDialog() {
        Log.d(TAG, "showImagePickerDialog called");

        // Cek context terlebih dahulu
        if (getContext() == null) {
            Log.e(TAG, "Context is null");
            return;
        }

        if (!hasPermissions()) {
            Log.d(TAG, "Permissions not granted, requesting...");
            requestPermissions();
            return;
        }

        String[] options = {"Kamera", "Galeri"};

        try {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Sumber Gambar")
                    .setItems(options, (dialog, which) -> {
                        Log.d(TAG, "Option selected: " + which);
                        if (which == 0) {
                            openCamera();
                        } else {
                            openGallery();
                        }
                    })
                    .setCancelable(true)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing image picker dialog", e);
            Toast.makeText(getContext(), "Error menampilkan dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                Toast.makeText(getContext(), "Permission diperlukan untuk mengakses kamera dan galeri", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                selectedImageUri = data.getData();
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                            requireContext().getContentResolver(), selectedImageUri);
                    displayPreviewImage();
                } catch (IOException e) {
                    Log.e(TAG, "Error loading image from gallery", e);
                    showError("Gagal memuat gambar dari galeri");
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    selectedImageBitmap = (Bitmap) extras.get("data");
                    if (selectedImageBitmap != null) {
                        displayPreviewImage(selectedImageBitmap);
                    }
                }
            }
        }
    }

    // Perbaikan method displayPreviewImage
    private void displayPreviewImage() {
        if (selectedImageBitmap != null) {
            layoutUploadPlaceholder.setVisibility(View.GONE);
            ivPreviewFoto.setVisibility(View.VISIBLE);
            ivPreviewFoto.setImageBitmap(selectedImageBitmap);

            // Set click listener untuk preview image juga
            ivPreviewFoto.setOnClickListener(v -> handleCardClick());
        } else if (selectedImageUri != null) {
            layoutUploadPlaceholder.setVisibility(View.GONE);
            ivPreviewFoto.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(ivPreviewFoto);

            // Set click listener untuk preview image juga
            ivPreviewFoto.setOnClickListener(v -> handleCardClick());
        }
    }

    // Perbaikan method displayPreviewImage(Bitmap)
    private void displayPreviewImage(Bitmap bitmap) {
        layoutUploadPlaceholder.setVisibility(View.GONE);
        ivPreviewFoto.setVisibility(View.VISIBLE);
        ivPreviewFoto.setImageBitmap(bitmap);

        // Set click listener untuk preview image juga
        ivPreviewFoto.setOnClickListener(v -> handleCardClick());
    }

    private boolean validateInput() {
        String nama = etNamaAlat.getText().toString().trim();
        String harga = etHargaAlat.getText().toString().trim();

        String kelompokTani = actKelompokTani.getText().toString().trim();
        String namaKetua = etNamaKetua.getText().toString().trim();
        String noWhatsapp = etNoWhatsapp.getText().toString().trim();

        if (nama.isEmpty()) {
            etNamaAlat.setError("Nama alat harus diisi");
            etNamaAlat.requestFocus();
            return false;
        }

        if (harga.isEmpty()) {
            etHargaAlat.setError("Harga harus diisi");
            etHargaAlat.requestFocus();
            return false;
        }

        try {
            double hargaValue = Double.parseDouble(harga);
            if (hargaValue <= 0) {
                etHargaAlat.setError("Harga harus lebih dari 0");
                etHargaAlat.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etHargaAlat.setError("Format harga tidak valid");
            etHargaAlat.requestFocus();
            return false;
        }


        if (kelompokTani.isEmpty()) {
            actKelompokTani.setError("Kelompok tani harus dipilih");
            actKelompokTani.requestFocus();
            return false;
        }

        if (namaKetua.isEmpty()) {
            etNamaKetua.setError("Nama ketua harus diisi");
            etNamaKetua.requestFocus();
            return false;
        }

        if (noWhatsapp.isEmpty()) {
            etNoWhatsapp.setError("Nomor WhatsApp harus diisi");
            etNoWhatsapp.requestFocus();
            return false;
        }

        // Validate WhatsApp number format
        if (!isValidWhatsAppNumber(noWhatsapp)) {
            etNoWhatsapp.setError("Format nomor WhatsApp tidak valid");
            etNoWhatsapp.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidWhatsAppNumber(String phoneNumber) {
        // Remove any spaces or dashes
        String cleanNumber = phoneNumber.replaceAll("[\\s-]", "");

        // Check if it's a valid Indonesian phone number (8-12 digits after country code)
        return cleanNumber.matches("^[8][0-9]{7,11}$");
    }

    private void saveAlat() {
        if (isUploading) return;

        // Show loading
        setLoadingState(true);

        // If there's a new image selected, upload it first
        if (shouldUploadNewImage()) {
            uploadImageToCloudinary();
        } else {
            // No new image, proceed with saving data
            saveAlatToFirestore();
        }
    }

    private boolean shouldUploadNewImage() {
        return selectedImageBitmap != null &&
                (uploadedImageUrl == null || uploadedImageUrl.isEmpty() || isEditMode);
    }

    private void uploadImageToCloudinary() {
        if (selectedImageBitmap == null) {
            saveAlatToFirestore();
            return;
        }

        isUploading = true;

        try {
            // Convert bitmap to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byteArray = stream.toByteArray();

            String publicId = "alat_pertanian_" + System.currentTimeMillis();

            MediaManager.get().upload(byteArray)
                    .option("public_id", publicId)
                    .option("folder", "alat_pertanian")
                    .option("resource_type", "image")
                    .option("quality", "auto:good")
                    .option("format", "jpg")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Upload started: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            double progress = (double) bytes / totalBytes * 100;
                            Log.d(TAG, "Upload progress: " + progress + "%");
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Log.d(TAG, "Upload successful: " + resultData);

                            String imageUrl = (String) resultData.get("secure_url");
                            if (imageUrl != null) {
                                uploadedImageUrl = imageUrl;
                                saveAlatToFirestore();
                            } else {
                                showError("Gagal mendapatkan URL gambar");
                                setLoadingState(false);
                            }
                            isUploading = false;
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Upload failed: " + error.getDescription());
                            showError("Gagal upload gambar: " + error.getDescription());
                            setLoadingState(false);
                            isUploading = false;
                        }


                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                        }
                    })
                    .dispatch();

        } catch (Exception e) {
            Log.e(TAG, "Error starting upload", e);
            showError("Error memulai upload: " + e.getMessage());
            setLoadingState(false);
            isUploading = false;
        }
    }

    private void saveAlatToFirestore() {
        String nama = etNamaAlat.getText().toString().trim();
        String deskripsi = etDeskripsiAlat.getText().toString().trim();
        double harga = Double.parseDouble(etHargaAlat.getText().toString().trim());

        // PERUBAHAN: Stok selalu 1, tidak perlu ambil dari EditText
        int stok = 1;

        String kelompokTani = actKelompokTani.getText().toString().trim();
        String namaKetua = etNamaKetua.getText().toString().trim();
        String noWhatsapp = etNoWhatsapp.getText().toString().trim();

        // Get kelompok tani details
        KelompokTani selectedKelompok = null;
        for (KelompokTani kelompok : kelompokTaniList) {
            if (kelompok.getNama().equals(kelompokTani)) {
                selectedKelompok = kelompok;
                break;
            }
        }

        Map<String, Object> alatData = new HashMap<>();
        alatData.put("nama", nama);
        alatData.put("deskripsi", deskripsi);
        alatData.put("harga", harga);
        alatData.put("stok", stok); // Selalu 1
        alatData.put("kelompokTani", kelompokTani);
        alatData.put("namaKetua", namaKetua);
        alatData.put("noWhatsapp", "+62" + noWhatsapp);
        alatData.put("fotoUrl", uploadedImageUrl);

        if (selectedKelompok != null) {
            alatData.put("kelompokTaniId", selectedKelompok.getId());
            alatData.put("alamatKelompok", selectedKelompok.getAlamat());
        }

        if (isEditMode) {
            alatData.put("updatedAt", new Date());

            db.collection("alat_pertanian")
                    .document(currentAlat.getId())
                    .update(alatData)
                    .addOnSuccessListener(aVoid -> {
                        setLoadingState(false);
                        Toast.makeText(getContext(), "Alat berhasil diupdate", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    })
                    .addOnFailureListener(e -> {
                        setLoadingState(false);
                        showError("Gagal mengupdate alat: " + e.getMessage());
                    });
        } else {
            alatData.put("createdAt", new Date());
            alatData.put("updatedAt", new Date());

            db.collection("alat_pertanian")
                    .add(alatData)
                    .addOnSuccessListener(documentReference -> {
                        setLoadingState(false);
                        Toast.makeText(getContext(), "Alat berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    })
                    .addOnFailureListener(e -> {
                        setLoadingState(false);
                        showError("Gagal menambahkan alat: " + e.getMessage());
                    });
        }
    }

    // Perbaikan method setLoadingState - hapus disable cardUploadFoto
    private void setLoadingState(boolean isLoading) {
        btnSimpan.setEnabled(!isLoading);
        btnBatal.setEnabled(!isLoading);

        // Jangan disable cardUploadFoto kecuali saat upload
        if (isUploading) {
            cardUploadFoto.setEnabled(false);
            cardUploadFoto.setClickable(false);
        } else {
            cardUploadFoto.setEnabled(true);
            cardUploadFoto.setClickable(true);
        }

        if (isLoading) {
            btnSimpan.setText("Menyimpan...");
        } else {
            btnSimpan.setText(isEditMode ? "Update Alat" : "Simpan Alat");
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    private void debugClickIssues() {
        Log.d(TAG, "=== DEBUG CLICK ISSUES ===");
        Log.d(TAG, "cardUploadFoto null: " + (cardUploadFoto == null));
        if (cardUploadFoto != null) {
            Log.d(TAG, "cardUploadFoto enabled: " + cardUploadFoto.isEnabled());
            Log.d(TAG, "cardUploadFoto clickable: " + cardUploadFoto.isClickable());
            Log.d(TAG, "cardUploadFoto focusable: " + cardUploadFoto.isFocusable());
            Log.d(TAG, "cardUploadFoto visibility: " + cardUploadFoto.getVisibility());
        }
        Log.d(TAG, "Context null: " + (getContext() == null));
        Log.d(TAG, "Fragment attached: " + isAdded());
        Log.d(TAG, "=========================");
    }
}