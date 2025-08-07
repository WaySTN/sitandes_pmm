package com.example.sitandes.features.alat.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sitandes.R;
import com.example.sitandes.models.AlatPertanian;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AlatAdminAdapter extends RecyclerView.Adapter<AlatAdminAdapter.AlatViewHolder> {

    private List<AlatPertanian> alatList;
    private OnAlatActionListener listener;
    private Context context;

    public interface OnAlatActionListener {
        void onEditAlat(AlatPertanian alat);
        void onDeleteAlat(AlatPertanian alat);
        void onTogglePinjam(AlatPertanian alat);
        void onToggleMaintenance(AlatPertanian alat);
    }

    public AlatAdminAdapter(List<AlatPertanian> alatList, OnAlatActionListener listener) {
        this.alatList = alatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_alat_admin, parent, false);
        return new AlatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlatViewHolder holder, int position) {
        AlatPertanian alat = alatList.get(position);
        holder.bind(alat);
    }

    @Override
    public int getItemCount() {
        return alatList.size();
    }

    class AlatViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivFotoAlat;
        private TextView tvNamaAlat, tvStatusAlat, tvKelompokTani, tvHargaAlat, tvStokAlat;
        private MaterialButton btnEditAlat, btnHapusAlat, btnTogglePinjam, btnMaintenance;

        public AlatViewHolder(@NonNull View itemView) {
            super(itemView);

            ivFotoAlat = itemView.findViewById(R.id.iv_foto_alat);
            tvNamaAlat = itemView.findViewById(R.id.tv_nama_alat);
            tvStatusAlat = itemView.findViewById(R.id.tv_status_alat);
            tvKelompokTani = itemView.findViewById(R.id.tv_kelompok_tani);
            tvHargaAlat = itemView.findViewById(R.id.tv_harga_alat);
            tvStokAlat = itemView.findViewById(R.id.tv_stok_alat);
            btnEditAlat = itemView.findViewById(R.id.btn_edit_alat);
            btnHapusAlat = itemView.findViewById(R.id.btn_hapus_alat);
            btnTogglePinjam = itemView.findViewById(R.id.btn_toggle_pinjam);
            btnMaintenance = itemView.findViewById(R.id.btn_maintenance);
        }

        public void bind(AlatPertanian alat) {
            // Set nama alat
            tvNamaAlat.setText(alat.getNama());

            // Set kelompok tani
            tvKelompokTani.setText(alat.getKelompokTani());

            // Set harga dengan format rupiah
            String formattedPrice = formatRupiah(alat.getHarga());
            tvHargaAlat.setText(formattedPrice + "/hari");

            // Set stok
            tvStokAlat.setText("Stok: " + alat.getStok());

            // Set status berdasarkan status alat
            updateStatusDisplay(alat);

            // Load foto alat menggunakan Glide
            if (alat.getFotoUrl() != null && !alat.getFotoUrl().isEmpty()) {
                Glide.with(context)
                        .load(alat.getFotoUrl())
                        .placeholder(R.drawable.ic_tools)
                        .error(R.drawable.ic_tools)
                        .centerCrop()
                        .into(ivFotoAlat);
            } else {
                ivFotoAlat.setImageResource(R.drawable.ic_tools);
            }

            // Update button states based on status
            updateButtonStates(alat);

            // Set click listeners
            btnEditAlat.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditAlat(alat);
                }
            });

            btnHapusAlat.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteAlat(alat);
                }
            });

            btnTogglePinjam.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTogglePinjam(alat);
                }
            });

            btnMaintenance.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleMaintenance(alat);
                }
            });
        }

        private void updateStatusDisplay(AlatPertanian alat) {
            if (alat.getStatus() != null) {
                switch (alat.getStatus()) {
                    case "available":
                        tvStatusAlat.setText("Tersedia");
                        tvStatusAlat.setBackgroundResource(R.drawable.bg_status_tersedia);
                        tvStatusAlat.setTextColor(ContextCompat.getColor(context, R.color.white));
                        break;
                    case "rented":
                        tvStatusAlat.setText("Dipinjam");
                        tvStatusAlat.setBackgroundResource(R.drawable.bg_status_habis);
                        tvStatusAlat.setTextColor(ContextCompat.getColor(context, R.color.white));
                        break;
                    case "maintenance":
                        tvStatusAlat.setText("Maintenance");
                        tvStatusAlat.setBackgroundResource(R.drawable.bg_status_maintenance);
                        tvStatusAlat.setTextColor(ContextCompat.getColor(context, R.color.white));
                        break;
                    default:
                        // Fallback to stock-based status
                        if (alat.getStok() > 0) {
                            tvStatusAlat.setText("Tersedia");
                            tvStatusAlat.setBackgroundResource(R.drawable.bg_status_tersedia);
                            tvStatusAlat.setTextColor(ContextCompat.getColor(context, R.color.white));
                        } else {
                            tvStatusAlat.setText("Habis");
                            tvStatusAlat.setBackgroundResource(R.drawable.bg_status_habis);
                            tvStatusAlat.setTextColor(ContextCompat.getColor(context, R.color.white));
                        }
                        break;
                }
            } else {
                // Fallback to stock-based status if status is null
                if (alat.getStok() > 0) {
                    tvStatusAlat.setText("Tersedia");
                    tvStatusAlat.setBackgroundResource(R.drawable.bg_status_tersedia);
                    tvStatusAlat.setTextColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    tvStatusAlat.setText("Habis");
                    tvStatusAlat.setBackgroundResource(R.drawable.bg_status_habis);
                    tvStatusAlat.setTextColor(ContextCompat.getColor(context, R.color.white));
                }
            }
        }

        private void updateButtonStates(AlatPertanian alat) {
            if (alat.isRented()) {
                // Jika sedang dipinjam
                btnTogglePinjam.setText("Kembalikan");
                btnTogglePinjam.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_return));
                btnTogglePinjam.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

                // Disable edit dan hapus saat dipinjam
                btnEditAlat.setEnabled(false);
                btnHapusAlat.setEnabled(false);
                btnMaintenance.setEnabled(false);
            } else {
                // Jika tersedia atau maintenance
                btnTogglePinjam.setText("Pinjamkan");
                btnTogglePinjam.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_handshake));
                btnTogglePinjam.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));

                // Enable semua button
                btnEditAlat.setEnabled(true);
                btnHapusAlat.setEnabled(true);
                btnMaintenance.setEnabled(true);
            }

            // Update maintenance button
            if ("maintenance".equals(alat.getStatus())) {
                btnMaintenance.setText("Selesai");
                btnMaintenance.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_check));
            } else {
                btnMaintenance.setText("Maintenance");
                btnMaintenance.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_build));
            }
        }

        private String formatRupiah(double amount) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String formatted = formatter.format(amount);
            // Remove the currency symbol and replace with "Rp "
            return formatted.replace("IDR", "Rp").replace("Rp", "Rp ");
        }
    }
}