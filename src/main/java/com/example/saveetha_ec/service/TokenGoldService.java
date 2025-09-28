package com.example.saveetha_ec.service;

import com.example.saveetha_ec.model.*;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
import com.example.saveetha_ec.repository.RedemptionRepository;
import com.example.saveetha_ec.repository.TokenGoldRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class TokenGoldService {

    @Autowired
    private OrderAndIdMatchingRepo orderAndIDRepo;
    
    @Autowired
    private TokenGoldRepository tokenGoldRepo;

    @Autowired
    private RedemptionRepository redemptionRepo; // Injected the new repository

    @Autowired
    private BlockchainService blockchainService;

    private RazorpayClient razorpay;

    public TokenGoldService() throws Exception {
        this.razorpay = new RazorpayClient("rzp_test_REd2QhwIgG7gTq", "A03T9xHF2zXF2CHyZ0NwUQmh");
    }

    public Order buyGold(long userId, double amount, BigDecimal grams) throws RazorpayException {
        int amountInPaise = (int) Math.round(amount * 100);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("payment_capture", 1);

        OrderAndIdMatching orderIdAndUserID = new OrderAndIdMatching();
        orderIdAndUserID.setAmount(amount);
        orderIdAndUserID.setGrams(grams);
        orderIdAndUserID.setUserId(userId);
        orderIdAndUserID.setPaymentId("PENDING");
        orderIdAndUserID.setStatus("PENDING");
        orderIdAndUserID.setProductType(Product.TOKEN_GOLD);
        orderIdAndUserID.setTransactionType(TransactionType.BUY); // Set transaction type

        Order order = razorpay.orders.create(orderRequest);
        orderIdAndUserID.setRazorpayOrderId(order.get("id"));
        orderAndIDRepo.save(orderIdAndUserID);
        return order;
    }

    @Transactional
    public String redeemTokensForUser(long userId, RedeemRequest redeemRequest) throws Exception {
        BigDecimal gramsToRedeem = redeemRequest.getGrams();
        BigDecimal redemptionRate = redeemRequest.getRedemptionRate();

        // 1. Fetch all active and partially redeemed tokens for the user (FIFO)
        List<TokenGold> userTokens = tokenGoldRepo.findTokensForRedemption(userId, StatusEnum.FULLY_REDEEMED);

        // 2. Verify sufficient balance
        BigDecimal totalBalance = userTokens.stream()
            .map(TokenGold::getGrams_remaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBalance.compareTo(gramsToRedeem) < 0) {
            throw new IllegalArgumentException("Insufficient token balance. You have " + totalBalance + " grams, but tried to redeem " + gramsToRedeem + " grams.");
        }

        // 3. Create a transaction record for this redemption
        OrderAndIdMatching redemptionOrder = new OrderAndIdMatching();
        redemptionOrder.setUserId(userId);
        redemptionOrder.setGrams(gramsToRedeem);
        redemptionOrder.setAmount(gramsToRedeem.multiply(redemptionRate).doubleValue());
        redemptionOrder.setTransactionType(TransactionType.REDEEM);
        redemptionOrder.setProductType(Product.TOKEN_GOLD);
        redemptionOrder.setStatus("PROCESSING");
        redemptionOrder.setPaymentId("N/A"); // No payment ID for redemptions
        redemptionOrder.setRazorpayOrderId("N/A");
        OrderAndIdMatching savedOrder = orderAndIDRepo.save(redemptionOrder);


        // 4. Call the BlockchainService to perform the on-chain burn
        BigInteger amountWithDecimals = gramsToRedeem.multiply(new BigDecimal("1E18")).toBigInteger();
        
        // The dataHash for redemption is created in the blockchain service now.
        // It's better to use a unique hash for each redemption transaction.
        String txHash = blockchainService.redeemTokens(userId, amountWithDecimals);


        // 5. If blockchain tx is successful, update the database using FIFO logic
        BigDecimal remainingToRedeem = gramsToRedeem;
        for (TokenGold batch : userTokens) {
            if (remainingToRedeem.compareTo(BigDecimal.ZERO) <= 0) break;
            
            // --- Data Hash Verification ---
            String expectedHashInput = "" + batch.getGrams_purchased() + batch.getPurchase_rate() + batch.getDateOfAcquisation();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] rehashedBytes = digest.digest(expectedHashInput.getBytes(StandardCharsets.UTF_8));
            String rehashedHex = bytesToHex(rehashedBytes);

            // Compare re-calculated hash with the one stored from the webhook
            if (!Objects.equals(rehashedHex, batch.getData_hash())) {
                 // You can decide how to handle this - throw an exception, log an alert, etc.
                 throw new SecurityException("Data hash mismatch for token ID " + batch.getTokenId() + ". Aborting redemption.");
            }
            
            BigDecimal amountFromBatch = batch.getGrams_remaining().min(remainingToRedeem);

            // --- Create Redemption Record for Tax Reporting ---
            Redemption redemptionRecord = new Redemption();
            redemptionRecord.setUserId(userId);
            redemptionRecord.setOrderId(savedOrder.getId()); // Link to the master transaction
            redemptionRecord.setTokenId(batch.getTokenId());
            redemptionRecord.setGramsRedeemed(amountFromBatch);
            redemptionRecord.setPurchaseRate(BigDecimal.valueOf(batch.getPurchase_rate()));
            redemptionRecord.setRedemptionRate(redemptionRate);
            redemptionRecord.setRedemptionDate(LocalDateTime.now());

            // Calculate tax fields
            BigDecimal costBasis = redemptionRecord.getPurchaseRate().multiply(amountFromBatch);
            BigDecimal redemptionValue = redemptionRate.multiply(amountFromBatch);
            BigDecimal gain = redemptionValue.subtract(costBasis);
            
            redemptionRecord.setCostBasis(costBasis);
            redemptionRecord.setRedemptionValue(redemptionValue);
            redemptionRecord.setGain(gain);
            
            // Determine gain type (LTCG/STCG) - assuming 1 year holding period
            long daysHeld = Duration.between(batch.getDateOfAcquisation(), redemptionRecord.getRedemptionDate()).toDays();
            redemptionRecord.setGainType(daysHeld > 365 ? "LTCG" : "STCG");

            redemptionRepo.save(redemptionRecord);


            // --- Update the Token Batch ---
            batch.setGrams_remaining(batch.getGrams_remaining().subtract(amountFromBatch));
            if (batch.getGrams_remaining().compareTo(BigDecimal.ZERO) == 0) {
                batch.setStatus(StatusEnum.FULLY_REDEEMED);
            } else {
                batch.setStatus(StatusEnum.PARTIALLY_REDEEMED);
            }
            tokenGoldRepo.save(batch);

            remainingToRedeem = remainingToRedeem.subtract(amountFromBatch);
        }
        
        // Mark the master transaction as successful
        savedOrder.setStatus("SUCCESS");
        savedOrder.setTxHash(txHash); // Optionally store the hash here too
        orderAndIDRepo.save(savedOrder);
        
        return txHash;
    }

    // Helper method to convert byte array to hex string for comparison
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}