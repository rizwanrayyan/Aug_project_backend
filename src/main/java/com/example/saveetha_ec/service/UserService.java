package com.example.saveetha_ec.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.UserDetail;
import com.example.saveetha_ec.repository.UserRepository;
@Service
public class UserService {
	@Autowired
	private UserRepository repo;
	@Autowired
	AuthenticationManager manager;
	@Autowired
	private JwtService jwtService;
	private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);
	
	
	public UserDetail register(UserDetail user) {
		user.setPassword(encoder.encode(user.getPassword()));
		user.setRole("ROLE_USER");
		return repo.save(user);
	}
	
	
	public String verify(UserDetail user) { 
		Authentication authentication = manager.authenticate( new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()) );
		if (authentication.isAuthenticated()) { UserDetails authUser = (UserDetails) authentication.getPrincipal();
		String role = authUser.getAuthorities().iterator().next().getAuthority(); 
		UserDetail userId = repo.findIdByUsername(authUser.getUsername()); 
		return jwtService.generateToken(authUser.getUsername(), role, userId.getId()); } return null; }
	
	

	public List<UserDetail> getUsers() {
		// TODO Auto-generated method stub
		return repo.findAll();
	}

}
