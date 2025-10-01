package com.example.saveetha_ec.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.saveetha_ec.model.OrderAndIdMatching;

@Repository
public interface TransactionRepo extends JpaRepository<OrderAndIdMatching, Long> {

	 @Query("SELECT o FROM OrderAndIdMatching o " +
	           "WHERE o.transactionType = 'BUY' AND o.userId = :userId AND o.status='CAPTURED' " +
	           "ORDER BY o.id ASC")
	    Page<OrderAndIdMatching> findPurchaseOrdersByUserId(@Param("userId") long userId,
	                                                       Pageable pageable);
	 @Query("SELECT o FROM OrderAndIdMatching o " +
	           "WHERE o.transactionType = 'REDEEM' AND o.userId = :userId AND o.status='SUCCESS' " +
	           "ORDER BY o.id ASC")
	    Page<OrderAndIdMatching> findRedeemOrdersByUserId(@Param("userId") long userId,
	                                                       Pageable pageable);
}
