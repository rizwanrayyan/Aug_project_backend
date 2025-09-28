package com.example.saveetha_ec.service;



import java.math.BigDecimal;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.DigiGoldWallet;
import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.model.Product;
import com.example.saveetha_ec.model.StatusEnum;
import com.example.saveetha_ec.repository.DigiGoldWalletRepo;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.transaction.Transactional;


@Service
public class DigiGoldService {
	@Autowired
	private OrderAndIdMatchingRepo orderAndIDRepo;
	@Autowired
	private DigiGoldWalletRepo repo;
	private RazorpayClient razorpay;

	public DigiGoldService() throws Exception{
		this.razorpay=new RazorpayClient("rzp_test_REd2QhwIgG7gTq","A03T9xHF2zXF2CHyZ0NwUQmh");
	}
public Order buyGold(long userId,double amount,BigDecimal grams) throws RazorpayException {
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
	orderIdAndUserID.setProductType(Product.DIGITAL_GOLD);
	orderAndIDRepo.save(orderIdAndUserID);
	return order;
}
@Transactional
public String redeemGold(long userId, BigDecimal gramsToRedeem) {
    List<DigiGoldWallet> wallets = repo.findGoldForRedemption(userId, StatusEnum.FULLY_REDEEMED);

    if (wallets.isEmpty()) {
        return "No balance available for redemption.";
    }

    BigDecimal remainingToRedeem = gramsToRedeem;

    for (DigiGoldWallet wallet : wallets) {
        BigDecimal available = wallet.getGramsRemaining();

        if (available.compareTo(remainingToRedeem) >= 0) {
            // Case 1: Current wallet has enough to cover redemption
            wallet.setGramsRemaining(available.subtract(remainingToRedeem));

            if (wallet.getGramsRemaining().compareTo(BigDecimal.ZERO) == 0) {
                wallet.setStatus(StatusEnum.FULLY_REDEEMED);
            }

            repo.save(wallet);
            return "Redeemed successfully.";
        } else {
            // Case 2: Redeem all from this wallet and continue
            remainingToRedeem = remainingToRedeem.subtract(available);
            wallet.setGramsRemaining(BigDecimal.ZERO);
            wallet.setStatus(StatusEnum.FULLY_REDEEMED);
            repo.save(wallet);
        }
    }

    // If we exit the loop and still have redemption left → not enough balance
    if (remainingToRedeem.compareTo(BigDecimal.ZERO) > 0) {
        return "Not enough balance to redeem the requested grams.";
    }

    return "Redeemed successfully.";
}

}
