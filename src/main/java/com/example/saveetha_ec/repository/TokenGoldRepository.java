	package com.example.saveetha_ec.repository;
	
	import java.util.List;
	
	import org.springframework.data.jpa.repository.JpaRepository;
	import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.saveetha_ec.model.StatusEnum;
import com.example.saveetha_ec.model.TokenGold;
	
	public interface TokenGoldRepository extends JpaRepository<TokenGold, Long> {
	    @Query("SELECT t FROM TokenGold t " +
	            "WHERE t.userId = :uid AND t.status <> :status " +
	            "ORDER BY t.tokenId ASC")
	     List<TokenGold> findTokensForRedemption(@Param("uid") long userId,
	                                             @Param("status") StatusEnum status);
	}
