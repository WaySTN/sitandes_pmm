package com.example.sitandes.models;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class AlatPertanian implements Serializable {
    private String id;
    private String nama;
    private String deskripsi;
    private double harga;
    private int stok;
    private String kelompokTani;
    private String kelompokTaniId;
    private String namaKetua;
    private String noWhatsapp;
    private String alamatKelompok;
    private String fotoUrl;
    private String status; // "available", "rented", "maintenance", "kosong", "tidak_aktif"
    private Date createdAt;
    private Date updatedAt;

    // Rental-related fields
    private String currentBorrower; // ID user yang sedang meminjam
    private String borrowerName; // Nama user yang sedang meminjam
    private String borrowerPhone; // No HP user yang sedang meminjam
    private Date borrowedAt; // Tanggal dipinjam
    private String expectedReturnDate; // Tanggal diharapkan kembali

    // Default constructor (required for Firebase)
    public AlatPertanian() {
    }

    // Constructor with all parameters
    public AlatPertanian(String id, String nama, String deskripsi, double harga, int stok,
                         String kelompokTani, String kelompokTaniId, String namaKetua,
                         String noWhatsapp, String alamatKelompok, String fotoUrl) {
        this.id = id;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.stok = stok;
        this.kelompokTani = kelompokTani;
        this.kelompokTaniId = kelompokTaniId;
        this.namaKetua = namaKetua;
        this.noWhatsapp = noWhatsapp;
        this.alamatKelompok = alamatKelompok;
        this.fotoUrl = fotoUrl;
        this.status = determineStatus();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor without ID (for new entries)
    public AlatPertanian(String nama, String deskripsi, double harga, int stok,
                         String kelompokTani, String kelompokTaniId, String namaKetua,
                         String noWhatsapp, String alamatKelompok, String fotoUrl) {
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.stok = stok;
        this.kelompokTani = kelompokTani;
        this.kelompokTaniId = kelompokTaniId;
        this.namaKetua = namaKetua;
        this.noWhatsapp = noWhatsapp;
        this.alamatKelompok = alamatKelompok;
        this.fotoUrl = fotoUrl;
        this.status = determineStatus();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Helper method to determine status based on stock
    private String determineStatus() {
        if (stok > 0) {
            return "available";
        } else {
            return "kosong";
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
        this.updatedAt = new Date();
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
        if (!"rented".equals(this.status) && !"maintenance".equals(this.status)) {
            this.status = determineStatus();
        }
        this.updatedAt = new Date();
    }

    public String getKelompokTani() {
        return kelompokTani;
    }

    public void setKelompokTani(String kelompokTani) {
        this.kelompokTani = kelompokTani;
    }

    public String getKelompokTaniId() {
        return kelompokTaniId;
    }

    public void setKelompokTaniId(String kelompokTaniId) {
        this.kelompokTaniId = kelompokTaniId;
    }

    public String getNamaKetua() {
        return namaKetua;
    }

    public void setNamaKetua(String namaKetua) {
        this.namaKetua = namaKetua;
    }

    public String getNoWhatsapp() {
        return noWhatsapp;
    }

    public void setNoWhatsapp(String noWhatsapp) {
        this.noWhatsapp = noWhatsapp;
    }

    public String getAlamatKelompok() {
        return alamatKelompok;
    }

    public void setAlamatKelompok(String alamatKelompok) {
        this.alamatKelompok = alamatKelompok;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getStatus() {
        return status != null ? status : determineStatus();
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Rental-related getters and setters
    public String getCurrentBorrower() {
        return currentBorrower;
    }

    public void setCurrentBorrower(String currentBorrower) {
        this.currentBorrower = currentBorrower;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getBorrowerPhone() {
        return borrowerPhone;
    }

    public void setBorrowerPhone(String borrowerPhone) {
        this.borrowerPhone = borrowerPhone;
    }

    public Date getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(Date borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public String getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(String expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    // Utility methods
    public String getFormattedHarga() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(harga);
    }

    public String getFormattedHargaSimple() {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        return "Rp " + formatter.format(harga);
    }

    public boolean isOutOfStock() {
        return stok <= 0;
    }

    public String getStockStatus() {
        if (stok > 10) {
            return "Stok banyak";
        } else if (stok > 5) {
            return "Stok sedang";
        } else if (stok > 0) {
            return "Stok sedikit";
        } else {
            return "Stok habis";
        }
    }

    public String getFormattedWhatsappNumber() {
        if (noWhatsapp == null) return "";

        // Ensure the number has +62 prefix for WhatsApp
        String cleanNumber = noWhatsapp.replaceAll("[^0-9]", "");
        if (cleanNumber.startsWith("0")) {
            return "+62" + cleanNumber.substring(1);
        } else if (!cleanNumber.startsWith("62")) {
            return "+62" + cleanNumber;
        }
        return "+" + cleanNumber;
    }

    public String getWhatsappUrl(String message) {
        String phoneNumber = getFormattedWhatsappNumber();
        String encodedMessage = message != null ? message.replace(" ", "%20") : "";
        return "https://wa.me/" + phoneNumber.replace("+", "") + "?text=" + encodedMessage;
    }

    public String getDefaultWhatsappMessage() {
        return "Halo " + namaKetua + ", saya tertarik dengan alat pertanian " + nama +
                " dari kelompok " + kelompokTani + ". Apakah masih tersedia?";
    }

    public void reduceStock(int quantity) {
        if (quantity > 0 && stok >= quantity) {
            this.stok -= quantity;
            if (!"rented".equals(this.status) && !"maintenance".equals(this.status)) {
                this.status = determineStatus();
            }
            this.updatedAt = new Date();
        }
    }

    public void addStock(int quantity) {
        if (quantity > 0) {
            this.stok += quantity;
            if (!"rented".equals(this.status) && !"maintenance".equals(this.status)) {
                this.status = determineStatus();
            }
            this.updatedAt = new Date();
        }
    }

    // Status checking methods
    public boolean isAvailable() {
        return "available".equals(status) && stok > 0;
    }

    public boolean isRented() {
        return "rented".equals(status);
    }

    public boolean isInMaintenance() {
        return "maintenance".equals(status);
    }

    public boolean canBeRented() {
        return isAvailable() && !isRented() && !isInMaintenance();
    }

    // Rental management methods
    public void setAsRented(String borrowerId, String borrowerName, String borrowerPhone, Date borrowedAt, String expectedReturnDate) {
        this.status = "rented";
        this.currentBorrower = borrowerId;
        this.borrowerName = borrowerName;
        this.borrowerPhone = borrowerPhone;
        this.borrowedAt = borrowedAt;
        this.expectedReturnDate = expectedReturnDate;
        this.updatedAt = new Date();
    }

    public void setAsReturned() {
        this.status = "available";
        this.currentBorrower = null;
        this.borrowerName = null;
        this.borrowerPhone = null;
        this.borrowedAt = null;
        this.expectedReturnDate = null;
        this.updatedAt = new Date();
    }

    public void setAsMaintenance() {
        this.status = "maintenance";
        this.updatedAt = new Date();
    }

    public void setAsAvailable() {
        if (!isRented()) { // Only set to available if not currently rented
            this.status = determineStatus();
            this.updatedAt = new Date();
        }
    }

    // Status display methods
    public String getStatusDisplay() {
        switch (status) {
            case "available":
                return "Tersedia";
            case "rented":
                return "Dipinjam";
            case "maintenance":
                return "Maintenance";
            case "kosong":
                return "Stok Habis";
            case "tidak_aktif":
                return "Tidak Aktif";
            default:
                return "Unknown";
        }
    }

    public String getBorrowerInfo() {
        if (isRented() && borrowerName != null) {
            return borrowerName + (borrowerPhone != null ? " (" + borrowerPhone + ")" : "");
        }
        return null;
    }

    @Override
    public String toString() {
        return "AlatPertanian{" +
                "id='" + id + '\'' +
                ", nama='" + nama + '\'' +
                ", deskripsi='" + deskripsi + '\'' +
                ", harga=" + harga +
                ", stok=" + stok +
                ", kelompokTani='" + kelompokTani + '\'' +
                ", kelompokTaniId='" + kelompokTaniId + '\'' +
                ", namaKetua='" + namaKetua + '\'' +
                ", noWhatsapp='" + noWhatsapp + '\'' +
                ", alamatKelompok='" + alamatKelompok + '\'' +
                ", fotoUrl='" + fotoUrl + '\'' +
                ", status='" + status + '\'' +
                ", currentBorrower='" + currentBorrower + '\'' +
                ", borrowerName='" + borrowerName + '\'' +
                ", borrowerPhone='" + borrowerPhone + '\'' +
                ", borrowedAt=" + borrowedAt +
                ", expectedReturnDate='" + expectedReturnDate + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlatPertanian that = (AlatPertanian) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return nama != null ? nama.equals(that.nama) : that.nama == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (nama != null ? nama.hashCode() : 0);
        return result;
    }
}