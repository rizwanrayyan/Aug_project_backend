package com.example.saveetha_ec.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "digital_gold_wallet")
public class DigiGoldWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal gramsPurchased;

    private double purchaseRate;

    @Enumerated(EnumType.STRING)
    private StatusEnum status; // e.g., PURCHASED, REDEEMED

    private LocalDateTime acquisitionDate; // When the gold was purchased
    private BigDecimal gramsRemaining;

    public DigiGoldWallet() {}

    public DigiGoldWallet(Long userId, BigDecimal gramsPurchased, double purchaseRate, StatusEnum status, LocalDateTime acquisitionDate) {
        this.userId = userId;
        this.gramsPurchased = gramsPurchased;
        this.purchaseRate = purchaseRate;
        this.status = status;
        this.acquisitionDate = acquisitionDate;
    }

    // Getters and Setters
    public Long getWalletId() {
        return id;
    }

    public void setWalletId(Long walletId) {
        this.id = walletId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getGramsPurchased() {
        return gramsPurchased;
    }

    public void setGramsPurchased(BigDecimal gramsPurchased) {
        this.gramsPurchased = gramsPurchased;
    }

    public double getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDateTime localDateTime) {
        this.acquisitionDate = localDateTime;
    }

	public BigDecimal getGramsRemaining() {
		return gramsRemaining;
	}

	public void setGramsRemaining(BigDecimal gramsRemaining) {
		this.gramsRemaining = gramsRemaining;
	}

	public void setAcquisitionDate1(LocalDateTime createdAt) {
		this.acquisitionDate=createdAt;

	}


}
