package com.example.saveetha_ec.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.saveetha_ec.model.OrderAndIdMatching;

public interface OrderAndIdMatchingRepo extends JpaRepository<OrderAndIdMatching, Long> {

	public OrderAndIdMatching findByRazorpayOrderId(String razorpayOrderId);
}
