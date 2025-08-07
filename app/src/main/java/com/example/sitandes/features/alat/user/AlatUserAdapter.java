package com.example.sitandes.features.alat.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sitandes.R;
import com.example.sitandes.models.AlatPertanian;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AlatUserAdapter extends RecyclerView.Adapter<AlatUserAdapter.AlatViewHolder> {

    private List<AlatPertanian> alatList;
    private OnAlatClickListener listener;
    private Context context;

    public interface OnAlatClickListener {
        void onDetailClick(AlatPertanian alat);
        void onPinjamClick(AlatPertanian alat);
    }

    public AlatUserAdapter(List<AlatPertanian> alatList, OnAlatClickListener listener) {
        this.alatList = alatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_alat_user, parent, false);
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
        private TextView tvNamaAlat;
        private TextView tvKelompokTani;
        private TextView tvHargaAlat;
        private TextView tvStatusAlat;
        private TextView tvStokAlat;
        private MaterialButton btnDetailAlat;
        private MaterialButton btnPinjamAlat;

        public AlatViewHolder(@NonNull View itemView) {
            super(itemView);

            ivFotoAlat = itemView.findViewById(R.id.iv_foto_alat_user);
            tvNamaAlat = itemView.findViewById(R.id.tv_nama_alat_user);
            tvKelompokTani = itemView.findViewById(R.id.tv_kelompok_tani_user);
            tvHargaAlat = itemView.findViewById(R.id.tv_harga_alat_user);
            tvStatusAlat = itemView.findViewById(R.id.tv_status_alat_user);
            tvStokAlat = itemView.findViewById(R.id.tv_stok_alat_user);
            btnDetailAlat = itemView.findViewById(R.id.btn_detail_alat);
            btnPinjamAlat = itemView.findViewById(R.id.btn_pinjam_alat);
        }

        public void bind(AlatPertanian alat) {
            // Set basic info
            tvNamaAlat.setText(alat.getNama());
            tvKelompokTani.setText(alat.getKelompokTani());
            tvStokAlat.setText("Stok: " + alat.getStok());

            // Format and set price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String formattedPrice = formatter.format(alat.getHarga()).replace("IDR", "Rp") + "/hari";
            tvHargaAlat.setText(formattedPrice);

            // Set status and configure button using the same logic as DetailAlatFragment
            updateStatusAndButton(alat);

            // Load image
            if (alat.getFotoUrl() != null && !alat.getFotoUrl().isEmpty()) {
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_tools)
                        .error(R.drawable.ic_tools);

                Glide.with(context)
                        .load(alat.getFotoUrl())
                        .apply(options)
                        .into(ivFotoAlat);
            } else {
                ivFotoAlat.setImageResource(R.drawable.ic_tools);
            }

            // Set click listeners
            btnDetailAlat.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailClick(alat);
                }
            });

            btnPinjamAlat.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPinjamClick(alat);
                }
            });

            // Set item click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailClick(alat);
                }
            });
        }

        /**
         * Update status and button based on equipment status
         * This method uses the same logic as DetailAlatFragment
         */
        private void updateStatusAndButton(AlatPertanian alat) {
            String status = alat.getStatus();

            if (status == null) {
                // Fallback to old stock-based logic if status is null
                if (alat.getStok() > 0) {
                    tvStatusAlat.setText("Tersedia");
                    tvStatusAlat.setBackgroundResource(R.drawable.bg_status_tersedia);
                    btnPinjamAlat.setEnabled(true);
                    btnPinjamAlat.setText("Pinjam");
                    btnPinjamAlat.setAlpha(1.0f);
                } else {
                    tvStatusAlat.setText("Dipinjam");
                    tvStatusAlat.setBackgroundResource(R.drawable.bg_status_habis);
                    btnPinjamAlat.setEnabled(false);
                    btnPinjamAlat.setText("Tidak Tersedia");
                    btnPinjamAlat.setAlpha(0.6f);
                }
                return;
            }

            switch (status) {
                case "available":
                    if (alat.getStok() > 0) {
                        tvStatusAlat.setText("Tersedia");
                        tvStatusAlat.setBackgroundResource(R.drawable.bg_status_tersedia);
                        btnPinjamAlat.setEnabled(true);
                        btnPinjamAlat.setText("Pinjam");
                        btnPinjamAlat.setAlpha(1.0f);
                    } else {
                        tvStatusAlat.setText("Stok Habis");
                        tvStatusAlat.setBackgroundResource(R.drawable.bg_status_habis);
                        btnPinjamAlat.setEnabled(false);
                        btnPinjamAlat.setText("Stok Habis");
                        btnPinjamAlat.setAlpha(0.6f);
                    }
                    break;

                case "booked":
                    tvStatusAlat.setText("Dibooking");
                    tvStatusAlat.setBackgroundResource(R.drawable.bg_status_booked);
                    btnPinjamAlat.setEnabled(false);
                    btnPinjamAlat.setText("Dibooking");
                    btnPinjamAlat.setAlpha(0.6f);
                    break;

                case "rented":
                    tvStatusAlat.setText("Dipinjam");
                    tvStatusAlat.setBackgroundResource(R.drawable.bg_status_habis);
                    btnPinjamAlat.setEnabled(false);
                    btnPinjamAlat.setText("Dipinjam");
                    btnPinjamAlat.setAlpha(0.6f);
                    break;

                case "maintenance":
                    tvStatusAlat.setText("Maintenance");
                    tvStatusAlat.setBackgroundResource(R.drawable.bg_status_maintenance);
                    btnPinjamAlat.setEnabled(false);
                    btnPinjamAlat.setText("Diperbaiki");
                    btnPinjamAlat.setAlpha(0.6f);
                    break;

                default:
                    tvStatusAlat.setText("Tidak Tersedia");
                    tvStatusAlat.setBackgroundResource(R.drawable.bg_status_habis);
                    btnPinjamAlat.setEnabled(false);
                    btnPinjamAlat.setText("Tidak Tersedia");
                    btnPinjamAlat.setAlpha(0.6f);
                    break;
            }
        }
    }

    public void updateData(List<AlatPertanian> newAlatList) {
        this.alatList = newAlatList;
        notifyDataSetChanged();
    }

    public void addItem(AlatPertanian alat) {
        alatList.add(0, alat);
        notifyItemInserted(0);
    }

    public void updateItem(int position, AlatPertanian alat) {
        if (position >= 0 && position < alatList.size()) {
            alatList.set(position, alat);
            notifyItemChanged(position);
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < alatList.size()) {
            alatList.remove(position);
            notifyItemRemoved(position);
        }
    }
}