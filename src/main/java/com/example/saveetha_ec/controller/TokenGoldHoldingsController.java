package com.example.saveetha_ec.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;


import com.example.saveetha_ec.service.TokenGoldHoldingsService;
import com.example.saveetha_ec.service.UserService;

public class TokenGoldHoldingsController {
	@Autowired
	private TokenGoldHoldingsService holdingsService;
	@Autowired
	private UserService userService;
	@GetMapping("/gold")
	public ResponseEntity<?> getGoldHoldings() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();
	    long userId=userService.getUserIdByUsername(username);
		BigDecimal holdings=holdingsService.getGoldHoldings(userId);
		return ResponseEntity.ok(holdings);
	}

}
