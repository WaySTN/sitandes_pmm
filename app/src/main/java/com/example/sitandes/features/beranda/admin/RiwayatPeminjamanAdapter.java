package com.example.sitandes.features.beranda.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitandes.R;

import java.util.List;

public class RiwayatPeminjamanAdapter extends RecyclerView.Adapter<RiwayatPeminjamanAdapter.ViewHolder> {

    private Context context;
    private List<RiwayatPeminjamanFragment.RiwayatPeminjamanModel> riwayatList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RiwayatPeminjamanFragment.RiwayatPeminjamanModel riwayat);
    }

    public RiwayatPeminjamanAdapter(Context context, List<RiwayatPeminjamanFragment.RiwayatPeminjamanModel> riwayatList) {
        this.context = context;
        this.riwayatList = riwayatList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_riwayat_peminjaman, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RiwayatPeminjamanFragment.RiwayatPeminjamanModel riwayat = riwayatList.get(position);
        holder.bind(riwayat);
    }

    @Override
    public int getItemCount() {
        return riwayatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStatus;
        private TextView tvCreatedDate;
        private TextView tvNamaPeminjam;
        private TextView tvNamaKetua;
        private TextView tvPhone;
        private TextView tvNamaAlat;
        private TextView tvKelompokTani;
        private TextView tvBorrowedDate;
        private TextView tvBookingExpiry;
        private TextView tvHarga;
        private TextView tvStok;
        private LinearLayout llActionButtons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            tvNamaPeminjam = itemView.findViewById(R.id.tv_nama_peminjam);
            tvNamaKetua = itemView.findViewById(R.id.tv_nama_ketua);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvNamaAlat = itemView.findViewById(R.id.tv_nama_alat);
            tvKelompokTani = itemView.findViewById(R.id.tv_kelompok_tani);
            tvBorrowedDate = itemView.findViewById(R.id.tv_borrowed_date);
            tvBookingExpiry = itemView.findViewById(R.id.tv_booking_expiry);
            tvHarga = itemView.findViewById(R.id.tv_harga);
            tvStok = itemView.findViewById(R.id.tv_stok);
            llActionButtons = itemView.findViewById(R.id.ll_action_buttons);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(riwayatList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(RiwayatPeminjamanFragment.RiwayatPeminjamanModel riwayat) {
            // Set status dengan background color yang sesuai
            setStatusDisplay(riwayat.getStatus());

            // Set tanggal dibuat
            tvCreatedDate.setText(riwayat.getFormattedCreatedDate());

            // Set info peminjam
            tvNamaPeminjam.setText(riwayat.getBorrowerName() != null ? riwayat.getBorrowerName() : "-");
            tvNamaKetua.setText(riwayat.getNamaKetua() != null ? riwayat.getNamaKetua() : "-");
            tvPhone.setText(riwayat.getBorrowerPhone() != null ? riwayat.getBorrowerPhone() : "-");

            // Set info alat
            tvNamaAlat.setText(riwayat.getAlamatKelompok() != null ? riwayat.getAlamatKelompok() : "-");
            tvKelompokTani.setText(riwayat.getKelompokTani() != null ? riwayat.getKelompokTani() : "-");

            // Set tanggal peminjaman
            tvBorrowedDate.setText(riwayat.getFormattedBorrowedDate());
            tvBookingExpiry.setText(riwayat.getFormattedExpiryDate());

            // Set harga dan stok
            tvHarga.setText(riwayat.getFormattedHarga());
            tvStok.setText(riwayat.getStok() != null ? String.valueOf(riwayat.getStok()) : "0");

            // Hide action buttons untuk riwayat (bisa disesuaikan kebutuhan)
            llActionButtons.setVisibility(View.GONE);
        }

        private void setStatusDisplay(String status) {
            if (status == null) {
                status = "unknown";
            }

            switch (status.toLowerCase()) {
                case "rented":
                    tvStatus.setText("DIPINJAM");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_booked);
                    break;
                case "returned":
                    tvStatus.setText("DIKEMBALIKAN");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_tersedia);
                    break;
                case "overdue":
                    tvStatus.setText("TERLAMBAT");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_habis);
                    break;
                case "cancelled":
                    tvStatus.setText("DIBATALKAN");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_maintenance);
                    break;
                default:
                    tvStatus.setText("TIDAK DIKETAHUI");
                    tvStatus.setBackgroundResource(R.drawable.bg_status_tidak_diketahui);
                    break;
            }
        }
    }
}