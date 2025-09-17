package com.example.saveetha_ec.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import com.example.saveetha_ec.model.GoldPriceHistory;

@Repository
public interface GoldPriceRepository extends JpaRepository<GoldPriceHistory, Long> {
	@Query(value = "SELECT * FROM gold_price_history ORDER BY timestamp DESC LIMIT 20",nativeQuery = true)
	List<GoldPriceHistory> findLast20Prices();
}
