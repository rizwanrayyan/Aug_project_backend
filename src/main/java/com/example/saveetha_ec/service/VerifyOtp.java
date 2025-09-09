package com.example.saveetha_ec.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VerifyOtp {
@Autowired
private JavaMailSender mailSender;
@Autowired
private StringRedisTemplate redisTemplate;
public String createOtp() {
	return String.valueOf(100000+new Random().nextInt(900000));
}
public void sendEmail(String senderEmail) {
	String otp=createOtp();
	redisTemplate.opsForValue().set("EMAIL_OTP"+senderEmail,otp,5,TimeUnit.MINUTES);
	System.out.println(senderEmail);
	SimpleMailMessage mail=new SimpleMailMessage();
	mail.setTo(senderEmail);
	mail.setSubject("**OTP Verification !**");
	mail.setText("Your OTP is"+otp+". It will expire in 5 mins");
	mailSender.send(mail);
	
}
public boolean verifyMailOtp(String inputOtp,String email) {
	String key="EMAIL_OTP"+email;
	String storedOtp=(String)redisTemplate.opsForValue().get(key);
	if(storedOtp!=null && storedOtp.equals(inputOtp)) {
		redisTemplate.delete(key);
		return true;
	}
	return false;
}

}
