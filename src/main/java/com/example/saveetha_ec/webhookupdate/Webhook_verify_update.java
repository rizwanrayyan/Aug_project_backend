package com.example.saveetha_ec.webhookupdate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Formatter;

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

    @PostMapping("/ver")
    @Transactional
    public ResponseEntity<String> verifyPayment(@RequestHeader("X-Razorpay-Signature") String signature,
                                                @RequestBody String payload) {
        try {
            if (verifySignature(payload, signature)) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(payload);
                String orderId = rootNode.path("payload").path("payment").path("entity").path("order_id").asText();
                String paymentId = rootNode.path("payload").path("payment").path("entity").path("id").asText();
                OrderAndIdMatching orderAndIDMatch = orderAndIDRepo.findByRazorpayOrderId(orderId);

                if (orderAndIDMatch == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
                }
                
                Product type = orderAndIDMatch.getProductType();

                if ("PENDING".equals(orderAndIDMatch.getStatus()) && type.equals(Product.TOKEN_GOLD)) {
                    orderAndIDMatch.setStatus("CAPTURED");
                    orderAndIDMatch.setPaymentId(paymentId);
                    
                    long userId = orderAndIDMatch.getUserId();
                    BigDecimal grams = orderAndIDMatch.getGrams();
                    BigInteger amountWithDecimals = grams.multiply(new BigDecimal("1E18")).toBigInteger();
                    double rateOfPurchase = orderAndIDMatch.getAmount();
                    long acquisitionTimestamp = orderAndIDMatch.getCreatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                    String hashInput = "" + grams.toPlainString() + rateOfPurchase + acquisitionTimestamp;
                    
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] dataHashBytes = digest.digest(hashInput.getBytes(StandardCharsets.UTF_8));
                    String dataHashHex = bytesToHex(dataHashBytes);

                    // UPDATED: mintTokens now returns the unique batchId
                    BlockchainService.MintingResult mint = blockchainService.mintTokens(userId, amountWithDecimals, dataHashBytes);
                    orderAndIDMatch.setTxHash(mint.getTransactionHash());
                    orderAndIDRepo.save(orderAndIDMatch);

                    TokenGold tokenGold = new TokenGold();
                    tokenGold.setUserId(userId);
                    tokenGold.setGrams_purchased(grams);
                    tokenGold.setGrams_remaining(grams);
                    tokenGold.setPurchase_rate(orderAndIDMatch.getAmount());
                    tokenGold.setData_hash(dataHashHex);
                    
                    // --- CRITICAL UPDATE: Store the unique batchId ---
                    tokenGold.setBatchId(mint.getBatchId());
                    // --- END OF UPDATE ---
                    
                    tokenGold.setVaultId("VAULT0001");
                    tokenGold.setStatus(StatusEnum.ACTIVE);
                    tokenGold.setDateOfAcquisation(orderAndIDMatch.getCreatedAt());
                    tokenGoldRepo.save(tokenGold);

                } else if("PENDING".equals(orderAndIDMatch.getStatus()) && type.equals(Product.DIGITAL_GOLD)) {
                    // ... digital gold logic remains the same ...
                    orderAndIDMatch.setStatus("CAPTURED");
                    orderAndIDMatch.setPaymentId(paymentId);
                    orderAndIDRepo.save(orderAndIDMatch);
                    long userId=orderAndIDMatch.getUserId();
                    DigiGoldWallet wallet = new DigiGoldWallet();
                    wallet.setUserId(userId);
                    wallet.setGramsPurchased(orderAndIDMatch.getGrams());
                    wallet.setGramsRemaining(orderAndIDMatch.getGrams());
                    wallet.setPurchaseRate(orderAndIDMatch.getAmount());
                    wallet.setAcquisitionDate(LocalDate.now());
                    wallet.setStatus(StatusEnum.ACTIVE);
                    digiGoldRepo.save(wallet);
                }

                return ResponseEntity.ok("Webhook received and processed successfully.");

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook: " + e.getMessage());
        }
    }

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
    
    private String bytesToHex(byte[] hash) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }
}
