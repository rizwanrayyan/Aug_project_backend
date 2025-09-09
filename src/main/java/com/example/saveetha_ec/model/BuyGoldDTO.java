package com.example.saveetha_ec.model;

import java.math.BigDecimal;

public class BuyGoldDTO {
	private long userId;
	private BigDecimal grams;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public BigDecimal getGrams() {
		return grams;
	}
	public void setGrams(BigDecimal grams) {
		this.grams = grams;
	}

}
