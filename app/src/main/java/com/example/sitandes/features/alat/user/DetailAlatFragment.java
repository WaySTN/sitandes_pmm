package com.example.sitandes.features.alat.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sitandes.R;
import com.example.sitandes.models.AlatPertanian;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetailAlatFragment extends Fragment {

    private static final String ARG_ALAT_ID = "alat_id";

    private String alatId;
    private AlatPertanian currentAlat;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // UI Components
    private Toolbar toolbarDetail;
    private ImageView ivFotoAlatDetail;
    private TextView tvNamaAlatDetail;
    private TextView tvHargaDetail;
    private TextView tvStatusDetail;
    private TextView tvStokDetail;
    private TextView tvKelompokTaniDetail;
    private TextView tvNamaKetuaDetail;
    private TextView tvAlamatDetail;
    private TextView tvDeskripsiDetail;
    private MaterialButton btnPesanWhatsapp;

    public static DetailAlatFragment newInstance(String alatId) {
        DetailAlatFragment fragment = new DetailAlatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALAT_ID, alatId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            alatId = getArguments().getString(ARG_ALAT_ID);
        }
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_alat, container, false);

        initViews(view);
        setupToolbar();
        loadAlatDetail();

        return view;
    }

    private void initViews(View view) {
        toolbarDetail = view.findViewById(R.id.toolbar_detail);
        ivFotoAlatDetail = view.findViewById(R.id.iv_foto_alat_detail);
        tvNamaAlatDetail = view.findViewById(R.id.tv_nama_alat_detail);
        tvHargaDetail = view.findViewById(R.id.tv_harga_detail);
        tvStatusDetail = view.findViewById(R.id.tv_status_detail);
        tvStokDetail = view.findViewById(R.id.tv_stok_detail);
        tvKelompokTaniDetail = view.findViewById(R.id.tv_kelompok_tani_detail);
        tvNamaKetuaDetail = view.findViewById(R.id.tv_nama_ketua_detail);
        tvAlamatDetail = view.findViewById(R.id.tv_alamat_detail);
        tvDeskripsiDetail = view.findViewById(R.id.tv_deskripsi_detail);
        btnPesanWhatsapp = view.findViewById(R.id.btn_pesan_whatsapp);
    }

    private void setupToolbar() {
        toolbarDetail.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void loadAlatDetail() {
        if (alatId == null || alatId.isEmpty()) {
            Toast.makeText(getContext(), "ID alat tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("alat_pertanian")
                .document(alatId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentAlat = documentSnapshot.toObject(AlatPertanian.class);
                        if (currentAlat != null) {
                            currentAlat.setId(documentSnapshot.getId());
                            populateAlatData();
                        }
                    } else {
                        Toast.makeText(getContext(), "Alat tidak ditemukan", Toast.LENGTH_SHORT).show();
                        if (getParentFragmentManager() != null) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });
    }

    private void populateAlatData() {
        if (currentAlat == null) return;

        // Set basic information
        tvNamaAlatDetail.setText(currentAlat.getNama());
        tvKelompokTaniDetail.setText(currentAlat.getKelompokTani());
        tvNamaKetuaDetail.setText(currentAlat.getNamaKetua());
        tvAlamatDetail.setText(currentAlat.getAlamatKelompok());
        tvDeskripsiDetail.setText(currentAlat.getDeskripsi());
        tvStokDetail.setText("Stok: " + currentAlat.getStok());

        // Format and set price
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedPrice = formatter.format(currentAlat.getHarga()).replace("IDR", "Rp") + "/hari";
        tvHargaDetail.setText(formattedPrice);

        // Set status and button based on equipment status
        updateStatusAndButton();

        // Load image using Glide
        loadAlatImage();

        // Setup WhatsApp button
        setupWhatsAppButton();
    }

    private void updateStatusAndButton() {
        String status = currentAlat.getStatus();

        switch (status) {
            case "available":
                if (currentAlat.getStok() > 0) {
                    tvStatusDetail.setText("Tersedia");
                    tvStatusDetail.setBackgroundResource(R.drawable.bg_status_tersedia);
                    btnPesanWhatsapp.setEnabled(true);
                    btnPesanWhatsapp.setText("Pesan via WhatsApp");
                    btnPesanWhatsapp.setAlpha(1.0f);
                } else {
                    tvStatusDetail.setText("Stok Habis");
                    tvStatusDetail.setBackgroundResource(R.drawable.bg_status_habis);
                    btnPesanWhatsapp.setEnabled(false);
                    btnPesanWhatsapp.setText("Stok Habis");
                    btnPesanWhatsapp.setAlpha(0.6f);
                }
                break;

            case "booked":
                // Check if current user is the one who booked it
                if (currentUser != null && currentUser.getUid().equals(currentAlat.getCurrentBorrower())) {
                    tvStatusDetail.setText("Dibooking (Anda)");
                    tvStatusDetail.setBackgroundResource(R.drawable.bg_status_booked);
                    btnPesanWhatsapp.setEnabled(true);
                    btnPesanWhatsapp.setText("Batalkan Booking");
                    btnPesanWhatsapp.setAlpha(1.0f);
                } else {
                    tvStatusDetail.setText("Dibooking");
                    tvStatusDetail.setBackgroundResource(R.drawable.bg_status_booked);
                    btnPesanWhatsapp.setEnabled(false);
                    btnPesanWhatsapp.setText("Sedang Dibooking");
                    btnPesanWhatsapp.setAlpha(0.6f);
                }
                break;

            case "rented":
                tvStatusDetail.setText("Dipinjam");
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_habis);
                btnPesanWhatsapp.setEnabled(false);
                btnPesanWhatsapp.setText("Sedang Dipinjam");
                btnPesanWhatsapp.setAlpha(0.6f);
                break;

            case "maintenance":
                tvStatusDetail.setText("Maintenance");
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_maintenance);
                btnPesanWhatsapp.setEnabled(false);
                btnPesanWhatsapp.setText("Sedang Diperbaiki");
                btnPesanWhatsapp.setAlpha(0.6f);
                break;

            default:
                tvStatusDetail.setText("Tidak Tersedia");
                tvStatusDetail.setBackgroundResource(R.drawable.bg_status_habis);
                btnPesanWhatsapp.setEnabled(false);
                btnPesanWhatsapp.setText("Tidak Tersedia");
                btnPesanWhatsapp.setAlpha(0.6f);
                break;
        }
    }

    private void loadAlatImage() {
        if (currentAlat.getFotoUrl() != null && !currentAlat.getFotoUrl().isEmpty()) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ic_tools)
                    .error(R.drawable.ic_tools);

            Glide.with(this)
                    .load(currentAlat.getFotoUrl())
                    .apply(options)
                    .into(ivFotoAlatDetail);
        } else {
            ivFotoAlatDetail.setImageResource(R.drawable.ic_tools);
        }
    }

    private void setupWhatsAppButton() {
        btnPesanWhatsapp.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            String status = currentAlat.getStatus();

            if ("available".equals(status) && currentAlat.getStok() > 0) {
                // Book the equipment and send WhatsApp message
                bookEquipment();
            } else if ("booked".equals(status) && currentUser.getUid().equals(currentAlat.getCurrentBorrower())) {
                // Cancel booking
                cancelBooking();
            } else {
                Toast.makeText(getContext(), "Alat sedang tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bookEquipment() {
        if (currentAlat == null || currentUser == null) return;

        // Show loading state
        btnPesanWhatsapp.setEnabled(false);
        btnPesanWhatsapp.setText("Membooking...");

        // Get user profile data first
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(userDoc -> {
                    String userName = userDoc.exists() ? userDoc.getString("name") : "User";
                    String userPhone = userDoc.exists() ? userDoc.getString("phone") : "";

                    // Update equipment status to booked
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "booked");
                    updates.put("currentBorrower", currentUser.getUid());
                    updates.put("borrowerName", userName);
                    updates.put("borrowerPhone", userPhone);
                    updates.put("borrowedAt", new Date());
                    updates.put("bookingExpiry", new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000))); // 24 hours
                    updates.put("updatedAt", new Date());

                    db.collection("alat_pertanian")
                            .document(currentAlat.getId())
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Alat berhasil dibooking! Mengarahkan ke WhatsApp...", Toast.LENGTH_LONG).show();

                                // Create booking record
                                createBookingRecord(userName, userPhone);

                                // Send WhatsApp message
                                sendWhatsAppMessage();

                                // Reset button state will be handled by the listener
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Gagal booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                btnPesanWhatsapp.setEnabled(true);
                                btnPesanWhatsapp.setText("Pesan via WhatsApp");
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengambil data user", Toast.LENGTH_SHORT).show();
                    btnPesanWhatsapp.setEnabled(true);
                    btnPesanWhatsapp.setText("Pesan via WhatsApp");
                });
    }

    private void cancelBooking() {
        if (currentAlat == null || currentUser == null) return;

        // Show loading state
        btnPesanWhatsapp.setEnabled(false);
        btnPesanWhatsapp.setText("Membatalkan...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "available");
        updates.put("currentBorrower", null);
        updates.put("borrowerName", null);
        updates.put("borrowerPhone", null);
        updates.put("borrowedAt", null);
        updates.put("bookingExpiry", null);
        updates.put("updatedAt", new Date());

        db.collection("alat_pertanian")
                .document(currentAlat.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Booking berhasil dibatalkan", Toast.LENGTH_SHORT).show();

                    // Delete booking record
                    deleteBookingRecord();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal membatalkan booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Reset button state will be handled by the listener
                });
    }

    private void createBookingRecord(String userName, String userPhone) {
        Map<String, Object> booking = new HashMap<>();
        booking.put("alatId", currentAlat.getId());
        booking.put("alatName", currentAlat.getNama());
        booking.put("userId", currentUser.getUid());
        booking.put("userName", userName);
        booking.put("userPhone", userPhone);
        booking.put("kelompokTani", currentAlat.getKelompokTani());
        booking.put("kelompokTaniId", currentAlat.getKelompokTaniId());
        booking.put("status", "pending"); // pending, confirmed, cancelled
        booking.put("bookedAt", new Date());
        booking.put("expiresAt", new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000)));
        booking.put("createdAt", new Date());

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    // Booking record created successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error if needed
                });
    }

    private void deleteBookingRecord() {
        db.collection("bookings")
                .whereEqualTo("alatId", currentAlat.getId())
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }
                });
    }

    private void sendWhatsAppMessage() {
        String whatsappNumber = currentAlat.getNoWhatsapp();
        if (whatsappNumber == null || whatsappNumber.isEmpty()) {
            Toast.makeText(getContext(), "Nomor WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format WhatsApp number
        if (whatsappNumber.startsWith("0")) {
            whatsappNumber = "" + whatsappNumber.substring(1);
        } else if (!whatsappNumber.startsWith("")) {
            whatsappNumber = "" + whatsappNumber;
        }

        // Create WhatsApp message
        String message = createBookingWhatsAppMessage();

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + whatsappNumber + "&text=" + Uri.encode(message)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show();
        }
    }

    private String createBookingWhatsAppMessage() {
        StringBuilder message = new StringBuilder();
        message.append("🔔 *BOOKING ALAT PERTANIAN* 🔔\n\n");
        message.append("Halo ").append(currentAlat.getNamaKetua()).append(",\n\n");
        message.append("Saya ingin booking alat pertanian:\n");
        message.append("🚜 *").append(currentAlat.getNama()).append("*\n");
        message.append("💰 Harga: ").append(tvHargaDetail.getText().toString()).append("\n");
        message.append("📍 Lokasi: ").append(currentAlat.getAlamatKelompok()).append("\n");
        message.append("👥 Pemilik: ").append(currentAlat.getKelompokTani()).append("\n\n");

        message.append("⏰ *Status: TELAH DIBOOKING*\n");
        message.append("📱 Booking akan expire dalam 24 jam\n\n");

        message.append("Mohon konfirmasi ketersediaan dan jadwal pengambilan alat.\n");
        message.append("Jika tidak dikonfirmasi dalam 24 jam, booking akan otomatis dibatalkan.\n\n");
        message.append("Terima kasih 🙏");

        return message.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment resumes
        if (alatId != null) {
            loadAlatDetail();
        }
    }
}