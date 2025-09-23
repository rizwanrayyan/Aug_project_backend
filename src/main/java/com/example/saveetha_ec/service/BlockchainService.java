package com.example.saveetha_ec.service;

import com.example.saveetha_ec.DigitalGoldToken;
import com.example.saveetha_ec.model.UserDetail;
import com.example.saveetha_ec.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Service
public class BlockchainService {

    @Value("${blockchain.node.url}")
    private String nodeUrl;

    @Value("${backend.wallet.private-key}")
    private String privateKey;

    @Value("${contract.address}")
    private String contractAddress;

    private Web3j web3j;
    private Credentials credentials;
    private DigitalGoldToken contract;
    
    private final UserRepository userRepository;

    public BlockchainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        this.web3j = Web3j.build(new HttpService(nodeUrl));
        this.credentials = Credentials.create(privateKey);
        this.contract = DigitalGoldToken.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        System.out.println("BlockchainService initialized. Contract loaded at address: " + contract.getContractAddress());
    }

    public String mintTokens(Long userId, BigInteger amount, byte[] dataHash) throws Exception {
        UserDetail user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        String toAddress = user.getWalletAddress();
        if (toAddress == null || toAddress.isEmpty()) {
            throw new IllegalStateException("User does not have a wallet address set.");
        }

        var transactionReceipt = contract.purchaseGold(toAddress, amount, dataHash).send();
        
        if (!transactionReceipt.isStatusOK()) {
            throw new RuntimeException("Minting transaction failed with status: " + transactionReceipt.getStatus());
        }
        
        return transactionReceipt.getTransactionHash();
    }
}