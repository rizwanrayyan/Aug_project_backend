package com.example.saveetha_ec.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.repository.TransactionRepo;

@Service
public class TransactionService {
	@Autowired
	private TransactionRepo transactionRepo;

	public Page<OrderAndIdMatching> getPurchaseOrders(long userId, PageRequest of) {

		return transactionRepo.findPurchaseOrdersByUserId(userId,of);
	}
	public Page<OrderAndIdMatching> getRedeemOrders(long userId, PageRequest of) {

		return transactionRepo.findRedeemOrdersByUserId(userId,of);
	}


}
