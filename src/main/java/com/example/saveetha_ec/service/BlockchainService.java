package com.example.saveetha_ec.service;

import java.math.BigInteger;
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
    // We remove the contract and txManager from class-level fields
    // to create them dynamically for each transaction, ensuring nonce is always fresh.

    private final UserRepository userRepository;

    public BlockchainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        // A client with a long timeout is crucial for waiting for transaction confirmation
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

        // --- THE FIX: DYNAMIC NONCE AND TRANSACTION MANAGER ---

        // 1. Get the latest transaction count (nonce) right before sending.
        // We use 'PENDING' to ensure we don't conflict with transactions still in the mempool.
        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING).send().getTransactionCount();
        System.out.println("Using fresh nonce: " + nonce);

        long chainId = 80002; // Polygon Amoy testnet chain ID

        // 2. Create a new Transaction Manager for THIS transaction with the correct nonce.
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

        // 3. Define robust EIP-1559 gas fees to ensure the transaction is picked up by miners.
        BigInteger gasLimit = BigInteger.valueOf(500_000L); // A safe gas limit for your function
        BigInteger maxFeePerGas = Convert.toWei("150", Convert.Unit.GWEI).toBigInteger();
        BigInteger maxPriorityFeePerGas = Convert.toWei("30", Convert.Unit.GWEI).toBigInteger(); // A good tip for the miner
        ContractGasProvider gasProvider = new StaticEIP1559GasProvider(chainId, maxFeePerGas, maxPriorityFeePerGas, gasLimit);
        
        // 4. Load the contract instance using our new, single-use transaction manager.
        DigitalGoldToken contract = DigitalGoldToken.load(contractAddress, web3j, txManager, gasProvider);
        
        System.out.println("Attempting to mint " + Convert.fromWei(amount.toString(), Convert.Unit.ETHER) + " tokens for " + toAddress);

        // 5. Send the transaction.
        var transactionReceipt = contract.purchaseGold(toAddress, amount, dataHash).send();

        if (!transactionReceipt.isStatusOK()) {
            throw new RuntimeException("Minting transaction failed on-chain. Status: " + transactionReceipt.getStatus() 
                + ". Revert reason: " + transactionReceipt.getRevertReason());
        }
        
        System.out.println("✅ Transaction successful! Hash: " + transactionReceipt.getTransactionHash());
        return transactionReceipt.getTransactionHash();
    }

    public String redeemTokens(Long userId, BigInteger amount, byte[] dataHash) throws Exception {
        UserDetail user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        String userAddress = user.getWalletAddress();
        if (userAddress == null || userAddress.isBlank()) {
            throw new IllegalStateException("User " + userId + " does not have a wallet address set.");
        }

        // --- DYNAMIC NONCE AND TRANSACTION MANAGER FOR REDEMPTION ---
        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING).send().getTransactionCount();
        System.out.println("Using fresh nonce for redemption: " + nonce);

        long chainId = 80002; // Polygon Amoy
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);
        
        BigInteger gasLimit = BigInteger.valueOf(500_000L);
        BigInteger maxFeePerGas = Convert.toWei("150", Convert.Unit.GWEI).toBigInteger();
        BigInteger maxPriorityFeePerGas = Convert.toWei("30", Convert.Unit.GWEI).toBigInteger();
        ContractGasProvider gasProvider = new StaticEIP1559GasProvider(chainId, maxFeePerGas, maxPriorityFeePerGas, gasLimit);
        
        DigitalGoldToken contract = DigitalGoldToken.load(contractAddress, web3j, txManager, gasProvider);
        
        System.out.println("Attempting to redeem " + Convert.fromWei(amount.toString(), Convert.Unit.ETHER) + " tokens from " + userAddress);

        // This calls the 'redeemGold(address user, ...)' function on your optimized contract
        var transactionReceipt = contract.redeemGold(userAddress, amount, dataHash).send();

        if (!transactionReceipt.isStatusOK()) {
            throw new RuntimeException("Redemption transaction failed on-chain. Status: " + transactionReceipt.getStatus() 
                + ". Revert reason: " + transactionReceipt.getRevertReason());
        }

        System.out.println("✅ Redemption successful! Hash: " + transactionReceipt.getTransactionHash());
        return transactionReceipt.getTransactionHash();
    }
}
