package com.example.saveetha_ec.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "token_gold")
public class TokenGold {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long tokenId;
    private long userId;
    private double purchase_rate;
    private BigDecimal grams_purchased;
    private BigDecimal grams_remaining;
    private String transaction_hash;
    
    // Changed to String to store the Hex representation of the hash
    private String dataHash; 

    private String vaultId;
    
    @Column(name="date_of_acquisation")
    private LocalDateTime dateOfAcquisation;
    
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    // Getters and Setters
    public LocalDateTime getDateOfAcquisation() {
        return dateOfAcquisation;
    }
    public void setDateOfAcquisation(LocalDateTime dateOfAcquisation) {
        this.dateOfAcquisation = dateOfAcquisation;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public long getTokenId() {
        return tokenId;
    }
    public void setTokenId(long tokenId) {
        this.tokenId = tokenId;
    }
    public double getPurchase_rate() {
        return purchase_rate;
    }
    public void setPurchase_rate(double purchase_rate) {
        this.purchase_rate = purchase_rate;
    }
    public BigDecimal getGrams_purchased() {
        return grams_purchased;
    }
    public void setGrams_purchased(BigDecimal grams_purchased) {
        this.grams_purchased = grams_purchased;
    }
    public BigDecimal getGrams_remaining() {
        return grams_remaining;
    }
    public void setGrams_remaining(BigDecimal grams_remaining) {
        this.grams_remaining = grams_remaining;
    }
    public String getTransaction_hash() {
        return transaction_hash;
    }
    public void setTransaction_hash(String transaction_hash) {
        this.transaction_hash = transaction_hash;
    }
    public String getData_hash() {
        return dataHash;
    }
    public void setData_hash(String dataHash) {
        this.dataHash = dataHash;
    }
    public String getVaultId() {
        return vaultId;
    }
    public void setVaultId(String vaultId) {
        this.vaultId = vaultId;
    }
    public StatusEnum getStatus() {
        return status;
    }
    public void setStatus(StatusEnum status) {
        this.status = status;
    }
}