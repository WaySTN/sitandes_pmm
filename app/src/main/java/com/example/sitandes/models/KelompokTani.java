package com.example.sitandes.models;

import java.io.Serializable;
import java.util.Date;

public class KelompokTani implements Serializable {
    private String id;
    private String nama;
    private String ketua;
    private String noTelp;
    private String alamat;
    private String dusun;
    private Date createdAt;
    private Date updatedAt;

    // Default constructor (required for Firebase)
    public KelompokTani() {
    }

    // Constructor with all parameters
    public KelompokTani(String id, String nama, String ketua, String noTelp, String alamat) {
        this.id = id;
        this.nama = nama;
        this.ketua = ketua;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.dusun = extractDusunFromAlamat(alamat);
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor without ID (for new entries)
    public KelompokTani(String nama, String ketua, String noTelp, String alamat) {
        this.nama = nama;
        this.ketua = ketua;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.dusun = extractDusunFromAlamat(alamat);
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Helper method to extract dusun from alamat
    private String extractDusunFromAlamat(String alamat) {
        if (alamat == null) return "";

        // Extract dusun name from alamat string
        if (alamat.toLowerCase().contains("kopencungking")) {
            return "Kopencungking";
        } else if (alamat.toLowerCase().contains("krajan")) {
            return "Krajan";
        } else if (alamat.toLowerCase().contains("panggang")) {
            return "Panggang";
        } else if (alamat.toLowerCase().contains("rejopuro")) {
            return "Rejopuro";
        } else if (alamat.toLowerCase().contains("dusun")) {
            // If alamat contains "Dusun" but not matched above, try to extract
            String[] parts = alamat.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("dusun") && i + 1 < parts.length) {
                    return parts[i + 1];
                }
            }
        }
        return "";
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

    public String getKetua() {
        return ketua;
    }

    public void setKetua(String ketua) {
        this.ketua = ketua;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
        this.dusun = extractDusunFromAlamat(alamat);
    }

    public String getDusun() {
        return dusun;
    }

    public void setDusun(String dusun) {
        this.dusun = dusun;
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

    // Utility methods
    public String getFormattedPhoneNumber() {
        if (noTelp == null) return "";

        // Format phone number to include +62 prefix if not present
        String cleanNumber = noTelp.replaceAll("[^0-9]", "");
        if (cleanNumber.startsWith("0")) {
            return "+62" + cleanNumber.substring(1);
        } else if (!cleanNumber.startsWith("62")) {
            return "+62" + cleanNumber;
        }
        return "+" + cleanNumber;
    }

    public String getDisplayName() {
        return nama + " (" + ketua + ")";
    }

    public String getFullAddress() {
        if (dusun != null && !dusun.isEmpty()) {
            return "Dusun " + dusun;
        }
        return alamat != null ? alamat : "";
    }

    @Override
    public String toString() {
        return "KelompokTani{" +
                "id='" + id + '\'' +
                ", nama='" + nama + '\'' +
                ", ketua='" + ketua + '\'' +
                ", noTelp='" + noTelp + '\'' +
                ", alamat='" + alamat + '\'' +
                ", dusun='" + dusun + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KelompokTani that = (KelompokTani) o;

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