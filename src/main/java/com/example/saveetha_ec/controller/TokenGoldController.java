package com.example.saveetha_ec.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.BuyGoldDTO; // Assuming you'll rename your RedeemGoldDTO
import com.example.saveetha_ec.model.RedeemRequest;
import com.example.saveetha_ec.model.UserDetailsPrinciple;
import com.example.saveetha_ec.service.TokenGoldService;
import com.example.saveetha_ec.service.UserService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;


@RestController
@RequestMapping("api/token")
public class TokenGoldController {
	@Autowired
	private TokenGoldService tokenGoldService;
	@Autowired
	private UserService userService;

	@PostMapping("/buy")
	public ResponseEntity<?> buyTokenGold(@RequestParam double amount,@RequestBody BuyGoldDTO buyGoldDTO) throws RazorpayException{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();
	    long userId=userService.getUserIdByUsername(username);
		BigDecimal grams=buyGoldDTO.getGrams();
		Order order=tokenGoldService.buyGold(userId,amount,grams);
		return ResponseEntity.ok().body(order.get("id"));
	}

	@PostMapping("/redeem")
    public ResponseEntity<?> redeemTokenGold(@RequestBody RedeemRequest redeemRequest) {
        try {
            // Get the authenticated user's ID from the JWT token.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsPrinciple userDetails = (UserDetailsPrinciple) authentication.getPrincipal();
            // You may need to add getUserId() to your UserDetailsPrinciple class
            long userId = userDetails.getId();

            // Call the service to handle the redemption.
            String txHash = tokenGoldService.redeemTokensForUser(userId, redeemRequest);

            return ResponseEntity.ok().body(Map.of(
                "message", "Redemption processed successfully",
                "transactionHash", txHash
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An internal error occurred during redemption."));
        }
    }
}

