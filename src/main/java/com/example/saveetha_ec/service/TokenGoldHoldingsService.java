package com.example.saveetha_ec.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.TokenGoldHoldings;
import com.example.saveetha_ec.repository.TokenGoldHoldingsRepo;
@Service
public class TokenGoldHoldingsService {
	@Autowired
	private TokenGoldHoldingsRepo repo;
	public BigDecimal getGoldHoldings(long userId) {
		TokenGoldHoldings holdings = repo.findByUserId(userId)
	            .orElseThrow(() -> new RuntimeException("No TokenGoldHoldings found for userId: " + userId));
		return holdings.getGrams();
	}

}
