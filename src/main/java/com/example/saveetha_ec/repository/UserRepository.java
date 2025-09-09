package com.example.saveetha_ec.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.saveetha_ec.model.UserDetail;

public interface UserRepository extends JpaRepository<UserDetail, Long> {
	public UserDetail findByUsername(String username);
	public UserDetail findIdByUsername(String username); 

}
