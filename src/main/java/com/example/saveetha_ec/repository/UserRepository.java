package com.example.saveetha_ec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.saveetha_ec.model.UserDetail;

public interface UserRepository extends JpaRepository<UserDetail, Long> {
	public UserDetail findByUsername(String username);
	public UserDetail findIdByUsername(String username);
	@Query("select u.id from UserDetail u where u.username = :uname")
	long findUserIdByUsername(@Param("uname") String username);


}
