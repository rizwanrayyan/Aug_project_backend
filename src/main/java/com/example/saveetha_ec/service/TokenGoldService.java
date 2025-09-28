package com.example.saveetha_ec.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.model.Product;
import com.example.saveetha_ec.model.StatusEnum;
import com.example.saveetha_ec.model.TokenGold;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
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
	private BlockchainService blockchainService;

	private RazorpayClient razorpay;
	public TokenGoldService() throws Exception{
		this.razorpay=new RazorpayClient("rzp_test_REd2QhwIgG7gTq","A03T9xHF2zXF2CHyZ0NwUQmh");
	}

	public Order buyGold(long userId, double amount, BigDecimal grams) throws RazorpayException {
		int amountInPaise= (int) Math.round(amount * 100);
		 JSONObject orderRequest = new JSONObject();
	     orderRequest.put("amount", amountInPaise);
	     orderRequest.put("currency", "INR");
	     orderRequest.put("payment_capture", 1);
	     OrderAndIdMatching orderIdAndUserID=new OrderAndIdMatching();
	     orderIdAndUserID.setAmount(amount);
	     orderIdAndUserID.setGrams(grams);
	     orderIdAndUserID.setUserId(userId);
	     orderIdAndUserID.setPaymentId("PENDING");
		Order order=razorpay.orders.create(orderRequest);
		orderIdAndUserID.setRazorpayOrderId(order.get("id"));
		orderIdAndUserID.setProductType(Product.TOKEN_GOLD);
		orderAndIDRepo.save(orderIdAndUserID);
		return order;
	}
	
	@Transactional
    public String redeemTokensForUser(long userId, BigDecimal gramsToRedeem) throws Exception {
        // 1. Fetch all active and partially redeemed token batches for the user, ordered oldest first.
        List<TokenGold> userTokens = tokenGoldRepo.findTokensForRedemption(userId, StatusEnum.FULLY_REDEEMED);

        // 2. Verify the user has sufficient balance.
        BigDecimal totalBalance = userTokens.stream()
            .map(TokenGold::getGrams_remaining)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBalance.compareTo(gramsToRedeem) < 0) {
            throw new IllegalArgumentException("Insufficient token balance. You have " + totalBalance + " grams, but tried to redeem " + gramsToRedeem + " grams.");
        }

        // 3. Create a unique data hash for on-chain replay protection.
        String uniqueRedemptionId = UUID.randomUUID().toString();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] dataHash = digest.digest(uniqueRedemptionId.getBytes(StandardCharsets.UTF_8));
        
        // 4. Call the BlockchainService to perform the on-chain burn.
        BigInteger amountWithDecimals = gramsToRedeem.multiply(new BigDecimal("1E18")).toBigInteger();
        String txHash = blockchainService.redeemTokens(userId, amountWithDecimals, dataHash);

        // 5. If the blockchain transaction was successful, update the database using FIFO logic.
        BigDecimal remainingToRedeem = gramsToRedeem;
        for (TokenGold batch : userTokens) {
            if (remainingToRedeem.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal amountFromBatch = batch.getGrams_remaining().min(remainingToRedeem);
            batch.setGrams_remaining(batch.getGrams_remaining().subtract(amountFromBatch));
            
            if (batch.getGrams_remaining().compareTo(BigDecimal.ZERO) == 0) {
                batch.setStatus(StatusEnum.FULLY_REDEEMED);
            } else {
                batch.setStatus(StatusEnum.PARTIALLY_REDEEMED);
            }
            tokenGoldRepo.save(batch);
            remainingToRedeem = remainingToRedeem.subtract(amountFromBatch);
        }

        // (Optional but recommended) You can create a new record in OrderAndIdMatching for this redemption.
        
        return txHash;
    }
}
