package com.example.saveetha_ec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.saveetha_ec.model.Redemption;

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    
}