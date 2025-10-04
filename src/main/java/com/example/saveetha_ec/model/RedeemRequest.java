package com.example.saveetha_ec.model;

import java.math.BigDecimal;

public class RedeemRequest {

    private BigDecimal grams;
	private BigDecimal redemptionRate;


	public BigDecimal getGrams() {
		return grams;
	}
	public void setGrams(BigDecimal grams) {
		this.grams = grams;
	}
	public BigDecimal getRedemptionRate() {
        return redemptionRate;
    }
    public void setRedemptionRate(BigDecimal redemptionRate) {
        this.redemptionRate = redemptionRate;
    }
    // getters & setters
}