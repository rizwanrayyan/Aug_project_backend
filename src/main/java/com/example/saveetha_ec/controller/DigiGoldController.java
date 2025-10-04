package com.example.saveetha_ec.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.BuyGoldDTO;
import com.example.saveetha_ec.model.RedeemRequest;
import com.example.saveetha_ec.service.DigiGoldService;
import com.example.saveetha_ec.service.UserService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;





@RestController
@RequestMapping("api/gold")
public class DigiGoldController {
@Autowired
private DigiGoldService digiGoldService;
@Autowired
private UserService userService;


@PostMapping("/buy")
public ResponseEntity<?> getOrderId(@RequestParam double amount,@RequestBody BuyGoldDTO buyGoldDTO) throws RazorpayException{
	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    long userId=userService.getUserIdByUsername(username);
	BigDecimal grams=buyGoldDTO.getGrams();
	Order order=digiGoldService.buyGold(userId,amount,grams);
	return ResponseEntity.ok(Map.of("OrderId", order.get("id")));


}
@PostMapping("/redeem")
public ResponseEntity<String> redeemCall(@RequestBody RedeemRequest request) {
	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    long userId=userService.getUserIdByUsername(username);
    String result = digiGoldService.redeemGold(userId, request.getGrams());

    if (result.toLowerCase().contains("success")) {
        return ResponseEntity.ok(result);
    } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}

}
