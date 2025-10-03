package com.example.saveetha_ec.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.GoldHoldings;
import com.example.saveetha_ec.repository.GoldHoldingsRepo;

@Service
public class GoldHoldingsService {
	@Autowired
	private GoldHoldingsRepo repo;
	public BigDecimal getGoldHoldings(long userId) {
		GoldHoldings g= repo.findByUserId(userId).orElse(null);
		if(g==null)
			throw new NullPointerException();
		return g.getGrams();
	}

}
