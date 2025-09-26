package com.example.saveetha_ec.webhookupdate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.saveetha_ec.model.DigiGoldWallet;
import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.model.Product;
import com.example.saveetha_ec.model.StatusEnum;
import com.example.saveetha_ec.model.TokenGold;
import com.example.saveetha_ec.repository.DigiGoldWalletRepo;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
import com.example.saveetha_ec.repository.TokenGoldRepository;
import com.example.saveetha_ec.service.BlockchainService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
@RestController
@RequestMapping("/api/webhook")
public class Webhook_verify_update {


	@Autowired
	private OrderAndIdMatchingRepo orderAndIDRepo;
	@Autowired
	private TokenGoldRepository tokenGoldRepo;
    @Autowired
    private BlockchainService blockchainService;
    @Autowired
    private DigiGoldWalletRepo digiGoldRepo;

	private final String RAZORPAY_WEBHOOK_SECRET="Rizwan@6666";

@PostMapping("/veri")
@Transactional
    public ResponseEntity<String> verifyPayment(@RequestHeader("X-Razorpay-Signature") String signature,
                                                @RequestBody String payload) {
        try {
        	System.out.println("getting into webhook verify");
            if (verifySignature(payload, signature)) {
                System.out.println("Webhook Verified: " + payload);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(payload);
                String orderId = rootNode.path("payload").path("payment").path("entity").path("order_id").asText();
                String paymentId = rootNode.path("payload").path("payment").path("entity").path("id").asText();

                OrderAndIdMatching orderAndIDMatch = orderAndIDRepo.findByRazorpayOrderId(orderId);

                if (orderAndIDMatch == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
                }
                Product type=orderAndIDMatch.getProductType();

                // --- Start of Blockchain Integration ---
                if ("PENDING".equals(orderAndIDMatch.getStatus()) && type.equals(Product.TOKEN_GOLD)) {
                    orderAndIDMatch.setStatus("CAPTURED");
                    orderAndIDMatch.setPaymentId(paymentId);
                    orderAndIDRepo.save(orderAndIDMatch);

                    // 1. Prepare data for minting
                    long userId = orderAndIDMatch.getUserId();
                    BigDecimal grams = orderAndIDMatch.getGrams();
                    BigInteger amountWithDecimals = grams.multiply(new BigDecimal("1E18")).toBigInteger();

                    // 2. Create a unique data hash for the transaction
                    String hashInput = orderId + ":" + paymentId;
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] dataHash = digest.digest(hashInput.getBytes(StandardCharsets.UTF_8));

                    // 3. Call the blockchain service to mint tokens
                    System.out.println("going to call blockchain service");
                    String txHash = blockchainService.mintTokens(userId, amountWithDecimals, dataHash);
                    System.out.println("Minted tokens on-chain. Transaction Hash: " + txHash);

                    // 4. Save the token details with the transaction hash to your DB
                    TokenGold tokenGold = new TokenGold();
                    tokenGold.setUserId(userId);
                    tokenGold.setGrams_purchased(grams);
                    tokenGold.setGrams_remaining(grams);
                    tokenGold.setPurchase_rate(orderAndIDMatch.getAmount());
                    tokenGold.setTransaction_hash(txHash); // Save the blockchain hash
                    tokenGold.setVaultId("VAULT0001"); // Example vault ID
                    tokenGold.setStatus(StatusEnum.ACTIVE);
                    tokenGoldRepo.save(tokenGold);
                }
                else if("PENDING".equals(orderAndIDMatch.getStatus()) && type.equals(Product.DIGITAL_GOLD)) {
                	orderAndIDMatch.setStatus("CAPTURED");
                    orderAndIDMatch.setPaymentId(paymentId);
                    orderAndIDRepo.save(orderAndIDMatch);
                    long userId=orderAndIDMatch.getUserId();
                    DigiGoldWallet wallet = digiGoldRepo.findById(userId)
                            .orElse(new DigiGoldWallet());
                    wallet.setId(userId);
                    wallet.setGold(wallet.getGold().add(orderAndIDMatch.getGrams()));
                    digiGoldRepo.save(wallet);
                }

                // --- End of Blockchain Integration ---

                return ResponseEntity.ok("Webhook received and processed successfully.");

            }

            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook: " + e.getMessage());
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
