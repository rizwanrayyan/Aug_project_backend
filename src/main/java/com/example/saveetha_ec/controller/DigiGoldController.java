package com.example.saveetha_ec.controller;

import java.math.BigDecimal;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.BuyGoldDTO;

import com.example.saveetha_ec.service.DigiGoldService;

import com.razorpay.Order;
import com.razorpay.RazorpayException;



@RestController
@RequestMapping("api/gold")
public class DigiGoldController {
@Autowired
private DigiGoldService digiGoldService;



@PostMapping("/buy")
public String getOrderId(@RequestParam double amount,@RequestBody BuyGoldDTO buyGoldDTO) throws RazorpayException{
	long userId=buyGoldDTO.getUserId();
	BigDecimal grams=buyGoldDTO.getGrams();
	Order order=digiGoldService.buyGold(userId,amount,grams);
	return order.get("id");
	
	
}

}
