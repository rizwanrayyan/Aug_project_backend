package com.example.saveetha_ec.controller;

import java.math.BigDecimal;

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

import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.model.TokenGold;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
import com.example.saveetha_ec.repository.TokenGoldRepository;
import com.example.saveetha_ec.service.TokenGoldService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.RazorpayException;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("api/token")
public class TokenGoldController {
	@Autowired
	private TokenGoldService tokenGoldService;
	@Autowired
	private OrderAndIdMatchingRepo orderAndIDRepo;
	@Autowired
	private TokenGoldRepository tokenGoldRepo;
	
	private final String RAZORPAY_WEBHOOK_SECRET="Rizwan@6666";
@PostMapping("/buy")
public ResponseEntity<?> buyTokenGold(@RequestParam double amount,@RequestBody BuyGoldDTO buyGoldDTO) throws RazorpayException{
	
		long userId=buyGoldDTO.getUserId();
		BigDecimal grams=buyGoldDTO.getGrams();
		Order order=tokenGoldService.buyGold(userId,amount,grams);
		return ResponseEntity.ok().body(order.get("id"));
	}
@PostMapping("/verify")
@Transactional
public ResponseEntity<String> verifyPayment(@RequestHeader("X-Razorpay-Signature") String signature,
		@RequestBody String payload){
	try {
		System.out.println("Webhook called !");
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
                TokenGold tokenGold=new TokenGold();
                tokenGold.setUserId(userId);
                tokenGold.setGrams_purchased(orderAndIDMatch.getGrams());
                tokenGold.setGrams_remaining(orderAndIDMatch.getGrams());
                tokenGold.setPurchase_rate(orderAndIDMatch.getAmount());
                tokenGold.setVaultId("VAULT0001");
                tokenGoldRepo.save(tokenGold);
                

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
    StringBuilder sb = new StringBuilder();
    for (byte b : hash) {
        sb.append(String.format("%02x", b));
    }
    String generatedSignature = sb.toString();
    return generatedSignature.equals(signature);
}

}

