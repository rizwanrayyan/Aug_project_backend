package com.example.saveetha_ec.controller;



import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.UserDetail;
import com.example.saveetha_ec.model.loginDetail;
import com.example.saveetha_ec.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
	@Autowired
	private UserService userService;

@PostMapping("/signup")
public ResponseEntity<?> signUp(@RequestBody UserDetail user){
    try {
        UserDetail savedUser = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED) // 201 Created
                             .body(Map.of("message", "Successfully signed up!",
                                          "username", savedUser.getUsername()));
    } catch (DataIntegrityViolationException e) {
        // Example: duplicate username or email (unique constraint violation)
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(Map.of("error", "Username already exists"));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(Map.of("error", e.getMessage())); // e.g. "Password too weak"
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("error", "Something went wrong"));
    }

}
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody loginDetail login){
	UserDetail user=new UserDetail();
	user.setUsername(login.getUsername());
	user.setPassword(login.getPassword());
	System.out.println(login.getUsername());
	System.out.println(login.getPassword());
	String token=userService.verify(user);
	if(token!=null) {
	return ResponseEntity.status(200).body(Map.of("token",token));
	}
	return ResponseEntity.status(401).body("Invalid username or password!");
}
@GetMapping("/profile")
public ResponseEntity<?> profile(){
	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    long userId=userService.getUserIdByUsername(username);
    UserDetail user=userService.findByUserId(userId);
    if(user!=null)
	return ResponseEntity.ok(Map.of("userDetails",user));
    else
    	return ResponseEntity.status(400).body("invalid user!");

}
}
