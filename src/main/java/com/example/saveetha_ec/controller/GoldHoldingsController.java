package com.example.saveetha_ec.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.service.GoldHoldingsService;
import com.example.saveetha_ec.service.UserService;

@RestController
@RequestMapping("api/holdings")
public class GoldHoldingsController {
@Autowired
private GoldHoldingsService holdingsService;
@Autowired
private UserService userService;
@GetMapping("/gold")
public ResponseEntity<?> getGoldHoldings() {
	System.out.println("Getting into the getGold Controller");
	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    System.out.println(username);
    long userId=userService.getUserIdByUsername(username);
	BigDecimal holdings=holdingsService.getGoldHoldings(userId);
	return ResponseEntity.ok(holdings);
}
}
