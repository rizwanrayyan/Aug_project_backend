package com.example.saveetha_ec.model;

import java.math.BigDecimal;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="gold_price_history")
public class GoldPriceHistory {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(nullable = false, precision = 12, scale = 2)
	    private BigDecimal price;

	    @Column(nullable = false)
	    private long timestamp;

	    // Constructors

	    // Getters and Setters
	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }

	    public BigDecimal getPrice() {
	        return price;
	    }

	    public void setPrice(BigDecimal price) {
	        this.price = price;
	    }

	    public long getTimestamp() {
	        return timestamp;
	    }

	    public void setTimestamp(long l) {
	        this.timestamp = l;
	    }

}
