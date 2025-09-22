package com.example.saveetha_ec.service;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.model.Product;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class TokenGoldService {
	@Autowired
	private OrderAndIdMatchingRepo orderAndIDRepo;
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
	

}
