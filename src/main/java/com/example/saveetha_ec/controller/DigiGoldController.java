package com.example.saveetha_ec.controller;

import java.math.BigDecimal;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.BuyGoldDTO;
import com.example.saveetha_ec.model.DigiGoldWallet;
import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.repository.DigiGoldWalletRepo;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
import com.example.saveetha_ec.service.DigiGoldService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.RazorpayException;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("api/gold")
public class DigiGoldController {
@Autowired
private DigiGoldService digiGoldService;
@Autowired
private DigiGoldWalletRepo digiGoldRepo;
private static final String RAZORPAY_WEBHOOK_SECRET="Rizwan@6666";
@Autowired
private OrderAndIdMatchingRepo orderAndIDRepo;


@PostMapping("/buy")
public String getOrderId(@RequestParam double amount,@RequestBody BuyGoldDTO buyGoldDTO) throws RazorpayException{
	long userId=buyGoldDTO.getUserId();
	BigDecimal grams=buyGoldDTO.getGrams();
	Order order=digiGoldService.buyGold(userId,amount,grams);
	return order.get("id");
	
	
}
@PostMapping("/verify")
public ResponseEntity<String> verifyPayment(@RequestHeader("X-Razorpay-Signature") String signature,
		@RequestBody String payload){
	try {
        if (verifySignature(payload, signature)) {
            System.out.println("Webhook Verified: " + payload);
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(payload);
                String orderId = rootNode
                        .path("payload")
                        .path("payment")
                        .path("entity")
                        .path("order_id")
                        .asText();

                // now you have orderId
                System.out.println("Order ID from webhook: " + orderId);
                OrderAndIdMatching orderAndIDMatch=new OrderAndIdMatching();
                orderAndIDMatch=orderAndIDRepo.findByRazorpayOrderId(orderId);
                long userId=orderAndIDMatch.getUserId();
                String paymentId = rootNode
                        .path("payload")
                        .path("payment")
                        .path("entity")
                        .path("id")
                        .asText();
                if("PENDING".equals(orderAndIDMatch.getStatus())){
                orderAndIDMatch.setStatus("CAPTURED");
                orderAndIDMatch.setPaymentId(paymentId);
                orderAndIDRepo.save(orderAndIDMatch);
                }
                DigiGoldWallet wallet = digiGoldRepo.findById(userId)
                        .orElse(new DigiGoldWallet());
                wallet.setId(userId);
                wallet.setGold(wallet.getGold().add(orderAndIDMatch.getGrams()));
                digiGoldRepo.save(wallet);
                

            }
            catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error parsing webhook");
            }
            // TODO: Handle event, update DB for order/payment status
            
            return ResponseEntity.ok("Webhook received successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
    }

	
}
@Transactional
private boolean verifySignature(String payload, String signature) throws Exception {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(RAZORPAY_WEBHOOK_SECRET.getBytes(), "HmacSHA256");
    sha256_HMAC.init(secret_key);
    byte[] hash = sha256_HMAC.doFinal(payload.getBytes());
    String generatedSignature = Base64.getEncoder().encodeToString(hash);
    return generatedSignature.equals(signature);
}

@PostMapping("/paymentid")
public void paymentId(@RequestBody String orderId) throws RazorpayException {
	digiGoldService.getPaymentId(orderId);
}	

}
