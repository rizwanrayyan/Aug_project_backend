package com.example.saveetha_ec.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.saveetha_ec.model.TokenGoldHoldings;

@Repository
public interface TokenGoldHoldingsRepo extends JpaRepository<TokenGoldHoldings, Long>{


    @Query("SELECT g FROM TokenGoldHoldings g WHERE g.userId = :uid")
    Optional<TokenGoldHoldings> findByUserId(@Param("uid") Long userId);
}
