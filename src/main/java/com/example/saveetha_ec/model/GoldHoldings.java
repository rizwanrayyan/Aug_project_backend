package com.example.saveetha_ec.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Gold_Holdings")
public class GoldHoldings {
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private long id;
private long userId;
private BigDecimal grams=BigDecimal.ZERO;
public long getId() {
	return id;
}
public void setId(long id) {
	this.id = id;
}
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
