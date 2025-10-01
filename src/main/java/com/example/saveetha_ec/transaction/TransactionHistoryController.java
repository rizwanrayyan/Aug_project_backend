package com.example.saveetha_ec.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.OrderAndIdMatching;
import com.example.saveetha_ec.service.TransactionService;
import com.example.saveetha_ec.service.UserService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionHistoryController {
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private UserService userService;
	@GetMapping("/purchase")
	public ResponseEntity<Page<OrderAndIdMatching>> getPurchaseHistory(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName(); // or userId if stored in token

	    // now fetch the user from DB based on username
	    long userId = userService.getUserIdByUsername(username);

	    Page<OrderAndIdMatching> purchases = transactionService.getPurchaseOrders(userId, PageRequest.of(page, size));
	    return ResponseEntity.ok(purchases);
	}
	@GetMapping("/redeem")
	public ResponseEntity<Page<OrderAndIdMatching>> getRedeemHistory(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName(); // or userId if stored in token

	    // now fetch the user from DB based on username
	    long userId = userService.getUserIdByUsername(username);

	    Page<OrderAndIdMatching> purchases = transactionService.getRedeemOrders(userId, PageRequest.of(page, size));
	    return ResponseEntity.ok(purchases);
	}
}
