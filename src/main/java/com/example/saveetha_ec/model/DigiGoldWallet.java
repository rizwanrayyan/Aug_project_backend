package com.example.saveetha_ec.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="digi_gold_wallet")
public class DigiGoldWallet {

    @Id
    private long id;

    @Column(nullable = false, length = 10)
    private String units = "GRAMS";  // default is GRAMS

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal gold=BigDecimal.ZERO;  // avoid capitalized field name

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUnits() { return units; }
    public void setUnits(String units) { this.units = units; }

    public BigDecimal getGold() { return gold; }
    public void setGold(BigDecimal gold) { this.gold = gold; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
