package com.example.saveetha_ec.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticEIP1559GasProvider;
import org.web3j.utils.Convert;

import com.example.saveetha_ec.contracts.DigitalGoldToken;
import com.example.saveetha_ec.model.UserDetail;
import com.example.saveetha_ec.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;

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
    
    private final UserRepository userRepository;

    public BlockchainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        this.web3j = Web3j.build(new HttpService(nodeUrl, okHttpClient));
        this.credentials = Credentials.create(privateKey);
        System.out.println("✅ BlockchainService initialized. Backend Wallet Address: " + credentials.getAddress());
    }

    public String mintTokens(Long userId, BigInteger amount, byte[] dataHash) throws Exception {
        UserDetail user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        String toAddress = user.getWalletAddress();
        if (toAddress == null || toAddress.isBlank()) {
            throw new IllegalStateException("User " + userId + " does not have a wallet address set.");
        }

        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING).send().getTransactionCount();
        System.out.println("Using fresh nonce: " + nonce);

        long chainId = 80002; 

        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

        BigInteger gasLimit = BigInteger.valueOf(500_000L);
        BigInteger maxFeePerGas = Convert.toWei("150", Convert.Unit.GWEI).toBigInteger();
        BigInteger maxPriorityFeePerGas = Convert.toWei("30", Convert.Unit.GWEI).toBigInteger();
        ContractGasProvider gasProvider = new StaticEIP1559GasProvider(chainId, maxFeePerGas, maxPriorityFeePerGas, gasLimit);
        
        DigitalGoldToken contract = DigitalGoldToken.load(contractAddress, web3j, txManager, gasProvider);
        
        System.out.println("Attempting to mint " + Convert.fromWei(amount.toString(), Convert.Unit.ETHER) + " tokens for " + toAddress);

        var transactionReceipt = contract.purchaseGold(toAddress, amount, dataHash).send();

        if (!transactionReceipt.isStatusOK()) {
            throw new RuntimeException("Minting transaction failed on-chain. Status: " + transactionReceipt.getStatus() 
                + ". Revert reason: " + transactionReceipt.getRevertReason());
        }
        
        System.out.println("✅ Transaction successful! Hash: " + transactionReceipt.getTransactionHash());
        return transactionReceipt.getTransactionHash();
    }

    // Updated redeemTokens method
    public String redeemTokens(Long userId, BigInteger amount) throws Exception {
        UserDetail user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        String userAddress = user.getWalletAddress();
        if (userAddress == null || userAddress.isBlank()) {
            throw new IllegalStateException("User " + userId + " does not have a wallet address set.");
        }
        
        // Generate a unique data hash internally for this specific redemption transaction
        String uniqueRedemptionId = UUID.randomUUID().toString();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] dataHash = digest.digest(uniqueRedemptionId.getBytes(StandardCharsets.UTF_8));

        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING).send().getTransactionCount();
        System.out.println("Using fresh nonce for redemption: " + nonce);

        long chainId = 80002;
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);
        
        BigInteger gasLimit = BigInteger.valueOf(500_000L);
        BigInteger maxFeePerGas = Convert.toWei("150", Convert.Unit.GWEI).toBigInteger();
        BigInteger maxPriorityFeePerGas = Convert.toWei("30", Convert.Unit.GWEI).toBigInteger();
        ContractGasProvider gasProvider = new StaticEIP1559GasProvider(chainId, maxFeePerGas, maxPriorityFeePerGas, gasLimit);
        
        DigitalGoldToken contract = DigitalGoldToken.load(contractAddress, web3j, txManager, gasProvider);
        
        System.out.println("Attempting to redeem " + Convert.fromWei(amount.toString(), Convert.Unit.ETHER) + " tokens from " + userAddress);

        var transactionReceipt = contract.redeemGold(userAddress, amount, dataHash).send();

        if (!transactionReceipt.isStatusOK()) {
            throw new RuntimeException("Redemption transaction failed on-chain. Status: " + transactionReceipt.getStatus() 
                + ". Revert reason: " + transactionReceipt.getRevertReason());
        }

        System.out.println("✅ Redemption successful! Hash: " + transactionReceipt.getTransactionHash());
        return transactionReceipt.getTransactionHash();
    }
}