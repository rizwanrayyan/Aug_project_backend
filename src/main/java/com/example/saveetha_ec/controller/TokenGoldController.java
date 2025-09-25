package com.example.saveetha_ec.controller;

import java.math.BigDecimal;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.BuyGoldDTO;
import com.example.saveetha_ec.service.TokenGoldService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;


@RestController
@RequestMapping("api/token")
public class TokenGoldController {
	@Autowired
	private TokenGoldService tokenGoldService;
	
@PostMapping("/buy")
public ResponseEntity<?> buyTokenGold(@RequestParam double amount,@RequestBody BuyGoldDTO buyGoldDTO) throws RazorpayException{
	
		long userId=buyGoldDTO.getUserId();
		BigDecimal grams=buyGoldDTO.getGrams();
		Order order=tokenGoldService.buyGold(userId,amount,grams);
		return ResponseEntity.ok().body(order.get("id"));
	}

}

