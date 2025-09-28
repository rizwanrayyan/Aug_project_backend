package com.example.saveetha_ec.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.saveetha_ec.model.DigiGoldWallet;
import com.example.saveetha_ec.model.StatusEnum;

public interface DigiGoldWalletRepo extends JpaRepository<DigiGoldWallet, Long> {
    @Query("SELECT d FROM DigiGoldWallet d " +
            "WHERE d.userId = :uid AND d.status <> :status " +
            "ORDER BY d.id ASC")
     List<DigiGoldWallet> findGoldForRedemption(@Param("uid") long userId,
                                                @Param("status") StatusEnum status);

}
