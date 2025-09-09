package com.example.saveetha_ec.service;



import java.math.BigDecimal;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.repository.OrderAndIdMatchingRepo;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;


@Service
public class DigiGoldService {
	@Autowired
	private OrderAndIdMatchingRepo orderAndIDRepo;

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
	orderAndIDRepo.save(orderIdAndUserID);
	return order;
}
public void getPaymentId(String orderId) throws RazorpayException {
	JSONObject paymentRequest = new JSONObject();
	paymentRequest.put("amount", 0000); // in paise
	paymentRequest.put("currency", "INR");
	paymentRequest.put("order_id", "order_ABC123XYZ"); // your existing order
	paymentRequest.put("method", "card"); // test mode
	paymentRequest.put("card", new JSONObject()
	        .put("number", "4111111111111111")
	        .put("expiry_month", 12)
	        .put("expiry_year", 34)
	        .put("cvv", "123")
	);

	Payment payment = razorpay.payments.createJsonPayment(paymentRequest);
	System.out.println("Payment ID: " + payment.get("id"));
}
}
