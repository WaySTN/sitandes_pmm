package com.example.sitandes.models;

import com.google.firebase.Timestamp;

public class BookingModel {
    private String id;
    private String alatId;
    private String alatName;
    private Long bookedAt;
    private Long createdAt;
    private Long expiresAt;
    private String kelompokTani;
    private String kelompokTaniId;
    private String status;
    private String userId;
    private String userName;
    private String userPhone;
    private String borrowerName;
    private String borrowerPhone;
    private String currentBorrower;
    private String deskripsi;
    private String expectedReturnDate;
    private String fotoUrl;
    private Integer harga;
    private String namaKetua;
    private String noWhatsapp;
    private Integer stok;
    private Long updatedAt;
    private String alamatKelompok;

    // Default constructor required for Firestore
    public BookingModel() {}

    // Constructor with essential fields
    public BookingModel(String alatId, String userId, String kelompokTaniId, String status) {
        this.alatId = alatId;
        this.userId = userId;
        this.kelompokTaniId = kelompokTaniId;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
        this.bookedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlatId() {
        return alatId;
    }

    public void setAlatId(String alatId) {
        this.alatId = alatId;
    }

    public String getAlatName() {
        return alatName;
    }

    public void setAlatName(String alatName) {
        this.alatName = alatName;
    }

    public Long getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(Long bookedAt) {
        this.bookedAt = bookedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
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

    public String getCurrentBorrower() {
        return currentBorrower;
    }

    public void setCurrentBorrower(String currentBorrower) {
        this.currentBorrower = currentBorrower;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(String expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Integer getHarga() {
        return harga;
    }

    public void setHarga(Integer harga) {
        this.harga = harga;
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

    public Integer getStok() {
        return stok;
    }

    public void setStok(Integer stok) {
        this.stok = stok;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAlamatKelompok() {
        return alamatKelompok;
    }

    public void setAlamatKelompok(String alamatKelompok) {
        this.alamatKelompok = alamatKelompok;
    }

    // Helper methods
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isActive() {
        return "rented".equalsIgnoreCase(status) || "aktif".equalsIgnoreCase(status);
    }

    public boolean isReturned() {
        return "returned".equalsIgnoreCase(status) || "selesai".equalsIgnoreCase(status);
    }

    public boolean isOverdue() {
        return "overdue".equalsIgnoreCase(status) || "terlambat".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "BookingModel{" +
                "id='" + id + '\'' +
                ", alatId='" + alatId + '\'' +
                ", alatName='" + alatName + '\'' +
                ", bookedAt=" + bookedAt +
                ", status='" + status + '\'' +
                ", borrowerName='" + borrowerName + '\'' +
                ", kelompokTani='" + kelompokTani + '\'' +
                '}';
    }
}