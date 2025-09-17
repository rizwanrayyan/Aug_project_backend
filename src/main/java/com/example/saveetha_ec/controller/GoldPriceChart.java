package com.example.saveetha_ec.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.GoldPriceHistory;
import com.example.saveetha_ec.service.GoldPriceService;


@RestController
@RequestMapping("api/goldprice")
public class GoldPriceChart {
@Autowired
private GoldPriceService goldPriceService;
@GetMapping("/last20days")
public ResponseEntity<?> get20daysGoldPrice(){
	List<GoldPriceHistory> goldPrice=goldPriceService.get20daysGoldPrice();
	if(goldPrice!=null) {
		return ResponseEntity.status(200).body(goldPrice);
	}
	return ResponseEntity.status(401).body("Can't find data!");
}


}
