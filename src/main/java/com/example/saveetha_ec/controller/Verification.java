package com.example.saveetha_ec.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.saveetha_ec.model.EmailSendDTO;
import com.example.saveetha_ec.model.VerificationDTO;
import com.example.saveetha_ec.service.VerifyOtp;

@RestController
@RequestMapping("api/verify")
public class Verification {
	@Autowired
	private VerifyOtp verify;
	
	@PostMapping("/send")
	public void sendEmailOtp(@RequestBody EmailSendDTO send){
		verify.sendEmail(send.getEmail());
	}

	@PostMapping("/verify")
	public ResponseEntity<?> verifyOtp(@RequestBody VerificationDTO verifyDTO){
		boolean status=verify.verifyMailOtp(verifyDTO.getOtp(), verifyDTO.getEmail());
		if(status)
		return ResponseEntity.status(200).body(Map.of("success",true));
		return ResponseEntity.status(400).body(Map.of("success",false));
		
	}
}
