package com.example.saveetha_ec.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.saveetha_ec.model.UserDetail;
import com.example.saveetha_ec.repository.UserRepository;
import com.example.saveetha_ec.model.UserDetailsPrinciple;
@Service
public class MyUserDetailService implements UserDetailsService {
	@Autowired
	private UserRepository repo;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		UserDetail user=repo.findByUsername(username);
		 if (user == null) {
		        throw new UsernameNotFoundException("User not found with username: " + username);
		    }
		return new UserDetailsPrinciple(user);
	}

}
