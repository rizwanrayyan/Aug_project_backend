package com.example.saveetha_ec.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.model.Product;
import com.example.saveetha_ec.model.RedeemRequest;
import com.example.saveetha_ec.model.Redemption;
import com.example.saveetha_ec.model.StatusEnum;
import com.example.saveetha_ec.model.TokenGold;
import com.example.saveetha_ec.model.TransactionType;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
import com.example.saveetha_ec.repository.RedemptionRepository;
import com.example.saveetha_ec.repository.TokenGoldRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.transaction.Transactional;

@Service
public class TokenGoldService {

    @Autowired
    private OrderAndIdMatchingRepo orderAndIDRepo;
    
    @Autowired
    private TokenGoldRepository tokenGoldRepo;

    @Autowired
    private RedemptionRepository redemptionRepo;

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
        orderIdAndUserID.setTransactionType(TransactionType.BUY);
        Order order = razorpay.orders.create(orderRequest);
        orderIdAndUserID.setRazorpayOrderId(order.get("id"));
        orderAndIDRepo.save(orderIdAndUserID);
        return order;
    }

    @Transactional
    public String redeemTokensForUser(long userId, RedeemRequest redeemRequest) throws Exception {
        BigDecimal gramsToRedeem = redeemRequest.getGrams();
        BigDecimal redemptionRate = redeemRequest.getRedemptionRate();

        List<TokenGold> userTokens = tokenGoldRepo.findTokensForRedemption(userId, StatusEnum.FULLY_REDEEMED);

        BigDecimal totalBalance = userTokens.stream()
            .map(TokenGold::getGrams_remaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBalance.compareTo(gramsToRedeem) < 0) {
            throw new IllegalArgumentException("Insufficient token balance. You have " + totalBalance + " grams, but tried to redeem " + gramsToRedeem + " grams.");
        }

        OrderAndIdMatching redemptionOrder = new OrderAndIdMatching();
        redemptionOrder.setUserId(userId);
        redemptionOrder.setGrams(gramsToRedeem);
        redemptionOrder.setAmount(gramsToRedeem.multiply(redemptionRate).doubleValue());
        redemptionOrder.setTransactionType(TransactionType.REDEEM);
        redemptionOrder.setProductType(Product.TOKEN_GOLD);
        redemptionOrder.setStatus("PROCESSING");
        redemptionOrder.setPaymentId("N/A");
        redemptionOrder.setRazorpayOrderId("N/A");
        OrderAndIdMatching savedOrder = orderAndIDRepo.save(redemptionOrder);

        // --- UPDATED LOGIC ---
        // 1. Call the blockchain to burn the total amount. The contract handles FIFO.
        BigInteger amountWithDecimals = gramsToRedeem.multiply(new BigDecimal("1E18")).toBigInteger();
        String txHash = blockchainService.redeemGold(userId, amountWithDecimals);
        
        // 2. If the on-chain transaction succeeds, update the off-chain database records.
        BigDecimal remainingToRedeem = gramsToRedeem;
        for (TokenGold batch : userTokens) {
            if (remainingToRedeem.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal amountToRedeemFromBatch = batch.getGrams_remaining().min(remainingToRedeem);

            Redemption redemptionRecord = new Redemption();
            redemptionRecord.setUserId(userId);
            redemptionRecord.setOrderId(savedOrder.getId());
            redemptionRecord.setTokenId(batch.getTokenId());
            redemptionRecord.setGramsRedeemed(amountToRedeemFromBatch);
            redemptionRecord.setPurchaseRate(BigDecimal.valueOf(batch.getPurchase_rate()));
            redemptionRecord.setRedemptionRate(redemptionRate);
            redemptionRecord.setRedemptionDate(LocalDateTime.now());
            BigDecimal costBasis = redemptionRecord.getPurchaseRate().multiply(amountToRedeemFromBatch);
            BigDecimal redemptionValue = redemptionRate.multiply(amountToRedeemFromBatch);
            BigDecimal gain = redemptionValue.subtract(costBasis);
            redemptionRecord.setCostBasis(costBasis);
            redemptionRecord.setRedemptionValue(redemptionValue);
            redemptionRecord.setGain(gain);
            long daysHeld = Duration.between(batch.getDateOfAcquisation(), redemptionRecord.getRedemptionDate()).toDays();
            redemptionRecord.setGainType(daysHeld > 365 ? "LTCG" : "STCG");
            redemptionRepo.save(redemptionRecord);

            batch.setGrams_remaining(batch.getGrams_remaining().subtract(amountToRedeemFromBatch));
            if (batch.getGrams_remaining().compareTo(BigDecimal.ZERO) == 0) {
                batch.setStatus(StatusEnum.FULLY_REDEEMED);
            } else {
                batch.setStatus(StatusEnum.PARTIALLY_REDEEMED);
            }
            tokenGoldRepo.save(batch);

            remainingToRedeem = remainingToRedeem.subtract(amountToRedeemFromBatch);
        }
        
        savedOrder.setStatus("SUCCESS");
        savedOrder.setTxHash(txHash);
        orderAndIDRepo.save(savedOrder);
        
        return txHash;
    }
}