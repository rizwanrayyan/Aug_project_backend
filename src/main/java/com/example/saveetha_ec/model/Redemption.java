package com.example.saveetha_ec.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "redemption")
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    // This links the redemption record back to the main transaction log
    @Column(nullable = false)
    private Long orderId; 

    // This links to the specific token batch that was redeemed
    @Column(nullable = false)
    private Long tokenId;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal gramsRedeemed;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal purchaseRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal redemptionRate;
    
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal costBasis;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal redemptionValue;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal gain;

    private String gainType; // "LTCG" or "STCG"

    @Column(nullable = false)
    private LocalDateTime redemptionDate;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public BigDecimal getGramsRedeemed() {
        return gramsRedeemed;
    }

    public void setGramsRedeemed(BigDecimal gramsRedeemed) {
        this.gramsRedeemed = gramsRedeemed;
    }

    public BigDecimal getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(BigDecimal purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public BigDecimal getRedemptionRate() {
        return redemptionRate;
    }

    public void setRedemptionRate(BigDecimal redemptionRate) {
        this.redemptionRate = redemptionRate;
    }

    public BigDecimal getCostBasis() {
        return costBasis;
    }

    public void setCostBasis(BigDecimal costBasis) {
        this.costBasis = costBasis;
    }

    public BigDecimal getRedemptionValue() {
        return redemptionValue;
    }

    public void setRedemptionValue(BigDecimal redemptionValue) {
        this.redemptionValue = redemptionValue;
    }

    public BigDecimal getGain() {
        return gain;
    }

    public void setGain(BigDecimal gain) {
        this.gain = gain;
    }

    public String getGainType() {
        return gainType;
    }

    public void setGainType(String gainType) {
        this.gainType = gainType;
    }

    public LocalDateTime getRedemptionDate() {
        return redemptionDate;
    }

    public void setRedemptionDate(LocalDateTime redemptionDate) {
        this.redemptionDate = redemptionDate;
    }
}