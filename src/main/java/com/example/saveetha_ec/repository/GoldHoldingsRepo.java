package com.example.saveetha_ec.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.saveetha_ec.model.GoldHoldings;


@Repository
public interface GoldHoldingsRepo extends JpaRepository<GoldHoldings, Long> {

    @Query("SELECT g FROM GoldHoldings g WHERE g.userId = :uid")
    Optional<GoldHoldings> findByUserId(@Param("uid") Long userId);

}
